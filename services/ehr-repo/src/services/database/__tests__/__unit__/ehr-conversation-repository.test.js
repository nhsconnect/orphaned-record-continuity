import { updateConversationToCoreReceived } from '../../ehr-conversation-repository';
import { EhrTransferTracker } from '../../dynamo-ehr-transfer-tracker';
import { logError } from '../../../../middleware/logging';
import { buildConversationUpdateParams } from '../../../../models/conversation';
import { ConversationStatus } from '../../../../models/enums';
import { v4 as uuid } from 'uuid';

jest.mock('../../dynamo-ehr-transfer-tracker');
jest.mock('../../../../models/conversation');
jest.mock('../../../../middleware/logging');

describe('ehr-conversation-repository', () => {
  describe('updateConversationToCoreReceived', () => {
    it('should call updateItemsInTransaction when valid Conversation ID is provided', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const mockedUpdateParam = {};
      const mockedDbInstance = {
        updateItemsInTransaction: jest.fn().mockResolvedValueOnce()
      };

      // when
      EhrTransferTracker.getInstance.mockReturnValueOnce(mockedDbInstance);
      buildConversationUpdateParams.mockReturnValueOnce(mockedUpdateParam);

      await updateConversationToCoreReceived(conversationId);

      // then
      expect(EhrTransferTracker.getInstance).toHaveBeenCalled();
      expect(buildConversationUpdateParams).toHaveBeenCalledWith(conversationId, {
        TransferStatus: ConversationStatus.CORE_RECEIVED
      });
      expect(mockedDbInstance.updateItemsInTransaction).toHaveBeenCalledWith([mockedUpdateParam]);
    });

    it('should throw an error if updateItemsInTransaction throws an error', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const error = new Error();
      const mockedDbInstance = {
        updateItemsInTransaction: jest.fn().mockRejectedValueOnce(error)
      };

      // when
      EhrTransferTracker.getInstance.mockReturnValueOnce(mockedDbInstance);

      // then
      await expect(() => updateConversationToCoreReceived(conversationId)).rejects.toThrow(error);
      expect(logError).toHaveBeenCalled();
    });
  });
});
