import express from 'express';
import { logInfo } from '../middleware/logging';
import { getHealthCheck } from '../services/health-check/get-health-check';

const router = express.Router();

// eslint-disable-next-line no-unused-vars
router.get('/', (req, res, next) => {
  getHealthCheck().then(status => {
    logInfo('Health check successful');
    res.status(200).send(status);
  });
});

export default router;
