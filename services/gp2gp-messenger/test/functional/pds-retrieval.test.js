import axios from 'axios';
import { config } from '../config';
import { testData } from './test-data';

const TIMEOUT_30_S = 30000;
describe('End to end test of /patient-demographics/:nhsNumber', () => {
  it(
    'should return a 200 from PDS (successful retrieval) with a valid NHS number',
    () => {
      const { nhsNumber } = testData[config.nhsEnvironment];

      return expect(
        axios.get(`${config.gp2gpMessengerUrl}/patient-demographics/${nhsNumber}`, {
          headers: {
            Authorization: config.e2eTestAuthorizationKeysForGp2gpMessenger
          },
          adapter: 'http'
        })
      ).resolves.toEqual(
        expect.objectContaining({
          status: 200
        })
      );
    },
    TIMEOUT_30_S
  );

  it(
    'should return 503 from PDS if the nhs number is not exist',
    () => {
      const fakeNhsNumber = '0000000000';

      return expect(
        axios.get(`${config.gp2gpMessengerUrl}/patient-demographics/${fakeNhsNumber}`, {
          headers: {
            Authorization: config.e2eTestAuthorizationKeysForGp2gpMessenger
          },
          adapter: 'http'
        })
      ).rejects.toEqual(Error('Request failed with status code 503'));
    },
    TIMEOUT_30_S
  );
});
