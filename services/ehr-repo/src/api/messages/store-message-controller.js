import { body } from 'express-validator';
import { MessageType } from '../../models/enums';
import { logError, logInfo, logWarning } from '../../middleware/logging';
import { setCurrentSpanAttributes } from '../../config/tracing';
import { createCore } from '../../services/database/ehr-core-repository';
import {
  fragmentExistsInRecord,
  markFragmentAsReceivedAndCreateItsParts
} from '../../services/database/ehr-fragment-repository';
import {
  getConversationStatus,
  updateConversationCompleteness,
  updateConversationToCoreReceived
} from '../../services/database/ehr-conversation-repository';

export const storeMessageControllerValidation = [
  body('data.type').equals('messages'),
  body('data.id').isUUID().withMessage("'id' provided is not a UUID"),
  body('data.attributes.conversationId')
    .isUUID()
    .withMessage("'conversationId' provided is not a UUID"),
  body('data.attributes.nhsNumber')
    .if(body('data.attributes.messageType').equals(MessageType.EHR_EXTRACT))
    .notEmpty()
    .withMessage(`'nhsNumber' is required for messageType ${MessageType.EHR_EXTRACT}`)
    .isNumeric()
    .withMessage("'nhsNumber' provided is not numeric")
    .isLength({ min: 10, max: 10 })
    .withMessage("'nhsNumber' provided is not 10 characters"),
  body('data.attributes.nhsNumber')
    .if(body('data.attributes.messageType').equals(MessageType.FRAGMENT))
    .isEmpty()
    .withMessage(`'nhsNumber' should be empty for messageType ${MessageType.FRAGMENT}`),
  body('data.attributes.messageType')
    .isIn([MessageType.EHR_EXTRACT, MessageType.FRAGMENT])
    .withMessage(
      `'messageType' provided is not one of the following: ${MessageType.EHR_EXTRACT}, ${MessageType.FRAGMENT}`
    ),
  body('data.attributes.fragmentMessageIds.*')
    .isUUID()
    .withMessage("'fragmentMessageIds' should be UUIDs"),
  body('data.attributes.fragmentMessageIds')
    .isArray()
    .withMessage("'fragmentMessageIds' should be an array")
];

export const storeMessageController = async (req, res) => {
  let { id: messageId, attributes } = req.body.data;
  let { conversationId, messageType, fragmentMessageIds } = attributes;

  messageId = messageId.toUpperCase();
  conversationId = conversationId.toUpperCase();
  fragmentMessageIds = fragmentMessageIds.map((fragmentMessageId) =>
    fragmentMessageId.toUpperCase()
  );

  setCurrentSpanAttributes({ conversationId, messageId });

  try {
    if (messageType === MessageType.EHR_EXTRACT) {
      await createCore({
        messageId,
        conversationId,
        fragmentMessageIds
      });
      await updateConversationToCoreReceived(conversationId);
    }
    if (messageType === MessageType.FRAGMENT) {
      if (!(await fragmentExistsInRecord(messageId))) {
        logWarning(
          `Fragment message ${messageId} did not arrive in order. Fragment parts: ${JSON.stringify(
            fragmentMessageIds
          )}`
        );
      }
      await markFragmentAsReceivedAndCreateItsParts(messageId, conversationId, fragmentMessageIds);
    }
    await updateConversationCompleteness(conversationId);
    const healthRecordStatus = await getConversationStatus(conversationId);

    logInfo('Health record status for fragments: ' + healthRecordStatus);
    res.status(201).json({ healthRecordStatus });
  } catch (e) {
    logError(
      `Error encountered while storing message: ${
        e.message ? e.message : 'No error message present'
      }`
    );
    res.sendStatus(503);
  }
};
