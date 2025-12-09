import nock from 'nock';
import { getPracticeAsid } from '../sds-fhir-client';
import { initializeConfig } from '../../../config';
import { logError } from '../../../middleware/logging';

jest.mock('../../../config');
jest.mock('../../../middleware/logging');

describe('sds-fhir-client', () => {
  const sdsFhirUrl = 'http://localhost';
  const sdsFhirApiKey = 'key';
  const odsCode = 'A123456';
  const serviceId = 'urn:nhs:names:services:gp2gp:RCMR_IN010000UK05';
  const expectedAsid = '123456789012';
  initializeConfig.mockReturnValue({ sdsFhirUrl, sdsFhirApiKey });
  const mockResponse = {
    entry: [
      {
        resource: {
          identifier: [
            {
              system: 'https://fhir.nhs.uk/Id/nhsSpineASID',
              value: expectedAsid
            },
            {
              system: 'https://fake-fhir',
              value: 'B12345-836483'
            }
          ]
        }
      }
    ]
  };
  const mockResponseNoAsidIdentifier = {
    entry: [
      {
        resource: {
          identifier: [
            {
              system: 'https://fake-fhir',
              value: 'B12345-836483'
            }
          ]
        }
      }
    ]
  };
  const mockResponseMultipleAsidIdentifier = {
    entry: [
      {
        resource: {
          identifier: [
            {
              system: 'https://fhir.nhs.uk/Id/nhsSpineASID',
              value: expectedAsid
            },
            {
              system: 'https://fhir.nhs.uk/Id/nhsSpineASID',
              value: expectedAsid
            },
            {
              system: 'https://fhir.nhs.uk/Id/nhsSpineASID',
              value: expectedAsid
            }
          ]
        }
      }
    ]
  };
  const mockResponseNoAsidEntries = {
    entry: []
  };
  const mockResponseMultipleAsidEntries = {
    entry: [{ resource: {} }, { resource: {} }, { resource: {} }]
  };

  it('should make request to SDS FHIR URL and return ASID', async () => {
    const scope = nock(sdsFhirUrl, {
      reqheaders: {
        apiKey: sdsFhirApiKey
      }
    })
      .get(`/Device`)
      .query({
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      })
      .reply(200, mockResponse);

    const response = await getPracticeAsid(odsCode, serviceId);

    expect(scope.isDone()).toBe(true);
    expect(response).toBe(expectedAsid);
  });

  it('should throw an error when a status other than 200 is received', async () => {
    const errorMessage = new Error('Request failed with status code 500');
    const scope = nock(sdsFhirUrl, {
      reqheaders: {
        apiKey: sdsFhirApiKey
      }
    })
      .get(`/Device`)
      .query({
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      })
      .reply(500);

    await expect(getPracticeAsid(odsCode, serviceId)).rejects.toThrow(errorMessage);
    expect(scope.isDone()).toBe(true);
    expect(logError).toHaveBeenCalledWith(
      `Failed to retrieve ASID from FHIR for ODS Code: ${odsCode} - error: ${errorMessage.message}`
    );
  });

  it('should throw an error when no ASID identifiers are found in FHIR response', async () => {
    const errorMessage = new Error(`No ASID identifier found for ODS code ${odsCode}`);
    const scope = nock(sdsFhirUrl, {
      reqheaders: {
        apiKey: sdsFhirApiKey
      }
    })
      .get(`/Device`)
      .query({
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      })
      .reply(200, mockResponseNoAsidIdentifier);

    await expect(getPracticeAsid(odsCode, serviceId)).rejects.toThrow(errorMessage);
    expect(scope.isDone()).toBe(true);
    expect(logError).toHaveBeenCalledWith(
      `Failed to retrieve ASID from FHIR for ODS Code: ${odsCode} - error: ${errorMessage.message}`
    );
  });

  it('should throw an error when multiple ASID identifiers are found in FHIR response', async () => {
    const errorMessage = new Error(`Multiple ASID identifiers found for ODS code ${odsCode}`);
    const scope = nock(sdsFhirUrl, {
      reqheaders: {
        apiKey: sdsFhirApiKey
      }
    })
      .get(`/Device`)
      .query({
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      })
      .reply(200, mockResponseMultipleAsidIdentifier);

    await expect(getPracticeAsid(odsCode, serviceId)).rejects.toThrow(errorMessage);
    expect(scope.isDone()).toBe(true);
    expect(logError).toHaveBeenCalledWith(
      `Failed to retrieve ASID from FHIR for ODS Code: ${odsCode} - error: ${errorMessage.message}`
    );
  });

  it('should throw an error when no ASID entries are found in FHIR response', async () => {
    const errorMessage = new Error(`No ASID entries found for ODS code ${odsCode}`);
    const scope = nock(sdsFhirUrl, {
      reqheaders: {
        apiKey: sdsFhirApiKey
      }
    })
      .get(`/Device`)
      .query({
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      })
      .reply(200, mockResponseNoAsidEntries);

    await expect(getPracticeAsid(odsCode, serviceId)).rejects.toThrow(errorMessage);
    expect(scope.isDone()).toBe(true);
    expect(logError).toHaveBeenCalledWith(
      `Failed to retrieve ASID from FHIR for ODS Code: ${odsCode} - error: ${errorMessage.message}`
    );
  });

  it('should throw an error when multiple ASID entries are found in FHIR response', async () => {
    const errorMessage = new Error(`Multiple ASID entries found for ODS code ${odsCode}`);
    const scope = nock(sdsFhirUrl, {
      reqheaders: {
        apiKey: sdsFhirApiKey
      }
    })
      .get(`/Device`)
      .query({
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      })
      .reply(200, mockResponseMultipleAsidEntries);

    await expect(getPracticeAsid(odsCode, serviceId)).rejects.toThrow(errorMessage);
    expect(scope.isDone()).toBe(true);
    expect(logError).toHaveBeenCalledWith(
      `Failed to retrieve ASID from FHIR for ODS Code: ${odsCode} - error: ${errorMessage.message}`
    );
  });
});
