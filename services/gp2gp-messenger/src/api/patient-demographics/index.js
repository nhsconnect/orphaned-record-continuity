import express from 'express';
import { authenticateRequest } from '../../middleware/auth';
import { validate } from '../../middleware/validation';
import { pdsUpdate, pdsUpdateValidation } from './pds-update';
import { pdsRetrieval, pdsRetrievalValidation } from './pds-retrieval';
import * as tracing from '../../middleware/tracing';

const patientDemographicsRouter = express.Router();

patientDemographicsRouter.use(tracing.middleware);

patientDemographicsRouter.patch(
  '/:nhsNumber',
  authenticateRequest,
  pdsUpdateValidation,
  validate,
  pdsUpdate
);

patientDemographicsRouter.get(
  '/:nhsNumber',
  authenticateRequest,
  pdsRetrievalValidation,
  validate,
  pdsRetrieval
);

export { patientDemographicsRouter };
