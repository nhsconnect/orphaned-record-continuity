import { readFileSync } from 'fs';
import expect from 'expect';
import nock from 'nock';
import { setCurrentSpanAttributes } from '../../../config/tracing';
import { errorMessages, StatusUpdateError } from '../../../errors/errors';
import { logError, logInfo } from '../../../middleware/logging';
import {
  AcknowledgementErrorCode,
  ConversationStatus,
  CoreStatus,
  FragmentStatus
} from '../../../constants/enums';
import { getPdsOdsCode } from '../../gp2gp/pds-retrieval-request';
import {
  createAndStoreOutboundMessageIds,
  downloadFromUrl,
  getNewMessageIdForOldMessageId,
  patientAndPracticeOdsCodesMatch,
  replaceMessageIdsInObject,
  updateConversationStatus,
  updateCoreStatus,
  updateFragmentStatus
} from '../transfer-out-util';
import { updateOutboundConversationStatus } from '../../database/dynamodb/outbound-conversation-repository';
import { storeOutboundMessageIds } from '../../database/dynamodb/store-outbound-message-ids';
import { updateFragmentStatusInDb } from '../../database/dynamodb/ehr-fragment-repository';
import { v4 as uuid } from 'uuid';
import { updateCoreStatusInDb } from '../../database/dynamodb/ehr-core-repository';

// Mocking
jest.mock('../../../middleware/logging');
jest.mock('../../gp2gp/pds-retrieval-request');
jest.mock('../../database/dynamodb/outbound-conversation-repository');
jest.mock('../../database/dynamodb/store-outbound-message-ids');
jest.mock('../../database/dynamodb/ehr-fragment-repository');
jest.mock('../../database/dynamodb/ehr-core-repository');
jest.mock('../../../config/tracing');

