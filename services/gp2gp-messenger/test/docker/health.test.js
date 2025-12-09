import axios from 'axios';
import { config } from '../config';

describe('/health', () => {
  it('should return 200', () => {
    return expect(
      axios.get(`${config.gp2gpMessengerUrl}/health`, {
        adapter: 'http'
      })
    ).resolves.toEqual(expect.objectContaining({ status: 200 }));
  });

  it('should return matching data', () => {
    return expect(
      axios.get(`${config.gp2gpMessengerUrl}/health`, {
        adapter: 'http'
      })
    ).resolves.toEqual(
      expect.objectContaining({
        data: expect.objectContaining({
          version: '1',
          description: 'Health of GP2GP Messenger service'
        })
      })
    );
  });
});
