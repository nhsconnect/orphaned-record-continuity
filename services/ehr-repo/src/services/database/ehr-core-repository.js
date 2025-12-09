import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import { buildMultipleFragments } from '../../models/fragment';
import { buildCore } from '../../models/core';
import { RecordType } from '../../models/enums';
import { logError } from '../../middleware/logging';

export const createCore = async ({ conversationId, messageId, fragmentMessageIds = [] }) => {
  try {
    const db = EhrTransferTracker.getInstance();
    const itemsToWrite = [buildCore(conversationId, messageId)];

    if (fragmentMessageIds) {
      const directFragments = buildMultipleFragments({
        inboundConversationId: conversationId,
        fragmentMessageIds,
        parentMessageId: messageId
      });
      itemsToWrite.push(...directFragments);
    }

    await db.writeItemsInTransaction(itemsToWrite);
  } catch (e) {
    logError('Message could not be stored', e);
    throw e;
  }
};

export const getCoreByKey = (inboundConversationId, inboundMessageId) => {
  const db = EhrTransferTracker.getInstance();
  return db.getItemByKey(inboundConversationId, inboundMessageId, RecordType.CORE);
};
