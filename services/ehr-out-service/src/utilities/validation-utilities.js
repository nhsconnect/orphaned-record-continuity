import { validate } from 'uuid';
import { ValidationError } from '../errors/errors';

export const validateIds = (conversationId, messageId) => {
  const uuidsAreValid = validate(conversationId) && validate(messageId);
  if (!uuidsAreValid) {
    throw new ValidationError(
      'received invalid uuid as either conversationId or messageId. ' +
        `ConversationId: ${conversationId}, messageId: ${messageId}`
    );
  }
};

export const idValidator = (...ids) => {
  const invalidIds = ids.filter(id => validate(id) === false);

  if (invalidIds.length > 0) {
    throw new ValidationError(`One or more invalid IDs have been provided, details: ${invalidIds}`);
  }
};
