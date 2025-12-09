import { INTERACTION_IDS } from '../../constants/interaction-ids';
import { logError } from '../../middleware/logging';
import ehrRequestHandler from './ehr-request-handler';
import continueMessageHandler from './continue-message-handler';
import { acknowledgementMessageHandler } from './acknowledgement-handler';
import { parseInteractionId } from '../parser/parsing-utilities';

export default async function sendMessageToCorrespondingHandler(message) {
  const interactionId = await parseInteractionId(message);

  switch (interactionId) {
    case INTERACTION_IDS.EHR_REQUEST:
      await ehrRequestHandler(message);
      break;
    case INTERACTION_IDS.CONTINUE_REQUEST:
      await continueMessageHandler(message);
      break;
    case INTERACTION_IDS.ACKNOWLEDGEMENT:
      await acknowledgementMessageHandler(message);
      break;
    default: {
      const error = new Error(`Invalid interaction ID: ${interactionId}`);
      logError(error.message);
      throw error;
    }
  }
}
