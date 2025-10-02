export const logDebug = jest.fn();
export const logInfo = jest.fn();
export const logWarning = jest.fn();
export const logError = jest.fn();

export const middleware = (req, res, next) => next();

export const eventFinished = jest.fn();
