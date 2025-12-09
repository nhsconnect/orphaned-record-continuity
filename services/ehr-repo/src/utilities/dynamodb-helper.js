import { logError } from '../middleware/logging';
import { validate } from 'uuid';
import { getEpochTimeInSecond, getUKTimestamp } from '../services/time';
import moment from 'moment-timezone';

export const validateIds = (conversationId, messageId) => {
  const uuidsAreValid = validate(conversationId) && validate(messageId);
  if (!uuidsAreValid) {
    throw new Error('received invalid uuid as either conversationId or messageId');
  }
};
export const addChangesToUpdateParams = (params, changes, fieldsAllowedToUpdate) => {
  for (const [fieldName, updatedValue] of Object.entries(changes)) {
    if (!fieldsAllowedToUpdate.includes(fieldName)) {
      logError(`Ignoring attempt to update non-allowed field ${fieldName}`);
      continue;
    }
    const keyToken = `#${fieldName}`;
    const valueToken = `:${fieldName}`;

    params.UpdateExpression += `, ${keyToken} = ${valueToken}`;
    params.ExpressionAttributeValues[valueToken] = updatedValue;
    params.ExpressionAttributeNames = params.ExpressionAttributeNames ?? {};
    params.ExpressionAttributeNames[keyToken] = fieldName;
  }

  return params;
};

export const buildSoftDeleteUpdateParams = (item) => {
  const eightWeeksAfter = moment().add({ weeks: 8, hour: 0 }); // hour: 0 for enforce correct precise time diff related to DST

  return {
    Key: {
      InboundConversationId: item.InboundConversationId,
      Layer: item.Layer
    },
    UpdateExpression: `set UpdatedAt = :now, DeletedAt = :deletedAt`,
    ExpressionAttributeValues: {
      ':now': getUKTimestamp(),
      ':deletedAt': getEpochTimeInSecond(eightWeeksAfter)
    }
  };
};
