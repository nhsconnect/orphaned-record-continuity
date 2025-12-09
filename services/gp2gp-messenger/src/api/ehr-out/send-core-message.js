import { setCurrentSpanAttributes } from '../../config/tracing';
import { getPracticeAsid } from '../../services/fhir/sds-fhir-client';
import { logError } from '../../middleware/logging';
import { updateExtractForSending } from '../../services/parser/message/update-extract-for-sending';
import { initializeConfig } from '../../config/index';
import { sendMessage } from '../../services/mhs/mhs-outbound-client';
import {
  removeTitleFromExternalAttachments,
  wrangleAttachments
} from '../../services/mhs/mhs-attachments-wrangler';
import { body } from 'express-validator';

export const sendCoreMessageValidation = [
  body('conversationId').isUUID().withMessage("'conversationId' provided is not of type UUID"),
  body('messageId').isUUID().withMessage('Provided value is not of type UUID'),
  body('odsCode').notEmpty().withMessage('Value has not been provided'),
  body('ehrRequestId').isUUID().withMessage('Provided value is not of type UUID'),
  body('coreEhr').notEmpty().withMessage('Value has not been provided')
];
export const sendCoreMessage = async (req, res) => {
  const { conversationId, odsCode, coreEhr, ehrRequestId, messageId } = req.body;
  const { payload } = coreEhr;
  const interactionId = 'RCMR_IN030000UK06';
  const serviceId = `urn:nhs:names:services:gp2gp:${interactionId}`;
  const repositoryOdsCode = initializeConfig().repoOdsCode;
  setCurrentSpanAttributes({ conversationId });

  try {
    const receivingPracticeAsid = await getPracticeAsid(odsCode, serviceId);

    if (!payload) {
      throw new Error('Could not extract payload from the JSON message stored in EHR Repo');
    }

    const updatedEhrCorePayload = await updateExtractForSending(
      payload,
      ehrRequestId,
      receivingPracticeAsid,
      repositoryOdsCode,
      odsCode
    );

    const { attachments, external_attachments } = await wrangleAttachments(coreEhr);

    await sendMessage({
      interactionId,
      conversationId,
      odsCode,
      message: updatedEhrCorePayload,
      messageId,
      attachments,
      external_attachments: external_attachments
        ? removeTitleFromExternalAttachments(external_attachments)
        : null
    });

    res.sendStatus(204);
  } catch (err) {
    logError('Sending EHR Extract failed', { error: err.message });
    res.status(503).send({ errors: ['Sending EHR Extract failed', err.message] });
  }
};
