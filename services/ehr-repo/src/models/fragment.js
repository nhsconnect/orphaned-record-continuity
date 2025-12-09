import { getUKTimestamp } from '../services/time';
import { addChangesToUpdateParams, validateIds } from '../utilities/dynamodb-helper';
import { FragmentStatus, RecordType } from './enums';

const fieldsAllowedToUpdate = ['TransferStatus', 'ParentId', 'ReceivedAt', 'DeletedAt'];

export const buildFragment = ({ inboundConversationId, fragmentMessageId, parentMessageId }) => {
  const timestamp = getUKTimestamp();

  validateIds(inboundConversationId, fragmentMessageId);

  return {
    InboundConversationId: inboundConversationId,
    Layer: [RecordType.FRAGMENT, fragmentMessageId].join('#'),
    InboundMessageId: fragmentMessageId,
    ParentId: parentMessageId,
    TransferStatus: FragmentStatus.INBOUND_PENDING,
    CreatedAt: timestamp,
    UpdatedAt: timestamp
  };
};

export const buildMultipleFragments = ({
  inboundConversationId,
  fragmentMessageIds,
  parentMessageId
}) => {
  if (!fragmentMessageIds || !Array.isArray(fragmentMessageIds)) {
    return [];
  }
  return fragmentMessageIds.map((fragmentMessageId) =>
    buildFragment({ inboundConversationId, fragmentMessageId, parentMessageId })
  );
};

export const buildFragmentUpdateParams = (conversationId, messageId, changes) => {
  validateIds(conversationId, messageId);

  const params = {
    Key: {
      InboundConversationId: conversationId,
      Layer: [RecordType.FRAGMENT, messageId].join('#')
    },
    UpdateExpression: `set CreatedAt = if_not_exists(CreatedAt, :now), \
      InboundMessageId = if_not_exists(InboundMessageId, :messageId), \
      UpdatedAt = :now`,
    ExpressionAttributeValues: {
      ':now': getUKTimestamp(),
      ':messageId': messageId
    }
  };

  return addChangesToUpdateParams(params, changes, fieldsAllowedToUpdate);
};

export const isFragment = (dynamoDbItem) => {
  return dynamoDbItem?.Layer?.startsWith(RecordType.FRAGMENT);
};
