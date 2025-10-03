import express from 'express';
import { authenticateRequest } from '../../middleware/auth';
import { validate } from '../../middleware/validation';
import {
  registrationRequestStatus,
  registrationRequestStatusValidationRules
} from './registration-request-status';

export const registrationRequests = express.Router();

registrationRequests.get(
  '/:conversationId',
  authenticateRequest,
  registrationRequestStatusValidationRules,
  validate,
  registrationRequestStatus
);
