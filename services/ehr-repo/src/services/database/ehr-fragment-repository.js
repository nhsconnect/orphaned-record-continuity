import { getUKTimestamp } from '../time';
import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import { buildFragmentUpdateParams } from '../../models/fragment';
import { FragmentStatus, RecordType } from '../../models/enums';
import { logError } from '../../middleware/logging';

export const markFragmentAsReceivedAndCreateItsParts = async (
  messageId,
  conversationId,
  remainingPartsIds = []
) => {
  try {
    const db = EhrTransferTracker.getInstance();
    const timestamp = getUKTimestamp();

    const currentFragmentParams = buildFragmentUpdateParams(conversationId, messageId, {
      ReceivedAt: timestamp,
      TransferStatus: FragmentStatus.COMPLETE
    });

    const childFragmentsParams = remainingPartsIds.map((fragmentPartId) => {
      return buildFragmentUpdateParams(conversationId, fragmentPartId, { ParentId: messageId });
    });

    await db.updateItemsInTransaction([currentFragmentParams, ...childFragmentsParams]);
  } catch (e) {
    logError('Message could not be stored', e);
    throw e;
  }
};

export const getFragmentByKey = (inboundConversationId, inboundMessageId) => {
  const db = EhrTransferTracker.getInstance();
  return db.getItemByKey(inboundConversationId, inboundMessageId, RecordType.FRAGMENT);
};

export const fragmentExistsInRecord = async (inboundConversationId, inboundMessageId) => {
  try {
    const fragment = await getFragmentByKey(inboundConversationId, inboundMessageId);
    return !!fragment;
  } catch (e) {
    logError('Querying database for fragment message failed', e);
    throw e;
  }
};

export const fragmentAlreadyReceived = async (conversationId, messageId) => {
  try {
    const fragment = await getFragmentByKey(conversationId, messageId);
    return fragment?.ReceivedAt !== undefined;
  } catch (e) {
    logError('Querying database for fragment message failed', e);
    throw e;
  }
};
