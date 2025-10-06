import { XMLParser, XMLBuilder } from 'fast-xml-parser';

export const xmlStringToJsObject = xmlString => {
  return new XMLParser({
    processEntities: false,
    ignoreAttributes: false,
    trimValues: false,
    numberParseOptions: {
      leadingZeros: false
    }
  }).parse(xmlString);
};

export const jsObjectToXmlString = jsObject => {
  return new XMLBuilder({
    processEntities: false,
    ignoreAttributes: false,
    suppressBooleanAttributes: false
  }).build(jsObject);
};

export const updateIdExtension = (field, newId) => {
  field.id['@_extension'] = newId;
};

export const updateIdRoot = (field, newId) => {
  field.id['@_root'] = newId;
};
