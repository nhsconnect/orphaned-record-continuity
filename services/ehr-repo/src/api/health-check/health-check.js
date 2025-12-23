import express from 'express';
import { logError, logInfo } from '../../middleware/logging';
import { getHealthCheck } from '../../services/health-check/get-health-check';

export const healthCheck = express.Router();

healthCheck.get('/', (req, res, next) => {
  getHealthCheck()
    .then((status) => {
      if (status.details.filestore.writable && status.details.filestore.available) {
        logInfo('Health check successful');
        res.status(200).json(status);
      } else {
        logError('Health check failed', status);
        res.status(503).json(status);
      }
    })
    .catch((err) => {
      logError('Health check error', err);
      next(err);
    });
});
