import expect from 'expect';
import { logInfo } from '../../../middleware/logging';
import {
  transferOutFragmentsForNewContinueRequest,
  transferOutFragmentsForRetriedContinueRequest
} from '../transfer-out-fragments';
import { sendFragment } from '../../gp2gp/send-fragment';
import { replaceMessageIdsInObject, updateFragmentStatus } from '../transfer-out-util';
import {
  getFragment,
  getFragmentConversationAndMessageIdsFromEhrRepo
} from '../../ehr-repo/get-fragment';
import {
  getAllFragmentIdsToBeSent,
  getAllMessageIdPairs
} from '../../database/dynamodb/ehr-fragment-repository';
import { AcknowledgementErrorCode, FailureReason, FragmentStatus } from '../../../constants/enums';
import {
  DownloadError,
  FragmentSendingError,
  PresignedUrlNotFoundError
} from '../../../errors/errors';

// Mocking
jest.mock('../transfer-out-util');
jest.mock('../../gp2gp/send-fragment');
jest.mock('../../ehr-repo/get-fragment');
jest.mock('../../database/dynamodb/ehr-fragment-repository');
jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({
    rateLimitTimeoutMilliseconds: 1000
  })
}));
jest.mock('../../../middleware/logging');
jest.mock('../../gp2gp/pds-retrieval-request');

