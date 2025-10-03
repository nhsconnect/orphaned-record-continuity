import { FragmentSendingError } from '../../../errors/errors';
import { logInfo } from '../../../middleware/logging';
import { sendFragment } from '../send-fragment';
import expect from 'expect';
import nock from 'nock';
import { updateFragmentStatus } from '../../transfer/transfer-out-util';
import { setupMockConfigForTest } from './test-utils';

// Mocking
jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({})
}));
jest.mock('../../../middleware/logging');
jest.mock('../../transfer/transfer-out-util');

describe('sendFragment', () => {
  // ============ COMMON PROPERTIES ============
  const AUTH_KEYS = 'fake-keys';
  const REQUEST_BASE_URL = 'http://localhost';
  const REQUEST_ENDPOINT = '/ehr-out-transfers';
  const CONVERSATION_ID = '22D24155-C08C-4CAC-A84A-4A5DB46A2F99';
  const ODS_CODE = 'G67200';
  const MESSAGE_ID = '5CEFA7A2-FBCA-494A-A114-6F58FAE4BE4A';
  const FRAGMENT_MESSAGE = { ebXML: '', payload: '', attachments: [] };
  const HEADERS = { reqheaders: { Authorization: AUTH_KEYS } };
  // =================== END ===================

  beforeAll(() => {
    setupMockConfigForTest();
  });

  it('should send fragment successfully', async () => {
    // when
    const mockUrlRequest = nock(REQUEST_BASE_URL, HEADERS)
      .post(`${REQUEST_ENDPOINT}/fragment`)
      .reply(204);

    updateFragmentStatus.mockResolvedValueOnce(undefined);

    await sendFragment(CONVERSATION_ID, ODS_CODE, FRAGMENT_MESSAGE, MESSAGE_ID);

    // then
    expect(updateFragmentStatus).toBeCalledTimes(1);
    expect(mockUrlRequest.isDone()).toBe(true);
    expect(logInfo).toHaveBeenCalledWith('Successfully sent message fragment');
  });

  it('should throw error if sending message fragment unsuccessful', async () => {
    // when
    const mockUrlRequest = nock(REQUEST_BASE_URL, HEADERS)
      .post(`${REQUEST_ENDPOINT}/fragment`)
      .reply(404);

    await expect(() =>
      sendFragment(CONVERSATION_ID, ODS_CODE, FRAGMENT_MESSAGE, MESSAGE_ID)
    ).rejects.toThrow(FragmentSendingError);

    expect(mockUrlRequest.isDone()).toBe(true);
  });
});
