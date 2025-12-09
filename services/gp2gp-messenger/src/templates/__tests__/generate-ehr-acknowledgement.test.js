import dateFormat from 'dateformat';
import testData from './testData.json';
import { buildEhrAcknowledgementPayload } from '../generate-ehr-acknowledgement';
import { XmlParser } from '../../services/parser/xml-parser/xml-parser';
import { AcknowledgementErrorCode } from '../../constants/enums';

function parse(payload) {
  return new XmlParser().parse(payload);
}

describe('generateEhrAcknowledgement', () => {
  // ============ COMMON PROPERTIES ============
  const ACKNOWLEDGEMENT_MESSAGE_ID = '11111111-28C0-41EB-ADC1-0242AC120002';
  const RECEIVING_ASID = testData.emisPractise.asid;
  const SENDING_ASID = testData.mhs.asid;
  const ACKNOWLEDGED_MESSAGE_ID = '22222222-28C0-41EB-ADC1-0242AC120002';
  // =================== END ===================

  it('should return a positive acknowledgement message template with correct values populated', async () => {
    const timestamp = dateFormat(Date.now(), 'yyyymmddHHMMss');
    const payload = buildEhrAcknowledgementPayload({
      acknowledgementMessageId: ACKNOWLEDGEMENT_MESSAGE_ID,
      receivingAsid: RECEIVING_ASID,
      sendingAsid: SENDING_ASID,
      acknowledgedMessageId: ACKNOWLEDGED_MESSAGE_ID
    });

    const ack = (await new XmlParser().parse(payload)).data.MCCI_IN010000UK13;

    expect(ack.id.root).toEqual(ACKNOWLEDGEMENT_MESSAGE_ID);
    expect(ack.acknowledgement.messageRef.id.root).toEqual(ACKNOWLEDGED_MESSAGE_ID);
    expect(ack.acknowledgement.typeCode).toEqual('AA');
    expect(ack.acknowledgement.acknowledgementDetail).toBeUndefined();
    expect(ack.ControlActEvent.reason).toBeUndefined();

    expect(payload).toContain(SENDING_ASID);
    expect(payload).toContain(RECEIVING_ASID);
    expect(payload).toContain(timestamp);
  });

  it.each([
    [
      'AR',
      AcknowledgementErrorCode.ERROR_CODE_06.errorCode,
      AcknowledgementErrorCode.ERROR_CODE_06.errorDisplayName
    ],
    [
      'AE',
      AcknowledgementErrorCode.ERROR_CODE_99.errorCode,
      AcknowledgementErrorCode.ERROR_CODE_99.errorDisplayName
    ]
  ])(
    'should return a negative acknowledgement message template with correct values populated',
    async (typeCode, errorCode, errorDisplayName) => {
      const timestamp = dateFormat(Date.now(), 'yyyymmddHHMMss');
      const payload = buildEhrAcknowledgementPayload({
        acknowledgementMessageId: ACKNOWLEDGEMENT_MESSAGE_ID,
        receivingAsid: RECEIVING_ASID,
        sendingAsid: SENDING_ASID,
        acknowledgedMessageId: ACKNOWLEDGED_MESSAGE_ID,
        errorCode,
        errorDisplayName
      });

      const ack = (await new XmlParser().parse(payload)).data.MCCI_IN010000UK13;

      expect(ack.id.root).toEqual(ACKNOWLEDGEMENT_MESSAGE_ID);
      expect(ack.acknowledgement.typeCode).toEqual(typeCode);
      expect(ack.acknowledgement.acknowledgementDetail.typeCode).toEqual(typeCode);
      expect(ack.acknowledgement.acknowledgementDetail.code.code).toEqual(errorCode);
      expect(ack.acknowledgement.acknowledgementDetail.code.displayName).toEqual(errorDisplayName);
      expect(ack.acknowledgement.messageRef.id.root).toEqual(ACKNOWLEDGED_MESSAGE_ID);
      expect(ack.ControlActEvent.reason.justifyingDetectedIssueEvent.code.code).toEqual(errorCode);
      expect(ack.ControlActEvent.reason.justifyingDetectedIssueEvent.code.displayName).toEqual(
        errorDisplayName
      );

      expect(payload).toContain(SENDING_ASID);
      expect(payload).toContain(RECEIVING_ASID);
      expect(payload).toContain(timestamp);
    }
  );

  it('should populate the acknowledgement message forcing message IDs to uppercase', async () => {
    const parameters = {
      acknowledgementMessageId: ACKNOWLEDGEMENT_MESSAGE_ID,
      receivingAsid: RECEIVING_ASID,
      sendingAsid: SENDING_ASID,
      acknowledgedMessageId: ACKNOWLEDGED_MESSAGE_ID
    };
    const payload = buildEhrAcknowledgementPayload(parameters);

    const ack = (await parse(payload)).data.MCCI_IN010000UK13;

    expect(ack.id).toEqual({ root: parameters.acknowledgementMessageId.toUpperCase() });
    expect(ack.acknowledgement.messageRef.id).toEqual({
      root: parameters.acknowledgedMessageId.toUpperCase()
    });
  });
});
