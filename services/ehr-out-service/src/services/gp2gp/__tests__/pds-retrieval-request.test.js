import nock from 'nock';
import { getPdsOdsCode } from '../pds-retrieval-request';
import { logError, logInfo } from '../../../middleware/logging';
import { GetPdsCodeError } from '../../../errors/errors';
import { AcknowledgementErrorCode } from '../../../constants/enums';

// Mocking
jest.mock('../../../middleware/logging');
jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({
    gp2gpMessengerAuthKeys: 'fake-keys',
    gp2gpMessengerServiceUrl: 'http://localhost'
  })
}));

describe('getPdsOdsCode', () => {
  // ============ COMMON PROPERTIES ============
  const MOCK_GP2GP_MESSENGER_SERVICE_URL = 'http://localhost';
  const MOCK_GP2GP_MESSENGER_AUTH_KEYS = 'fake-keys';
  const HEADERS = { reqheaders: { Authorization: `${MOCK_GP2GP_MESSENGER_AUTH_KEYS}` } };
  const NHS_NUMBER = '1234567890';
  const SERIAL_CHANGE_NUMBER = '123';
  const PDS_ID = 'chs';
  const ODS_CODE = 'C12345';
  // =================== END ===================

  it('should retrieve patient and return 200 with ODS_CODE, PDS_ID and SERIAL_CHANGE_NUMBER', async () => {
    // given
    const expectedResponse = {
      data: {
        serialChangeNumber: SERIAL_CHANGE_NUMBER,
        pdsId: PDS_ID,
        odsCode: ODS_CODE
      }
    };

    // when
    const urlScope = nock(MOCK_GP2GP_MESSENGER_SERVICE_URL, HEADERS)
      .get(`/patient-demographics/${NHS_NUMBER}`)
      .reply(200, expectedResponse);

    const response = await getPdsOdsCode(NHS_NUMBER);

    // then
    expect(urlScope.isDone()).toBe(true);
    expect(logInfo).toHaveBeenCalledWith('Successfully retrieved patient from PDS');
    expect(response).toEqual(ODS_CODE);
  });

  it('should log and throw error when pds retrieval returns 500', async () => {
    // given
    let error;
    const http500Error = new Error('Request failed with status code 500');
    const expectedError = new GetPdsCodeError(
      http500Error,
      AcknowledgementErrorCode.ERROR_CODE_20_A
    );

    // when
    nock(MOCK_GP2GP_MESSENGER_SERVICE_URL, HEADERS)
      .get(`/patient-demographics/${NHS_NUMBER}`)
      .reply(500);
    try {
      await getPdsOdsCode(NHS_NUMBER);
    } catch (err) {
      error = err;
    }

    // then
    expect(error).toEqual(expectedError);
    expect(logError).toHaveBeenCalledWith(http500Error);
    expect(logError).toHaveBeenCalledWith(
      expect.stringContaining(
        'internalErrorCode is: ' + expectedError.acknowledgementErrorCode.internalErrorCode
      )
    );
  });
});
