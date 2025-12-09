import { TransactWriteCommand, QueryCommand, GetCommand } from '@aws-sdk/lib-dynamodb';
import chunk from 'lodash.chunk';

import { logError, logInfo, logWarning } from '../../middleware/logging';
import { RecordType } from '../../models/enums';
import { getDynamodbClient } from './dynamodb-client';
import { IS_IN_LOCAL } from '../../utilities/integration-test-utilities';

export class EhrTransferTracker {
  /**
   * An abstract layer for accessing the EhrTransferTracker table on dynamodb.
   *
   * Due to singleton nature, this class is NOT supposed to be instantiated by `new EhrTransferTracker()`.
   * Please call the class method `getInstance()` instead.
   */
  constructor() {
    if (EhrTransferTracker._instance) {
      throw new Error("Singleton classes can't be instantiated more than once.");
    }
    EhrTransferTracker._instance = this;

    this.tableName = process.env.DYNAMODB_NAME;

    if (IS_IN_LOCAL) {
      // for running integration test within IDE
      this.tableName = this.tableName ?? 'local-test-db';
    }

    this.client = getDynamodbClient();
  }

  static getInstance() {
    /**
     * Return the existing instance of this class. Create a new one if no instances was created before.
     * This is the supposed way to use this class.
     */
    if (this._instance) {
      return this._instance;
    }
    return new this();
  }

  async writeItemsInTransaction(items) {
    logInfo(`Writing ${items.length} items to dynamodb table`);
    if (!items || !Array.isArray(items)) {
      throw new TypeError('The given argument `items` is not an array');
    }
    if (items.length > 100) {
      logWarning('Cannot write all items in a single transaction due to limitation of dynamodb.');
      logWarning('Will write the items in multiple transactions');
      logWarning('If failed, a complete rollback cannot be guaranteed.');
    }

    const splitItemBy100 = chunk(items, 100);

    for (const batch of splitItemBy100) {
      const command = new TransactWriteCommand({
        TransactItems: batch.map((item) => ({
          Put: {
            TableName: this.tableName,
            Item: item
          }
        }))
      });
      await this.client.send(command);
    }
  }

  async updateItemsInTransaction(updateParams) {
    if (!updateParams || !Array.isArray(updateParams)) {
      throw new TypeError('The given argument `updateParams` is not an array');
    }

    if (updateParams.length > 100) {
      logWarning('Cannot update all items in a single transaction due to limitation of dynamodb.');
      logWarning('Will update the items in multiple transactions');
      logWarning('If failed, a complete rollback cannot be guaranteed.');
    }

    logInfo(`Updating dynamodb record with params: ${JSON.stringify(updateParams)}`);

    const splitItemBy100 = chunk(updateParams, 100);

    for (const batch of splitItemBy100) {
      logInfo(`Updating dynamodb record with params: ${JSON.stringify(batch)}`);
      const command = new TransactWriteCommand({
        TransactItems: batch.map((params) => ({
          Update: {
            TableName: this.tableName,
            ...params
          }
        }))
      });
      await this.client.send(command);
    }
  }

  async queryTableByNhsNumber(nhsNumber, includeDeletedRecord = true) {
    const params = {
      TableName: this.tableName,
      IndexName: 'NhsNumberSecondaryIndex',
      ExpressionAttributeValues: {
        ':nhsNumber': nhsNumber
      },
      ExpressionAttributeNames: {
        '#NhsNumber': 'NhsNumber'
      },
      KeyConditionExpression: '#NhsNumber = :nhsNumber'
    };
    if (!includeDeletedRecord) {
      params.FilterExpression = 'attribute_not_exists(DeletedAt)';
    }
    const command = new QueryCommand(params);

    const response = await this.client.send(command);
    const items = response?.Items;
    if (!items) {
      logError('Received an empty response from dynamodb during query');
      return [];
    }
    return items;
  }

  async queryTableByConversationId(
    inboundConversationId,
    recordType = RecordType.ALL,
    includeDeletedRecord = true
  ) {
    const params = {
      TableName: this.tableName,
      ExpressionAttributeNames: {
        '#PrimaryKey': 'InboundConversationId'
      },
      ExpressionAttributeValues: {
        ':InboundConversationId': inboundConversationId
      },
      KeyConditionExpression: '#PrimaryKey = :InboundConversationId'
    };
    if (!includeDeletedRecord) {
      params.FilterExpression = 'attribute_not_exists(DeletedAt)';
    }

    switch (recordType) {
      case RecordType.ALL:
        break;
      case RecordType.CONVERSATION:
      case RecordType.CORE:
      case RecordType.FRAGMENT:
        params.ExpressionAttributeNames['#sortKey'] = 'Layer';
        params.ExpressionAttributeValues[':sortKey'] = recordType;
        params.KeyConditionExpression += ' AND begins_with(#sortKey, :sortKey)';
        break;
      default:
        logInfo(`Received unexpected queryType: ${recordType}. Will treat it as 'ALL'.`);
    }

    const command = new QueryCommand(params);

    const response = await this.client.send(command);
    const items = response?.Items;
    if (!items) {
      logError('Received an empty response from dynamodb during query');
      return [];
    }
    return items;
  }

  async getItemByKey(inboundConversationId, inboundMessageId, recordType = RecordType.FRAGMENT) {
    const expectedTypes = [RecordType.CORE, RecordType.FRAGMENT];

    if (!expectedTypes.includes(recordType)) {
      throw new Error('recordType has to be either Core or Fragment');
    }
    if (!inboundConversationId && !inboundMessageId) {
      throw new Error('must be called with both inboundConversationId and inboundMessageId');
    }

    const sortKey =
      recordType === RecordType.FRAGMENT ? `${recordType}#${inboundMessageId}` : recordType;
    const command = new GetCommand({
      TableName: this.tableName,
      Key: {
        InboundConversationId: inboundConversationId,
        Layer: sortKey
      }
    });

    const response = await this.client.send(command);

    if (response?.Item) {
      logError('Received an empty response from dynamodb during query');
    }
    return response?.Item ?? null;
  }
}
