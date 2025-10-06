import { XmlParser } from '../xml-parser/xml-parser';

export const extractSerialChangeNumber = message =>
  new XmlParser()
    .parse(message)
    .then(jsObject => jsObject.findFirst('pertinentSerialChangeNumber'))
    .then(jsObject => jsObject.value.value)
    .catch(() => {
      throw Error('failed to extract PDS serial change number');
    });
