import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import { isCoreOrFragment } from '../../../models/core';
import { MessageIdUpdateError, ValidationError } from '../../../errors/errors';
import { buildUpdateParamFromItem } from '../../../utilities/dynamodb-helper';
import { logInfo } from '../../../middleware/logging';
import { validate as isValidUuid } from 'uuid';

export const storeOutboundMessageIds = async (messageIdReplacements, inboundConversationId) => {
  const db = EhrTransferTracker.getInstance();
  const wholeRecord = await db.queryTableByInboundConversationId(inboundConversationId);
  const coreAndFragment = wholeRecord.filter(isCoreOrFragment);

  validateInputValues(messageIdReplacements, inboundConversationId);

  const lookupDictionary = messageIdReplacements.reduce((dictionary, curr) => {
    const { oldMessageId, newMessageId } = curr;
    dictionary[oldMessageId.toLowerCase()] = newMessageId;
    return dictionary;
  }, {});

  if (coreAndFragment.length !== messageIdReplacements.length) {
    throw new MessageIdUpdateError(
      'Total number of core and fragments does not match the number of outbound Message IDs provided'
    );
  }

  const allUpdateParams = coreAndFragment.map(item => {
    const inboundMessageId = item.InboundMessageId;
    const outboundMessageId = lookupDictionary[inboundMessageId.toLowerCase()];
    if (!outboundMessageId) {
      throw new MessageIdUpdateError(
        'Input array `messageIdReplacements` does not match the actual InboundMessageId records in database'
      );
    }
    return buildUpdateParamFromItem(item, { OutboundMessageId: outboundMessageId });
  });

  await db.updateItemsInTransaction(allUpdateParams);
  logInfo('Recorded outbound message IDs in database');
};

const validateInputValues = (messageIdReplacements, inboundConversationId) => {
  let errors = [];
  if (!inboundConversationId) {
    errors.push('inboundConversationId cannot be empty');
  }
  if (!messageIdReplacements.every(pair => isValidUuid(pair.newMessageId))) {
    errors.push('OutboundMessageIds must be valid UUID');
  }
  if (errors.length > 0) {
    throw new ValidationError(errors);
  }
  return true;
};
