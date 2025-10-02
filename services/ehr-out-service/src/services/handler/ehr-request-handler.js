import { logError, logInfo } from '../../middleware/logging';
import { transferOutEhrCore } from '../transfer/transfer-out-ehr-core';
import { parseEhrRequestMessage } from '../parser/ehr-request-parser';
import { parseConversationId, parseMessageId } from '../parser/parsing-utilities';
import { setCurrentSpanAttributes } from '../../config/tracing';

export default async function ehrRequestHandler(message) {
  const ehrRequest = await parseEhrRequestMessage(message);
  const conversationId = await parseConversationId(message);
  const messageId = await parseMessageId(message);
  setCurrentSpanAttributes({ conversationId });

  logInfo('Trying to handle EHR request');

  try {
    await transferOutEhrCore({
      conversationId,
      nhsNumber: ehrRequest.nhsNumber,
      odsCode: ehrRequest.odsCode,
      ehrRequestId: ehrRequest.ehrRequestId,
      incomingMessageId: messageId
    });
  } catch (error) {
    logError('EHR out transfer failed due to unexpected error', error);
  }
}
