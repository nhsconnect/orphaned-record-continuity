import { sendToQueue } from '../sqs-client';
import { SQSClient } from '@aws-sdk/client-sqs';
jest.mock('@aws-sdk/client-sqs');

describe('sqs-client', () => {
  it('puts message on the queue', async () => {
    const sqsSendMessageSpy = jest.spyOn(SQSClient.prototype, 'send');
    const messageToSend = 'An awesome test message';
    await sendToQueue(messageToSend);
    //TODO: expand this assertion to check that the call happened with the expected message?
    expect(sqsSendMessageSpy).toHaveBeenCalled();
  });
});
