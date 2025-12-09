import { v4 as uuid } from 'uuid';

import { logError } from '../../../middleware/logging';
import { createCore, getCoreByKey } from '../ehr-core-repository';
import { getFragmentByKey } from '../ehr-fragment-repository';
import { FragmentStatus, RecordType } from '../../../models/enums';

// Mocking
jest.mock('../../../middleware/logging');

describe('ehr-core-repository', () => {
  const expectedTimestamp = '2024-03-06T12:34:56+00:00';
  const mockTime = new Date(Date.parse(expectedTimestamp));

  beforeEach(async () => {
    jest.useFakeTimers().setSystemTime(mockTime);
  });

  describe('createCore', () => {
    it('should create core message in db', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();
      const ehrExtract = { messageId, conversationId, fragmentMessageIds: [] };

      // when
      await createCore(ehrExtract);

      // then
      const actualMessage = await getCoreByKey(conversationId, messageId);

      expect(actualMessage.InboundMessageId).toBe(messageId);
      expect(actualMessage.InboundConversationId).toBe(conversationId);
      expect(actualMessage.Layer).toBe(RecordType.CORE);
      expect(actualMessage.ReceivedAt).toEqual(expectedTimestamp);
    });

    // Note: old test it('should create health record in db') is not migrated,
    // as now the responsibility of creating a new conversation is handled by another service

    it('should create fragments message in db when health record has fragments', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();
      const fragmentMessageId = uuid().toUpperCase();
      const fragmentMessageIds = [fragmentMessageId];
      const ehrExtract = { messageId, conversationId, fragmentMessageIds };

      // when
      await createCore(ehrExtract);

      // then
      const fragmentMessage = await getFragmentByKey(conversationId, fragmentMessageId);
      expect(fragmentMessage.InboundConversationId).toBe(conversationId);
      expect(fragmentMessage.Layer).toBe(`FRAGMENT#${fragmentMessageId}`);
      expect(fragmentMessage.InboundMessageId).toBe(fragmentMessageId);
      expect(fragmentMessage.ParentId).toBe(messageId);
      expect(fragmentMessage.TransferStatus).toBe(FragmentStatus.INBOUND_PENDING);
      expect(fragmentMessage.ReceivedAt).toBeUndefined();
    });

    it('should not save message with wrong type', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = 'not-a-valid-message-id';
      const ehrExtract = {
        messageId,
        conversationId,
        fragmentMessageIds: []
      };

      try {
        // when
        await createCore(ehrExtract);
      } catch (err) {
        // then
        expect(err).not.toBeNull();
        expect(logError).toHaveBeenCalledWith('Message could not be stored', err);
      }
      const actualMessage = await getCoreByKey(conversationId, messageId);
      expect(actualMessage).toBeNull();
    });

    // Note: not migrating old test it('should not save message or health record with wrong nhs number'),
    // because now the nhs number field only present at conversation level.
  });
});
