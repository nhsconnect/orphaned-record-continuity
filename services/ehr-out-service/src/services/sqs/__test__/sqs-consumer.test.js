import sendMessageToCorrespondingHandler from '../../handler/broker';
import { logError, logWarning } from '../../../middleware/logging';
import { pollQueueOnce } from '../sqs-consumer.js';
import { readFileSync } from 'fs';
import expect from 'expect';
import path from 'path';

// Mocking
jest.mock('../../handler/broker');
jest.mock('../../../middleware/logging');
jest.mock('@aws-sdk/client-sqs');

describe('sqs consumer', () => {
  // ============ COMMON PROPERTIES ============
  const SQS_CLIENT = { send: jest.fn() };
  const MESSAGE_BODY = readFileSync(
    path.join(__dirname, 'data', 'ehr-requests', 'RCMR_IN010000UK05'),
    'utf-8'
  );
  // =================== END ===================

  it('should read a single message from the queue and invoke the broker with the message and acknowledge on success', async () => {
    // when
    sendMessageToCorrespondingHandler.mockReturnValueOnce(undefined);

    SQS_CLIENT.send.mockResolvedValue({
      $metadata: { attempts: 1, httpStatusCode: 200, totalRetryDelay: 0 },
      Messages: [
        {
          Attributes: { SentTimestamp: '1671103624717' },
          Body: MESSAGE_BODY
        }
      ]
    });

    await pollQueueOnce(SQS_CLIENT);

    // then
    await expect(SQS_CLIENT.send).toHaveBeenCalledTimes(2); // receive + ack
    await expect(sendMessageToCorrespondingHandler).toHaveBeenCalledWith(MESSAGE_BODY);
    await expect(sendMessageToCorrespondingHandler).toHaveBeenCalledTimes(1);
  });

  it('reads and delegates to broker but does not acknowledge on failure', async () => {
    // when
    sendMessageToCorrespondingHandler.mockRejectedValue(undefined);

    SQS_CLIENT.send.mockResolvedValue({
      $metadata: { attempts: 1, httpStatusCode: 200, totalRetryDelay: 0 },
      Messages: [
        {
          Attributes: { SentTimestamp: '1671103624717' },
          Body: MESSAGE_BODY
        }
      ]
    });

    await pollQueueOnce(SQS_CLIENT);

    // then
    await expect(SQS_CLIENT.send).toHaveBeenCalledTimes(1); // receive + no ack
    await expect(sendMessageToCorrespondingHandler).toHaveBeenNthCalledWith(1, MESSAGE_BODY);
    await expect(sendMessageToCorrespondingHandler).toHaveBeenCalledTimes(1);
    await expect(logError).toHaveBeenCalled();
  });

  it('should log an error if it fails to read a message from the queue', async () => {
    // given
    const errorMessage = 'test error message';

    // when
    SQS_CLIENT.send.mockRejectedValue(errorMessage);
    await pollQueueOnce(SQS_CLIENT);

    // then
    await expect(SQS_CLIENT.send).toHaveBeenCalledTimes(1);
    await expect(logError).toHaveBeenCalledTimes(1);
    expect(logError).toHaveBeenCalledWith(
      'Error reading from EHR out incoming queue, receive call parameters: {"AttributeNames":["SentTimestamp"],"MaxNumberOfMessages":1,"MessageAttributeNames":["All"],"WaitTimeSeconds":20}',
      errorMessage
    );
  });

  it('should log a warning if the receive call does not return a messages list', async () => {
    // when
    SQS_CLIENT.send.mockResolvedValue({
      $metadata: { attempts: 1, httpStatusCode: 200, totalRetryDelay: 0 }
    });

    await pollQueueOnce(SQS_CLIENT);

    // then
    await expect(SQS_CLIENT.send).toHaveBeenCalledTimes(1);
    await expect(logWarning).toHaveBeenCalled();
  });
});
