import axios from 'axios';
import { config } from '../../config';
import { logInfo } from '../../middleware/logging';
import { GetPdsCodeError } from '../../errors/errors';
import { AcknowledgementErrorCode } from '../../constants/enums';

export const getPdsOdsCode = async nhsNumber => {
  const { gp2gpMessengerAuthKeys, gp2gpMessengerServiceUrl } = config();
  const url = `${gp2gpMessengerServiceUrl}/patient-demographics/${nhsNumber}`;

  return await axios
    .get(url, { headers: { Authorization: gp2gpMessengerAuthKeys } })
    .then(response => {
      logInfo('Successfully retrieved patient from PDS');
      return response.data.data.odsCode;
    })
    .catch(error => {
      throw new GetPdsCodeError(error, AcknowledgementErrorCode.ERROR_CODE_20_A);
    });
};
