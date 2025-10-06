import { logInfo } from './logging';

export const perfTestStep = (req, res, next) => {
  if ('perf' === process.env.NHS_ENVIRONMENT) {
    logInfo('Perf test request detected, returning 204');
    res.status(204).send({ result: 'OK' });
    return;
  }
  next();
};
