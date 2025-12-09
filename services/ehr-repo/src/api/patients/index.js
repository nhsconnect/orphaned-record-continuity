import express from 'express';
import {
  healthRecordController,
  healthRecordControllerValidation
} from './health-record-controller';
import { validate } from '../../middleware/validation';
import { authenticateRequest } from '../../middleware/auth';
import { patientDetailsController, patientDetailsValidation } from './patient-details-controller';
import { deleteEhrController, deleteEhrValidation } from './delete-ehr-controller';
import * as tracing from '../../middleware/tracing';

export const patients = express.Router();

patients.use(tracing.middleware);

patients.get(
  '/:nhsNumber/health-records/:conversationId',
  authenticateRequest,
  healthRecordControllerValidation,
  validate,
  healthRecordController
);

patients.get(
  '/:nhsNumber',
  authenticateRequest,
  patientDetailsValidation,
  validate,
  patientDetailsController
);

patients.delete(
  '/:nhsNumber',
  authenticateRequest,
  deleteEhrValidation,
  validate,
  deleteEhrController
);
