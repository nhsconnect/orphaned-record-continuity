import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import { buildCoreUpdateParams, isCore } from '../../../models/core';
import { logInfo } from '../../../middleware/logging';
import { CoreStatus } from '../../../constants/enums';

export const messageIdMatchOutboundCore = async (outboundConversationId, messageId) => {
  logInfo('Comparing the received messageId with outbound records');

  const db = EhrTransferTracker.getInstance();
  const queryResult = await db.queryTableByOutboundConversationId(outboundConversationId);
  const core = queryResult.filter(isCore)[0];

  logInfo(
    'Found core record for this conversation, will compare the messageId with the OutboundMessageId of core'
  );

  return messageId === core.OutboundMessageId;
};

export const updateCoreStatusInDb = async (
  outboundConversationId,
  status,
  failureReason = null
) => {
  const database = EhrTransferTracker.getInstance();

  const coreRecord = await getCoreByOutboundConversationId(outboundConversationId);
  if (!coreRecord) {
    throw new Error(
      `Could not find a CORE record with Outbound Conversation ID ${outboundConversationId}`
    );
  }

  const inboundConversationId = coreRecord.InboundConversationId;
  const updateContent = {
    TransferStatus: status
  };
  if (status === CoreStatus.OUTBOUND_FAILED && failureReason) {
    updateContent.FailureReason = failureReason;
  }

  const updateParams = buildCoreUpdateParams(inboundConversationId, updateContent);
  await database.updateSingleItem(updateParams);
};

export const getCoreByOutboundConversationId = async outboundConversationId => {
  const database = EhrTransferTracker.getInstance();
  const outboundRecords = await database.queryTableByOutboundConversationId(outboundConversationId);
  const coreRecord = outboundRecords?.filter(isCore)?.[0];
  if (!coreRecord) {
    return null;
  }
  return coreRecord;
};
