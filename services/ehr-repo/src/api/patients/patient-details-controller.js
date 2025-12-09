import { param } from 'express-validator';
import { logError, logInfo, logWarning } from '../../middleware/logging';
import getSignedUrl from '../../services/storage/get-signed-url';
import { setCurrentSpanAttributes } from '../../config/tracing';
import {
  getCurrentConversationIdForPatient,
  getMessageIdsForConversation
} from '../../services/database/ehr-conversation-repository';
import { HealthRecordNotFoundError, CoreNotFoundError } from '../../errors/errors';

export const patientDetailsValidation = [
  param('nhsNumber')
    .isNumeric()
    .withMessage("'nhsNumber' provided is not numeric")
    .isLength({ min: 10, max: 10 })
    .withMessage("'nhsNumber' provided is not 10 characters")
];

export const patientDetailsController = async (req, res) => {
  const { nhsNumber } = req.params;
  addConversationIdToLogContext(req.get('conversationId'));

  try {
    const currentHealthRecordConversationId = await getCurrentConversationIdForPatient(nhsNumber);

    logInfo('Getting fragment message ids');
    const { coreMessageId, fragmentMessageIds } = await getMessageIdsForConversation(
      currentHealthRecordConversationId
    );

    const getOperation = 'getObject';
    const coreMessageUrl = await getSignedUrl(
      currentHealthRecordConversationId,
      coreMessageId,
      getOperation
    );

    const responseBody = {
      coreMessageUrl,
      fragmentMessageIds,
      // TODO: remove this duplicated `conversationIdFromEhrIn` field,
      // after updating ehr-out to use the field name "inboundConversationId" (planned in PRMT-4587)
      conversationIdFromEhrIn: currentHealthRecordConversationId,
      inboundConversationId: currentHealthRecordConversationId
    };

    res.status(200).json(responseBody);
  } catch (err) {
    if (err instanceof HealthRecordNotFoundError) {
      logInfo('Did not find a complete patient health record');
      res.sendStatus(404);
      return;
    }

    if (err instanceof CoreNotFoundError) {
      logInfo('Did not find a core message');
      res.sendStatus(503);
      return;
    }

    logError('Could not retrieve patient health record', err);
    res.sendStatus(503);
  }
};

function addConversationIdToLogContext(conversationId) {
  if (!conversationId) {
    logWarning('conversationId not passed as header');
  } else {
    logInfo('Putting conversation ID into log context', conversationId);
    setCurrentSpanAttributes({ conversationId: conversationId });
  }
}
