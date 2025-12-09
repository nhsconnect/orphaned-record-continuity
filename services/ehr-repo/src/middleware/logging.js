import { logger } from '../config/logging';
import { startRequest } from '../config/tracing';

export const logError = (status, error) => logger.error(status, { error });

export const logWarning = (...logs) => logs.forEach((log) => logger.warn(log));

export const logInfo = (...logs) => logs.forEach((log) => logger.info(log));

export const logDebug = (status) => logger.debug(status);

export const middleware = (req, res, next) => {
  startRequest(next);

  res.on('finish', () => {
    eventFinished(req, res);
  });
};

export const eventFinished = (req, res) => {
  const url = req.originalUrl;
  const reqLog = { headers: req.headers, method: req.method };
  const resLog = { statusCode: res.statusCode, statusMessage: res.statusMessage };

  if (res.statusCode < 400) {
    logInfo(url, { req: reqLog, res: resLog });
  } else if (res.statusCode === 404) {
    logWarning(url, { req: reqLog, res: resLog });
  } else {
    logError(url, { req: reqLog, res: resLog });
  }
};
