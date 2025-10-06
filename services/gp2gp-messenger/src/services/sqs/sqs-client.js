import { SQSClient, SendMessageCommand } from '@aws-sdk/client-sqs';
import { trimMessageForObservabilityQueue } from '../mhs/logging-utils';

const client = new SQSClient({ region: process.env.AWS_DEFAULT_REGION || 'eu-west-2' });
const sqsObservabilityQueueUrl = process.env.SQS_OBSERVABILITY_QUEUE_URL;

export const sendToQueue = async (message, attributes) => {
  try {
    const params = {
      MessageAttributes: attributes,
      MessageBody: JSON.stringify(trimMessageForObservabilityQueue(message)),
      QueueUrl: sqsObservabilityQueueUrl
    };
    const command = new SendMessageCommand(params);
    await client.send(command);
  } catch (error) {
    console.log(error);
  }
};
