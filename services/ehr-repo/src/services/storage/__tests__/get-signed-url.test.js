import { S3 } from 'aws-sdk';
import getSignedUrl from '../get-signed-url';

jest.mock('aws-sdk');

describe('getSignedUrl', () => {
  ['getObject', 'putObject'].forEach((operation) => {
    const conversationId = 'some-id';
    const messageId = 'some-message';
    it('should return presigned url from s3 for storing ehr', async () => {
      const mockGetSignedUrlPromise = jest.fn().mockResolvedValue('some-url');
      S3.mockImplementation(() => ({
        getSignedUrlPromise: mockGetSignedUrlPromise
      }));

      const url = await getSignedUrl(conversationId, messageId, operation);
      expect(mockGetSignedUrlPromise).toHaveBeenCalledWith(operation, expect.anything());
      expect(url).toEqual('some-url');
    });
  });

  it('should normalise IDs to lowercase', async () => {
    const conversationId = 'SOME-ID';
    const messageId = 'SOME-MESSAGE';

    const mockGetSignedUrlPromise = jest.fn();
    S3.mockImplementation(() => ({
      getSignedUrlPromise: mockGetSignedUrlPromise
    }));

    await getSignedUrl(conversationId, messageId, 'getObject');
    expect(mockGetSignedUrlPromise).toHaveBeenCalledWith(
      'getObject',
      expect.objectContaining({ Key: `${conversationId.toLowerCase()}/${messageId.toLowerCase()}` })
    );
  });
});