describe('testTransferOutUtil', () => {
  // ============ COMMON PROPERTIES ============
  const CONVERSATION_ID = '7FBEABA2-CA21-4AF7-8F88-29D805B28411';
  const INBOUND_CONVERSATION_ID = uuid().toUpperCase();
  const MESSAGE_ID = '2C1EDC4D-052F-42B6-A03F-4470FF88EF05';
  const UUID_UPPERCASE_REGEX =
    /^[0-9A-F]{8}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{12}$/;

  function getValidEhrCore() {
    return JSON.parse(readFileSync('src/__tests__/data/ehr_with_fragments/ehr-core', 'utf8'));
  }

  afterEach(() => {
    jest.resetAllMocks();
  });

  // =================== END ===================

  describe('downloadFromUrl', () => {
    // ============ COMMON PROPERTIES ============
    const REQUEST_BASE_URL = 'https://example.com';
    const REQUEST_ENDPOINT = '/test';
    const FULL_URL = REQUEST_BASE_URL + REQUEST_ENDPOINT;
    // =================== END ===================

    it('should be 200 OK', async () => {
      // given
      const expectedResponse = { status: 200 };

      // when
      const urlScope = nock(REQUEST_BASE_URL).get(REQUEST_ENDPOINT).reply(200, expectedResponse);

      const response = await downloadFromUrl(FULL_URL);

      // then
      expect(urlScope.isDone()).toEqual(true);
      expect(response).toEqual(expectedResponse);
    });

    it('should be 404 Not Found and throw DownloadError', async () => {
      // given
      let actualError;

      // when
      const urlScope = nock(REQUEST_BASE_URL).get(REQUEST_ENDPOINT).reply(404);

      try {
        await downloadFromUrl(FULL_URL);
      } catch (error) {
        actualError = error;
      }

      // then
      expect(urlScope.isDone()).toEqual(true);
      expect(actualError).not.toBeNull();
      expect(logError).toBeCalledWith(
        `${errorMessages.DOWNLOAD_ERROR}. ` +
          `internalErrorCode is: ${AcknowledgementErrorCode.ERROR_CODE_10_A.internalErrorCode} and ` +
          `internalErrorDescription is: ${AcknowledgementErrorCode.ERROR_CODE_10_A.internalErrorDescription}`
      );
      expect(logError).toBeCalledWith(new Error('Request failed with status code 404'));
    });
  });

  describe('patientAndPracticeOdsCodesMatch', () => {
    // ============ COMMON PROPERTIES ============
    const NHS_NUMBER = 1234567890;
    const ODS_CODES = ['K81003002', 'G9047420'];
    // =================== END ===================

    it('should return true when practice and patient ODS codes match', async () => {
      // when
      getPdsOdsCode.mockReturnValueOnce(ODS_CODES[0]);
      const response = await patientAndPracticeOdsCodesMatch(NHS_NUMBER, ODS_CODES[0]);

      // then
      expect(response).toBe(true);
      expect(getPdsOdsCode).toBeCalledTimes(1);
    });

    it('should return false when practice and patient ODS codes do not match', async () => {
      // when
      getPdsOdsCode.mockReturnValueOnce(ODS_CODES[0]);
      const response = await patientAndPracticeOdsCodesMatch(NHS_NUMBER, ODS_CODES[1]);

      // then
      expect(response).toBe(false);
      expect(getPdsOdsCode).toBeCalledTimes(1);
    });
  });

  describe('updateConversationStatus', () => {
    // ============ COMMON PROPERTIES ============
    const STATUS = ConversationStatus.OUTBOUND_STARTED;
    const LOG_MESSAGE = 'This is an example log message';
    // =================== END ===================

    it('should update the conversation status successfully', async () => {
      // when
      updateOutboundConversationStatus.mockResolvedValueOnce(undefined);
      await updateConversationStatus(CONVERSATION_ID, STATUS);

      // then
      expect(setCurrentSpanAttributes).toBeCalledTimes(1);
      expect(setCurrentSpanAttributes).toBeCalledWith({ conversationId: CONVERSATION_ID });
      expect(updateOutboundConversationStatus).toBeCalledTimes(1);
      expect(logInfo).toBeCalledTimes(1);
      expect(logInfo).toBeCalledWith(`Updating conversation with status: ${STATUS}`);
    });

    it('should log the provided message successfully', async () => {
      // when
      updateOutboundConversationStatus.mockResolvedValueOnce(undefined);
      await updateConversationStatus(CONVERSATION_ID, STATUS, null, LOG_MESSAGE);

      // then
      expect(setCurrentSpanAttributes).toBeCalledTimes(1);
      expect(setCurrentSpanAttributes).toBeCalledWith({ conversationId: CONVERSATION_ID });
      expect(logInfo).toBeCalledTimes(2);
      expect(logInfo).toBeCalledWith(`Updating conversation with status: ${STATUS}`);
      expect(logInfo).toBeCalledWith(LOG_MESSAGE);
    });

    it('should throw a StatusUpdateError error', async () => {
      // when
      updateOutboundConversationStatus.mockRejectedValueOnce(undefined);

      // then
      await expect(() =>
        updateConversationStatus(CONVERSATION_ID, STATUS, LOG_MESSAGE)
      ).rejects.toThrowError(StatusUpdateError);
    });
  });

  describe('updateFragmentStatus', () => {
    // ============ COMMON PROPERTIES ============
    const STATUS = FragmentStatus.OUTBOUND_FAILED;
    // =================== END ===================

    it('should update the fragment status successfully', async () => {
      // when
      updateFragmentStatusInDb.mockResolvedValueOnce(undefined);
      await updateFragmentStatus(CONVERSATION_ID, MESSAGE_ID, STATUS);

      // then
      expect(updateFragmentStatusInDb).toBeCalledTimes(1);
      expect(logInfo).toBeCalledTimes(2);
      expect(logInfo).toBeCalledWith(`Updating fragment with status: ${STATUS}`);
      expect(logInfo).toBeCalledWith(`Updated fragment status to: ${STATUS}`);
    });

    it('should throw a StatusUpdateError error', async () => {
      // when
      updateFragmentStatusInDb.mockRejectedValueOnce(undefined);

      // then
      await expect(() =>
        updateFragmentStatus(CONVERSATION_ID, MESSAGE_ID, STATUS)
      ).rejects.toThrowError(StatusUpdateError);
    });
  });

  describe('updateCoreStatus', () => {
    // ============ COMMON PROPERTIES ============
    const STATUS = CoreStatus.OUTBOUND_SENT;
    // =================== END ===================

    it('should update the core status successfully', async () => {
      // when
      updateCoreStatusInDb.mockResolvedValueOnce(undefined);
      await updateCoreStatus(CONVERSATION_ID, STATUS);

      // then
      expect(updateCoreStatusInDb).toBeCalledTimes(1);
      expect(updateCoreStatusInDb).toHaveBeenCalledWith(CONVERSATION_ID, STATUS, null);
    });

    it('should throw a StatusUpdateError error', async () => {
      // when
      updateCoreStatusInDb.mockRejectedValueOnce(undefined);

      // then
      await expect(() => updateCoreStatus(CONVERSATION_ID, STATUS)).rejects.toThrowError(
        StatusUpdateError
      );
    });
  });

  describe('replaceMessageIdsInObject', () => {
    it('should replace the message ids successfully', () => {
      // given
      const ehrCore = getValidEhrCore();
      const messageIdReplacements = [
        {
          oldMessageId: 'DFBA6AC0-DDC7-41ED-808B-AC162D1F16F0',
          newMessageId: '337EEC56-AE57-4C6A-B06A-3EFE17DD7481'
        },
        {
          oldMessageId: 'DFEC7740-DDC7-41ED-808B-AC162D1F16F0',
          newMessageId: '04486698-7644-4689-A281-3143AEF6C3EB'
        }
      ];

      // when
      const result = JSON.stringify(replaceMessageIdsInObject(ehrCore, messageIdReplacements));

      // then
      expect(result.includes(messageIdReplacements[0].newMessageId)).toBe(true);
      expect(result.includes(messageIdReplacements[1].newMessageId)).toBe(true);
      expect(result.includes(messageIdReplacements[0].oldMessageId)).toBe(false);
      expect(result.includes(messageIdReplacements[1].oldMessageId)).toBe(false);
    });
  });

  describe('createNewMessageIds', () => {
    const oldFragmentMessageIds = [
      '94F76288-DE8F-421A-8BE1-4D6EB28D6E1D',
      'E48DEF26-0E44-4B12-98E3-B20271CE35EA',
      'DD552736-5DA9-4CA4-A37B-CE43E7A50294'
    ];

    it('should create new fragment message ids and store them in database', async () => {
      // when
      storeOutboundMessageIds.mockResolvedValueOnce(undefined);

      await createAndStoreOutboundMessageIds(oldFragmentMessageIds, INBOUND_CONVERSATION_ID);

      // then
      const expectedMessageIdPairs = [
        {
          oldMessageId: '94F76288-DE8F-421A-8BE1-4D6EB28D6E1D',
          newMessageId: expect.stringMatching(UUID_UPPERCASE_REGEX)
        },
        {
          oldMessageId: 'E48DEF26-0E44-4B12-98E3-B20271CE35EA',
          newMessageId: expect.stringMatching(UUID_UPPERCASE_REGEX)
        },
        {
          oldMessageId: 'DD552736-5DA9-4CA4-A37B-CE43E7A50294',
          newMessageId: expect.stringMatching(UUID_UPPERCASE_REGEX)
        }
      ];
      expect(storeOutboundMessageIds).toHaveBeenCalledWith(
        expectedMessageIdPairs,
        INBOUND_CONVERSATION_ID
      );
    });

    it('should throw an error when failed to store the old and new message id pairs in database', async () => {
      // given
      const expectedError = new Error('some database error');
      storeOutboundMessageIds.mockRejectedValueOnce(expectedError);

      // when
      await expect(storeOutboundMessageIds(oldFragmentMessageIds))
        // then
        .rejects.toThrowError(expectedError);
    });
  });

  describe('getNewMessageIdForOldMessageId', () => {
    it('should return a new message ID for an old message ID', () => {
      // given
      const oldMessageId = 'E48DEF26-0E44-4B12-98E3-B20271CE35EA';
      const messageIdReplacements = [
        {
          oldMessageId: '94F76288-DE8F-421A-8BE1-4D6EB28D6E1D',
          newMessageId: '62151C2F-D8C8-4EA9-B5FC-E0E603D063EF'
        },
        {
          oldMessageId,
          newMessageId: '7F3BD33E-5CF9-40EB-A246-4EE0D73F0C9B'
        }
      ];

      // when
      const result = getNewMessageIdForOldMessageId(oldMessageId, messageIdReplacements);

      // then
      expect(result).toBe(messageIdReplacements[1].newMessageId);
    });
  });
});
