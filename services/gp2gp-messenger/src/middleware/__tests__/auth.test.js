import request from 'supertest';
import { pdsRetrieval } from '../../api/patient-demographics/pds-retrieval';
import app from '../../app';
import { initializeConfig } from '../../config';
import { logInfo, logWarning } from '../logging';

jest.mock('../logging');
jest.mock('../../api/patient-demographics/pds-retrieval');
jest.mock('../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({
    consumerApiKeys: {
      TEST_USER: 'correct-key',
      DUPLICATE_TEST_USER: 'correct-key',
      USER_2: 'key_2'
    }
  })
}));

// In all other unit tests we want to pass through all of this logic and should therefore call jest.mock
// jest.mock('../auth') will call the manual mock in __mocks__ automatically
describe('auth', () => {
  describe('authenticated successfully', () => {
    it('should return HTTP 201 when correctly authenticated', async () => {
      pdsRetrieval.mockImplementation((req, res) => res.sendStatus(201));
      const res = await request(app)
        .get('/patient-demographics/0000000000')
        .set('Authorization', 'correct-key');

      expect(res.statusCode).toBe(201);
    });
  });

  describe('consumerApiKeys environment variables not provided', () => {
    it('should return 412 with an explicit error message if consumerApiKeys have not been set', async () => {
      initializeConfig.mockReturnValueOnce({ consumerApiKeys: {} });
      const errorMessage = {
        error: 'Server-side Authorization keys have not been set, cannot authenticate'
      };

      const res = await request(app)
        .get('/patient-demographics/0000000000')
        .set('Authorization', 'correct-key');

      expect(res.statusCode).toBe(412);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Authorization header not provided', () => {
    it('should return HTTP 401 with an explicit error message when no authorization header provided', async () => {
      const errorMessage = {
        error: 'The request (/patient-demographics) requires a valid Authorization header to be set'
      };
      const res = await request(app).get('/patient-demographics/0000000000');

      expect(res.statusCode).toBe(401);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Incorrect Authorisation header value provided ', () => {
    it('should return HTTP 403 with an explicit error message when authorization key is incorrect', async () => {
      const errorMessage = { error: 'Authorization header is provided but not valid' };
      const res = await request(app)
        .get('/patient-demographics/0000000000')
        .set('Authorization', 'incorrect-key');

      expect(res.statusCode).toBe(403);
      expect(res.body).toEqual(errorMessage);
    });
  });

  describe('Auth logging', () => {
    it('should log consumer, method and url for correctly authenticated request', async () => {
      const logMessage = 'Consumer: USER_2, Request: GET /patient-demographics/0000000000';
      await request(app).get('/patient-demographics/0000000000').set('Authorization', 'key_2');

      expect(logInfo).toHaveBeenCalledWith(logMessage);
    });

    it('should log multiple consumers when they use the same key value', async () => {
      const logMessage =
        'Consumer: TEST_USER/DUPLICATE_TEST_USER, Request: GET /patient-demographics/0000000000';
      await request(app)
        .get('/patient-demographics/0000000000')
        .set('Authorization', 'correct-key');

      expect(logInfo).toHaveBeenCalledWith(logMessage);
    });

    it('should log the method, url and partial api key when a request is unsuccessful', async () => {
      const logMessage =
        'Unsuccessful Request: GET /patient-demographics/0000000000, API Key: ******key';
      await request(app)
        .get('/patient-demographics/0000000000')
        .send({ nhsNumber: '0000000000' })
        .set('Authorization', 'incorrect-key');

      expect(logWarning).toHaveBeenCalledWith(logMessage);
    });
  });
});
