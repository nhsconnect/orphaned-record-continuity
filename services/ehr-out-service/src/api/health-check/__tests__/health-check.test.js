import request from 'supertest';
import { getHealthCheck } from '../../../services/health-check/get-health-check';
import { logInfo, logError, logDebug } from '../../../middleware/logging';
import { buildTestApp } from '../../../__builders__/test-app';
import { healthCheck } from '../health-check';

jest.mock('../../../middleware/logging');
jest.mock('../../../services/health-check/get-health-check');

describe('GET /health', () => {
  const testApp = buildTestApp('/health', healthCheck);

  it('should return HTTP status code 200', async () => {
    getHealthCheck.mockResolvedValue(expectedHealthCheckBase());
    const res = await request(testApp).get('/health');

    expect(res.statusCode).toBe(200);
    expect(res.body).toEqual(expectedHealthCheckBase());
    expect(logDebug).toHaveBeenCalledWith('Health check completed');
  });

  it('should return 500 if getHealthCheck if it cannot provide a health check', async () => {
    getHealthCheck.mockRejectedValue('some-error');
    const res = await request(testApp).get('/health');

    expect(res.statusCode).toBe(500);
    expect(logInfo).not.toHaveBeenCalled();
    expect(logError).toHaveBeenCalledWith('Health check error', 'some-error');
  });
});

const expectedHealthCheckBase = (db_writable = true, db_connected = true) => ({
  details: {
    database: getExpectedDatabase(db_writable, db_connected)
  }
});

const getExpectedDatabase = (isWritable, isConnected) => {
  const baseConf = {
    connection: isConnected,
    writable: isWritable
  };

  return !isWritable
    ? {
        ...baseConf,
        error: 'some-error'
      }
    : baseConf;
};
