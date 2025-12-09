import { ACKNOWLEDGEMENT_TYPES } from '../../../constants/acknowledgement-types';
import { parseAcknowledgementMessage } from '../acknowledgement-parser';
import { SERVICES } from '../../../constants/services';
import { readFileSync } from 'fs';
import expect from 'expect';
import path from 'path';
import { validateFieldsHaveSuccessfullyParsed } from '../parsing-validation';

// Mocking
jest.mock('../parsing-validation');

describe('parseCommonAcknowledgementFields', () => {
  // ============ COMMON PROPERTIES ============
  const negativeTppAcknowledgementOne = JSON.parse(
    readFileSync(
      path.join(__dirname, 'data', 'acknowledgements', 'negative', 'MCCI_IN010000UK13_TPP_AR_01'),
      'utf-8'
    )
  );
  const negativeTppAcknowledgementTwo = JSON.parse(
    readFileSync(
      path.join(__dirname, 'data', 'acknowledgements', 'negative', 'MCCI_IN010000UK13_TPP_AR_02'),
      'utf-8'
    )
  );
  const positiveTppAcknowledgementOne = JSON.parse(
    readFileSync(
      path.join(__dirname, 'data', 'acknowledgements', 'positive', 'MCCI_IN010000UK13_TPP_AA_01'),
      'utf-8'
    )
  );
  // =================== END ===================

  it('should parse a negative acknowledgement from TPP successfully', async () => {
    // given
    const messageRef = '1800becd-710c-4e6e-871b-1f1844c32d00';
    const acknowledgementMessageId = 'BB8FC948-FA40-41ED-A594-F40343488B16';
    const referencedMessageId = '608368A0-DEC0-496B-9C4F-47CA90B81B58';
    const acknowledgementDetail =
      'hl7:{interactionId}/hl7:communicationFunctionRcv/hl7:device/hl7:id/@extension is missing, empty, invalid or ACL violation';
    const acknowledgementTypeCode = 'AR';
    const acknowledgementCode = '506';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedMessage = await parseAcknowledgementMessage(negativeTppAcknowledgementOne);

    // then
    expect(parsedMessage.messageRef).toEqual(messageRef);
    expect(parsedMessage.messageId).toEqual(acknowledgementMessageId);
    expect(parsedMessage.referencedMessageId).toEqual(referencedMessageId);
    expect(parsedMessage.acknowledgementDetail).toEqual(acknowledgementDetail);
    expect(parsedMessage.acknowledgementTypeCode).toEqual(acknowledgementTypeCode);
    expect(parsedMessage.acknowledgementCode).toEqual(acknowledgementCode);
    expect(SERVICES.gp2gp).toEqual(parsedMessage.service);
    expect(ACKNOWLEDGEMENT_TYPES.NEGATIVE).toContain(parsedMessage.acknowledgementTypeCode);
  });

  it('should parse a negative acknowledgement from TPP with no referencedMessageId successfully', async () => {
    // given
    const acknowledgementMessageId = '7E6C9590-FA4B-41ED-808B-AC162D1F16F0';
    const messageRef = '4660EAE7-FEB6-4858-93F6-17A177472F2F';
    const referencedMessageId = 'NOT FOUND';
    const acknowledgementDetail = 'Large Message general failure';
    const acknowledgementTypeCode = 'AR';
    const acknowledgementCode = '30';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedMessage = await parseAcknowledgementMessage(negativeTppAcknowledgementTwo);

    // then
    expect(parsedMessage.messageId).toEqual(acknowledgementMessageId);
    expect(parsedMessage.messageRef).toEqual(messageRef);
    expect(parsedMessage.referencedMessageId).toEqual(referencedMessageId);
    expect(parsedMessage.acknowledgementDetail).toEqual(acknowledgementDetail);
    expect(parsedMessage.acknowledgementTypeCode).toEqual(acknowledgementTypeCode);
    expect(parsedMessage.acknowledgementCode).toEqual(acknowledgementCode);
    expect(ACKNOWLEDGEMENT_TYPES.NEGATIVE).toContain(parsedMessage.acknowledgementTypeCode);
    expect(SERVICES.gp2gp).toEqual(parsedMessage.service);
  });

  it('should parse a positive acknowledgement from TPP successfully', async () => {
    // given
    const messageId = '3B768FD0-FECD-41ED-808B-AC162D1F16F0';
    const messageRef = '82BFE6C0-56CE-4466-886A-3FDE9D08D0C2';
    const referencedMessageId = 'NOT FOUND';
    const acknowledgementDetail = 'NOT FOUND';
    const acknowledgementTypeCode = 'AA';
    const acknowledgementCode = 'NOT FOUND';

    // when
    validateFieldsHaveSuccessfullyParsed.mockReturnValueOnce(undefined);

    const parsedMessage = await parseAcknowledgementMessage(positiveTppAcknowledgementOne);

    // then
    expect(parsedMessage.messageId).toEqual(messageId);
    expect(parsedMessage.messageRef).toEqual(messageRef);
    expect(parsedMessage.referencedMessageId).toEqual(referencedMessageId);
    expect(parsedMessage.acknowledgementDetail).toEqual(acknowledgementDetail);
    expect(parsedMessage.acknowledgementTypeCode).toEqual(acknowledgementTypeCode);
    expect(parsedMessage.acknowledgementCode).toEqual(acknowledgementCode);
    expect(ACKNOWLEDGEMENT_TYPES.POSITIVE).toContain(parsedMessage.acknowledgementTypeCode);
    expect(SERVICES.gp2gp).toEqual(parsedMessage.service);
  });
});
