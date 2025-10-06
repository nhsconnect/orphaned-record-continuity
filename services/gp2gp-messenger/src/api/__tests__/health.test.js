import request from 'supertest';
import app from '../../app';
import { getHealthCheck } from '../../services/health-check/get-health-check';

jest.mock('../../config/logging');
jest.mock('../../services/health-check/get-health-check');
jest.mock('../../middleware/logging');

// Mocked so need to get real for expectedHealthCheckBase ??
describe('GET /health', () => {
  it('should return 200 and the response from getHealthCheck', done => {
    getHealthCheck.mockReturnValue(Promise.resolve(expectedHealthCheckBase(true)));

    request(app)
      .get('/health')
      .expect(200)
      .expect(res => {
        expect(res.body).toEqual(expectedHealthCheckBase(true));
      })
      .end(done);
  });

  it('should call health check service with no parameters', done => {
    getHealthCheck.mockReturnValue(Promise.resolve(expectedHealthCheckBase(true)));

    request(app)
      .get('/health')
      .expect(() => {
        expect(getHealthCheck).toHaveBeenCalledTimes(1);
      })
      .end(done);
  });

  const expectedHealthCheckBase = () => ({
    version: '1',
    description: 'Health of GP2GP Messenger service',
    node_env: process.env.NHS_ENVIRONMENT,
    details: {}
  });
});
