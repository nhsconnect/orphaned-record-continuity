// to replace create-registration-request.js registration-request-repository.js

import { validate as isValidUuid } from 'uuid';

import { EhrTransferTracker } from './dynamo-ehr-transfer-tracker';
import {
  buildConversationUpdateParams,
  isAvailableToSendOut,
  isConversation
} from '../../../models/conversation';
import {
  NhsNumberNotFoundError,
  OutboundConversationResetError,
  PatientRecordNotFoundError,
  ValidationError
} from '../../../errors/errors';
import { logError, logInfo } from '../../../middleware/logging';
import { AcknowledgementErrorCode, ConversationStatus } from '../../../constants/enums';
import {
  buildParamsToClearPreviousOutboundRecord,
  buildUpdateParamFromItem
} from '../../../utilities/dynamodb-helper';
import { isCore } from '../../../models/core';
import { isFragment } from '../../../models/fragment';

export const createOutboundConversation = async (outboundConversationId, nhsNumber, odsCode) => {
  validateInputValues(outboundConversationId, nhsNumber, odsCode);

  const db = EhrTransferTracker.getInstance();
  const conversation = await getCurrentConversationForPatient(nhsNumber);
  const inboundConversationId = conversation.InboundConversationId;

  const wholeRecord = await db.queryTableByInboundConversationId(inboundConversationId);

  if (conversation.OutboundConversationId) {
    logInfo('This record has got an outbound request in the past already');
    logInfo('Past record:');
    logInfo(JSON.stringify(wholeRecord));
    logInfo('Will clear the previous outbound request before proceed.');

    await clearPreviousOutboundRecord(wholeRecord);
  }

  const updateParamsForConversation = buildConversationUpdateParams(inboundConversationId, {
    OutboundConversationId: outboundConversationId,
    TransferStatus: ConversationStatus.OUTBOUND_STARTED,
    DestinationGp: odsCode
  });
  const updateParamsForCoreAndFragments = wholeRecord
    .filter(item => isCore(item) || isFragment(item))
    .map(item =>
      buildUpdateParamFromItem(item, { OutboundConversationId: outboundConversationId })
    );

  await db.updateItemsInTransaction([
    updateParamsForConversation,
    ...updateParamsForCoreAndFragments
  ]);
  logInfo('Outbound conversation has been stored');
};

const validateInputValues = (outboundConversationId, nhsNumber, odsCode) => {
  const errors = [];
  if (!isValidUuid(outboundConversationId)) {
    errors.push('OutboundConversationId is not a valid UUID');
  }
  if (!nhsNumber.match(/\d{10}/)) {
    errors.push('NhsNumber must be a 10 digits number');
  }
  if (!odsCode || odsCode.length === 0) {
    errors.push('ods code cannot be empty');
  }

  if (errors.length > 0) {
    throw new ValidationError(errors);
  }
  return true;
};

const clearPreviousOutboundRecord = async items => {
  try {
    const db = EhrTransferTracker.getInstance();
    const allUpdateParams = items.map(buildParamsToClearPreviousOutboundRecord);
    await db.updateItemsInTransaction(allUpdateParams);
  } catch (e) {
    throw new OutboundConversationResetError(e);
  }
};

const getCurrentConversationForPatient = async nhsNumber => {
  const db = EhrTransferTracker.getInstance();
  const conversations = await db.queryTableByNhsNumber(nhsNumber);

  if (!conversations || conversations.length === 0) {
    throw new PatientRecordNotFoundError(AcknowledgementErrorCode.ERROR_CODE_06_A);
  }

  const currentConversation = conversations.reduce((prev, current) => {
    return current?.CreatedAt > prev?.CreatedAt ? current : prev;
  });

  if (!isAvailableToSendOut(currentConversation)) {
    throw new PatientRecordNotFoundError(AcknowledgementErrorCode.ERROR_CODE_06_B);
  }

  return currentConversation;
};

export const getOutboundConversationById = async outboundConversationId => {
  const db = EhrTransferTracker.getInstance();
  const outboundRecords = await db.queryTableByOutboundConversationId(outboundConversationId);
  const outboundConversation = outboundRecords?.filter(isConversation)?.[0];
  if (!outboundConversation) {
    logError('cannot find any record of provided outboundConversationId');
    // return null to match the previous implementation by sequelize
    return null;
  }
  return outboundConversation;
};

export const getNhsNumberByOutboundConversationId = async outboundConversationId => {
  const conversation = await getOutboundConversationById(outboundConversationId);
  if (!conversation?.NhsNumber) {
    throw new NhsNumberNotFoundError();
  }
  return conversation.NhsNumber;
};

export const updateOutboundConversationStatus = async (
  outboundConversationId,
  status,
  failureReason = null
) => {
  const db = await EhrTransferTracker.getInstance();

  logInfo(
    `Updating outbound conversation status ${status}, conversationId: ${outboundConversationId}`
  );
  const conversation = await getOutboundConversationById(outboundConversationId);
  if (!conversation) {
    throw new Error('no record was found by given conversationId');
  }

  const inboundConversationId = conversation.InboundConversationId;
  const updateContent = {
    TransferStatus: status
  };
  if (status === ConversationStatus.OUTBOUND_FAILED && failureReason) {
    updateContent.FailureReason = failureReason;
  }

  const updateParam = buildConversationUpdateParams(inboundConversationId, updateContent);

  await db.updateSingleItem(updateParam);
};
