import { logError } from '../middleware/logging';

export const errorMessages = {
  DOWNLOAD_ERROR: 'Cannot retrieve message from presigned URL',
  PRESIGNED_URL_NOT_FOUND_ERROR: 'The presigned URL could not be retrieved',
  SEND_CORE_ERROR: 'Failed while trying to send ehr core',
  SEND_ACKNOWLEDGEMENT_ERROR: 'Failed while trying to send acknowledgement',
  SEND_FRAGMENT_ERROR: 'Failed while trying to send message fragment with message ID: ',
  GET_PDS_CODE_ERROR: 'Unable to retrieve patient from PDS',
  PATIENT_RECORD_NOT_FOUND_ERROR: 'Cannot find the requested patient record on the database',
  NHS_NUMBER_NOT_FOUND_ERROR: 'Cannot find an NHS number related to given conversation ID',
  STATUS_ERROR: 'The status could not be updated',
  FILE_READ_ERROR: 'Failed to read file',
  DUPLICATED_REQUEST_ERROR: 'Got a duplicated request',
  PARSING_ERROR: 'Unable to parse XML',
  MESSAGE_ID_UPDATE_ERROR: 'Failed while trying to store outbound message ids in database',
  MESSAGE_ID_RECORD_CREATION_ERROR: 'Failed to record the message id replacements in database',
  FRAGMENT_MESSAGE_RECORD_NOT_FOUND_ERROR:
    'Cannot find the fragment message record within the database',
  ACKNOWLEDGEMENT_RECORD_NOT_FOUND_ERROR:
    'Cannot find an acknowledgement record within the database',
  FRAGMENT_MESSAGE_ID_REPLACEMENT_RECORD_NOT_FOUND_ERROR:
    'Cannot find one or more newMessageId for message fragment within the database',
  OUTBOUND_CONVERSATION_RESET_ERROR: 'Failed to clear the previous record of outbound ehr transfer',
  VALIDATION_ERROR: 'Validation error, details: '
};

class NegativeAcknowledgementError extends Error {
  constructor(errorMessage, acknowledgementErrorCode, error) {
    super(errorMessage);

    this.acknowledgementErrorCode = acknowledgementErrorCode;

    // log the internal failure details
    logError(
      `${errorMessage}. internalErrorCode is: ${acknowledgementErrorCode.internalErrorCode} and ` +
        `internalErrorDescription is: ${acknowledgementErrorCode.internalErrorDescription}`
    );

    if (error) {
      // may not be populated. Not all errors producing negative acknowledgements will have an external cause
      logError(error);
    }
  }
}

export class ParsingError extends Error {
  constructor(error) {
    super(errorMessages.PARSING_ERROR);
    logError(errorMessages.PARSING_ERROR, error);
  }
}

export class GetPdsCodeError extends NegativeAcknowledgementError {
  constructor(error, acknowledgementErrorCode) {
    super(errorMessages.GET_PDS_CODE_ERROR, acknowledgementErrorCode, error);
  }
}

export class PresignedUrlNotFoundError extends NegativeAcknowledgementError {
  constructor(error, acknowledgementErrorCode) {
    super(errorMessages.PRESIGNED_URL_NOT_FOUND_ERROR, acknowledgementErrorCode, error);
  }
}

export class PresignedUrlNotFoundWhileDeletingEhrError extends Error {
  constructor(error) {
    super(errorMessages.PRESIGNED_URL_NOT_FOUND_ERROR);
    logError(errorMessages.PRESIGNED_URL_NOT_FOUND_ERROR, error);
  }
}

export class DownloadError extends NegativeAcknowledgementError {
  constructor(error, acknowledgementErrorCode) {
    super(errorMessages.DOWNLOAD_ERROR, acknowledgementErrorCode, error);
  }
}

export class SendCoreError extends Error {
  constructor(error) {
    super(errorMessages.SEND_CORE_ERROR);
    logError(errorMessages.SEND_CORE_ERROR, error);
  }
}

export class SendAcknowledgementError extends Error {
  constructor(error) {
    super(errorMessages.SEND_ACKNOWLEDGEMENT_ERROR);
    logError(errorMessages.SEND_ACKNOWLEDGEMENT_ERROR, error);
  }
}

export class FragmentSendingError extends Error {
  constructor(error, messageId) {
    super(errorMessages.SEND_FRAGMENT_ERROR + messageId);
    logError(errorMessages.SEND_FRAGMENT_ERROR + messageId, error);
  }
}

export class PatientRecordNotFoundError extends NegativeAcknowledgementError {
  constructor(acknowledgementErrorCode) {
    super(errorMessages.PATIENT_RECORD_NOT_FOUND_ERROR, acknowledgementErrorCode);
  }
}

export class NhsNumberNotFoundError extends Error {
  constructor() {
    super(errorMessages.NHS_NUMBER_NOT_FOUND_ERROR);
    logError(errorMessages.NHS_NUMBER_NOT_FOUND_ERROR);
  }
}

export class StatusUpdateError extends Error {
  constructor(error) {
    super(errorMessages.STATUS_ERROR);
    logError(errorMessages.STATUS_ERROR, error);
  }
}

export class MessageIdUpdateError extends Error {
  constructor(error) {
    super(errorMessages.MESSAGE_ID_UPDATE_ERROR);
    logError(errorMessages.MESSAGE_ID_UPDATE_ERROR, error);
  }
}

export class FragmentMessageRecordNotFoundError extends Error {
  constructor(messageIds) {
    super(errorMessages.FRAGMENT_MESSAGE_RECORD_NOT_FOUND_ERROR);
    logError(
      `${errorMessages.FRAGMENT_MESSAGE_RECORD_NOT_FOUND_ERROR}, related messageId(s): ${messageIds}`
    );
  }
}

export class AcknowledgementRecordNotFoundError extends Error {
  constructor(messageId) {
    super(`${errorMessages.ACKNOWLEDGEMENT_RECORD_NOT_FOUND_ERROR} with message id ${messageId}`);
    logError(
      `${errorMessages.ACKNOWLEDGEMENT_RECORD_NOT_FOUND_ERROR} with message id ${messageId}`
    );
  }
}

export class FileReadError extends Error {
  constructor(error) {
    super(errorMessages.FILE_READ_ERROR);
    logError(errorMessages.FILE_READ_ERROR, error);
  }
}

export class FragmentMessageIdReplacementRecordNotFoundError extends Error {
  constructor(numberOfOldMessageIds, numberOfMessageIdReplacements) {
    super(errorMessages.FRAGMENT_MESSAGE_ID_REPLACEMENT_RECORD_NOT_FOUND_ERROR);
    logError(
      errorMessages.FRAGMENT_MESSAGE_ID_REPLACEMENT_RECORD_NOT_FOUND_ERROR +
        ` expected ${numberOfOldMessageIds} message ID replacements but found ${numberOfMessageIdReplacements}`
    );
  }
}

export class OutboundConversationResetError extends Error {
  constructor(error) {
    super(errorMessages.OUTBOUND_CONVERSATION_RESET_ERROR);
    logError(errorMessages.OUTBOUND_CONVERSATION_RESET_ERROR, error);
  }
}

export class ValidationError extends Error {
  constructor(details) {
    const fullErrorMessage = errorMessages.VALIDATION_ERROR + details;
    super(fullErrorMessage);
    logError(fullErrorMessage);
  }
}