describe('transferOutFragments', () => {
  // ============ COMMON PROPERTIES ============
  const odsCode = 'B12345';
  const nhsNumber = 1234567890;
  const inboundConversationId = 'A52E8782-2268-4D49-AB69-5FCFA10EB43A';
  const outboundConversationId = 'CBC75815-8EE8-4554-980A-F7F8FD2CD341';

  const inboundMessageId1 = '2526ea9f-f800-461d-8046-8bc512920d23';
  const inboundMessageId2 = '6e7725e3-0cab-4fd8-a518-eeda7b5925a6';
  const outboundMessageId1 = '7B2D6A16-2C50-4BF8-A923-E14003816ECC';
  const outboundMessageId2 = 'DDA75C2B-18B6-424D-B94B-83205AAF2FEA';

  const messageIdsWithReplacements = [
    { oldMessageId: inboundMessageId1, newMessageId: outboundMessageId1 },
    { oldMessageId: inboundMessageId2, newMessageId: outboundMessageId2 }
  ];

  const ehrRepositoryResponse = {
    inboundConversationId,
    messageIds: [inboundMessageId1, inboundMessageId2]
  };

  const fragment1 = { ebXML: '1', payload: '1', attachments: [], externalAttachments: [] };
  const fragment2 = { ebXML: '2', payload: '2', attachments: [], externalAttachments: [] };

  const updatedFragment1 = { newMessageId: outboundMessageId1, message: fragment1 };
  const updatedFragment2 = { newMessageId: outboundMessageId2, message: fragment2 };

  // =================== END ===================

  afterEach(() => {
    jest.resetAllMocks();
  });

  it('should transfer out fragments when new continue request is received', async () => {
    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepositoryResponse);
    getAllMessageIdPairs.mockResolvedValueOnce(messageIdsWithReplacements);
    getFragment.mockResolvedValueOnce(fragment1);
    getFragment.mockResolvedValueOnce(fragment2);
    replaceMessageIdsInObject.mockReturnValueOnce(updatedFragment1);
    replaceMessageIdsInObject.mockReturnValueOnce(updatedFragment2);
    sendFragment.mockResolvedValue(undefined);

    const result = await transferOutFragmentsForNewContinueRequest({
      conversationId: outboundConversationId,
      nhsNumber: nhsNumber,
      odsCode: odsCode
    });

    // then
    expect(result).toBe(undefined);

    expect(getFragmentConversationAndMessageIdsFromEhrRepo).toHaveBeenCalledWith(nhsNumber);
    expect(getAllMessageIdPairs).toHaveBeenCalledWith(
      ehrRepositoryResponse.messageIds,
      inboundConversationId
    );
    expect(getFragment).toHaveBeenCalledWith(
      inboundConversationId,
      ehrRepositoryResponse.messageIds[0]
    );
    expect(getFragment).toHaveBeenCalledWith(
      inboundConversationId,
      ehrRepositoryResponse.messageIds[1]
    );
    expect(replaceMessageIdsInObject).toHaveBeenCalledWith(fragment1, messageIdsWithReplacements);
    expect(replaceMessageIdsInObject).toHaveBeenCalledWith(fragment2, messageIdsWithReplacements);
    expect(sendFragment).toHaveBeenCalledWith(
      inboundConversationId,
      outboundConversationId,
      odsCode,
      updatedFragment1,
      outboundMessageId1,
      inboundMessageId1
    );
    expect(sendFragment).toHaveBeenCalledWith(
      inboundConversationId,
      outboundConversationId,
      odsCode,
      updatedFragment2,
      outboundMessageId2,
      inboundMessageId2
    );

    expect(logInfo).toHaveBeenCalledTimes(5);
    expect(logInfo).toHaveBeenCalledWith(`Initiated the EHR Fragment transfer.`);
    expect(logInfo).toHaveBeenCalledWith('Retrieved all fragment Message IDs for transfer.');
    expect(logInfo).toHaveBeenCalledWith(
      `Fragment 1 of 2 sent to the GP2GP Messenger - with old Message ID ${inboundMessageId1}, and new Message ID ${outboundMessageId1}.`
    );
    expect(logInfo).toHaveBeenCalledWith(
      `Fragment 2 of 2 sent to the GP2GP Messenger - with old Message ID ${inboundMessageId2}, and new Message ID ${outboundMessageId2}.`
    );
    expect(logInfo).toHaveBeenCalledWith(
      `All fragments have been successfully sent to GP2GP Messenger.`
    );
  });

  /*
    this test assumes that the fragment transfer has previously taken place, the first message fragment sent correctly
    but crashed during the sending of the second message. It should not re-send the first fragment, but should re-send
    the second fragment.

    This is a situation we've seen previously when the system crashed, producing an OutOfMemoryError. It then rebooted
    and attempted to retry sending the message, as it was not acknowledged.
   */
  it('should send fragment with database status FRAGMENT_REQUEST_RECEIVED when receiving a retried continue request', async () => {
    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepositoryResponse);
    getAllMessageIdPairs.mockResolvedValueOnce(messageIdsWithReplacements);
    getAllFragmentIdsToBeSent.mockResolvedValueOnce([
      { oldMessageId: inboundMessageId2, newMessageId: outboundMessageId2 }
    ]);

    getFragment.mockResolvedValueOnce(fragment2);
    replaceMessageIdsInObject.mockReturnValueOnce(updatedFragment2);
    sendFragment.mockResolvedValue(undefined);

    const result = await transferOutFragmentsForRetriedContinueRequest({
      conversationId: outboundConversationId,
      nhsNumber: nhsNumber,
      odsCode: odsCode
    });

    // then
    expect(result).toBe(undefined);

    expect(getFragmentConversationAndMessageIdsFromEhrRepo).toHaveBeenCalledWith(nhsNumber);
    expect(getAllFragmentIdsToBeSent).toHaveBeenCalledWith(inboundConversationId);
    expect(getFragment).toHaveBeenCalledTimes(1);
    expect(getFragment).toHaveBeenCalledWith(
      inboundConversationId,
      ehrRepositoryResponse.messageIds[1]
    );
    expect(replaceMessageIdsInObject).toHaveBeenCalledTimes(1);
    expect(replaceMessageIdsInObject).toHaveBeenCalledWith(fragment2, [
      messageIdsWithReplacements[1]
    ]);
    expect(sendFragment).toHaveBeenCalledTimes(1);
    expect(sendFragment).toHaveBeenCalledWith(
      inboundConversationId,
      outboundConversationId,
      odsCode,
      updatedFragment2,
      outboundMessageId2,
      inboundMessageId2
    );
    expect(logInfo).toHaveBeenCalledWith(
      'All fragments have been successfully sent to GP2GP Messenger.'
    );
  });

  it('should update fragment status to MISSING_FROM_REPO when unable to find fragment in repo', async () => {
    // given
    const acknowledgementErrorCode = AcknowledgementErrorCode.ERROR_CODE_10_B;
    const error = new PresignedUrlNotFoundError('error', acknowledgementErrorCode);

    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepositoryResponse);
    getAllMessageIdPairs.mockResolvedValueOnce(messageIdsWithReplacements);
    getFragment.mockRejectedValueOnce(error);

    // then
    await expect(
      transferOutFragmentsForNewContinueRequest({
        conversationId: outboundConversationId,
        nhsNumber: nhsNumber,
        odsCode: odsCode
      })
    ).rejects.toThrow(error);

    expect(getFragmentConversationAndMessageIdsFromEhrRepo).toHaveBeenCalledWith(nhsNumber);
    expect(getAllMessageIdPairs).toHaveBeenCalledWith(
      ehrRepositoryResponse.messageIds,
      inboundConversationId
    );
    expect(getFragment).toHaveBeenCalledTimes(1);
    expect(getFragment).toHaveBeenCalledWith(
      inboundConversationId,
      ehrRepositoryResponse.messageIds[0]
    );
    expect(updateFragmentStatus).toHaveBeenCalledWith(
      inboundConversationId,
      inboundMessageId1,
      FragmentStatus.OUTBOUND_FAILED,
      FailureReason.MISSING_FROM_REPO
    );

    expect(replaceMessageIdsInObject).not.toHaveBeenCalled();
    expect(sendFragment).not.toHaveBeenCalled();
  });

  it('should update fragment status to DOWNLOAD_FAILED when unable to retrieve fragment from presigned url', async () => {
    // given
    const acknowledgementErrorCode = AcknowledgementErrorCode.ERROR_CODE_10_A;
    const error = new DownloadError(null, acknowledgementErrorCode);

    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepositoryResponse);
    getAllMessageIdPairs.mockResolvedValueOnce(messageIdsWithReplacements);
    getFragment.mockRejectedValueOnce(error);

    // then
    await expect(
      transferOutFragmentsForNewContinueRequest({
        conversationId: outboundConversationId,
        nhsNumber: nhsNumber,
        odsCode: odsCode
      })
    ).rejects.toThrow(error);

    expect(getFragmentConversationAndMessageIdsFromEhrRepo).toHaveBeenCalledWith(nhsNumber);
    expect(getAllMessageIdPairs).toHaveBeenCalledWith(
      ehrRepositoryResponse.messageIds,
      inboundConversationId
    );
    expect(getFragment).toHaveBeenCalledTimes(1);
    expect(getFragment).toHaveBeenCalledWith(
      inboundConversationId,
      ehrRepositoryResponse.messageIds[0]
    );
    expect(updateFragmentStatus).toHaveBeenCalledWith(
      inboundConversationId,
      inboundMessageId1,
      FragmentStatus.OUTBOUND_FAILED,
      FailureReason.DOWNLOAD_FAILED
    );

    expect(replaceMessageIdsInObject).not.toHaveBeenCalled();
    expect(sendFragment).not.toHaveBeenCalled();
  });

  it('should update fragment status to SENDING_FAILED when sending fragment to GP2GP Messenger fails', async () => {
    // given
    const error = new FragmentSendingError('error', outboundMessageId1);

    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepositoryResponse);
    getAllMessageIdPairs.mockResolvedValueOnce(messageIdsWithReplacements);
    getFragment.mockResolvedValueOnce(fragment1);
    replaceMessageIdsInObject.mockReturnValueOnce(updatedFragment1);
    sendFragment.mockRejectedValueOnce(error);

    // then
    await expect(
      transferOutFragmentsForNewContinueRequest({
        conversationId: outboundConversationId,
        nhsNumber: nhsNumber,
        odsCode: odsCode
      })
    ).rejects.toThrow(error);

    expect(getFragmentConversationAndMessageIdsFromEhrRepo).toHaveBeenCalledWith(nhsNumber);
    expect(getAllMessageIdPairs).toHaveBeenCalledWith(
      ehrRepositoryResponse.messageIds,
      inboundConversationId
    );
    expect(getFragment).toHaveBeenCalledTimes(1);
    expect(getFragment).toHaveBeenCalledWith(
      inboundConversationId,
      ehrRepositoryResponse.messageIds[0]
    );
    expect(replaceMessageIdsInObject).toHaveBeenCalledTimes(1);
    expect(replaceMessageIdsInObject).toHaveBeenCalledWith(fragment1, messageIdsWithReplacements);
    expect(sendFragment).toHaveBeenCalledTimes(1);
    expect(updateFragmentStatus).toHaveBeenCalledWith(
      inboundConversationId,
      inboundMessageId1,
      FragmentStatus.OUTBOUND_FAILED,
      FailureReason.SENDING_FAILED
    );
  });
});
