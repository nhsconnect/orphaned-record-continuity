import axios from 'axios';
import { logInfo } from '../../middleware/logging';
import { config } from '../../config';
import { FragmentSendingError } from '../../errors/errors';
import { logOutboundMessage } from './logging-utils';
import { updateFragmentStatus } from '../transfer/transfer-out-util';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { FragmentStatus } from '../../constants/enums';

export const sendFragment = async (
  inboundConversationId,
  outboundConversationId,
  odsCode,
  fragmentMessage,
  outboundMessageId,
  inboundMessageId
) => {
  const { gp2gpMessengerAuthKeys, gp2gpMessengerServiceUrl } = config();
  const sendFragmentEndpoint = `${gp2gpMessengerServiceUrl}/ehr-out-transfers/fragment`;
  const requestBody = {
    conversationId: outboundConversationId,
    odsCode,
    messageId: outboundMessageId,
    fragmentMessage
  };

  setCurrentSpanAttributes({ conversationId: outboundConversationId, outboundMessageId });

  logInfo(
    `Started to send fragment with outbound Message ID: ${outboundMessageId}, outbound Conversation ID ${outboundConversationId}.`
  );

  logInfo(
    'POST request has been made to the GP2GP Messenger `/ehr-out-transfers/fragment` endpoint with request body as below:'
  );
  logOutboundMessage(requestBody);

  await axios
    .post(sendFragmentEndpoint, requestBody, { headers: { Authorization: gp2gpMessengerAuthKeys } })
    .then(() => {
      logInfo('Successfully sent message fragment');
    })
    .catch(async error => {
      throw new FragmentSendingError(error, outboundMessageId);
    });

  await updateFragmentStatus(inboundConversationId, inboundMessageId, FragmentStatus.OUTBOUND_SENT);
};
