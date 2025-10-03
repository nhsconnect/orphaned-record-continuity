import { EhrTransferTracker } from '../dynamo-ehr-transfer-tracker';
import { v4 as uuid } from 'uuid';
import { RecordType } from '../../../../constants/enums';
import { cleanupRecordsForTest } from '../../../../utilities/integration-test-utilities';
import { createRandomUUID } from '../../../gp2gp/__tests__/test-utils';
import { buildFragmentUpdateParams } from '../../../../models/fragment';

// suppress logs
jest.mock('../../../../middleware/logging');

describe('EhrTransferTracker', () => {
  const inboundConversationId = uuid().toUpperCase();

  afterEach(async () => {
    await cleanupRecordsForTest(inboundConversationId);
  });

  it('can create and read a record in dynamodb', async () => {
    // given
    const db = EhrTransferTracker.getInstance();
    const testMessageId = uuid().toUpperCase();

    const ehrCore = {
      InboundConversationId: inboundConversationId,
      InboundMessageId: testMessageId,
      Layer: 'CORE'
    };

    await db.writeItemsInTransaction([ehrCore]);

    // then
    const actual = await db.queryTableByInboundConversationId(
      inboundConversationId,
      RecordType.CORE
    );

    expect(actual).toHaveLength(1);
    expect(actual[0]).toMatchObject({
      InboundConversationId: inboundConversationId,
      InboundMessageId: testMessageId,
      Layer: 'CORE'
    });
  });

  describe('writeItemsInTransaction / updateItemsInTransaction', () => {
    it('can write / update multiple items into dynamodb', async () => {
      const testSize = 120;
      const db = EhrTransferTracker.getInstance();
      const fragmentIds = createRandomUUID(testSize);
      const fragments = fragmentIds.map(fragmentId => {
        return {
          InboundConversationId: inboundConversationId,
          Layer: `FRAGMENT#${fragmentId}`,
          TestColumn: 'test',
          CreatedAt: 'test'
        };
      });

      await db.writeItemsInTransaction(fragments);

      const records = await db.queryTableByInboundConversationId(inboundConversationId);
      expect(records).toHaveLength(testSize);
      records.forEach(item => {
        expect(item).toMatchObject({
          InboundConversationId: inboundConversationId,
          Layer: expect.stringContaining('FRAGMENT#'),
          TestColumn: 'test'
        });
      });

      const updates = fragmentIds.map(fragmentId =>
        buildFragmentUpdateParams(inboundConversationId, fragmentId, {
          TransferStatus: 'test update fields'
        })
      );

      await db.updateItemsInTransaction(updates);

      const updatedRecords = await db.queryTableByInboundConversationId(inboundConversationId);
      updatedRecords.forEach(item => {
        expect(item).toMatchObject({
          InboundConversationId: inboundConversationId,
          Layer: expect.stringContaining('FRAGMENT#'),
          TransferStatus: 'test update fields',
          TestColumn: 'test'
        });
      });
    });
  });
});
