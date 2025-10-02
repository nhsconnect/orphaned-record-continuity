import {
  DownloadError,
  errorMessages,
  PatientRecordNotFoundError,
  PresignedUrlNotFoundError
} from '../errors';
import { AcknowledgementErrorCode } from '../../constants/enums';
import expect from 'expect';
import { logError } from '../../middleware/logging';

// mocking
jest.mock('../../middleware/logging');

describe('errors tests', () => {
  const negativeAcknowledgementErrors = [
    {
      type: PatientRecordNotFoundError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_06_A,
      errorMessage: errorMessages.PATIENT_RECORD_NOT_FOUND_ERROR
    },
    {
      type: PatientRecordNotFoundError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_06_B,
      errorMessage: errorMessages.PATIENT_RECORD_NOT_FOUND_ERROR
    },
    {
      type: DownloadError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_10_A,
      errorMessage: errorMessages.DOWNLOAD_ERROR
    },
    {
      type: PresignedUrlNotFoundError,
      acknowledgementErrorCode: AcknowledgementErrorCode.ERROR_CODE_10_B,
      errorMessage: errorMessages.PRESIGNED_URL_NOT_FOUND_ERROR
    }
  ];

  it.each(negativeAcknowledgementErrors)(
    'should logError and set acknowledgementErrorCode upon creation of a NegativeAcknowledgementError - $acknowledgementErrorCode.internalErrorCode $type.name',
    ({ type, acknowledgementErrorCode, errorMessage }) => {
      // when
      // not all errorTypes have an external cause so don't have an 'error' field. We want it to always be null for this test anyway
      const error =
        type.length === 1
          ? new type(acknowledgementErrorCode)
          : new type(null, acknowledgementErrorCode);

      // then
      expect(logError).toHaveBeenCalledWith(
        `${errorMessage}. ` +
          `internalErrorCode is: ${acknowledgementErrorCode.internalErrorCode} and ` +
          `internalErrorDescription is: ${acknowledgementErrorCode.internalErrorDescription}`
      );

      expect(error.acknowledgementErrorCode).toEqual(acknowledgementErrorCode);
    }
  );

  it("should logError with external 'error' if present upon creation of NegativeAcknowledgementError", () => {
    // given
    const externalErrorMessage = 'Something went wrong!';

    // when
    new PresignedUrlNotFoundError(externalErrorMessage, AcknowledgementErrorCode.ERROR_CODE_10_B);

    // then
    expect(logError).toHaveBeenCalledWith(externalErrorMessage);
  });
});
