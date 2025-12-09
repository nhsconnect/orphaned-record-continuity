import {
  patientAndPracticeOdsCodesMatch,
  updateConversationStatus,
  updateCoreStatus
} from '../../transfer/transfer-out-util';
import { parseContinueRequestMessage } from '../../parser/continue-request-parser';
import {
  transferOutFragmentsForNewContinueRequest,
  transferOutFragmentsForRetriedContinueRequest
} from '../../transfer/transfer-out-fragments';
import { parseConversationId } from '../../parser/parsing-utilities';
import continueMessageHandler from '../continue-message-handler';
import {
  AcknowledgementErrorCode,
  ConversationStatus as conversationStatus,
  ConversationStatus,
  CoreStatus,
  FailureReason
} from '../../../constants/enums';
import { readFileSync } from 'fs';
import expect from 'expect';
import path from 'path';
import { logError, logInfo, logWarning } from '../../../middleware/logging';
import { hasServiceStartedInTheLast5Minutes } from '../../../config';
import {
  DownloadError,
  GetPdsCodeError,
  NhsNumberNotFoundError,
  PresignedUrlNotFoundError
} from '../../../errors/errors';
import {
  getNhsNumberByOutboundConversationId,
  getOutboundConversationById
} from '../../database/dynamodb/outbound-conversation-repository';
import { sendAcknowledgement } from '../../gp2gp/send-acknowledgement';

// Mocking
jest.mock('../../../middleware/logging');
jest.mock('../../transfer/transfer-out-fragments');
jest.mock('../../transfer/transfer-out-util');
jest.mock('../../gp2gp/send-acknowledgement');
jest.mock('../../database/dynamodb/outbound-conversation-repository');
jest.mock('../../parser/continue-request-parser');
jest.mock('../../parser/parsing-utilities');
jest.mock('../../../config', () => ({
  hasServiceStartedInTheLast5Minutes: jest.fn()
}));

