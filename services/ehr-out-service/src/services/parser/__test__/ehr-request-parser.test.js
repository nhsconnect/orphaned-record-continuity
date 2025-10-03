import path from 'path';
import expect from 'expect';
import { readFileSync } from 'fs';
import { parseEhrRequestMessage } from '../ehr-request-parser';
import { validateFieldsHaveSuccessfullyParsed } from '../parsing-validation';

// Mocking
jest.mock('../parsing-validation');

describe('ehr-request-parser.js', () => {
  // ============ COMMON PROPERTIES ============
  const exampleEhrRequest = JSON.parse(
    readFileSync(path.join(__dirname, 'data', 'ehr-requests', 'RCMR_IN010000UK05'), 'utf-8')
  );
  // =================== END ===================

  it('should parse an ehr request successfully', async () => {
    // given
    const ehrRequestId = 'FFFB3C70-0BCC-4D9E-A441-7E9C41A897AA';
    const nhsNumber = '9692842304';
    const odsCode = 'A91720';

    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    // when
    const parsedMessage = await parseEhrRequestMessage(exampleEhrRequest);

    // then
    expect(parsedMessage.ehrRequestId).toEqual(ehrRequestId);
    expect(parsedMessage.nhsNumber).toEqual(nhsNumber);
    expect(parsedMessage.odsCode).toEqual(odsCode);
  });
});
