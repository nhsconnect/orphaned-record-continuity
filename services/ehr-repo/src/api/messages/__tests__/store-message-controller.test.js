import { v4 as uuid } from 'uuid';
import request from 'supertest';
import app from '../../../app';
import { initializeConfig } from '../../../config';
import { MessageType } from '../../../models/enums';
import {
  getConversationStatus,
  updateConversationCompleteness
} from '../../../services/database/ehr-conversation-repository';
import { createCore } from '../../../services/database/ehr-core-repository';
import {
  fragmentExistsInRecord,
  markFragmentAsReceivedAndCreateItsParts
} from '../../../services/database/ehr-fragment-repository';

jest.mock('../../../services/database/ehr-conversation-repository');
jest.mock('../../../services/database/ehr-core-repository');
jest.mock('../../../services/database/ehr-fragment-repository');
jest.mock('../../../middleware/logging');
jest.mock('../../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({})
}));

describe('storeMessageController', () => {
  initializeConfig.mockReturnValue({
    consumerApiKeys: { TEST_USER: 'correct-key' }
  });

  const authorizationKeys = 'correct-key';
  const conversationId = uuid().toUpperCase();
  const nhsNumber = '1234567890';
  const messageId = uuid().toUpperCase();
  const fragmentMessageIds = [uuid().toUpperCase()];

  describe('success', () => {
    let requestBody;

    beforeEach(() => {
      requestBody = {
        data: {
          type: 'messages',
          id: messageId,
          attributes: {
            conversationId,
            messageType: MessageType.EHR_EXTRACT,
            nhsNumber,
            fragmentMessageIds
          }
        }
      };
    });

    it('should return a 201 and health record status when message has successfully been stored in database', async () => {
      getConversationStatus.mockResolvedValueOnce('complete');
      const ehrExtract = { messageId, conversationId, fragmentMessageIds };
      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(201);
      expect(res.body).toEqual({ healthRecordStatus: 'complete' });
      expect(createCore).toHaveBeenCalledWith(ehrExtract);
      expect(updateConversationCompleteness).toHaveBeenCalledWith(conversationId);
      expect(getConversationStatus).toHaveBeenCalledWith(conversationId);
    });

    it('should update receivedAt for given fragment and store its parts', async () => {
      const nestedFragmentId = uuid().toUpperCase();
      requestBody.data.attributes = {
        messageType: MessageType.FRAGMENT,
        conversationId,
        fragmentMessageIds: [nestedFragmentId]
      };

      fragmentExistsInRecord.mockResolvedValueOnce(true);

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(201);
      expect(createCore).not.toHaveBeenCalled();
      expect(markFragmentAsReceivedAndCreateItsParts).toHaveBeenCalledWith(
        messageId,
        conversationId,
        [nestedFragmentId]
      );
      expect(updateConversationCompleteness).toHaveBeenCalledWith(conversationId);
    });

    it('should create message in the database when a nested fragment arrives before first fragment', async () => {
      const nestedFragmentId = uuid().toUpperCase();
      requestBody.data.id = nestedFragmentId;
      requestBody.data.attributes = {
        messageType: MessageType.FRAGMENT,
        conversationId,
        fragmentMessageIds: []
      };

      fragmentExistsInRecord.mockResolvedValueOnce(false);

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(201);
      expect(fragmentExistsInRecord).toHaveBeenCalledWith(nestedFragmentId);
      expect(markFragmentAsReceivedAndCreateItsParts).toHaveBeenCalledWith(
        nestedFragmentId,
        conversationId,
        []
      );
      expect(updateConversationCompleteness).toHaveBeenCalledWith(conversationId);
    });
  });

  describe('failure', () => {
    const requestBody = {
      data: {
        type: 'messages',
        id: messageId,
        attributes: {
          conversationId,
          messageType: MessageType.EHR_EXTRACT,
          nhsNumber,
          fragmentMessageIds
        }
      }
    };
    it('should return a 503 when message cannot be stored in the database', async () => {
      createCore.mockRejectedValueOnce({ error: 'db is down' });
      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(503);
    });
  });

  describe('validation', () => {
    it('should return 422 and an error message when conversationId is not a UUID', async () => {
      const requestBody = {
        data: {
          attributes: {
            conversationId: 'not-a-uuid'
          }
        }
      };
      const errorMessage = {
        'data.attributes.conversationId': "'conversationId' provided is not a UUID"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when messageId is not a UUID', async () => {
      const requestBody = {
        data: {
          id: 'not-a-uuid'
        }
      };
      const errorMessage = { 'data.id': "'id' provided is not a UUID" };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when type is not messages', async () => {
      const requestBody = {
        data: {
          type: 'not-messages'
        }
      };
      const errorMessage = { 'data.type': 'Invalid value' };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when nhsNumber is not numeric', async () => {
      const requestBody = {
        data: {
          type: 'messages',
          id: uuid().toUpperCase(),
          attributes: {
            conversationId: uuid().toUpperCase(),
            nhsNumber: 'not-an-nhs-number',
            messageType: 'ehrExtract'
          }
        }
      };
      const errorMessage = { 'data.attributes.nhsNumber': "'nhsNumber' provided is not numeric" };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when nhsNumber is not 10 digits long', async () => {
      const requestBody = {
        data: {
          attributes: {
            nhsNumber: '1234567890987654',
            messageType: 'ehrExtract'
          }
        }
      };
      const errorMessage = {
        'data.attributes.nhsNumber': "'nhsNumber' provided is not 10 characters"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when messageType is not recognised', async () => {
      const requestBody = {
        data: {
          attributes: {
            messageType: 'not-a-message-type'
          }
        }
      };
      const errorMessage = {
        'data.attributes.messageType':
          "'messageType' provided is not one of the following: ehrExtract, fragment"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 201 when messageType is recognised', async () => {
      const requestBody = {
        data: {
          type: 'messages',
          id: uuid().toUpperCase(),
          attributes: {
            conversationId: uuid().toUpperCase(),
            nhsNumber: '1234567890',
            messageType: 'ehrExtract',
            fragmentMessageIds: []
          }
        }
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(201);
    });

    it('should return 422 and an error message when nhsNumber has not been provided for messageType ehrExtract', async () => {
      const requestBody = {
        data: {
          attributes: {
            messageType: 'ehrExtract'
          }
        }
      };

      const errorMessage = {
        'data.attributes.nhsNumber': "'nhsNumber' is required for messageType ehrExtract"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 201 when nhsNumber has been provided for messageType ehrExtract', async () => {
      const requestBody = {
        data: {
          type: 'messages',
          id: uuid().toUpperCase(),
          attributes: {
            conversationId: uuid().toUpperCase(),
            nhsNumber: '1234567890',
            messageType: 'ehrExtract',
            fragmentMessageIds: []
          }
        }
      };
      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(201);
    });

    it('should return 422 and an error message when nhsNumber has been provided for messageType fragment', async () => {
      const requestBody = {
        data: {
          attributes: {
            nhsNumber: '1234567890',
            messageType: 'fragment'
          }
        }
      };

      const errorMessage = {
        'data.attributes.nhsNumber': "'nhsNumber' should be empty for messageType fragment"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 201 when nhsNumber has not been provided for messageType fragment', async () => {
      const requestBody = {
        data: {
          type: 'messages',
          id: uuid().toUpperCase(),
          attributes: {
            conversationId: uuid().toUpperCase(),
            messageType: 'fragment',
            fragmentMessageIds: []
          }
        }
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(201);
    });

    it('should return 422 and an error message when fragmentMessageIds is not uuids', async () => {
      const requestBody = {
        data: {
          attributes: {
            fragmentMessageIds: ['not-a-uuid']
          }
        }
      };

      const errorMessage = {
        'data.attributes.fragmentMessageIds[0]': "'fragmentMessageIds' should be UUIDs"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when fragmentMessageIds is not an array', async () => {
      const requestBody = {
        data: {
          attributes: {
            fragmentMessageIds: 'not-an-array'
          }
        }
      };

      const errorMessage = {
        'data.attributes.fragmentMessageIds': "'fragmentMessageIds' should be an array"
      };

      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });
  });

  describe('authentication', () => {
    const requestBody = {
      data: {
        type: 'messages',
        id: uuid().toUpperCase(),
        attributes: {
          conversationId: uuid().toUpperCase(),
          nhsNumber: '1234567890',
          messageType: 'ehrExtract'
        }
      }
    };

    it('should return 401 when authentication keys are missing', async () => {
      const res = await request(app).post('/messages').send(requestBody);

      expect(res.status).toBe(401);
    });

    it('should return 403 when authentication keys are incorrect', async () => {
      const res = await request(app)
        .post('/messages')
        .send(requestBody)
        .set('Authorization', 'incorrect');

      expect(res.status).toBe(403);
    });
  });
});
