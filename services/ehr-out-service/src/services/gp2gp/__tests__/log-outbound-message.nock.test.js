import { sendCore } from '../send-core';
import { sendFragment } from '../send-fragment';
import { logInfo } from '../../../middleware/logging';
import { removeBase64Payloads } from '../logging-utils';
import {
  createMockGP2GPScope,
  createRandomUUID,
  EhrMessageType,
  isSmallerThan256KB,
  loadTestData,
  setupMockConfigForTest
} from './test-utils';
import { updateFragmentStatus } from '../../transfer/transfer-out-util';

jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({})
}));
jest.mock('../../../middleware/logging');
jest.mock('../../transfer/transfer-out-util');

describe('logOutboundMessage', () => {
  beforeAll(() => {
    setupMockConfigForTest();
  });

  const testCases = [EhrMessageType.core, EhrMessageType.fragment];

  describe.each(testCases)('Test case for EHR %s', testCase => {
    it('should log the outbound message with all base64 payload removed', async () => {
      // given
      const [conversationId, messageId, ehrRequestId] = createRandomUUID(3);
      const inputEhrMessage = loadTestData(`TestEhr${testCase}`);
      const odsCode = 'test-ods-code';

      // when
      updateFragmentStatus.mockResolvedValueOnce(undefined);

      const scope = createMockGP2GPScope(testCase);
      if (testCase === EhrMessageType.core) {
        await sendCore(conversationId, odsCode, inputEhrMessage, ehrRequestId, messageId);
      } else {
        await sendFragment(conversationId, odsCode, inputEhrMessage, messageId);
      }

      // then
      expect(scope.isDone()).toBe(true);

      const loggedRequestBody = logInfo.mock.calls
        .map(args => args[0])
        .filter(args => args.conversationId !== undefined)
        .pop();

      // verify that the log message exists and is less than 256KB in size
      expect(loggedRequestBody).not.toEqual(undefined);
      expect(isSmallerThan256KB(loggedRequestBody)).toBe(true);

      // the loggedRequestBody should be same as the actual outbound request body,
      // except that base64 content are removed
      expect(loggedRequestBody).not.toEqual(scope.outboundRequestBody);
      expect(loggedRequestBody).toEqual(removeBase64Payloads(scope.outboundRequestBody));
    });

    it('should keep the base64 content in the actual outbound post request unchanged', async () => {
      // given
      const [
        inboundConversationId,
        outboundConversationId,
        outboundMessageId,
        inboundMessageId,
        ehrRequestId
      ] = createRandomUUID(5);
      const inputEhrMessage = loadTestData(`TestEhr${testCase}`);
      const odsCode = 'test-ods-code';

      // when
      updateFragmentStatus.mockResolvedValueOnce(undefined);

      const scope = createMockGP2GPScope(testCase);
      if (testCase === EhrMessageType.core) {
        await sendCore(
          outboundConversationId,
          odsCode,
          inputEhrMessage,
          ehrRequestId,
          outboundMessageId
        );
      } else {
        await sendFragment(
          inboundConversationId,
          outboundConversationId,
          odsCode,
          inputEhrMessage,
          outboundMessageId,
          inboundMessageId
        );
      }

      // then
      const actualOutboundRequestBody = scope.outboundRequestBody;

      if (testCase === EhrMessageType.core) {
        expect(actualOutboundRequestBody).toMatchObject({
          conversationId: outboundConversationId,
          odsCode,
          messageId: outboundMessageId
        });
      } else {
        expect(actualOutboundRequestBody).toMatchObject({
          conversationId: outboundConversationId,
          odsCode,
          messageId: outboundMessageId
        });
      }

      const outboundEhrMessage =
        testCase === EhrMessageType.core
          ? actualOutboundRequestBody.coreEhr
          : actualOutboundRequestBody.fragmentMessage;

      // verify that the outbound ehr message is unchanged
      expect(outboundEhrMessage).toEqual(inputEhrMessage);
      expect(isSmallerThan256KB(outboundEhrMessage)).toBe(false);
    });
  });

  describe('Special test case: EHR with Large medical history which is > 256KB even without the base64 content', () => {
    it(`should log the whole outbound message in multiple lines of logs, each line to be within 256 KB`, async () => {
      // given
      const [conversationId, messageId, ehrRequestId] = createRandomUUID(3);
      const inputEhrMessage = loadTestData('TestEhrCoreWithLargeMedicalHistory');
      const odsCode = 'test-ods-code';

      // when
      const scope = createMockGP2GPScope(EhrMessageType.core);
      await sendCore(conversationId, odsCode, inputEhrMessage, ehrRequestId, messageId);

      // then
      expect(scope.isDone()).toBe(true);

      const relevantLogs = logInfo.mock.calls
        .map(args => args[0])
        .filter(loggedText => loggedText.match(/Part \d+ of \d+: /));

      // verify that every line of the log is less than 256 KB
      expect(relevantLogs.length).toBeGreaterThan(0);
      relevantLogs.forEach(singleLineOfLog => {
        expect(isSmallerThan256KB(singleLineOfLog)).toBe(true);
      });

      // verify that we can combine all lines of the log to reconstruct the actual outbound message (with base64 contents removed)
      const combinedLogs = relevantLogs
        .map(singleLineOfLog => singleLineOfLog.replace(/Part \d+ of \d+: /, ''))
        .join('');
      const restoredMessage = JSON.parse(combinedLogs);

      expect(restoredMessage.coreEhr).toEqual(removeBase64Payloads(inputEhrMessage));
    });
  });
});
