import express from 'express';
import { authenticateRequest } from '../../middleware/auth';
import { validate } from '../../middleware/validation';
import {
  messageLocationController,
  messageLocationControllerValidation
} from './message-location-controller';
import {
  storeMessageController,
  storeMessageControllerValidation
} from './store-message-controller';
import * as tracing from '../../middleware/tracing';

export const messages = express.Router();

messages.use(tracing.middleware);

messages.get(
  '/:conversationId/:messageId',
  authenticateRequest,
  messageLocationControllerValidation,
  validate,
  messageLocationController
);

messages.post(
  '/',
  authenticateRequest,
  storeMessageControllerValidation,
  validate,
  storeMessageController
);
