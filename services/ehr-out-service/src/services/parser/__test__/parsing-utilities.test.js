import { validateFieldsHaveSuccessfullyParsed } from '../parsing-validation';
import { readFileSync } from 'fs';
import expect from 'expect';
import path from 'path';
import {
  extractReferencedFragmentMessageIds,
  parseConversationId,
  parseInteractionId,
  parseMessageId
} from '../parsing-utilities';

// Mocking
jest.mock('../parsing-validation');

describe('parsing-utilities.js', () => {
  // ============ COMMON PROPERTIES ============
  const exampleEhrRequest = JSON.parse(
    readFileSync(path.join(__dirname, 'data', 'ehr-requests', 'RCMR_IN010000UK05'), 'utf-8')
  );
  const exampleContinueRequest = JSON.parse(
    readFileSync(path.join(__dirname, 'data', 'continue-requests', 'COPC_IN000001UK01'), 'utf-8')
  );
  const exampleNegativeAcknowledgement = JSON.parse(
    readFileSync(
      path.join(__dirname, 'data', 'acknowledgements', 'negative', 'MCCI_IN010000UK13_TPP_AR_01'),
      'utf-8'
    )
  );
  const exampleEhrCore = JSON.parse(
    readFileSync('src/__tests__/data/ehr_with_fragments/ehr-core', 'utf-8')
  );
  const exampleEhrCoreOneReference = JSON.parse(
    readFileSync('src/__tests__/data/ehr_with_fragments/ehr-core-with-only-one-ref', 'utf-8')
  );
  const exampleMessageFragment = JSON.parse(
    readFileSync('src/__tests__/data/ehr_with_fragments/fragment-2', 'utf-8')
  );
  // =================== END ===================

  it('should parse the interaction ID successfully, given a EHR Request', async () => {
    // given
    const interactionID = 'RCMR_IN010000UK05';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedInteractionId = await parseInteractionId(exampleEhrRequest);

    // then
    expect(parsedInteractionId).toEqual(interactionID);
  });

  it('should parse the interaction ID successfully, given a Continue Request', async () => {
    // given
    const interactionID = 'COPC_IN000001UK01';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedInteractionId = await parseInteractionId(exampleContinueRequest);

    // then
    expect(parsedInteractionId).toEqual(interactionID);
  });

  it('should parse the interaction ID successfully, given a Negative Acknowledgement', async () => {
    // given
    const interactionID = 'MCCI_IN010000UK13';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedInteractionId = await parseInteractionId(exampleNegativeAcknowledgement);

    // then
    expect(parsedInteractionId).toEqual(interactionID);
  });

  it('should parse the conversation ID successfully, given a EHR Request', async () => {
    // given
    const conversationId = '95C5A27C-9DE3-4C3A-A6CA-D9CD437BC6CC';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedConversationId = await parseConversationId(exampleEhrRequest);

    // then
    expect(parsedConversationId).toEqual(conversationId);
  });

  it('should parse the conversation ID successfully, given a Continue Request', async () => {
    // given
    const conversationId = 'DBC31D30-F984-41ED-A4C4-956AA80C6B4E';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedConversationId = await parseConversationId(exampleContinueRequest);

    // then
    expect(parsedConversationId).toEqual(conversationId);
  });

  it('should parse the conversation ID successfully, given a Negative Acknowledgement', async () => {
    // given
    const conversationId = 'DBC31D30-F984-41ED-A4C4-956AA80C6B4E';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedConversationId = await parseConversationId(exampleNegativeAcknowledgement);

    // then
    expect(parsedConversationId).toEqual(conversationId);
  });

  it('should parse the correct message ID, given a EHR Core', async () => {
    // given
    const messageId = 'DF91D420-DDC7-41ED-808B-AC162D1F16F0';

    // when
    const parsedMessageId = await parseMessageId(exampleEhrCore);

    // then
    expect(messageId).toEqual(parsedMessageId);
  });

  it('should parse the correct message ID, given a fragment', async () => {
    // given
    const messageId = 'DFEC7740-DDC7-41ED-808B-AC162D1F16F0';

    // when
    const parsedMessageId = await parseMessageId(exampleMessageFragment);

    // then
    expect(messageId).toEqual(parsedMessageId);
  });

  it('should parse the correct message ID, given a continue request', async () => {
    // given
    const messageId = 'DE304CA0-F984-41ED-808B-AC162D1F16F0';

    // when
    const parsedMessageId = await parseMessageId(exampleContinueRequest);

    // then
    expect(messageId).toEqual(parsedMessageId);
  });

  it('should parse the correct message ID, given a negative acknowledgement', async () => {
    // given
    const messageId = 'BB8FC948-FA40-41ED-A594-F40343488B16';

    // when
    const parsedMessageId = await parseMessageId(exampleNegativeAcknowledgement);

    // then
    expect(messageId).toEqual(parsedMessageId);
  });

  it('should extract the message IDs from the references, given a EHR core', async () => {
    // given
    const messageIds = [
      'DFBA6AC0-DDC7-41ED-808B-AC162D1F16F0',
      'DFEC7740-DDC7-41ED-808B-AC162D1F16F0'
    ];

    // when
    const parsedMessageIds = await extractReferencedFragmentMessageIds(exampleEhrCore);

    // then
    expect(parsedMessageIds).toEqual(messageIds);
  });

  it('should extract the message IDs from the references, given a EHR core with one reference', async () => {
    // given
    const messageIds = ['D6BB8150-D478-41ED-808B-AC162D1F16F0'];

    // when
    const parsedMessageIds = await extractReferencedFragmentMessageIds(exampleEhrCoreOneReference);

    // then
    expect(parsedMessageIds).toEqual(messageIds);
    expect(parsedMessageIds).toHaveLength(1);
  });

  it('should extract the message IDs from the references, given a fragment', async () => {
    // given
    const messageIds = [
      'DFEC7741-DDC7-41ED-808B-AC162D1F16F0',
      'DFF61430-DDC7-41ED-808B-AC162D1F16F0'
    ];

    // when
    const parsedMessageIds = await extractReferencedFragmentMessageIds(exampleMessageFragment);

    // then
    expect(parsedMessageIds).toEqual(messageIds);
  });
});
