import { acknowledgementMessageHandler } from '../acknowledgement-handler';
import { INTERACTION_IDS } from '../../../constants/interaction-ids';
import { parseInteractionId } from '../../parser/parsing-utilities';
import continueMessageHandler from '../continue-message-handler';
import sendMessageToCorrespondingHandler from '../broker';
import { logError } from '../../../middleware/logging';
import ehrRequestHandler from '../ehr-request-handler';
import { readFileSync } from 'fs';
import expect from 'expect';
import path from 'path';

// Mocking
jest.mock('../ehr-request-handler');
jest.mock('../continue-message-handler');
jest.mock('../acknowledgement-handler');
jest.mock('../../parser/parsing-utilities');
jest.mock('../../../middleware/logging');

describe('broker', () => {
  it('should hand off to the ehr-request-handler if it is a EHR request', async () => {
    // given
    const ehrRequest = readFileSync(
      path.join(__dirname, 'data', 'ehr-requests', 'RCMR_IN010000UK05'),
      'utf-8'
    );

    // when
    parseInteractionId.mockResolvedValueOnce(Promise.resolve(INTERACTION_IDS.EHR_REQUEST));

    await sendMessageToCorrespondingHandler(ehrRequest);

    // then
    await expect(ehrRequestHandler).toHaveBeenCalledWith(ehrRequest);
  });

  it('should hand off to the continue-message-handler if it is a continue request', async () => {
    // given
    const continueRequest = readFileSync(
      path.join(__dirname, 'data', 'continue-requests', 'COPC_IN000001UK01'),
      'utf-8'
    );

    // when
    parseInteractionId.mockResolvedValueOnce(Promise.resolve(INTERACTION_IDS.CONTINUE_REQUEST));

    await sendMessageToCorrespondingHandler(continueRequest);

    // then
    await expect(continueMessageHandler).toHaveBeenCalledWith(continueRequest);
  });

  it('should hand off to the acknowledgement-handler if it is a negative acknowledgement', async () => {
    // given
    const negativeAcknowledgement = readFileSync(
      path.join(__dirname, 'data', 'acknowledgements', 'negative', 'MCCI_IN010000UK13_TPP_AR_01'),
      'utf-8'
    );

    // when
    parseInteractionId.mockResolvedValueOnce(Promise.resolve(INTERACTION_IDS.ACKNOWLEDGEMENT));

    await sendMessageToCorrespondingHandler(negativeAcknowledgement);

    // then
    await expect(acknowledgementMessageHandler).toHaveBeenCalledWith(negativeAcknowledgement);
  });

  it('should throw an error if an invalid interaction ID is passed', async () => {
    // given
    const invalidInteractionId = 'DERP_IN023451UK01';

    // then
    await expect(() =>
      sendMessageToCorrespondingHandler({
        interactionId: invalidInteractionId
      })
    ).rejects.toThrow(/Invalid interaction ID/);
    await expect(logError).toHaveBeenCalled();
    await expect(ehrRequestHandler).not.toHaveBeenCalled();
    await expect(continueMessageHandler).not.toHaveBeenCalled();
    await expect(acknowledgementMessageHandler).not.toHaveBeenCalled();
  });
});
