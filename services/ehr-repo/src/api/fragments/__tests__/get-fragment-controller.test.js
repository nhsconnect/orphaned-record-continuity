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

describe('getFragmentController', () => {
  initializeConfig.mockReturnValue({
    consumerApiKeys: { TEST_USER: 'correct-key' }
  });

  const authorizationKeys = 'correct-key';

  describe('success', () => {
    it('should return a 200 with presigned url in body', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();
      const presignedUrl = 'presigned-url';

      // when
      fragmentAlreadyReceived.mockResolvedValueOnce(true);
      getSignedUrl.mockResolvedValue(presignedUrl);

      const response = await request(app)
        .get(`/fragments/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      // then
      expect(response.status).toBe(200);
      expect(getSignedUrl).toHaveBeenCalledWith(conversationId, messageId, 'getObject');
      expect(response.text).toEqual(presignedUrl);
      expect(logInfo).toHaveBeenCalledWith('Presigned URL sent successfully');
    });
  });

  describe('error', () => {
    // given
    const conversationId = uuid().toUpperCase();
    const messageId = uuid().toUpperCase();

    it('should return a 404 error when the record is not found', async () => {
      // when
      fragmentAlreadyReceived.mockResolvedValueOnce(false);

      const response = await request(app)
        .get(`/fragments/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      // then
      expect(response.status).toBe(404);
      expect(getSignedUrl).not.toHaveBeenCalled();
      expect(fragmentAlreadyReceived).toHaveBeenCalledWith(conversationId, messageId);
    });

    it('should return a 503 when getSignedUrl promise is rejected', async () => {
      // given
      const error = new Error('error');

      // when
      fragmentAlreadyReceived.mockResolvedValueOnce(true);
      getSignedUrl.mockRejectedValueOnce(error);

      const response = await request(app)
        .get(`/fragments/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      // then
      expect(getSignedUrl).toHaveBeenCalledWith(conversationId, messageId, 'getObject');
      expect(logError).toHaveBeenCalledWith('Failed to retrieve pre-signed url', error);
      expect(response.status).toBe(503);
    });
  });

  describe('validation', () => {
    it('should return 422 and an error message when conversationId is not a UUID', async () => {
      // given
      const conversationId = 'not-a-uuid';
      const messageId = uuid().toUpperCase();
      const expectedErrorMessage = [{ conversationId: "'conversationId' provided is not a UUID" }];

      // when
      const response = await request(app)
        .get(`/fragments/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      // then
      expect(response.status).toBe(422);
      expect(response.body).toEqual({
        errors: expectedErrorMessage
      });
    });

    it('should return 422 and an error message when messageId is not a UUID', async () => {
      // given
      const conversationId = uuid().toUpperCase();
      const messageId = 'not-a-uuid';
      const errorMessage = [{ messageId: "'messageId' provided is not a UUID" }];

      // when
      const response = await request(app)
        .get(`/fragments/${conversationId}/${messageId}`)
        .set('Authorization', authorizationKeys);

      // then
      expect(response.status).toBe(422);
      expect(response.body).toEqual({
        errors: errorMessage
      });
    });
  });
});