describe('continueMessageHandler', () => {
  // ============ COMMON PROPERTIES ============
  const ODS_CODE = 'YGM24';
  const NHS_NUMBER = '1234567890';
  const CONVERSATION_ID = 'DBC31D30-F984-41ED-A4C4-956AA80C6B4E';
  const CONTINUE_REQUEST_MESSAGE_ID = 'C998B3F1-9B03-41EC-B8D6-6C4BA7CD6934';
  const CONTINUE_MESSAGE = readFileSync(
    path.join(__dirname, 'data', 'continue-requests', 'COPC_IN000001UK01'),
    'utf-8'
  );
  const PARSED_CONTINUE_MESSAGE = {
    odsCode: ODS_CODE,
    messageId: CONTINUE_REQUEST_MESSAGE_ID
  };
  // =================== END ===================

  it('should forward a new continue request to initiate transfer out fragment', async () => {
    // when
    parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
    parseContinueRequestMessage.mockResolvedValueOnce({ odsCode: ODS_CODE });
    getOutboundConversationById.mockResolvedValueOnce(null);
    getNhsNumberByOutboundConversationId.mockReturnValueOnce(NHS_NUMBER);
    patientAndPracticeOdsCodesMatch.mockReturnValueOnce(true);
    updateConversationStatus.mockResolvedValueOnce(undefined);
    transferOutFragmentsForNewContinueRequest.mockResolvedValueOnce(undefined);

    await continueMessageHandler(CONTINUE_MESSAGE);

    // then
    expect(parseConversationId).toHaveBeenCalledWith(CONTINUE_MESSAGE);
    expect(parseContinueRequestMessage).toHaveBeenCalledWith(CONTINUE_MESSAGE);
    expect(getOutboundConversationById).toHaveBeenCalledWith(CONVERSATION_ID);
    expect(getNhsNumberByOutboundConversationId).toHaveBeenCalledWith(CONVERSATION_ID);
    expect(patientAndPracticeOdsCodesMatch).toHaveBeenCalledWith(NHS_NUMBER, ODS_CODE);
    expect(updateConversationStatus).toHaveBeenCalledWith(
      CONVERSATION_ID,
      ConversationStatus.OUTBOUND_CONTINUE_REQUEST_RECEIVED
    );
    expect(transferOutFragmentsForNewContinueRequest).toHaveBeenCalledWith({
      conversationId: CONVERSATION_ID,
      nhsNumber: NHS_NUMBER,
      odsCode: ODS_CODE
    });

    expect(logInfo).toHaveBeenCalledWith('Trying to handle continue request');
  });

  /*
    'valid circumstances' in this case refers to either:
    A) A request that is marked as 'CONTINUE_REQUEST_RECEIVED' but the service has only started in the past 5 minutes,
    this indicates that the system must have crashed and just restarted, and is a valid case for a retried continue request.

    B) A request that is marked as 'FRAGMENTS_SENDING_FAILED'. In real circumstances, it's impossible (as of 11/10/2023)
    that we'd ever receive a continue request for a request marked like this, but for future proofing, we've added the ability
    to retry the fragment sending if one was to be received.
   */
  const testCasesForRetry = [
    ConversationStatus.OUTBOUND_CONTINUE_REQUEST_RECEIVED,
    ConversationStatus.OUTBOUND_FRAGMENTS_SENDING_FAILED
  ];
  it.each(testCasesForRetry)(
    'should forward a retried continue request in valid circumstances - %s',
    async status => {
      // when
      parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
      parseContinueRequestMessage.mockResolvedValueOnce({ odsCode: ODS_CODE });
      getOutboundConversationById.mockReturnValueOnce({ TransferStatus: status });
      hasServiceStartedInTheLast5Minutes.mockReturnValueOnce(
        status === ConversationStatus.OUTBOUND_CONTINUE_REQUEST_RECEIVED
      );
      getNhsNumberByOutboundConversationId.mockResolvedValueOnce(NHS_NUMBER);
      patientAndPracticeOdsCodesMatch.mockResolvedValueOnce(true);
      transferOutFragmentsForRetriedContinueRequest.mockResolvedValue(undefined);

      await continueMessageHandler(CONTINUE_MESSAGE);

      // then
      expect(parseConversationId).toHaveBeenCalledWith(CONTINUE_MESSAGE);
      expect(parseContinueRequestMessage).toHaveBeenCalledWith(CONTINUE_MESSAGE);
      expect(getOutboundConversationById).toHaveBeenCalledWith(CONVERSATION_ID);
      expect(getNhsNumberByOutboundConversationId).toHaveBeenCalledWith(CONVERSATION_ID);
      expect(patientAndPracticeOdsCodesMatch).toHaveBeenCalledWith(NHS_NUMBER, ODS_CODE);
      expect(transferOutFragmentsForRetriedContinueRequest).toHaveBeenCalledWith({
        conversationId: CONVERSATION_ID,
        nhsNumber: NHS_NUMBER,
        odsCode: ODS_CODE
      });

      expect(logInfo).toHaveBeenCalledWith('Trying to handle continue request');
    }
  );

  const testCasesForDuplicatedRequest = [
    ConversationStatus.OUTBOUND_SENT_FRAGMENTS,
    ConversationStatus.OUTBOUND_COMPLETE // equal to previous EHR_INTEGRATED
  ];
  it.each(testCasesForDuplicatedRequest)(
    'should reject a continue request if the transfer has already successfully completed - %s',
    async status => {
      // when
      parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
      parseContinueRequestMessage.mockResolvedValueOnce({ odsCode: ODS_CODE });
      getOutboundConversationById.mockReturnValueOnce({ TransferStatus: status });

      await continueMessageHandler(CONTINUE_MESSAGE);

      // then
      expect(parseConversationId).toHaveBeenCalledWith(CONTINUE_MESSAGE);
      expect(parseContinueRequestMessage).toHaveBeenCalledWith(CONTINUE_MESSAGE);
      expect(logInfo).toHaveBeenCalledWith('Trying to handle continue request');
      expect(logWarning).toHaveBeenCalledWith(
        `Ignoring duplicate continue request. Conversation ID ${CONVERSATION_ID} already completed successfully`
      );
    }
  );

  it('should reject a continue request if the transfer has already failed and is unable to retry', async () => {
    // when
    const status = ConversationStatus.OUTBOUND_FAILED;
    parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
    parseContinueRequestMessage.mockResolvedValueOnce(PARSED_CONTINUE_MESSAGE);
    getOutboundConversationById.mockReturnValueOnce({ TransferStatus: status });

    await continueMessageHandler(CONTINUE_MESSAGE);

    // then
    expect(parseConversationId).toHaveBeenCalledWith(CONTINUE_MESSAGE);
    expect(parseContinueRequestMessage).toHaveBeenCalledWith(CONTINUE_MESSAGE);

    expect(logInfo).toHaveBeenCalledWith('Trying to handle continue request');
    expect(logWarning).toHaveBeenCalledWith(
      `Ignoring duplicate continue request. Conversation ID ${CONVERSATION_ID} already failed and is unable to retry`
    );
  });

  it('should reject a continue request if the transfer is still in progress', async () => {
    // when
    parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
    parseContinueRequestMessage.mockResolvedValueOnce(PARSED_CONTINUE_MESSAGE);
    getOutboundConversationById.mockReturnValueOnce({
      TransferStatus: ConversationStatus.OUTBOUND_CONTINUE_REQUEST_RECEIVED
    });
    hasServiceStartedInTheLast5Minutes.mockReturnValue(false);

    await continueMessageHandler(CONTINUE_MESSAGE);

    // then
    expect(parseConversationId).toHaveBeenCalledWith(CONTINUE_MESSAGE);
    expect(parseContinueRequestMessage).toHaveBeenCalledWith(CONTINUE_MESSAGE);
    expect(logWarning).toHaveBeenCalledWith(
      `Fragment transfer with conversation ID ${CONVERSATION_ID} is already in progress`
    );
  });

  it('should throw an error if it cannot find an nhs number related to given conversation id', async () => {
    // when
    parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
    parseContinueRequestMessage.mockResolvedValueOnce(PARSED_CONTINUE_MESSAGE);
    getNhsNumberByOutboundConversationId.mockRejectedValueOnce(new NhsNumberNotFoundError());

    await expect(continueMessageHandler(CONTINUE_MESSAGE))
      // then
      .rejects.toThrow(NhsNumberNotFoundError);

    // then
    expect(updateConversationStatus).not.toHaveBeenCalled();
    expect(transferOutFragmentsForNewContinueRequest).not.toHaveBeenCalled();

    expect(logError).toHaveBeenCalledWith(
      'Cannot find an NHS number related to given conversation ID'
    );
  });

  it('should not send fragments if ods codes of the patient and GP practice does not match', async () => {
    // when
    parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
    parseContinueRequestMessage.mockResolvedValueOnce(PARSED_CONTINUE_MESSAGE);
    patientAndPracticeOdsCodesMatch.mockReturnValueOnce(false);

    await continueMessageHandler(CONTINUE_MESSAGE);

    // then
    expect(updateConversationStatus).toHaveBeenCalledWith(
      CONVERSATION_ID,
      ConversationStatus.OUTBOUND_FAILED,
      FailureReason.INCORRECT_ODS_CODE,
      'Patients ODS Code in PDS does not match requesting practices ODS Code'
    );
    expect(transferOutFragmentsForNewContinueRequest).not.toBeCalled();
  });

  const testCasesForFragmentTransferErrors = [
    {
      errorType: GetPdsCodeError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_20_A,
      conversationStatus: conversationStatus.OUTBOUND_FRAGMENTS_SENDING_FAILED,
      failureReason: FailureReason.SENDING_FAILED
    },
    {
      errorType: DownloadError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_10_A,
      conversationStatus: conversationStatus.OUTBOUND_FRAGMENTS_SENDING_FAILED,
      failureReason: FailureReason.EHR_DOWNLOAD_FAILED
    },
    {
      errorType: PresignedUrlNotFoundError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_10_B,
      conversationStatus: conversationStatus.OUTBOUND_FRAGMENTS_SENDING_FAILED,
      failureReason: FailureReason.MISSING_FROM_REPO
    }
  ];

  it.each(testCasesForFragmentTransferErrors)(
    'should stop the transfer and send negative acknowledgement if any part of the transfer failed with a $acknowledgementErrorCode.internalErrorCode $errorType.name',
    async ({ errorType, acknowledgementErrorCode, conversationStatus, failureReason }) => {
      // given
      const error = new errorType(null, acknowledgementErrorCode);

      // when
      parseConversationId.mockResolvedValueOnce(CONVERSATION_ID);
      parseContinueRequestMessage.mockResolvedValueOnce(PARSED_CONTINUE_MESSAGE);
      getOutboundConversationById.mockReturnValueOnce({
        TransferStatus: ConversationStatus.OUTBOUND_SENT_CORE
      });
      getNhsNumberByOutboundConversationId.mockResolvedValueOnce(NHS_NUMBER);
      patientAndPracticeOdsCodesMatch.mockResolvedValueOnce(true);
      transferOutFragmentsForNewContinueRequest.mockRejectedValueOnce(error);

      await continueMessageHandler(CONTINUE_MESSAGE);

      // then
      expect(updateConversationStatus).toHaveBeenNthCalledWith(
        1,
        CONVERSATION_ID,
        ConversationStatus.OUTBOUND_CONTINUE_REQUEST_RECEIVED
      );
      expect(updateCoreStatus).toHaveBeenCalledWith(CONVERSATION_ID, CoreStatus.OUTBOUND_COMPLETE);
      expect(sendAcknowledgement).toHaveBeenCalledWith(
        NHS_NUMBER,
        ODS_CODE,
        CONVERSATION_ID,
        CONTINUE_REQUEST_MESSAGE_ID,
        acknowledgementErrorCode.gp2gpError
      );
      expect(updateConversationStatus).toHaveBeenNthCalledWith(
        2,
        CONVERSATION_ID,
        conversationStatus,
        failureReason
      );
    }
  );
});
