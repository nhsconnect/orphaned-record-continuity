import { XmlParser } from '../xml-parser/xml-parser';

export const extractPdsId = message =>
  new XmlParser()
    .parse(message)
    .then(jsObject => jsObject.findAll('patientCareProvisionEvent'))
    .then(jsObjectArray => jsObjectArray.find(jsObject => jsObject.code.code === '1').id.extension)
    .catch(() => {
      throw Error('Failed to extract PDS ID');
    });
