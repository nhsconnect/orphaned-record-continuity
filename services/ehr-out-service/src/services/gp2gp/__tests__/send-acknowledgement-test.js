import { SendAcknowledgementError } from '../../../errors/errors';
import { setupMockConfigForTest } from './test-utils';
import expect from 'expect';
import nock from 'nock';
import { sendAcknowledgement } from '../send-acknowledgement';
import { AcknowledgementErrorCode } from '../../../constants/enums';

// Mocking
jest.mock('../../../config');
jest.mock('../../transfer/transfer-out-util');

describe('sendAcknowledgement', () => {
  // ============ COMMON PROPERTIES ============
  const AUTH_KEYS = 'fake-keys';
  const REPOSITORY_ASID = 'fake-asid';
  const HEADERS = { reqheaders: { Authorization: AUTH_KEYS } };
  const REQUEST_BASE_URL = 'http://localhost';

  const NHS_NUMBER = '9000000001';
  const REQUEST_ENDPOINT = `/health-record-requests/${NHS_NUMBER}/acknowledgement`;
  const CONVERSATION_ID = '629D5A4A-63FA-4F3E-9186-052D50AB3D91';
  const MESSAGE_ID = '39B866B5-F521-429D-B6E4-BAA56ABBCDE5';
  const ODS_CODE = 'G67200';
  // =================== END ===================

  beforeAll(() => {
    setupMockConfigForTest();
  });

  it('Should send Positive Acknowledgement successfully', async () => {
    // given
    const requestBody = {
      repositoryAsid: REPOSITORY_ASID,
      odsCode: ODS_CODE,
      conversationId: CONVERSATION_ID,
      messageId: MESSAGE_ID
    };

    // when
    const mockUrlRequest = nock(REQUEST_BASE_URL, HEADERS)
      .post(REQUEST_ENDPOINT, requestBody)
      .reply(204);

    await sendAcknowledgement(NHS_NUMBER, ODS_CODE, CONVERSATION_ID, MESSAGE_ID);

    // then
    expect(mockUrlRequest.isDone()).toBe(true);
  });

  it('Should throw SendAcknowledgementError when failing to send Positive Acknowledgement', async () => {
    // given
    const requestBody = {
      repositoryAsid: REPOSITORY_ASID,
      odsCode: ODS_CODE,
      conversationId: CONVERSATION_ID,
      messageId: MESSAGE_ID
    };

    // when
    nock(REQUEST_BASE_URL, HEADERS).post(REQUEST_ENDPOINT, requestBody).reply(404);

    // then
    await expect(() =>
      sendAcknowledgement(NHS_NUMBER, ODS_CODE, CONVERSATION_ID, MESSAGE_ID)
    ).rejects.toThrowError(SendAcknowledgementError);
  });

  it('Should send Negative Acknowledgement successfully', async () => {
    // given
    const requestBody = {
      repositoryAsid: REPOSITORY_ASID,
      odsCode: ODS_CODE,
      conversationId: CONVERSATION_ID,
      messageId: MESSAGE_ID,
      errorCode: AcknowledgementErrorCode.ERROR_CODE_06_A.errorCode,
      errorDisplayName: AcknowledgementErrorCode.ERROR_CODE_06_A.errorDisplayName
    };

    // when
    const mockUrlRequest = nock(REQUEST_BASE_URL, HEADERS)
      .post(REQUEST_ENDPOINT, requestBody)
      .reply(204);

    await sendAcknowledgement(
      NHS_NUMBER,
      ODS_CODE,
      CONVERSATION_ID,
      MESSAGE_ID,
      AcknowledgementErrorCode.ERROR_CODE_06_A
    );

    // then
    expect(mockUrlRequest.isDone()).toBe(true);
  });

  it('Should throw SendAcknowledgementError when failing to send Negative Acknowledgement', async () => {
    // given
    const requestBody = {
      repositoryAsid: REPOSITORY_ASID,
      odsCode: ODS_CODE,
      conversationId: CONVERSATION_ID,
      messageId: MESSAGE_ID,
      errorCode: AcknowledgementErrorCode.ERROR_CODE_06_A.errorCode,
      errorDisplayName: AcknowledgementErrorCode.ERROR_CODE_06_A.errorDisplayName
    };

    // when
    nock(REQUEST_BASE_URL, HEADERS).post(REQUEST_ENDPOINT, requestBody).reply(404);

    // then
    await expect(() =>
      sendAcknowledgement(
        NHS_NUMBER,
        ODS_CODE,
        CONVERSATION_ID,
        MESSAGE_ID,
        AcknowledgementErrorCode.ERROR_CODE_06_A
      )
    ).rejects.toThrowError(SendAcknowledgementError);
  });
});
