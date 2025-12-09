import { ConversationStatus, HealthRecordStatus, RecordType } from '../../models/enums';
import { logError, logInfo } from '../../middleware/logging';
import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import { buildConversationUpdateParams, isInCompleteStatus } from '../../models/conversation';
import { HealthRecordNotFoundError, CoreNotFoundError } from '../../errors/errors';
import { isCore } from '../../models/core';
import { isFragment } from '../../models/fragment';
import { buildSoftDeleteUpdateParams } from '../../utilities/dynamodb-helper';

export const getConversationStatus = async (conversationId) => {
  try {
    const conversation = await getConversationById(conversationId);
    if (conversation.TransferStatus === ConversationStatus.COMPLETE) {
      return HealthRecordStatus.COMPLETE;
    } else {
      return HealthRecordStatus.PENDING;
    }
  } catch (err) {
    if (err instanceof HealthRecordNotFoundError) {
      logInfo('No record was found for given conversationId');
      return HealthRecordStatus.NOT_FOUND;
    }

    logError('Health Record could not be retrieved from database', err);
    throw err;
  }
};

export const getConversationById = async (conversationId) => {
  const db = EhrTransferTracker.getInstance();

  const results = await db.queryTableByConversationId(conversationId, RecordType.CONVERSATION);
  if (!results || results.length === 0) {
    logInfo('Health Record not found');
    throw new HealthRecordNotFoundError();
  }
  logInfo('Health Record retrieved from the database');
  return results[0];
};

export const updateConversationCompleteness = async (conversationId) => {
  try {
    const db = EhrTransferTracker.getInstance();

    const allFragments = await db.queryTableByConversationId(conversationId, RecordType.FRAGMENT);

    const pendingMessages = allFragments.filter((fragment) => fragment.ReceivedAt === undefined);

    if (pendingMessages.length !== 0) {
      logInfo(`${pendingMessages.length} more fragments to be received.`);
      return;
    }

    logInfo('All fragments are received. Will mark this inbound conversation as complete');

    const updateParam = buildConversationUpdateParams(conversationId, {
      TransferStatus: ConversationStatus.COMPLETE
    });

    await db.updateItemsInTransaction([updateParam]);
  } catch (err) {
    logError('Failed to update health record completeness', err);
    throw err;
  }
};

export const updateConversationToCoreReceived = async (conversationId) => {
  try {
    const db = EhrTransferTracker.getInstance();

    const updateParam = buildConversationUpdateParams(conversationId, {
      TransferStatus: ConversationStatus.CORE_RECEIVED
    });

    await db.updateItemsInTransaction([updateParam]);
  } catch (err) {
    logError(
      `Failed to update Conversation status to ${ConversationStatus.CORE_RECEIVED}: ${
        err.message ? err.message : 'No error message present'
      }`
    );
    throw err;
  }
};

export const getCurrentConversationIdForPatient = async (nhsNumber) => {
  const db = EhrTransferTracker.getInstance();
  const conversations = await db.queryTableByNhsNumber(nhsNumber);

  const completedRecords = conversations?.filter(isInCompleteStatus);

  if (!completedRecords || completedRecords.length === 0) {
    throw new HealthRecordNotFoundError();
  }

  const currentRecord = completedRecords.reduce((prev, current) => {
    return current?.CreatedAt > prev?.CreatedAt ? current : prev;
  });

  return currentRecord.InboundConversationId;
};

export const markRecordAsSoftDeleteForPatient = async (nhsNumber) => {
  const db = EhrTransferTracker.getInstance();
  const allConversations = await db.queryTableByNhsNumber(nhsNumber);
  const allConversationIds = allConversations.map((item) => item.InboundConversationId);

  const allRecords = [];
  for (const conversationId of allConversationIds) {
    const items = await db.queryTableByConversationId(conversationId, RecordType.ALL);
    allRecords.push(...items);
  }

  const allUpdateParams = allRecords.map(buildSoftDeleteUpdateParams);

  await db.updateItemsInTransaction(allUpdateParams);
  return allConversationIds;
};

export const getMessageIdsForConversation = async (conversationId) => {
  const db = EhrTransferTracker.getInstance();
  const items = await db.queryTableByConversationId(conversationId, RecordType.ALL);

  const core = items.filter(isCore)?.[0];
  const fragments = items.filter(isFragment);

  if (!core) {
    throw new CoreNotFoundError();
  }
  const coreMessageId = core.InboundMessageId;
  const fragmentMessageIds = fragments.map((message) => message.InboundMessageId);

  return { coreMessageId, fragmentMessageIds };
};
