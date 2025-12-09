import { parseAcknowledgementMessage } from '../parser/acknowledgement-parser';
import { ACKNOWLEDGEMENT_TYPES } from '../../constants/acknowledgement-types';
import { storeAcknowledgement } from '../database/dynamodb/ehr-fragment-repository';
import { sendDeleteRequestToEhrRepo } from '../ehr-repo/delete-ehr';
import { parseConversationId } from '../parser/parsing-utilities';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { logError, logInfo } from '../../middleware/logging';
import { messageIdMatchOutboundCore } from '../database/dynamodb/ehr-core-repository';
import { getNhsNumberByOutboundConversationId } from '../database/dynamodb/outbound-conversation-repository';
import { updateConversationStatus, updateCoreStatus } from '../transfer/transfer-out-util';
import { ConversationStatus, CoreStatus } from '../../constants/enums';

export const acknowledgementMessageHandler = async message => {
  const conversationId = await parseConversationId(message);
  const parsedAcknowledgementFields = await parseAcknowledgementMessage(message);
  const { acknowledgementTypeCode, messageRef, acknowledgementDetail } =
    parsedAcknowledgementFields;
  const isIntegrationAcknowledgement = await messageIdMatchOutboundCore(conversationId, messageRef);
  const nhsNumber = await getNhsNumberByOutboundConversationId(conversationId);

  setCurrentSpanAttributes({ conversationId });

  await storeAcknowledgement(parsedAcknowledgementFields, conversationId);

  if (ACKNOWLEDGEMENT_TYPES.POSITIVE.includes(acknowledgementTypeCode)) {
    isIntegrationAcknowledgement === true
      ? await handlePositiveIntegrationAcknowledgement(nhsNumber, conversationId)
      : handlePositiveAcknowledgement(nhsNumber, conversationId);
  } else if (ACKNOWLEDGEMENT_TYPES.NEGATIVE.includes(acknowledgementTypeCode)) {
    isIntegrationAcknowledgement === true
      ? await handleNegativeIntegrationAcknowledgement(nhsNumber, conversationId)
      : handleNegativeAcknowledgement(acknowledgementDetail, nhsNumber, conversationId);
  } else {
    logError(`Acknowledgement type ${acknowledgementTypeCode} is unknown.`);
  }
};

const handlePositiveIntegrationAcknowledgement = async (nhsNumber, conversationId) => {
  const usefulDetails = `for Conversation ID ${conversationId}, and NHS number ${nhsNumber}.`;
  logInfo(`Positive integration acknowledgement received ${usefulDetails}`);

  await updateCoreStatus(conversationId, CoreStatus.OUTBOUND_COMPLETE);
  await updateConversationStatus(conversationId, ConversationStatus.OUTBOUND_COMPLETE);
  await deleteEhrFromRepo(nhsNumber, conversationId);
};

const handleNegativeIntegrationAcknowledgement = async (nhsNumber, conversationId) => {
  const usefulDetails = `for Conversation ID ${conversationId}, and NHS number ${nhsNumber}.`;
  logInfo(`Negative integration acknowledgement received ${usefulDetails}`);

  await updateCoreStatus(conversationId, CoreStatus.OUTBOUND_FAILED);
  await updateConversationStatus(conversationId, ConversationStatus.OUTBOUND_FAILED);
  await deleteEhrFromRepo(nhsNumber, conversationId);
};

const handlePositiveAcknowledgement = (nhsNumber, conversationId) => {
  const usefulDetails = `for Conversation ID ${conversationId}, and NHS number ${nhsNumber}.`;
  logInfo(`Positive acknowledgement received ${usefulDetails}`);
};

const handleNegativeAcknowledgement = (acknowledgementDetail, nhsNumber, conversationId) => {
  const usefulDetails = `for Conversation ID ${conversationId}, and NHS number ${nhsNumber}.`;
  logInfo(`Negative acknowledgement received - detail: ${acknowledgementDetail} ${usefulDetails}`);
};

const deleteEhrFromRepo = async (nhsNumber, conversationId) => {
  logInfo(
    `Sending delete request to ehr out repository for Conversation ID ${conversationId}, and NHS number ${nhsNumber}.`
  );
  await sendDeleteRequestToEhrRepo(nhsNumber, conversationId);
};
