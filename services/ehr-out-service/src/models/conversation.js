import { getUKTimestamp } from '../services/time';
import { addChangesToUpdateParams } from '../utilities/dynamodb-helper';
import { ConversationStatus, RecordType } from '../constants/enums';

const fieldsAllowedToUpdate = [
  'TransferStatus',
  'OutboundConversationId',
  'FailureCode',
  'FailureReason',
  'DestinationGp'
];

export const buildConversationUpdateParams = (inboundConversationId, changes) => {
  const baseParams = {
    Key: {
      InboundConversationId: inboundConversationId,
      Layer: RecordType.CONVERSATION
    },
    UpdateExpression: 'set UpdatedAt = :now',
    ExpressionAttributeValues: {
      ':now': getUKTimestamp()
    }
  };

  return addChangesToUpdateParams(baseParams, changes, fieldsAllowedToUpdate);
};

export const isConversation = item => {
  return item.Layer === RecordType.CONVERSATION;
};

export const isAvailableToSendOut = conversation => {
  const status = conversation?.TransferStatus;
  return status === ConversationStatus.INBOUND_COMPLETE || status?.startsWith('OUTBOUND');
};
