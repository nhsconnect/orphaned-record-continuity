import request from 'supertest';
import app from '../../app';
import { logError } from '../../middleware/logging';

jest.mock('../../config/logging');
jest.mock('../../middleware/logging');

describe('GET /error', () => {
  it('should return 200', done => {
    request(app)
      .get('/error')
      .expect(201)
      .expect(res => {
        expect(res.text).toEqual('Added test Error to the log');
      })
      .end(done);
  });

  it('should log error', done => {
    request(app)
      .get('/error')
      .expect(201)
      .expect(() => {
        expect(logError).toHaveBeenCalledTimes(1);
        expect(logError).toHaveBeenCalledWith('TEST: GP2GP Messenger Error logging test entry');
      })
      .end(done);
  });
});
