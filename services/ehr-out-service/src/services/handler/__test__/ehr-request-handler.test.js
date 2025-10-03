import expect from 'expect';
import ehrRequestHandler from '../ehr-request-handler';
import { parseConversationId } from '../../parser/parsing-utilities';
import { parseEhrRequestMessage } from '../../parser/ehr-request-parser';
import { transferOutEhrCore } from '../../transfer/transfer-out-ehr-core';
import { logError } from '../../../middleware/logging';

// Mocking
jest.mock('../../../middleware/logging');
jest.mock('../../parser/parsing-utilities');
jest.mock('../../parser/ehr-request-parser');
jest.mock('../../transfer/transfer-out-ehr-core');

describe('ehrRequestHandler', () => {
  // ============ COMMON PROPERTIES ============
  const CONVERSATION_ID = '17A757F2-F4D2-444E-A246-9CB77BEF7F22';
  const EHR_REQUEST_ID = 'FFFB3C70-0BCC-4D9E-A441-7E9C41A897AA';
  const NHS_NUMBER = '9692842304';
  const ODS_CODE = 'A91720';
  const EHR_REQUEST = {
    nhsNumber: NHS_NUMBER,
    odsCode: ODS_CODE,
    ehrRequestId: EHR_REQUEST_ID
  };
  // =================== END ===================

  it('should forward the ehr request to initiate ehr out transfer', async () => {
    // when
    parseEhrRequestMessage.mockResolvedValueOnce(Promise.resolve(EHR_REQUEST));
    parseConversationId.mockResolvedValueOnce(Promise.resolve(CONVERSATION_ID));

    await ehrRequestHandler(EHR_REQUEST);

    // then
    await expect(transferOutEhrCore).toHaveBeenCalledWith({
      conversationId: CONVERSATION_ID,
      nhsNumber: EHR_REQUEST.nhsNumber,
      odsCode: EHR_REQUEST.odsCode,
      ehrRequestId: EHR_REQUEST.ehrRequestId
    });
  });

  it('should log error when transfer fails due to error which was not handled in transferOutEhrCore', async () => {
    // when
    const error = new Error('some special error');
    parseEhrRequestMessage.mockResolvedValueOnce(Promise.resolve(EHR_REQUEST));
    parseConversationId.mockResolvedValueOnce(Promise.resolve(CONVERSATION_ID));
    transferOutEhrCore.mockRejectedValueOnce(error);

    await ehrRequestHandler(EHR_REQUEST);

    // then
    await expect(transferOutEhrCore).toHaveBeenCalledWith({
      conversationId: CONVERSATION_ID,
      nhsNumber: EHR_REQUEST.nhsNumber,
      odsCode: EHR_REQUEST.odsCode,
      ehrRequestId: EHR_REQUEST.ehrRequestId
    });
    expect(logError).toHaveBeenCalledWith('EHR out transfer failed due to unexpected error', error);
  });
});
