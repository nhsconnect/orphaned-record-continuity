import dateFormat from 'dateformat';
import { body, param } from 'express-validator';
import { initializeConfig } from '../../config';
import { logInfo, logError, logWarning } from '../../middleware/logging';
import { sendMessage } from '../../services/mhs/mhs-outbound-client';
import generateUpdateOdsRequest from '../../templates/generate-update-ods-request';
import { setCurrentSpanAttributes } from '../../config/tracing';

export const pdsUpdateValidation = [
  param('nhsNumber').isNumeric().withMessage(`'nhsNumber' provided is not numeric`),
  param('nhsNumber')
    .isLength({ min: 10, max: 10 })
    .withMessage("'nhsNumber' provided is not 10 characters"),
  body('serialChangeNumber')
    .isNumeric()
    .withMessage(`'serialChangeNumber' provided is not numeric`),
  body('serialChangeNumber')
    .not()
    .isEmpty()
    .withMessage(`'serialChangeNumber' has not been provided`),
  body('pdsId').not().isEmpty().withMessage(`'pdsId' has not been provided`),
  body('newOdsCode').not().isEmpty().withMessage(`'newOdsCode' has not been provided`),
  body('conversationId').isUUID('4').withMessage("'conversationId' provided is not of type UUIDv4"),
  body('conversationId').not().isEmpty().withMessage(`'conversationId' has not been provided`)
];

export const pdsUpdate = async (req, res, next) => {
  const { repoAsid, pdsAsid, nhsNumberPrefix } = initializeConfig();
  const { conversationId, newOdsCode, pdsId, serialChangeNumber } = req.body;
  const { nhsNumber } = req.params;
  const timestamp = dateFormat(Date.now(), 'yyyymmddHHMMss');
  const interactionId = 'PRPA_IN000203UK03';
  const config = initializeConfig();
  const spineOrgCode = config.spineOrgCode;
  setCurrentSpanAttributes({ conversationId });

  try {
    if (!checkNhsNumberPrefix(nhsNumberPrefix, nhsNumber)) {
      const notASyntheticPatientMessage =
        'The NHS number provided is not one for a synthetic patient';

      logWarning(notASyntheticPatientMessage);
      res.status(422).json({
        errors: notASyntheticPatientMessage
      });
      return;
    }
    const message = await generateUpdateOdsRequest({
      id: conversationId,
      timestamp,
      receivingService: { asid: pdsAsid },
      sendingService: { asid: repoAsid },
      newOdsCode: newOdsCode,
      patient: {
        nhsNumber: nhsNumber,
        pdsId: pdsId,
        pdsUpdateChangeNumber: serialChangeNumber
      }
    });

    const messageResponse = await sendMessage({
      interactionId,
      conversationId,
      odsCode: spineOrgCode,
      message
    });

    switch (messageResponse.status) {
      case 202:
        logInfo('200 PDS Update response received', {
          conversationId,
          response: messageResponse
        });
        res.status(204).json(messageResponse.data);
        break;
      case 500:
        throw new Error(`MHS Error: ${messageResponse.data}`);
      default:
        throw new Error(`Unexpected Error - HTTP code: ${messageResponse.status}`);
    }
    next();
  } catch (err) {
    logError('pdsUpdate failed', err);
    res.status(503).json({
      errors: err.message
    });
  }
};

const checkNhsNumberPrefix = (nhsNumberPrefix, nhsNumber) => {
  if (!nhsNumberPrefix) {
    // TODO: Found that previous team has hardcoded checking for synthetic patients only. We need to process real patients.
    // logWarning('PDS Update request failed as no nhs number prefix env variable has been set');
    // return false;
    logInfo('NhsNumberPrefix has not been set.');
    return true;
  }
  if (!nhsNumber.startsWith(nhsNumberPrefix)) {
    logWarning(
      `PDS Update request failed as nhs number does not start with expected prefix: ${nhsNumberPrefix}`
    );
    return false;
  }
  return true;
};
