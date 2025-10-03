import axios from 'axios';
import { config } from '../../src/config';

describe('/health', () => {
  const { repoToGpServiceUrl } = config();
  const healthUrl = `${repoToGpServiceUrl}/health`;

  it('should return 200', async () => {
    const res = await axios.get(healthUrl, { adapter: 'http' });
    expect(res.status).toEqual(200);
  });
});
