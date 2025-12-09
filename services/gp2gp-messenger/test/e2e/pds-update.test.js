import axios from 'axios';
import { config } from '../config';

process.on('unhandledRejection', console.warn);

describe('Patient ODS code update in PDS', () => {
  const RETRY_COUNT = 20;
  const POLLING_INTERVAL_MS = 500;
  const TEST_TIMEOUT = 3 * RETRY_COUNT * POLLING_INTERVAL_MS;

  const testData = {
    dev: {
      odsCode1: 'A20047',
      odsCode2: 'B85002',
      nhsNumber: 9693795911
    },
    test: {
      odsCode1: 'A20047',
      odsCode2: 'B86041',
      nhsNumber: 9692295990
    },
    'pre-prod': {
      odsCode1: 'A20047',
      odsCode2: 'N85027',
      nhsNumber: 9693642104
    }
  };

  it(
    'should update ODS code of the patient',
    async () => {
      const { nhsNumber, odsCode1, odsCode2 } = testData[config.nhsEnvironment];
      const {
        odsCode: oldOdsCode,
        patientPdsId,
        serialChangeNumber,
        conversationId
      } = await getAndValidatePatientPdsDetails(nhsNumber);

      let newOdsCode;
      if (oldOdsCode === odsCode1) {
        newOdsCode = odsCode2;
      } else if (oldOdsCode === odsCode2) {
        newOdsCode = odsCode1;
      } else {
        expect(
          true,
          'Patient allocated to automated tests is assigned to unexpected ODS code'
        ).toBe(false);
      }

      await updateAndValidatePatientOdsCode(
        nhsNumber,
        patientPdsId,
        serialChangeNumber,
        newOdsCode,
        conversationId
      );

      // poll until ODS is as expected
      let patientOdsCode;
      for (let i = 0; i < RETRY_COUNT; i++) {
        const pdsDetails = await getAndValidatePatientPdsDetails(nhsNumber);
        patientOdsCode = pdsDetails.odsCode;
        await sleep(POLLING_INTERVAL_MS);
        if (patientOdsCode === newOdsCode) {
          break;
        }
      }

      expect(patientOdsCode).toBe(newOdsCode);
    },
    TEST_TIMEOUT
  );
});

const sleep = async ms => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

const getAndValidatePatientPdsDetails = async nhsNumber => {
  const pdsResponse = await axios.get(
    `${config.gp2gpMessengerUrl}/patient-demographics/${nhsNumber}`,
    {
      headers: {
        Authorization: config.e2eTestAuthorizationKeysForGp2gpMessenger
      },
      adapter: 'http'
    }
  );
  expect(pdsResponse.status).toBe(200);

  return { ...pdsResponse.data.data, conversationId: pdsResponse.data.conversationId };
};

const updateAndValidatePatientOdsCode = async (
  nhsNumber,
  pdsId,
  serialChangeNumber,
  newOdsCode,
  conversationId
) => {
  const pdsResponse = await axios.patch(
    `${config.gp2gpMessengerUrl}/patient-demographics/${nhsNumber}`,
    {
      pdsId,
      serialChangeNumber,
      newOdsCode,
      conversationId
    },
    {
      headers: {
        Authorization: config.e2eTestAuthorizationKeysForGp2gpMessenger
      },
      adapter: 'http'
    }
  );

  expect(pdsResponse.status).toBe(204);
};
