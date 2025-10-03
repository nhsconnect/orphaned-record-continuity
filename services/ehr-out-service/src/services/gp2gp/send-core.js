import axios from 'axios';
import { logInfo } from '../../middleware/logging';
import { config } from '../../config';
import { SendCoreError } from '../../errors/errors';
import { logOutboundMessage } from './logging-utils';
import { updateConversationStatus, updateCoreStatus } from '../transfer/transfer-out-util';
import { ConversationStatus, CoreStatus } from '../../constants/enums';

export const sendCore = async (
  outboundConversationId,
  odsCode,
  coreEhr,
  ehrRequestId,
  newMessageId
) => {
  const { gp2gpMessengerServiceUrl, gp2gpMessengerAuthKeys } = config();
  const url = `${gp2gpMessengerServiceUrl}/ehr-out-transfers/core`;

  const requestBody = {
    conversationId: outboundConversationId,
    odsCode,
    coreEhr,
    ehrRequestId,
    messageId: newMessageId
  };

  logInfo('POST request to gp2gp /ehr-out-transfers/core with request body as below:');
  logOutboundMessage(requestBody);

  // TODO: We could have an additional status here for OUTBOUND_SENDING in the event
  // TODO: that the core is sent, but we have trouble updating the database to say it's been sent.

  await axios
    .post(url, requestBody, { headers: { Authorization: gp2gpMessengerAuthKeys } })
    .then(async () => {
      // 2XX
      await updateConversationStatus(
        outboundConversationId,
        ConversationStatus.OUTBOUND_SENT_CORE,
        null,
        `The EHR Core with Outbound Conversation ID ${outboundConversationId} has successfully been sent.`
      );

      await updateCoreStatus(outboundConversationId, CoreStatus.OUTBOUND_SENT);
    })
    .catch(error => {
      throw new SendCoreError(error);
    });
};
