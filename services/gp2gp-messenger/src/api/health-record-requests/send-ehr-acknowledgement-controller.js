import { body, param } from 'express-validator';
import { buildEhrAcknowledgementPayload } from '../../templates/generate-ehr-acknowledgement';
import { sendMessage } from '../../services/mhs/mhs-outbound-client';
import { logInfo, logError } from '../../middleware/logging';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { getPracticeAsid } from '../../services/fhir/sds-fhir-client';

export const acknowledgementValidation = [
  param('nhsNumber').isNumeric().withMessage(`'nhsNumber' provided is not numeric`),
  param('nhsNumber')
    .isLength({ min: 10, max: 10 })
    .withMessage("'nhsNumber' provided is not 10 digits"),
  body('conversationId').isUUID('4').withMessage("'conversationId' provided is not of type UUIDv4"),
  body('conversationId').notEmpty().withMessage("'conversationId' is not configured"),
  body('messageId').isUUID().withMessage("'messageId' provided is not of type UUID"),
  body('messageId').notEmpty().withMessage("'messageId' is not configured"),
  body('odsCode').notEmpty().withMessage("'odsCode' is not configured"),
  body('repositoryAsid').notEmpty().withMessage("'repositoryAsid' is not configured")
];

export const sendEhrAcknowledgement = async (req, res) => {
  try {
    const interactionId = 'MCCI_IN010000UK13';
    const serviceId = `urn:nhs:names:services:gp2gp:${interactionId}`;
    const {
      messageId: ehrCoreMessageId,
      conversationId,
      odsCode,
      repositoryAsid,
      errorCode,
      errorDisplayName
    } = req.body;
    const practiceAsid = await getPracticeAsid(odsCode, serviceId);

    setCurrentSpanAttributes({ conversationId, messageId: ehrCoreMessageId });

    const message = buildEhrAcknowledgementPayload({
      acknowledgementMessageId: conversationId,
      receivingAsid: practiceAsid,
      sendingAsid: repositoryAsid,
      acknowledgedMessageId: ehrCoreMessageId,
      errorCode,
      errorDisplayName
    });
    await sendMessage({ interactionId, conversationId, odsCode, message });
    logInfo('Acknowledgement sent to MHS');
    res.sendStatus(204);
  } catch (err) {
    logError('sendEhrAcknowledgement failed', err);
    res.status(503).json({
      errors: err.message
    });
  }
};
