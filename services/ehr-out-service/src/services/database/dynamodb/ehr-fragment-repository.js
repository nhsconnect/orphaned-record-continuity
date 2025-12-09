import { logError, logInfo } from '../../../middleware/logging';
import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import {
  FragmentMessageIdReplacementRecordNotFoundError,
  ValidationError
} from '../../../errors/errors';
import { buildFragmentUpdateParams, isFragment, isNotSentOut } from '../../../models/fragment';
import { FragmentStatus, RecordType } from '../../../constants/enums';
import { ACKNOWLEDGEMENT_TYPES } from '../../../constants/acknowledgement-types';

export const getAllMessageIdPairs = async (oldMessageIds, inboundConversationId) => {
  if (!inboundConversationId) {
    throw new ValidationError('inboundConversationId cannot be empty');
  }
  const db = EhrTransferTracker.getInstance();
  const wholeRecord = await db.queryTableByInboundConversationId(inboundConversationId);
  const fragments = wholeRecord.filter(isFragment);

  const messageIdReplacements = fragments.map(item => ({
    oldMessageId: item.InboundMessageId,
    newMessageId: item.OutboundMessageId
  }));

  verifyMessageIdReplacementWasFoundForEachMessageId(oldMessageIds, messageIdReplacements);

  logInfo(`Successfully retrieved ${fragments.length} message record(s).`);

  return messageIdReplacements;
};

export const updateFragmentStatusInDb = async (
  inboundConversationId,
  inboundMessageId,
  status,
  failureReason = null
) => {
  logInfo(`Updating message fragment status to ${status}`);

  const db = EhrTransferTracker.getInstance();
  const updateContent = {
    TransferStatus: status
  };
  if (status === FragmentStatus.OUTBOUND_FAILED && failureReason) {
    updateContent.FailureReason = failureReason;
  }

  const updateParams = buildFragmentUpdateParams(
    inboundConversationId,
    inboundMessageId,
    updateContent
  );

  await db.updateSingleItem(updateParams);
  logInfo('Updated message fragment status has been stored');
};

export const getAllFragmentIdsToBeSent = async inboundConversationId => {
  const db = EhrTransferTracker.getInstance();
  const allFragments = await db.queryTableByInboundConversationId(
    inboundConversationId,
    RecordType.FRAGMENT
  );
  const eligibleRecords = allFragments.filter(isNotSentOut);
  logInfo(
    `Found ${eligibleRecords.length} eligible records, returning the Outbound Message ID(s).`
  );
  return eligibleRecords.map(fragment => ({
    oldMessageId: fragment.InboundMessageId,
    newMessageId: fragment.OutboundMessageId
  }));
};

const verifyMessageIdReplacementWasFoundForEachMessageId = (
  oldMessageIds,
  messageIdReplacements
) => {
  const lengthCorrect = messageIdReplacements.length === oldMessageIds.length;
  const allIdsMatch = messageIdReplacements.every(pair =>
    oldMessageIds.includes(pair.oldMessageId)
  );

  if (!lengthCorrect || !allIdsMatch) {
    throw new FragmentMessageIdReplacementRecordNotFoundError(
      oldMessageIds.length,
      messageIdReplacements.length
    );
  }
};

export const storeAcknowledgement = async (
  parsedAcknowledgementMessage,
  outboundConversationId
) => {
  try {
    const { acknowledgementCode, acknowledgementTypeCode, acknowledgementDetail, messageRef } =
      parsedAcknowledgementMessage;
    const db = EhrTransferTracker.getInstance();
    const allRecords = await db.queryTableByOutboundConversationId(outboundConversationId);

    const relatedMessage = allRecords
      .filter(isFragment)
      .find(item => item?.OutboundMessageId?.toUpperCase() === messageRef?.toUpperCase());

    if (!relatedMessage) {
      logError(
        'Received an acknowledgement message that refers to unknown messageId. Will not proceed further.'
      );
      logError(`Details of said acknowledgement: ${JSON.stringify(parsedAcknowledgementMessage)}`);
      return;
    }

    const updateContent = {
      FailureCode: acknowledgementCode === 'NOT FOUND' ? null : acknowledgementCode,
      FailureReason: acknowledgementDetail === 'NOT FOUND' ? null : acknowledgementDetail
    };

    if (ACKNOWLEDGEMENT_TYPES.POSITIVE.includes(acknowledgementTypeCode)) {
      updateContent.TransferStatus = FragmentStatus.OUTBOUND_COMPLETE;
    } else if (ACKNOWLEDGEMENT_TYPES.NEGATIVE.includes(acknowledgementTypeCode)) {
      updateContent.TransferStatus = FragmentStatus.OUTBOUND_FAILED;
    }

    const updateParams = buildFragmentUpdateParams(
      relatedMessage.InboundConversationId,
      relatedMessage.InboundMessageId,
      updateContent
    );

    logInfo(`Storing acknowledgement related to outboundMessageId: ${messageRef}`);
    await db.updateSingleItem(updateParams);
    logInfo('Acknowledgement has been stored');
  } catch (e) {
    logError('Failed to store acknowledgement due to error', e);
  }
};
