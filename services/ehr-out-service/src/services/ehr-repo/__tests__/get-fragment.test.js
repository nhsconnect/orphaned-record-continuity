import { DownloadError, PresignedUrlNotFoundError, errorMessages } from '../../../errors/errors';
import { downloadFromUrl } from '../../transfer/transfer-out-util';
import { logError, logInfo } from '../../../middleware/logging';
import { getFragment } from '../get-fragment';
import expect from 'expect';
import nock from 'nock';
import { AcknowledgementErrorCode } from '../../../constants/enums';

jest.mock('../../../middleware/logging');
jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({
    ehrRepoAuthKeys: 'fake-keys',
    ehrRepoServiceUrl: 'http://fake-ehr-repo-url'
  })
}));
jest.mock('../../transfer/transfer-out-util');

describe('getFragment', () => {
  const messageId = 'fake-messageId';
  const mockEhrRepoServiceUrl = 'http://fake-ehr-repo-url';
  const inboundConversationId = 'fake-conversation-id';
  const headers = { reqheaders: { Authorization: 'fake-keys' } };
  const fragmentPresignedUrlRoot = 'http://fake-presigned-url/';
  const ehrFragment = {
    payload: '<?xml a very large xml>',
    attachments: ['attachment1', 'attachment2'],
    external_attachments: ['ext_attachment1', 'ext_attachment2']
  };

  it('should get a fragment successfully', async () => {
    // given
    const inboundConversationId = '5F29004E-5D59-458A-A84C-87BC9213FB40';
    const messageId = '5B440C49-ED70-40B4-9471-291015A7C1DC';

    // when
    const downloadFragmentPresignedUrlScope = nock(mockEhrRepoServiceUrl, headers)
      .get(`/fragments/${inboundConversationId}/${messageId}`)
      .reply(200, fragmentPresignedUrlRoot);

    downloadFromUrl.mockResolvedValueOnce(ehrFragment);

    await getFragment(inboundConversationId, messageId);

    // then
    expect(downloadFragmentPresignedUrlScope.isDone()).toBe(true);
    expect(downloadFromUrl).toBeCalledTimes(1);
    expect(logInfo).toBeCalledTimes(2);
    expect(downloadFromUrl).toBeCalledWith(fragmentPresignedUrlRoot);
    expect(logInfo).toBeCalledWith(
      `Successfully retrieved fragment presigned url with messageId: ${messageId}`
    );
    expect(logInfo).toBeCalledWith(`Successfully retrieved fragment with messageId: ${messageId}`);
  });

  it('should throw an EhrUrlNotFoundError if the given message fragment is not stored in the repo', async () => {
    // given
    const axios404Error = new Error('Request failed with status code 404');

    // when
    const repoScope = nock(mockEhrRepoServiceUrl, headers)
      .get(`/fragments/${inboundConversationId}/${messageId}`)
      .reply(404);

    await expect(getFragment(inboundConversationId, messageId)).rejects.toThrow(
      PresignedUrlNotFoundError
    );

    // then
    expect(repoScope.isDone()).toBe(true);
    expect(logError).toBeCalledWith(
      `${errorMessages.PRESIGNED_URL_NOT_FOUND_ERROR}. ` +
        `internalErrorCode is: ${AcknowledgementErrorCode.ERROR_CODE_10_B.internalErrorCode} and ` +
        `internalErrorDescription is: ${AcknowledgementErrorCode.ERROR_CODE_10_B.internalErrorDescription}`
    );
    expect(logError).toBeCalledWith(axios404Error);
  });

  it('should throw a DownloadError if it failed to download the EHR from the presigned URL', async () => {
    // given
    const inboundConversationId = '0E745F0A-1E8D-4C5F-AE13-B5758F331B29';
    const messageId = 'EBFAE96A-ED38-40C4-A854-58175D15EAEC';
    const acknowledgementErrorCode = AcknowledgementErrorCode.ERROR_CODE_10_A;
    const error = new DownloadError(null, acknowledgementErrorCode);

    // when
    const downloadFragmentPresignedUrlScope = nock(mockEhrRepoServiceUrl, headers)
      .get(`/fragments/${inboundConversationId}/${messageId}`)
      .reply(200, fragmentPresignedUrlRoot);

    downloadFromUrl.mockRejectedValueOnce(error);

    // then
    await expect(getFragment(inboundConversationId, messageId)).rejects.toThrow(error);
    expect(downloadFragmentPresignedUrlScope.isDone()).toBe(true);
    expect(downloadFromUrl).toBeCalledTimes(1);
    expect(logInfo).toBeCalledTimes(1);
    expect(logError).toBeCalledTimes(1);
  });
});
