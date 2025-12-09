import { XmlParser } from '../xml-parser/xml-parser';
import { logInfo } from '../../../middleware/logging';

export const extractOdsCode = message =>
  new XmlParser()
    .parse(message)
    .then(jsObject => jsObject.findAll('patientCareProvisionEvent'))
    .then(patientCareProvisionEvents => {
      logInfo('Total number of patientCareProvisionEvents: ' + patientCareProvisionEvents.length);
      const odsCode = patientCareProvisionEvents.find(jsObject => jsObject.code.code === '1')
        .performer.assignedEntity.id.extension;
      logInfo('Previous patientCareProvisionEvents: ' + JSON.stringify(patientCareProvisionEvents));
      return odsCode;
    })
    .catch(() => {
      throw Error('Failed to extract ODS Code');
    });
