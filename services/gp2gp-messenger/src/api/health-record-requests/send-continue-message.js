import { body } from 'express-validator';
import { v4 as uuid } from 'uuid';
import { logError, logInfo } from '../../middleware/logging';
import { generateContinueRequest } from '../../templates/generate-continue-request';
import { initializeConfig } from '../../config';
import { sendMessage } from '../../services/mhs/mhs-outbound-client';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { getPracticeAsid } from '../../services/fhir/sds-fhir-client';

export const continueMessageValidation = [
  body('conversationId').isUUID().withMessage("'conversationId' provided is not of type UUID"),
  body('gpOdsCode').notEmpty().withMessage("'gpOdsCode' has not been provided"),
  body('ehrExtractMessageId')
    .isUUID()
    .withMessage("'ehrExtractMessageId' provided is not of type UUID")
];

export const sendContinueMessage = async (req, res) => {
  const config = initializeConfig();
  const messageId = uuid().toUpperCase();
  const interactionId = 'COPC_IN000001UK01';
  const serviceId = `urn:nhs:names:services:gp2gp:${interactionId}`;
  const { conversationId, gpOdsCode, ehrExtractMessageId } = req.body;
  setCurrentSpanAttributes({ messageId, conversationId });

  try {
    const practiceAsid = await getPracticeAsid(gpOdsCode, serviceId);
    const message = await generateContinueRequest({
      messageId,
      receivingAsid: practiceAsid,
      sendingAsid: config.repoAsid,
      ehrExtractMessageId: ehrExtractMessageId.toUpperCase(),
      gpOdsCode
    });

    await sendMessage({
      interactionId,
      conversationId: conversationId.toUpperCase(),
      odsCode: gpOdsCode,
      message,
      messageId
    });
    logInfo('Continue message sent to MHS');
    res.sendStatus(204);
  } catch (err) {
    logError('Could not send continue message', err);
    res.sendStatus(503);
  }
};
