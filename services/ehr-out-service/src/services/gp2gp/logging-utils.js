import { logError, logInfo } from '../../middleware/logging';

export const LOG_SIZE_LIMIT = 256 * 1024; // the 256 KB size limit of Cloud Watch

export const logOutboundMessage = message => {
  /**
   * @param {object} message
   * Helper function to log an outbound message within the 256KB size limit of Cloud Watch.
   * It will attempt to do this by two steps:
   * 1. Remove any base64 content in the message
   * 2. If the message is still > 256KB without the base64 content, split the message into multiple lines
   *
   * Internally it is just composing removeBase64Payloads -> checkSizeAndLogMessage.
   */
  if (typeof message !== 'object')
    logInfo('logOutboundMessage got an input which is not a JS object. May not work as expected.');

  try {
    checkSizeAndLogMessage(removeBase64Payloads(message));
  } catch (error) {
    // catch any error and let the original workflow to continue
    logError('Encountered error while trying to log the outbound message', error);
  }
};

export const removeBase64Payloads = inputObject => {
  /**
   * @param {object} inputObject
   * Recursively drill down the input object and remove any base64 payloads found.
   * The initial input is assumed to be a JS object or an array containing JS objects.
   */
  // If the input isn't an JS object, just return it unchanged
  if (inputObject === null || typeof inputObject !== 'object') return inputObject;

  // If the input is an array, run this function on each member and return a new copy
  if (Array.isArray(inputObject)) return inputObject.map(removeBase64Payloads);

  // If the input is an JS object, continue below
  const result = {};
  for (let key in inputObject) {
    // apply this function on each child of the object
    result[key] = removeBase64Payloads(inputObject[key]);
  }

  // If the current level got a base64 payload, replace the payload with the string '[REMOVED]'
  if (inputObject?.is_base64 === true && 'payload' in inputObject) {
    result['payload'] = '[REMOVED]';
  }

  return result;
};

export const checkSizeAndLogMessage = message => {
  /**
   * @param {object} message
   * A logging function that will try to keep a long message intact.
   * If given message size is larger than 256KB, it will split the message into multiple lines,
   * so that the full log is kept intact and not truncated by size limit.
   */

  const messageAsString = JSON.stringify(message);
  if (messageAsString.length < LOG_SIZE_LIMIT) {
    logInfo(message);
  } else {
    logMessageInMultipleParts(messageAsString);
  }
};

const logMessageInMultipleParts = messageAsString => {
  /**
   * @param {string} messageAsString
   * Split a long message in multiple parts and log each of them in a human-readable and recoverable way.
   * Give a number to each part as logging in cloudwatch may be not in right order
   */

  const lineLength = LOG_SIZE_LIMIT - 50; // leave some space for metadata "Part x of y"
  const numberOfParts = Math.ceil(messageAsString.length / lineLength);
  logInfo(
    `Received a message larger than the size limit of a single log. Attempt to log it in ${numberOfParts} parts`
  );

  for (let i = 0; i < numberOfParts; i++) {
    const startIndex = i * lineLength;
    const stringPart = messageAsString.slice(startIndex, startIndex + lineLength);
    logInfo(`Part ${i} of ${numberOfParts}: ${stringPart}`);
  }

  logInfo(`Finished logging all parts of the message.`);
};
