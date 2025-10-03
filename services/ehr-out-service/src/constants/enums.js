import { errorMessages } from '../errors/errors';

export const RecordType = {
  ALL: 'ALL', // Option for querying all layers in one call
  CONVERSATION: 'CONVERSATION',
  CORE: 'CORE',
  FRAGMENT: 'FRAGMENT'
};

export const ConversationStatus = {
  INBOUND_STARTED: 'INBOUND_STARTED',
  INBOUND_REQUEST_SENT: 'INBOUND_REQUEST_SENT',
  INBOUND_CONTINUE_REQUEST_SENT: 'INBOUND_CONTINUE_REQUEST_SENT',
  INBOUND_COMPLETE: 'INBOUND_COMPLETE',
  INBOUND_FAILED: 'INBOUND_FAILED',
  INBOUND_TIMEOUT: 'INBOUND_TIMEOUT',

  OUTBOUND_STARTED: 'OUTBOUND_STARTED',
  OUTBOUND_SENT_CORE: 'OUTBOUND_SENT_CORE',
  OUTBOUND_CONTINUE_REQUEST_RECEIVED: 'OUTBOUND_CONTINUE_REQUEST_RECEIVED',
  OUTBOUND_FRAGMENTS_SENDING_FAILED: 'OUTBOUND_FRAGMENTS_SENDING_FAILED',
  OUTBOUND_SENT_FRAGMENTS: 'OUTBOUND_SENT_FRAGMENTS',
  OUTBOUND_COMPLETE: 'OUTBOUND_COMPLETE', // == EHR_INTEGRATED
  OUTBOUND_FAILED: 'OUTBOUND_FAILED'
};

export const CoreStatus = {
  INBOUND_COMPLETE: 'INBOUND_COMPLETE',
  OUTBOUND_SENT: 'OUTBOUND_SENT',
  OUTBOUND_COMPLETE: 'OUTBOUND_COMPLETE',
  OUTBOUND_FAILED: 'OUTBOUND_FAILED'
};

export const FragmentStatus = {
  INBOUND_COMPLETE: 'INBOUND_COMPLETE',
  OUTBOUND_REQUEST_RECEIVED: 'OUTBOUND_FRAGMENT_REQUEST_RECEIVED',
  OUTBOUND_SENT: 'OUTBOUND_SENT',
  OUTBOUND_COMPLETE: 'OUTBOUND_COMPLETE',
  OUTBOUND_FAILED: 'OUTBOUND_FAILED'
};

export const QueryKeyType = {
  InboundConversationId: 'InboundConversationId',
  NhsNumber: 'NhsNumber',
  OutboundConversationId: 'OutboundConversationId'
};

export const FailureReason = {
  // Conversation / Core level
  INCORRECT_ODS_CODE: 'OUTBOUND:incorrect_ods_code',
  MISSING_FROM_REPO: 'OUTBOUND:missing_from_repo',
  EHR_DOWNLOAD_FAILED: 'OUTBOUND:ehr_download_failed',
  CORE_SENDING_FAILED: 'OUTBOUND:core_sending_failed',
  EHR_INTEGRATION_FAILED: 'OUTBOUND:ehr_integration_failed',

  // Fragment level
  DOWNLOAD_FAILED: 'OUTBOUND:download_failed',
  SENDING_FAILED: 'OUTBOUND:sending_failed'
};

/**
 * The gp2gpError with an errorCode and errorDisplayName - these are the details that are sent across the spine
 *
 * @errorCode - the Spine error code to use in the sent negative acknowledgement
 * @errorDisplayName - the Spine error code to use in the sent negative acknowledgement
 */
const Gp2gpError = {
  CODE_06: {
    errorCode: '06',
    errorDisplayName: 'Patient not at surgery.'
  },
  CODE_10: {
    errorCode: '10',
    errorDisplayName: 'Failed to successfully generate EHR Extract.'
  },
  CODE_20: {
    errorCode: '20',
    errorDisplayName: 'Spine system responded with an error.'
  }
};

/**
 * These are the GP2GP error codes that are attached to negative acknowledgement messages as well as a set of internal
 * error codes that provide more nuance for failure diagnosis.
 *
 * For more info on these codes, see the ORC GP2GP NACKs confluence document:
 * https://nhse-dsic.atlassian.net/wiki/spaces/TW/pages/12452233304/ORC+-+GP2GP+NACKs
 *
 * @gp2gpError - the gp2gpError with an errorCode and errorDisplayname - these are the details that are sent across
 *               the spine, see the const above for info
 * @internalErrorCode - the error code that is stored in the database as the failureCode - this is only used internally
 *                      and SHOULD NOT be sent out of the system
 * @internalErrorDescription - the error description that is stored in the database as the failureDescription - this is
 *                             only used internally and SHOULD NOT be sent out of the system
 */
export const AcknowledgementErrorCode = {
  ERROR_CODE_06_A: {
    gp2gpError: Gp2gpError.CODE_06,
    internalErrorCode: `${Gp2gpError.CODE_06.errorCode}-A`,
    internalErrorDescription: 'NHS Number does not exist in the database'
  },
  ERROR_CODE_06_B: {
    gp2gpError: Gp2gpError.CODE_06,
    internalErrorCode: `${Gp2gpError.CODE_06.errorCode}-B`,
    internalErrorDescription: 'Partial ingestion, cannot send a full EHR out'
  },
  ERROR_CODE_10_A: {
    gp2gpError: Gp2gpError.CODE_10,
    internalErrorCode: `${Gp2gpError.CODE_10.errorCode}-A`,
    internalErrorDescription: errorMessages.DOWNLOAD_ERROR
  },
  ERROR_CODE_10_B: {
    gp2gpError: Gp2gpError.CODE_10,
    internalErrorCode: `${Gp2gpError.CODE_10.errorCode}-B`,
    internalErrorDescription: errorMessages.PRESIGNED_URL_NOT_FOUND_ERROR
  },
  ERROR_CODE_20_A: {
    gp2gpError: Gp2gpError.CODE_20,
    internalErrorCode: `${Gp2gpError.CODE_20.errorCode}-A`,
    internalErrorDescription: errorMessages.GET_PDS_CODE_ERROR
  }
};

Object.freeze(RecordType);
Object.freeze(ConversationStatus);
Object.freeze(CoreStatus);
Object.freeze(FragmentStatus);
