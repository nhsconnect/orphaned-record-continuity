import axios from 'axios';
import { initializeConfig } from '../../config';
import { logError, logInfo } from '../../middleware/logging';
import { sendToQueue } from '../sqs/sqs-client';
import { logOutboundMessage } from './logging-utils';

const validateInputs = ({ interactionId, conversationId, message }) => {
  if (interactionId && conversationId && message) return;

  const errorMessages = [];
  if (!interactionId) errorMessages.push('interactionId must be passed in');
  if (!conversationId) errorMessages.push('conversationId must be passed in');
  if (!message) errorMessages.push('message must be passed in');

  const error = new Error(errorMessages);

  logError('validation failed', error);
  throw error;
};

export const stripXMLMessage = xml =>
  xml
    .trim()
    .replace(/\r?\n|\r/g, '')
    .replace(/>\s+</g, '><');

export const sendMessage = async ({
  interactionId,
  conversationId,
  odsCode,
  message,
  messageId = null,
  attachments = null,
  external_attachments = null
} = {}) => {
  const config = initializeConfig();

  // If conversationId or messageId is defined, convert to uppercase in order to meet gp2gp specification.
  conversationId = conversationId ? conversationId.toUpperCase() : conversationId;
  messageId = messageId ? messageId.toUpperCase() : messageId;

  validateInputs({ interactionId, conversationId, message });

  const axiosBody = {
    payload: stripXMLMessage(message)
  };

  if (attachments) {
    logInfo(`Sending ${attachments.length} attachments to MHS`);
    axiosBody.attachments = attachments;
  }

  if (external_attachments) {
    logInfo(`Sending ${external_attachments.length} external_attachments to MHS`);
    axiosBody.external_attachments = external_attachments;
  }

  const headers = {
    headers: {
      'Content-Type': 'application/json',
      'Interaction-ID': interactionId,
      'Correlation-Id': conversationId,
      'Ods-Code': odsCode,
      'from-asid': config.repoAsid,
      'wait-for-response': 'false'
    }
  };

  const axiosHeaders = !messageId
    ? headers
    : { headers: { ...headers.headers, 'Message-Id': messageId } };

  try {
    const response = await axios.post(config.mhsOutboundUrl, axiosBody, axiosHeaders);
    logOutboundMessage({ body: axiosBody, headers: response.request?.headers });
    await sendToObservabilityQueue(
      {
        response: { data: response.data, status: response.status },
        request: { body: axiosBody, headers: axiosHeaders }
      },
      {
        conversationId: {
          DataType: 'String',
          StringValue: conversationId
        },
        interactionId: {
          DataType: 'String',
          StringValue: interactionId
        },
        responseStatus: {
          DataType: 'String',
          StringValue: response.statusText
        }
      }
    );
    return response;
  } catch (error) {
    const errorMessage = `POST ${config.mhsOutboundUrl} - ${error.message || 'Request failed'}`;
    const axiosError = new Error(errorMessage);
    logError(errorMessage, axiosError);
    logOutboundMessage({ body: axiosBody, headers: error?.request?.headers });
    await sendToObservabilityQueue(
      { error: axiosError, request: { body: axiosBody, headers: axiosHeaders } },
      {
        conversationId: {
          DataType: 'String',
          StringValue: conversationId
        },
        interactionId: {
          DataType: 'String',
          StringValue: interactionId
        }
      }
    );
    throw axiosError;
  }
};

async function sendToObservabilityQueue(message, attributes) {
  await sendToQueue(message, attributes);
}
