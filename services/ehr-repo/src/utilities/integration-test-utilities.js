import { v4 as uuidv4 } from 'uuid';
import { getUKTimestamp } from '../services/time';
import { EhrTransferTracker } from '../services/database/dynamo-ehr-transfer-tracker';
import { TransactWriteCommand } from '@aws-sdk/lib-dynamodb';
import { RecordType } from '../models/enums';
import chunk from 'lodash.chunk';

export const generateRandomNhsNumber = () => (Math.floor(Math.random() * 9e9) + 1e9).toString();

export const generateMultipleUUID = (amount) =>
  Array(amount)
    .fill(undefined)
    .map(() => uuidv4().toUpperCase());

export const createConversationForTest = async (conversationId, nhsNumber, overrides) => {
  // This method is only meant for testing purpose.
  // the inbound conversation record is supposed to be created by other service.

  if (!IS_IN_LOCAL) {
    throw new Error('Unexpected call to createConversationForTest method in non-local environment');
  }

  const timestamp = getUKTimestamp();
  const db = EhrTransferTracker.getInstance();

  const item = {
    InboundConversationId: conversationId,
    Layer: RecordType.CONVERSATION,
    NhsNumber: nhsNumber,
    CreatedAt: timestamp,
    UpdatedAt: timestamp,
    ...overrides
  };

  await db.writeItemsInTransaction([item]);
};

export const cleanupRecordsForTest = async (conversationId) => {
  // This method is only meant for testing purpose

  if (!IS_IN_LOCAL) {
    throw new Error('Unexpected call to cleanupRecordsForTest method in non-local environment');
  }

  const db = EhrTransferTracker.getInstance();
  const records = await db.queryTableByConversationId(conversationId, RecordType.ALL, true);
  const splitItemBy100 = chunk(records, 100);

  for (const batch of splitItemBy100) {
    const deleteCommand = new TransactWriteCommand({
      TransactItems: batch.map((item) => ({
        Delete: {
          TableName: db.tableName,
          Key: {
            InboundConversationId: item.InboundConversationId,
            Layer: item.Layer
          }
        }
      }))
    });
    await db.client.send(deleteCommand);
  }
};

export const cleanupRecordsForTestByNhsNumber = async (nhsNumber) => {
  // This method is only meant for testing purpose
  const db = EhrTransferTracker.getInstance();
  const allConversations = await db.queryTableByNhsNumber(nhsNumber);
  const removeAllRecords = allConversations.map((item) =>
    cleanupRecordsForTest(item.InboundConversationId)
  );
  return Promise.all(removeAllRecords);
};

export const IS_IN_LOCAL = process.env.NHS_ENVIRONMENT === 'local' || !process.env.NHS_ENVIRONMENT;
