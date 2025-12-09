import { body } from 'express-validator';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { getPracticeAsid } from '../../services/fhir/sds-fhir-client';
import { updateFragmentForSending } from '../../services/parser/message/update-fragment-for-sending';
import { sendMessage } from '../../services/mhs/mhs-outbound-client';
import {
  removeTitleFromExternalAttachments,
  wrangleAttachments
} from '../../services/mhs/mhs-attachments-wrangler';
import { logError } from '../../middleware/logging';

export const sendFragmentMessageValidation = [
  body('conversationId').isUUID().withMessage("'conversationId' provided is not of type UUID"),
  body('odsCode').notEmpty().withMessage('Value has not been provided'),
  body('messageId').isUUID().withMessage('Provided value is not of type UUID'),
  body('fragmentMessage').notEmpty().withMessage('Value has not been provided')
];

export const sendFragmentMessage = async (req, res) => {
  try {
    const COPC_INTERACTION_ID = 'COPC_IN000001UK01';
    const serviceId = `urn:nhs:names:services:gp2gp:${COPC_INTERACTION_ID}`;
    const { conversationId, odsCode, fragmentMessage } = req.body;
    let { messageId } = req.body;
    messageId = messageId.toUpperCase();

    setCurrentSpanAttributes(conversationId);

    const receivingPractiseAsid = await getPracticeAsid(odsCode, serviceId);
    const updatedFragmentPayload = await updateFragmentForSending(
      fragmentMessage.payload,
      messageId,
      receivingPractiseAsid,
      odsCode
    );

    const { attachments, external_attachments } = await wrangleAttachments(fragmentMessage);

    await sendMessage({
      interactionId: COPC_INTERACTION_ID,
      conversationId,
      odsCode: odsCode,
      message: updatedFragmentPayload,
      messageId,
      attachments,
      external_attachments: external_attachments
        ? removeTitleFromExternalAttachments(external_attachments)
        : null
    });

    res.sendStatus(204);
  } catch (err) {
    logError('Sending Ehr fragment message failed', { errors: err.message });
    res.status(503).send({ errors: ['Sending Ehr fragment message failed', err.message] });
  }
};
