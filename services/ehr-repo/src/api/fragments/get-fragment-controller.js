import { getSignedUrl } from '../../services/storage';
import { param } from 'express-validator';
import { logError, logInfo } from '../../middleware/logging';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { fragmentAlreadyReceived } from '../../services/database/ehr-fragment-repository';

export const getFragmentControllerValidation = [
  param('conversationId').isUUID().withMessage("'conversationId' provided is not a UUID"),
  param('messageId').isUUID().withMessage("'messageId' provided is not a UUID")
];

export const getFragmentController = async (req, res) => {
  const { conversationId, messageId } = req.params;
  setCurrentSpanAttributes({ conversationId, messageId });
  const operation = 'getObject';

  try {
    const noFragmentRecordFound = !(await fragmentAlreadyReceived(conversationId, messageId));
    if (noFragmentRecordFound) {
      logInfo('NO FRAGMENT RECORD: Could not find message Id: ' + messageId + ' in the database');
      res.sendStatus(404);
      return;
    }

    logInfo(`Retrieving presigned url to download the fragment with message id: ${messageId}`);

    const presignedUrl = await getSignedUrl(conversationId, messageId, operation);
    res.status(200).send(presignedUrl);
    logInfo('Presigned URL sent successfully');
  } catch (err) {
    logError('Failed to retrieve pre-signed url', err);
    res.sendStatus(503);
  }
};
