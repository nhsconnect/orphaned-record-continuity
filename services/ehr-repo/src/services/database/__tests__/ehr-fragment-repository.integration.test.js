import { v4 as uuid } from 'uuid';
import { logError } from '../../../middleware/logging';
import { createCore } from '../ehr-core-repository';
import {
  fragmentAlreadyReceived,
  fragmentExistsInRecord,
  getFragmentByKey,
  markFragmentAsReceivedAndCreateItsParts
} from '../ehr-fragment-repository';
import { buildFragment } from '../../../models/fragment';
import { EhrTransferTracker } from '../dynamo-ehr-transfer-tracker';

// Mocking
jest.mock('../../../middleware/logging');

describe('ehr-fragment-repository', () => {
  // ========================= COMMON PROPERTIES =========================
  const expectedTimestamp = '2024-03-06T12:34:56+00:00';
  const mockTime = new Date(Date.parse(expectedTimestamp));
  const db = EhrTransferTracker.getInstance();

  // ========================= HELPERS========== =========================
  const markFragmentAsReceived = markFragmentAsReceivedAndCreateItsParts;
  const tableNameBackup = db.tableName;
  const mimicDynamodbFail = () => {
    db.tableName = 'non-exist-table';
  };
  const undoMimicDynamodbFail = () => {
    db.tableName = tableNameBackup;
  };

  // ========================= SET UP / TEAR DOWN ========================
  beforeEach(async () => {
    jest.useFakeTimers().setSystemTime(mockTime);
  });
  afterEach(() => {
    undoMimicDynamodbFail();
  });
  // =====================================================================
  describe('markFragmentAsReceivedAndCreateItsParts', () => {
    it('should update receivedAt for a fragment with current date', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const ehrMessageId = uuid().toUpperCase();
      const fragmentMessageId = uuid().toUpperCase();
      await createCore({
        conversationId,
        messageId: ehrMessageId,
        fragmentMessageIds: [fragmentMessageId]
      });

      // when
      await markFragmentAsReceivedAndCreateItsParts(fragmentMessageId, conversationId);

      // then
      const fragment = await getFragmentByKey(conversationId, fragmentMessageId);
      expect(fragment.ReceivedAt).toEqual(expectedTimestamp);
    });

    it('should not update receivedAt for a given fragment if database update query throws', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      try {
        // when
        await markFragmentAsReceivedAndCreateItsParts('not-valid', conversationId);
      } catch (err) {
        // then
        expect(err).not.toBeNull();
        expect(logError).toHaveBeenCalledWith('Message could not be stored', err);
      }
    });

    it('should create messages for nested fragments', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const ehrMessageId = uuid().toUpperCase();
      const fragmentMessageId = uuid().toUpperCase();
      const nestedFragmentMessageId = uuid().toUpperCase();
      await createCore({
        conversationId,
        messageId: ehrMessageId,
        fragmentMessageIds: [fragmentMessageId]
      });

      // when
      await markFragmentAsReceivedAndCreateItsParts(fragmentMessageId, conversationId, [
        nestedFragmentMessageId
      ]);

      // then
      const nestedFragmentMessage = await getFragmentByKey(conversationId, nestedFragmentMessageId);
      expect(nestedFragmentMessage.ReceivedAt).toEqual(undefined);
      expect(nestedFragmentMessage.ParentId).toEqual(fragmentMessageId);
      expect(nestedFragmentMessage.InboundConversationId).toEqual(conversationId);
    });

    it('should update parentId for a nested fragment already existing in the DB', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const ehrMessageId = uuid().toUpperCase();
      const fragmentMessageId = uuid().toUpperCase();
      const nestedFragmentMessageId = uuid().toUpperCase();

      await createCore({
        conversationId,
        messageId: ehrMessageId,
        fragmentMessageIds: [fragmentMessageId]
      });

      const nestedFragmentArrivedEarly = buildFragment({
        inboundConversationId: conversationId,
        fragmentMessageId: nestedFragmentMessageId
      });
      const db = EhrTransferTracker.getInstance();
      await db.writeItemsInTransaction([nestedFragmentArrivedEarly]);

      // when
      await markFragmentAsReceivedAndCreateItsParts(fragmentMessageId, conversationId, [
        nestedFragmentMessageId
      ]);

      // then
      const nestedFragmentMessage = await getFragmentByKey(conversationId, nestedFragmentMessageId);
      expect(nestedFragmentMessage.ParentId).toEqual(fragmentMessageId);
    });
  });

  describe('fragmentExistsInRecord', () => {
    it('should return true for a fragment existing in the database', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();

      const fragment = buildFragment({
        inboundConversationId: conversationId,
        fragmentMessageId: messageId
      });
      const db = EhrTransferTracker.getInstance();
      await db.writeItemsInTransaction([fragment]);

      // when
      const result = await fragmentExistsInRecord(conversationId, messageId);

      // then
      expect(result).toBe(true);
    });

    it('should return false for a fragment that does not exist in the database', async () => {
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();
      expect(await fragmentExistsInRecord(conversationId, messageId)).toBe(false);
    });

    it('should throw if database querying throws', async () => {
      // given
      mimicDynamodbFail();

      try {
        // when
        await fragmentExistsInRecord(uuid().toUpperCase(), uuid().toUpperCase());
      } catch (err) {
        // then
        expect(err).not.toBeNull();
        expect(logError).toHaveBeenCalledWith('Querying database for fragment message failed', err);
      }
    });
  });

  describe('markFragmentAsReceivedAndCreateItsParts', () => {
    // Note: this describe block is migrated from the tests of old method "createFragmentPart"
    it('should create fragment entry in the database', async () => {
      const messageId = uuid().toUpperCase();
      const conversationId = uuid().toUpperCase();
      await markFragmentAsReceivedAndCreateItsParts(messageId, conversationId);

      const fragment = await getFragmentByKey(conversationId, messageId);

      expect(fragment.InboundConversationId).toEqual(conversationId);
      expect(fragment.ReceivedAt).toEqual(expectedTimestamp);
      expect(fragment.Layer).toEqual(`FRAGMENT#${messageId}`);
      expect(fragment.ParentId).toBeUndefined();
    });

    it('should throw if database creation query throws', async () => {
      const conversationId = uuid().toUpperCase();
      const messageId = 'not-valid';
      let err = null;
      try {
        await markFragmentAsReceivedAndCreateItsParts(messageId, conversationId);
      } catch (error) {
        err = error;
      }
      expect(err).not.toBeNull();
      expect(logError).toHaveBeenCalledWith('Message could not be stored', err);
    });
  });

  describe('fragmentAlreadyReceived', () => {
    it("should return false if 'messageId' is not found in db", async () => {
      // given
      const messageId = uuid().toUpperCase();

      // when
      const result = await fragmentAlreadyReceived(messageId);

      // then
      expect(result).toEqual(false);
    });

    it("should return true if 'messageId' is received in db", async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();
      const fragmentMessageId = uuid().toUpperCase();

      await createCore({ conversationId, messageId, fragmentMessageIds: [fragmentMessageId] });
      await markFragmentAsReceived(fragmentMessageId, conversationId);

      // when
      const result = await fragmentAlreadyReceived(conversationId, fragmentMessageId);

      // then
      expect(result).toEqual(true);
    });

    it('should throw if database querying throws', async () => {
      // given
      const messageId = uuid().toUpperCase();
      mimicDynamodbFail();

      // when
      try {
        await fragmentAlreadyReceived(messageId);
        fail('should have throw');
      } catch (err) {
        // then
        expect(err).not.toBeNull();
        expect(logError).toHaveBeenCalledWith('Querying database for fragment message failed', err);
      }
    });
  });
});
