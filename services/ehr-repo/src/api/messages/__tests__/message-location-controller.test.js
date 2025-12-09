import request from 'supertest';
import app from '../../../app';
import { getSignedUrl } from '../../../services/storage';
import { v4 as uuid } from 'uuid';
import { logError, logInfo } from '../../../middleware/logging';
import { initializeConfig } from '../../../config';
import { fragmentAlreadyReceived } from '../../../services/database/ehr-fragment-repository';

jest.mock('../../../services/storage');
jest.mock('../../../services/database/ehr-fragment-repository');
jest.mock('../../../middleware/logging');
jest.mock('../../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({})
}));

describe('messageLocationController', () => {
  initializeConfig.mockReturnValue({
    consumerApiKeys: { TEST_USER: 'correct-key' }
  });

  const authorizationKeys = 'correct-key';

  describe('success', () => {
    const conversationId = uuid().toUpperCase();
    const messageId = uuid().toUpperCase();

    it('should return a 200 with presigned url in body', async () => {
      getSignedUrl.mockResolvedValue('presigned-url');

      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(200);
      expect(getSignedUrl).toHaveBeenCalledWith(conversationId, messageId, 'putObject');
      expect(res.text).toEqual('presigned-url');
      expect(logInfo).toHaveBeenCalledWith('Presigned URL sent successfully');
    });
  });

  describe('conflict', () => {
    const conversationId = uuid().toUpperCase();
    const messageId = uuid().toUpperCase();

    it('should return a 409 when ehr already exists', async () => {
      fragmentAlreadyReceived.mockResolvedValueOnce(true);

      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(409);
      expect(getSignedUrl).not.toHaveBeenCalled();
      expect(fragmentAlreadyReceived).toHaveBeenCalledWith(messageId);
    });
  });

  describe('error', () => {
    const conversationId = uuid().toUpperCase();
    const messageId = uuid().toUpperCase();

    it('should return a 503 when getSignedUrl promise is rejected', async () => {
      const error = new Error('error');
      getSignedUrl.mockRejectedValueOnce(error);
      fragmentAlreadyReceived.mockResolvedValueOnce(false);

      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      expect(getSignedUrl).toHaveBeenCalledWith(conversationId, messageId, 'putObject');
      expect(logError).toHaveBeenCalledWith('Failed to retrieve pre-signed url', error);
      expect(res.status).toBe(503);
    });
  });

  describe('validation', () => {
    it('should return 422 and an error message when conversationId is not a UUID', async () => {
      const conversationId = 'not-a-uuid';
      const messageId = uuid().toUpperCase();
      const errorMessage = [{ conversationId: "'conversationId' provided is not a UUID" }];

      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body).toEqual({
        errors: errorMessage
      });
    });

    it('should return 422 and an error message when messageId is not a UUID', async () => {
      const conversationId = uuid().toUpperCase();
      const messageId = 'not-a-uuid';
      const errorMessage = [{ messageId: "'messageId' provided is not a UUID" }];

      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body).toEqual({
        errors: errorMessage
      });
    });
  });
});
