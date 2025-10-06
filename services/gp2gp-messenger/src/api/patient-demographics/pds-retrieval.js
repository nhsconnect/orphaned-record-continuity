import dateFormat from 'dateformat';
import { param } from 'express-validator';
import { v4 as uuid } from 'uuid';
import { initializeConfig } from '../../config';
import { logInfo, logError } from '../../middleware/logging';
import { sendMessage } from '../../services/mhs/mhs-outbound-client';
import { PDSRetrievalQueryResponse } from '../../services/pds';
import generatePdsRetrievalQuery from '../../templates/generate-pds-retrieval-request';
import { setCurrentSpanAttributes } from '../../config/tracing';

export const pdsRetrievalValidation = [
  param('nhsNumber').isNumeric().withMessage("'nhsNumber' provided is not numeric"),
  param('nhsNumber')
    .isLength({ min: 10, max: 10 })
    .withMessage("'nhsNumber' provided is not 10 characters")
];

export const pdsRetrieval = async (req, res, next) => {
  const interactionId = 'QUPA_IN000008UK02';
  const timestamp = dateFormat(Date.now(), 'yyyymmddHHMMss');
  const conversationId = uuid();
  const responseBody = { conversationId, data: {}, errors: [] };
  const config = initializeConfig();
  const spineOrgCode = config.spineOrgCode;
  setCurrentSpanAttributes({ conversationId });

  try {
    const message = await generatePdsRetrievalQuery({
      id: conversationId,
      timestamp,
      receivingService: { asid: config.pdsAsid },
      sendingService: { asid: config.repoAsid },
      patient: { nhsNumber: req.params.nhsNumber }
    });

    if (!message.includes(interactionId)) {
      throw new Error('interactionId is not included in the message');
    }

    if (!spineOrgCode) {
      throw new Error('Spine Org code is undefined.');
    }

    const messageResponse = await sendMessage({
      interactionId,
      conversationId,
      odsCode: spineOrgCode,
      message
    });

    switch (messageResponse.status) {
      case 200:
        logInfo(`200 PDS response received for conversation id: ${conversationId}`);

        responseBody.data = await new PDSRetrievalQueryResponse().handleMessage(
          messageResponse.data
        );
        res.status(200).json(responseBody);
        break;
      case 500:
        throw new Error(`MHS Error: ${messageResponse.data}`);
      default:
        throw new Error(`Unexpected Error: ${messageResponse.data}`);
    }

    next();
  } catch (err) {
    responseBody.errors.push(err.message);
    logError('PDS retrieval error', err);
    res.status(503).json(responseBody);
  }
};
