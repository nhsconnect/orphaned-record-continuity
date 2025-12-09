import { EhrTransferTracker } from '../dynamo-ehr-transfer-tracker';
import { v4 as uuid } from 'uuid';
import { RecordType } from '../../../models/enums';
import {
  cleanupRecordsForTest,
  createConversationForTest,
  generateMultipleUUID
} from '../../../utilities/integration-test-utilities';
import { buildCore } from '../../../models/core';
import { buildFragmentUpdateParams } from '../../../models/fragment';

// suppress logs
jest.mock('../../../middleware/logging');

describe('EhrTransferTracker', () => {
  const testConversationId = uuid().toUpperCase();
  const testNhsNumber = '9000000001';

  afterEach(async () => {
    await cleanupRecordsForTest(testConversationId);
  });

  it('can create and read a record in dynamodb', async () => {
    // given
    const db = EhrTransferTracker.getInstance();
    const testMessageId = uuid().toUpperCase();
    await createConversationForTest(testConversationId, testNhsNumber);

    const ehrCore = buildCore(testConversationId, testMessageId);

    await db.writeItemsInTransaction([ehrCore]);

    // then
    const actual = await db.queryTableByConversationId(testConversationId, RecordType.CORE);

    expect(actual).toHaveLength(1);
    expect(actual[0]).toMatchObject({
      InboundConversationId: testConversationId,
      InboundMessageId: testMessageId,
      Layer: RecordType.CORE,
      ReceivedAt: expect.any(String),
      CreatedAt: expect.any(String),
      UpdatedAt: expect.any(String)
    });
  });

  describe('writeItemsInTransaction / updateItemsInTransaction', () => {
    it('can write / update multiple items into dynamodb', async () => {
      const testSize = 120;
      const db = EhrTransferTracker.getInstance();
      const fragmentIds = generateMultipleUUID(testSize);
      const fragments = fragmentIds.map((fragmentId) => {
        return {
          InboundConversationId: testConversationId,
          Layer: `FRAGMENT#${fragmentId}`,
          TestColumn: 'test'
        };
      });

      await db.writeItemsInTransaction(fragments);

      const records = await db.queryTableByConversationId(testConversationId);
      expect(records).toHaveLength(testSize);
      records.forEach((item) => {
        expect(item).toMatchObject({
          InboundConversationId: testConversationId,
          Layer: expect.stringContaining('FRAGMENT#'),
          TestColumn: 'test'
        });
      });

      const updates = fragmentIds.map((fragmentId) =>
        buildFragmentUpdateParams(testConversationId, fragmentId, {
          TransferStatus: 'test update fields'
        })
      );

      await db.updateItemsInTransaction(updates);

      const updatedRecords = await db.queryTableByConversationId(testConversationId);
      updatedRecords.forEach((item) => {
        expect(item).toMatchObject({
          InboundConversationId: testConversationId,
          Layer: expect.stringContaining('FRAGMENT#'),
          TransferStatus: 'test update fields',
          TestColumn: 'test'
        });
      });
    });
  });
});
