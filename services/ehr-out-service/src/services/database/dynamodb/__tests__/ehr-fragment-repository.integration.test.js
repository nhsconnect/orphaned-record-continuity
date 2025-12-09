import { v4 } from 'uuid';
import { FragmentMessageIdReplacementRecordNotFoundError } from '../../../../errors/errors';
import { EhrTransferTracker } from '../dynamo-ehr-transfer-tracker';
import {
  cleanupRecordsForTest,
  createInboundCompleteRecordForTest
} from '../../../../utilities/integration-test-utilities';
import {
  getAllMessageIdPairs,
  getAllFragmentIdsToBeSent,
  updateFragmentStatusInDb,
  storeAcknowledgement
} from '../ehr-fragment-repository';
import { createOutboundConversation } from '../outbound-conversation-repository';
import { storeOutboundMessageIds } from '../store-outbound-message-ids';
import { FailureReason, FragmentStatus, RecordType } from '../../../../constants/enums';
import { logError, logInfo } from '../../../../middleware/logging';
import { buildUpdateParamFromItem } from '../../../../utilities/dynamodb-helper';

jest.mock('../../../../middleware/logging');

const uuid = () => v4().toUpperCase();
describe('dynamodb-fragment-repository', () => {
  // ================ CONSTANTS AND SETUPS =====================
  const db = EhrTransferTracker.getInstance();

  const ODS_CODE = 'B12345';
  const INBOUND_CONVERSATION_ID = uuid().toUpperCase();
  const OUTBOUND_CONVERSATION_ID = uuid().toUpperCase();
  const NHS_NUMBER = '9000000005';

  let messageIdReplacementRecords = [];
  const numberOfRecordsToSeed = 10;

  let inboundCoreMessageId;
  let inboundFragmentMessageIds;

  // Seed the database with test values.
  beforeEach(async () => {
    messageIdReplacementRecords = seedTestData(numberOfRecordsToSeed);
    const oldMessageIds = getAllOldMessageIds(messageIdReplacementRecords);
    inboundCoreMessageId = oldMessageIds[0];
    inboundFragmentMessageIds = oldMessageIds.slice(1);

    // mimic the flow of ehr-in and then ehr-out
    await createInboundCompleteRecordForTest(
      INBOUND_CONVERSATION_ID,
      NHS_NUMBER,
      inboundCoreMessageId,
      inboundFragmentMessageIds
    );
    await createOutboundConversation(OUTBOUND_CONVERSATION_ID, NHS_NUMBER, ODS_CODE);
    await storeOutboundMessageIds(messageIdReplacementRecords, INBOUND_CONVERSATION_ID);
  });

  afterEach(async () => {
    await cleanupRecordsForTest(INBOUND_CONVERSATION_ID);
    messageIdReplacementRecords = {};
  });

  describe('getAllMessageIdPairs', () => {
    it('should retrieve all of the message ids with replacements successfully', async () => {
      // given
      const expectedFragmentIdPairs = messageIdReplacementRecords.slice(1);
      const oldFragmentIds = getAllOldMessageIds(expectedFragmentIdPairs);

      // when
      const result = await getAllMessageIdPairs(oldFragmentIds, INBOUND_CONVERSATION_ID);

      // then
      expect(result.length).toEqual(oldFragmentIds.length);
      expect(result).toEqual(expect.arrayContaining(expectedFragmentIdPairs));
    });

    it('should throw FragmentMessageIdReplacementRecordNotFoundError if provided a non-existent message id', async () => {
      // given
      const nonExistentMessageId = 'faed05ff-7f8f-41f9-b44f-5e98289c98f2';
      const oldMessageIdsWithOneInvalid = [
        ...getAllOldMessageIds(messageIdReplacementRecords).slice(0, numberOfRecordsToSeed - 1),
        nonExistentMessageId
      ];

      // when
      await expect(
        getAllMessageIdPairs(oldMessageIdsWithOneInvalid, INBOUND_CONVERSATION_ID)
        // then
      ).rejects.toThrow(FragmentMessageIdReplacementRecordNotFoundError);
    });
  });

  describe('updateFragmentStatusInDb', () => {
    it('should update message fragment status successfully', async () => {
      // given
      const updatedStatus = FragmentStatus.OUTBOUND_COMPLETE;
      const inboundMessageId = getAllOldMessageIds(messageIdReplacementRecords)[1];
      const outboundMessageId = getAllNewMessageIds(messageIdReplacementRecords)[1];

      // when
      await updateFragmentStatusInDb(INBOUND_CONVERSATION_ID, inboundMessageId, updatedStatus);

      // then
      const updatedFragment = await db.getItemByKey(
        INBOUND_CONVERSATION_ID,
        inboundMessageId,
        RecordType.FRAGMENT
      );

      expect(updatedFragment).toMatchObject({
        InboundConversationId: INBOUND_CONVERSATION_ID,
        OutboundConversationId: OUTBOUND_CONVERSATION_ID,
        InboundMessageId: inboundMessageId,
        OutboundMessageId: outboundMessageId,
        TransferStatus: updatedStatus
      });

      expect(logInfo).toHaveBeenCalledWith('Updated message fragment status has been stored');
    });

    it('should update message fragment status with a failure reason if given', async () => {
      // given
      const updatedStatus = FragmentStatus.OUTBOUND_FAILED;
      const failureReason = FailureReason.CORE_SENDING_FAILED;
      const inboundMessageId = getAllOldMessageIds(messageIdReplacementRecords)[1];
      const outboundMessageId = getAllNewMessageIds(messageIdReplacementRecords)[1];

      // when
      await updateFragmentStatusInDb(
        INBOUND_CONVERSATION_ID,
        inboundMessageId,
        updatedStatus,
        failureReason
      );

      // then
      const updatedFragment = await db.getItemByKey(
        INBOUND_CONVERSATION_ID,
        inboundMessageId,
        RecordType.FRAGMENT
      );

      expect(updatedFragment).toMatchObject({
        InboundConversationId: INBOUND_CONVERSATION_ID,
        OutboundConversationId: OUTBOUND_CONVERSATION_ID,
        InboundMessageId: inboundMessageId,
        OutboundMessageId: outboundMessageId,
        TransferStatus: updatedStatus,
        FailureReason: failureReason
      });

      expect(logInfo).toHaveBeenCalledWith('Updated message fragment status has been stored');
    });
  });

  describe('getAllFragmentIdsToBeSent', () => {
    it('should get all eligible fragment Message IDs successfully', async () => {
      // given
      // calling .slice(1) here as the 0th message id in seed array is used for the core
      const allFragmentMessageIds = messageIdReplacementRecords.slice(1);

      // when
      const response = await getAllFragmentIdsToBeSent(INBOUND_CONVERSATION_ID);

      // then
      expect(response.length).toEqual(allFragmentMessageIds.length);
      expect(response).toEqual(expect.arrayContaining(allFragmentMessageIds));
    });

    it('should only return the eligible fragments', async () => {
      // given
      const allFragmentMessageIds = messageIdReplacementRecords.slice(1);
      const eligibleMessageIds = allFragmentMessageIds.slice(5);
      const ineligibleMessageIds = allFragmentMessageIds.filter(
        item => !eligibleMessageIds.includes(item)
      );

      const mockSomeFragmentsAlreadySentOut = ineligibleMessageIds.map(({ oldMessageId }) =>
        buildUpdateParamFromItem(
          {
            InboundConversationId: INBOUND_CONVERSATION_ID,
            Layer: `FRAGMENT#${oldMessageId}`
          },
          { TransferStatus: FragmentStatus.OUTBOUND_SENT }
        )
      );
      await db.updateItemsInTransaction(mockSomeFragmentsAlreadySentOut);

      // when
      const response = await getAllFragmentIdsToBeSent(INBOUND_CONVERSATION_ID);

      // then
      expect(response.length).not.toEqual(allFragmentMessageIds.length);
      expect(response.length).toEqual(eligibleMessageIds.length);
      expect(response).toEqual(expect.arrayContaining(eligibleMessageIds));
    });
  });

  describe('storeAcknowledgement', () => {
    it('should create an acknowledgement with the correct values', async () => {
      const outboundMessageIdOfFragment = getAllNewMessageIds(messageIdReplacementRecords)[1];
      const inboundMessageIdOfFragment = messageIdReplacementRecords.find(
        pair => pair.newMessageId === outboundMessageIdOfFragment
      ).oldMessageId;
      const acknowledgementDetail =
        'hl7:{interactionId}/hl7:communicationFunctionRcv/hl7:device/hl7:id/@extension is missing, empty, invalid or ACL violation';
      const acknowledgementTypeCode = 'AR';
      const acknowledgementCode = 30;

      // given
      const parsedAcknowledgement = {
        service: 'urn:nhs:names:services:gp2gp',
        messageId: uuid().toUpperCase(),
        referencedMessageId: uuid().toUpperCase(),
        messageRef: outboundMessageIdOfFragment,
        acknowledgementTypeCode,
        acknowledgementDetail,
        acknowledgementCode
      };

      // when
      await storeAcknowledgement(parsedAcknowledgement, OUTBOUND_CONVERSATION_ID);

      // then
      const allRecords = await db.queryTableByOutboundConversationId(OUTBOUND_CONVERSATION_ID);
      const updatedFragment = allRecords.find(
        item => item.OutboundMessageId === outboundMessageIdOfFragment
      );
      expect(updatedFragment).not.toBeUndefined();
      expect(updatedFragment).toMatchObject({
        InboundConversationId: INBOUND_CONVERSATION_ID,
        OutboundConversationId: OUTBOUND_CONVERSATION_ID,
        OutboundMessageId: outboundMessageIdOfFragment,
        InboundMessageId: inboundMessageIdOfFragment,
        FailureCode: acknowledgementCode,
        FailureReason: acknowledgementDetail,
        TransferStatus: FragmentStatus.OUTBOUND_FAILED
      });

      expect(logInfo).toHaveBeenCalled();
      expect(logInfo).toHaveBeenCalledWith('Acknowledgement has been stored');
    });

    it('should log errors when invalid parsed acknowledgement is passed', async () => {
      // when
      await storeAcknowledgement({ messageId: 'hello-world' }, INBOUND_CONVERSATION_ID);

      // then
      const errorMessage =
        'Received an acknowledgement message that refers to unknown messageId. Will not proceed further.';
      expect(logError).toHaveBeenCalledWith(errorMessage);
    });
  });
});

const seedTestData = numberOfRecordsToSeed => {
  return Array(numberOfRecordsToSeed)
    .fill(undefined)
    .map(() => {
      return {
        oldMessageId: uuid().toUpperCase(),
        newMessageId: uuid().toUpperCase()
      };
    });
};

const getAllOldMessageIds = records => records.map(record => record.oldMessageId);
const getAllNewMessageIds = records => records.map(record => record.newMessageId);
