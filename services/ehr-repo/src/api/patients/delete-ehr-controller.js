import { param } from 'express-validator';
import { logError, logInfo, logWarning } from '../../middleware/logging';
import { markRecordAsSoftDeleteForPatient } from '../../services/database/ehr-conversation-repository';
export const deleteEhrValidation = [
  param('nhsNumber')
    .isNumeric()
    .withMessage("'nhsNumber' provided is not numeric")
    .isLength({ min: 10, max: 10 })
    .withMessage("'nhsNumber' provided is not 10 characters")
];

export const deleteEhrController = async (req, res) => {
  const { nhsNumber } = req.params;

  try {
    const conversationIds = await markRecordAsSoftDeleteForPatient(nhsNumber);
    if (!conversationIds || conversationIds.length === 0) {
      logWarning('Could not find EHR record');
      res.sendStatus(404);
      return;
    }

    logInfo('EHR record marked as deleted successfully');

    const responseBody = {
      data: {
        type: 'patients',
        id: nhsNumber,
        conversationIds
      }
    };

    res.status(200).json(responseBody);
  } catch (err) {
    logError('Could not mark as deleted EHR record', err);
    res.sendStatus(503);
  }
};
