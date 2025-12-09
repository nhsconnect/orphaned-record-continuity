import axios from 'axios';

describe('GET /health', () => {
  it('should return true for all dependencies', async () => {
    const res = await axios.get(`${process.env.SERVICE_URL}/health`, { adapter: 'http' });

    expect(res.data).toEqual(
      expect.objectContaining({
        version: '1',
        description: 'Health of the EHR Repo S3 Bucket',
        details: expect.objectContaining({
          filestore: expect.objectContaining({
            available: true,
            writable: true
          })
        })
      })
    );
  });
});
