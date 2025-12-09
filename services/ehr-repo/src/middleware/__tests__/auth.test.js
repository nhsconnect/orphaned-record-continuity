import request from 'supertest';
import app from '../../app';
import { v4 as uuid } from 'uuid';
import { initializeConfig } from '../../config';
import { logInfo, logWarning } from '../logging';
import { fragmentAlreadyReceived } from '../../services/database/ehr-fragment-repository';

jest.mock('../logging');
jest.mock('../../services/database/ehr-fragment-repository');
jest.mock('../../services/storage/get-signed-url', () =>
  jest.fn().mockReturnValue(Promise.resolve('some-url'))
);
jest.mock('../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({
    consumerApiKeys: {
      TEST_USER: 'correct-key',
      DUPLICATE_TEST_USER: 'correct-key',
      USER_2: 'key_2'
    }
  })
}));

describe('auth', () => {
  const conversationId = uuid().toUpperCase();
  const messageId = uuid().toUpperCase();

  describe('Authenticated successfully', () => {
    it('should return HTTP 200 when correctly authenticated', async () => {
      fragmentAlreadyReceived.mockResolvedValueOnce(false);

      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', 'correct-key');

      expect(res.status).toBe(200);
    });
  });

  describe('Authorization key environment variables not provided', () => {
    it('should return 412 with an explicit error message if auth keys have not been set', async () => {
      const errorMessage = {
        error: 'Server-side Authorization keys have not been set, cannot authenticate'
      };
      initializeConfig.mockReturnValueOnce({ consumerApiKeys: {} });
      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', 'correct-key');

      expect(res.status).toBe(412);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Authorization header not provided', () => {
    it('should return HTTP 401 with an explicit error message when no authorization header provided', async () => {
      const errorMessage = {
        error: 'The request (/messages) requires a valid Authorization header to be set'
      };
      const res = await request(app).get(`/messages/${conversationId}/${messageId}`);

      expect(res.status).toBe(401);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Incorrect Authorisation header value provided ', () => {
    it('should return HTTP 403 with an explicit error message when authorization key is incorrect', async () => {
      const errorMessage = { error: 'Authorization header is provided but not valid' };
      const res = await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', 'incorrect-key');

      expect(res.status).toBe(403);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Auth logging', () => {
    it('should log consumer, method and url for correctly authenticated request', async () => {
      const logMessage = `Consumer: USER_2, Request: GET /messages/${conversationId}/${messageId}`;
      await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', 'key_2');

      expect(logInfo).toHaveBeenCalledWith(logMessage);
    });

    it('should log multiple consumers when they use the same key value', async () => {
      const logMessage = `Consumer: TEST_USER/DUPLICATE_TEST_USER, Request: GET /messages/${conversationId}/${messageId}`;
      await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', 'correct-key');

      expect(logInfo).toHaveBeenCalledWith(logMessage);
    });

    it('should log the method, url and partial api key when a request is unsuccessful', async () => {
      const logMessage = `Unsuccessful Request: GET /messages/${conversationId}/${messageId}, API Key: ******key`;
      await request(app)
        .get(`/messages/${conversationId}/${messageId}`)
        .set('Authorization', 'incorrect-key');

      expect(logWarning).toHaveBeenCalledWith(logMessage);
    });
  });
});
