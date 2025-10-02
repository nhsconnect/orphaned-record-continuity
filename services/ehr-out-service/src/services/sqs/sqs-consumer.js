import { DeleteMessageCommand, ReceiveMessageCommand, SQSClient } from '@aws-sdk/client-sqs';
import { logError, logInfo, logWarning } from '../../middleware/logging';
import sendMessageToCorrespondingHandler from '../handler/broker';

const INTER_POLL_DELAY_MS = 50;
const POLL_WAIT_TIME_SECONDS = 20;

const receiveCallParameters = () => {
  return {
    AttributeNames: ['SentTimestamp'],
    MaxNumberOfMessages: 1,
    MessageAttributeNames: ['All'],
    QueueUrl: process.env.SQS_EHR_OUT_INCOMING_QUEUE_URL,
    WaitTimeSeconds: POLL_WAIT_TIME_SECONDS
  };
};

let stop;

export const startSqsConsumer = (
  config = { region: process.env.AWS_DEFAULT_REGION || 'eu-west-2' }
) => {
  logInfo('Starting SQS consumer');
  stop = false;
  const sqsClient = new SQSClient(config);
  pollQueue(sqsClient);
};

export const stopSqsConsumer = () => {
  logInfo('Requesting stop of SQS consumer');
  stop = true;
};

export const pollQueueOnce = sqsClient => {
  logInfo('Polling for incoming messages');
  return sqsClient
    .send(new ReceiveMessageCommand(receiveCallParameters()))
    .then(data => {
      logInfo('Received message data');
      processMessages(sqsClient, data);
    })
    .catch(err => {
      logError(
        `Error reading from EHR out incoming queue, receive call parameters: ${readable(
          receiveCallParameters()
        )}`,
        err
      );
    });
};

const pollQueue = async sqsClient => {
  if (stop) {
    logInfo('SQS consumer poll stopped.');
    return;
  }
  await pollQueueOnce(sqsClient);
  setTimeout(() => pollQueue(sqsClient), INTER_POLL_DELAY_MS);
};

async function deleteToAcknowledge(sqsClient, message) {
  logInfo('Attempting to acknowledge message (delete from SQS).');

  await sqsClient.send(
    new DeleteMessageCommand({
      QueueUrl: receiveCallParameters().QueueUrl,
      ReceiptHandle: message.ReceiptHandle
    })
  );

  const interactionIdRegex = /eb:Action>(.*?)<\/eb:Action/;
  logInfo(
    `Message acknowledged (deleted from SQS) with Interaction ID: ${
      interactionIdRegex.exec(JSON.stringify(message))[1]
    }.`
  );
}

const processMessages = async (sqsClient, receiveResponse) => {
  try {
    let messages = receiveResponse.Messages;
    if (messages === undefined) {
      logWarning(
        'Messages undefined on response, metadata: ' + JSON.stringify(receiveResponse.$metadata)
      );
      return;
    }
    for (const message of messages) {
      await sendMessageToCorrespondingHandler(message.Body);
      await deleteToAcknowledge(sqsClient, message);
    }
  } catch (err) {
    logError('error processing receive response messages', err); // NB: single error skips out of all messages
  }
};

const readable = obj => JSON.stringify(obj);
