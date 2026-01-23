import requests from 'http';

describe('GET /health', () => {
  it('should return true for all dependencies', async () => {
    const res = await requests.get(`http://localhost:3000/health`, (res) => {
      return res;
    });
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
