import axios from 'axios';
import { initializeConfig } from '../../config';
import { logError, logInfo } from '../../middleware/logging';

export const getPracticeAsid = async (odsCode, serviceId) => {
  const { sdsFhirUrl, sdsFhirApiKey } = initializeConfig();
  logInfo(`Getting ASID via FHIR for ODS code ${odsCode}`);
  try {
    const response = await axios.get(`${sdsFhirUrl}/Device`, {
      params: {
        organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
        identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|${serviceId}`
      },
      headers: {
        apiKey: sdsFhirApiKey
      }
    });

    const entries = response.data.entry;

    if (entries.length === 0) {
      throw new Error(`No ASID entries found for ODS code ${odsCode}`);
    }

    if (entries.length > 1) {
      throw new Error(`Multiple ASID entries found for ODS code ${odsCode}`);
    }

    const identifiers = entries[0].resource.identifier;
    const asidIdentifier = identifiers.filter(
      identifier => identifier.system === 'https://fhir.nhs.uk/Id/nhsSpineASID'
    );

    if (asidIdentifier.length === 0) {
      throw new Error(`No ASID identifier found for ODS code ${odsCode}`);
    }

    if (asidIdentifier.length > 1) {
      throw new Error(`Multiple ASID identifiers found for ODS code ${odsCode}`);
    }

    const asidCode = asidIdentifier[0].value;
    logInfo(`Successfully retrieved ASID: ${asidCode} via FHIR for ODS code ${odsCode}`);
    return asidCode;
  } catch (err) {
    logError(
      `Failed to retrieve ASID from FHIR for ODS Code: ${odsCode} - error: ${
        err.message ? err.message : 'No Error Message'
      }`
    );
    if (err.response) {
      logError(
        `Error: Request failed with non-2xx status code\n
        Response body: ${err.response.data}\n
        HTTP Status code: ${err.response.status}`
      );
    }

    throw err;
  }
};
