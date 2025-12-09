import express from 'express';
import { authenticateRequest } from '../../middleware/auth';
import { validate } from '../../middleware/validation';

import * as tracing from '../../middleware/tracing';
import { getFragmentController, getFragmentControllerValidation } from './get-fragment-controller';

export const fragments = express.Router();

fragments.use(tracing.middleware);

fragments.get(
  '/:conversationId/:messageId',
  authenticateRequest,
  getFragmentControllerValidation,
  validate,
  getFragmentController
);
