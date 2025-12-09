import request from 'supertest';
import { v4 } from 'uuid';
import { config } from '../../config';
import { buildTestApp } from '../../__builders__/test-app';
import { registrationRequests } from '../../api/registration-request';
import { getOutboundConversationById } from '../../services/database/dynamodb/outbound-conversation-repository';
import { getPdsOdsCode } from '../../services/gp2gp/pds-retrieval-request';
import { logInfo, logWarning } from '../logging';

jest.mock('../../services/database/dynamodb/outbound-conversation-repository');
jest.mock('../../services/gp2gp/pds-retrieval-request');
jest.mock('../../middleware/logging');
jest.mock('../../config', () => ({
  config: jest.fn().mockReturnValue({
    consumerApiKeys: {
      TEST_USER: 'correct-key',
      DUPLICATE_TEST_USER: 'correct-key',
      USER_2: 'key_2'
    }
  })
}));

describe('auth', () => {
  const testApp = buildTestApp('/registration-requests', registrationRequests);
  const odsCode = 'A12345';
  const nhsNumber = '1234567890';
  const conversationId = v4().toUpperCase();

  describe('authenticated successfully', () => {
    it('should return HTTP 204 when correctly authenticated', async () => {
      // given
      const registrationRequestRecord = {
        OutboundConversationId: conversationId,
        NhsNumber: nhsNumber,
        DestinationGp: odsCode,
        TransferStatus: 'test-record'
      };

      getOutboundConversationById.mockResolvedValue(registrationRequestRecord);
      getPdsOdsCode.mockResolvedValue({ data: { data: { odsCode } } });

      const res = await request(testApp)
        .get(`/registration-requests/${conversationId}`)
        .set('Authorization', 'correct-key');

      expect(res.statusCode).toBe(200);
    });
  });

  describe('consumerApiKeys environment variables not provided', () => {
    it('should return 412 with an explicit error message if repoToGpAuthKeys have not been set', async () => {
      config.mockReturnValueOnce({ consumerApiKeys: {} });
      const errorMessage = {
        error: 'Server-side Authorization keys have not been set, cannot authenticate'
      };

      const res = await request(testApp)
        .get(`/registration-requests/${conversationId}`)
        .set('Authorization', 'correct-key');

      expect(res.statusCode).toBe(412);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Authorization header not provided', () => {
    it('should return HTTP 401 with an explicit error message when no authorization header provided', async () => {
      const errorMessage = {
        error:
          'The request (/registration-requests) requires a valid Authorization header to be set'
      };

      const res = await request(testApp).get(`/registration-requests/${conversationId}`);

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Incorrect Authorisation header value provided ', () => {
    it('should return HTTP 403 with an explicit error message when authorization key is incorrect', async () => {
      const errorMessage = { error: 'Authorization header is provided but not valid' };

      const res = await request(testApp)
        .get(`/registration-requests/${conversationId}`)
        .set('Authorization', 'incorrect-key');

      expect(res.statusCode).toBe(403);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Auth logging', () => {
    it('should log consumer, method and url for correctly authenticated request', async () => {
      const logMessage = `Consumer: USER_2, Request: GET /registration-requests/${conversationId}`;
      await request(testApp)
        .get(`/registration-requests/${conversationId}`)
        .set('Authorization', 'key_2');

      expect(logInfo).toHaveBeenCalledWith(logMessage);
    });

    it('should log multiple consumers when they use the same key value', async () => {
      const logMessage = `Consumer: TEST_USER/DUPLICATE_TEST_USER, Request: GET /registration-requests/${conversationId}`;
      await request(testApp)
        .get(`/registration-requests/${conversationId}`)
        .set('Authorization', 'correct-key');

      expect(logInfo).toHaveBeenCalledWith(logMessage);
    });

    it('should log the method, url and partial api key when a request is unsuccessful', async () => {
      const logMessage = `Unsuccessful Request: GET /registration-requests/${conversationId}, Invalid API Key presented`;
      await request(testApp)
        .get(`/registration-requests/${conversationId}`)
        .set('Authorization', 'incorrect-key');

      expect(logWarning).toHaveBeenCalledWith(logMessage);
    });
  });
});
