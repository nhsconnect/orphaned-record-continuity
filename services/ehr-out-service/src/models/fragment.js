import { FragmentStatus, RecordType } from '../constants/enums';
import { getUKTimestamp } from '../services/time';
import { addChangesToUpdateParams } from '../utilities/dynamodb-helper';
import { validateIds } from '../utilities/validation-utilities';

const fieldsAllowedToUpdate = [
  'TransferStatus',
  'FailureReason',
  'FailureCode',
  'OutboundMessageId'
];

export const buildFragmentUpdateParams = (inboundConversationId, inboundMessageId, changes) => {
  validateIds(inboundConversationId, inboundMessageId);

  const params = {
    Key: {
      InboundConversationId: inboundConversationId,
      Layer: [RecordType.FRAGMENT, inboundMessageId].join('#')
    },
    UpdateExpression: `set UpdatedAt = :now`,
    ExpressionAttributeValues: {
      ':now': getUKTimestamp()
    }
  };

  return addChangesToUpdateParams(params, changes, fieldsAllowedToUpdate);
};

export const isSentOut = fragment => {
  return [FragmentStatus.OUTBOUND_SENT, FragmentStatus.OUTBOUND_COMPLETE].includes(
    fragment?.TransferStatus
  );
};

export const isNotSentOut = fragment => {
  return !isSentOut(fragment);
};

export const isFragment = dynamoDbItem => {
  return dynamoDbItem?.Layer?.startsWith(RecordType.FRAGMENT);
};
