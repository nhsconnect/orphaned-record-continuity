import { getSignedUrl } from '../../services/storage';
import { param } from 'express-validator';
import { logError, logInfo } from '../../middleware/logging';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { fragmentAlreadyReceived } from '../../services/database/ehr-fragment-repository';

export const messageLocationControllerValidation = [
  param('conversationId').isUUID().withMessage("'conversationId' provided is not a UUID"),
  param('messageId').isUUID().withMessage("'messageId' provided is not a UUID")
];

export const messageLocationController = async (req, res) => {
  const { conversationId, messageId } = req.params;
  setCurrentSpanAttributes({ conversationId, messageId });
  const operation = 'putObject';

  try {
    if (await fragmentAlreadyReceived(messageId)) {
      logInfo('DUPLICATE: Message Id: ' + messageId + ' already received in the database');
      res.sendStatus(409);
      return;
    }
    logInfo(`Retrieving presigned url to upload the fragment with message id: ${messageId}`);
    const presignedUrl = await getSignedUrl(conversationId, messageId, operation);
    res.status(200).send(presignedUrl);
    logInfo('Presigned URL sent successfully');
  } catch (err) {
    logError('Failed to retrieve pre-signed url', err);
    res.sendStatus(503);
  }
};
