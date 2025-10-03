import { validateFieldsHaveSuccessfullyParsed } from '../parsing-validation';
import { parseContinueRequestMessage } from '../continue-request-parser';
import { readFileSync } from 'fs';
import expect from 'expect';
import path from 'path';

// Mocking
jest.mock('../parsing-validation');

describe('continue-request-parser.js', () => {
  // ============ COMMON PROPERTIES ============
  const exampleContinueRequest = JSON.parse(
    readFileSync(path.join(__dirname, 'data', 'continue-requests', 'COPC_IN000001UK01'), 'utf-8')
  );
  // =================== END ===================

  it('should parse a continue request successfully', async () => {
    // given
    const odsCode = 'M85019';

    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    // when
    const parsedMessage = await parseContinueRequestMessage(exampleContinueRequest);

    // then
    expect(parsedMessage.odsCode).toEqual(odsCode);
  });
});
