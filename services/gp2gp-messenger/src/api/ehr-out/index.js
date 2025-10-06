import express from 'express';
import { authenticateRequest } from '../../middleware/auth';
import { validate } from '../../middleware/validation';
import { sendCoreMessage, sendCoreMessageValidation } from './send-core-message';
import { sendFragmentMessage, sendFragmentMessageValidation } from './send-fragment-message';

export const ehrOutRouter = express.Router();

ehrOutRouter.post(
  '/core',
  authenticateRequest,
  sendCoreMessageValidation,
  validate,
  sendCoreMessage
);

ehrOutRouter.post(
  '/fragment',
  authenticateRequest,
  sendFragmentMessageValidation,
  validate,
  sendFragmentMessage
);
