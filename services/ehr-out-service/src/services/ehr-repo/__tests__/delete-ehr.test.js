import nock from 'nock';
import { v4 as uuid } from 'uuid';
import { sendDeleteRequestToEhrRepo } from '../delete-ehr';
import { PresignedUrlNotFoundWhileDeletingEhrError } from '../../../errors/errors';

// Mocking
jest.mock('../../../middleware/logging');
jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({
    ehrRepoAuthKeys: 'fake-keys',
    ehrRepoServiceUrl: 'http://localhost'
  })
}));

describe('delete-ehr.js', () => {
  describe('sendDeleteRequestToEhrRepo', () => {
    // ============ COMMON PROPERTIES ============
    const NOCK_BASE_URL = 'http://localhost';
    const NHS_NUMBER = 1234567890;
    const CONVERSATION_ID = uuid().toUpperCase();
    const EXPECTED_RESPONSE = {
      data: {
        type: 'patients',
        id: uuid().toUpperCase(),
        conversationIds: [uuid().toUpperCase()]
      }
    };
    // =================== END ===================

    it('should send a delete request to the ehr repo successfully', async () => {
      // when
      const urlScope = nock(NOCK_BASE_URL, {})
        .delete(`/patients/${NHS_NUMBER}`)
        .reply(200, EXPECTED_RESPONSE);

      const actualResponse = await sendDeleteRequestToEhrRepo(NHS_NUMBER, CONVERSATION_ID);

      // then
      expect(urlScope.isDone()).toBe(true);
      expect(actualResponse).toEqual(EXPECTED_RESPONSE);
    });

    it('should throw a EhrUrlNotFoundError when the response status is 404', async () => {
      // given
      const ERROR_MESSAGE = 'Request failed with status code 404';
      const EXPECTED_ERROR = new PresignedUrlNotFoundWhileDeletingEhrError(ERROR_MESSAGE);

      // when
      const urlScope = nock(NOCK_BASE_URL, {})
        .delete(`/patients/${NHS_NUMBER}`)
        .reply(404, EXPECTED_ERROR);

      // then
      await expect(async () =>
        sendDeleteRequestToEhrRepo(NHS_NUMBER, CONVERSATION_ID)
      ).rejects.toThrow(PresignedUrlNotFoundWhileDeletingEhrError);
      expect(urlScope.isDone()).toBe(true);
    });

    it('should throw an error when the response status is 401', async () => {
      // given
      const EXPECTED_ERROR = new Error();

      // when
      const urlScope = nock(NOCK_BASE_URL, {})
        .delete(`/patients/${NHS_NUMBER}`)
        .reply(401, EXPECTED_ERROR);

      // then
      await expect(async () =>
        sendDeleteRequestToEhrRepo(NHS_NUMBER, CONVERSATION_ID)
      ).rejects.toThrow(Error);
      expect(urlScope.isDone()).toBe(true);
    });
  });
});
