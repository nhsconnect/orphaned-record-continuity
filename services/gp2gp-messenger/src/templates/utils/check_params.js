const recursiveTemplateCheck = (inputObject, maxDepth = 10, errorMessages) => {
  if (maxDepth-- === 0) {
    throw new Error('maxDepth reached, exiting');
  }

  if (typeof inputObject !== 'object') {
    throw new Error('input object is not an object');
  }

  Object.keys(inputObject).forEach(key => {
    if (inputObject[key] === undefined) {
      errorMessages.push(` ${key} is undefined`);
    }
    if (typeof inputObject[key] === 'object') {
      recursiveTemplateCheck(inputObject[key], maxDepth, errorMessages);
    }
  });
};

const checkTemplateArguments = (inputObject, maxDepth) => {
  const errorMessages = [];
  recursiveTemplateCheck(inputObject, maxDepth, errorMessages);
  if (errorMessages.length > 0) throw new Error(`Check template parameter error:${errorMessages}`);
};

module.exports = checkTemplateArguments;
