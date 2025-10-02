import xml2js from 'xml2js';

const XmlParser = function () {
  this.options = {
    // Changes key name from '_' to 'innerText'
    charkey: 'innerText',

    // Only multiple defined children are put into Array instead of everything
    explicitArray: false,

    // Puts attributes inside the object that they're associated with
    mergeAttrs: true,

    // Removes SOAP and eb prefixes from '$'
    attrNameProcessors: [xml2js.processors.stripPrefix],

    // Removes SOAP and eb prefixes from '_'
    tagNameProcessors: [xml2js.processors.stripPrefix]
  };

  return this;
};

XmlParser.prototype.parse = function (rawXml) {
  rawXml = rawXml.toString().replace('\ufeff', '');
  return new xml2js.Parser(this.options)
    .parseStringPromise(rawXml)
    .then(result => (this.data = result))
    .then(() => this);
};

XmlParser.prototype.findAll = function (key, maxDepth = 50) {
  return searchData(this.data, key, maxDepth);
};

XmlParser.prototype.findFirst = function (key) {
  const foundAll = this.findAll(key);

  if (foundAll.length === 0) {
    throw new Error(`The key '${key}' was not found in the message`);
  }
  return foundAll[0];
};

/**
 * recursively search the XML to find a key (to a maximum depth), all values that are found will be returned as an array
 */
const searchData = (object, key, maxDepth, found = []) => {
  let param;

  // if we've recursed deeply enough for maxDepth to have been reached, return
  if (maxDepth-- === 0) {
    return found;
  }

  // if we've found the key we're looking for, add the associated value(s) to 'found'
  if (key in object) {
    const value = object[key];

    Array.isArray(value) ? found.push(...value) : found.push(value);
  }

  // for each child node of this one, recurse
  for (param in object) {
    if (Object.prototype.hasOwnProperty.call(object, param) && typeof object[param] === 'object') {
      searchData(object[param], key, maxDepth, found);
    }
  }

  return found;
};

export { XmlParser };
