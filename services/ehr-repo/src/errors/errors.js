import { logError } from '../middleware/logging';

export const errorMessages = {
  HealthRecordNotFound: 'No complete health record was found with given criteria',
  MessageNotFound: 'There were no existing messages associated with conversation id'
};

export class HealthRecordNotFoundError extends Error {
  constructor(error) {
    super(errorMessages.HealthRecordNotFound);
    logError(errorMessages.HealthRecordNotFound, error);
  }
}

export class CoreNotFoundError extends Error {
  constructor(error) {
    super(errorMessages.MessageNotFound);
    logError(errorMessages.MessageNotFound, error);
  }
}
