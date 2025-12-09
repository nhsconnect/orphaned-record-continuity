import { extractSerialChangeNumber } from '../extract-serial-change-number';

describe('extractSerialChangeNumber', () => {
  const testSerialChangeNumber = '2';

  const pdsResponseWithSerialChangeNumber = `
      <pertinentSerialChangeNumber classCode="OBS" moodCode="EVN">
        <code code="2" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.35"/>
        <value value="${testSerialChangeNumber}"/>
      </pertinentSerialChangeNumber>
    `;

  const noSerialNumber = `
      <a classCode="OBS" moodCode="EVN">
        <code code="2" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.35"/>
        <value value="${testSerialChangeNumber}"/>
      </a>
    `;

  it('should extract the conversationId from XML body', () => {
    return extractSerialChangeNumber(pdsResponseWithSerialChangeNumber).then(serialChangeNumber =>
      expect(serialChangeNumber).toBe(testSerialChangeNumber)
    );
  });

  it('should throw error if it could not extract the serial change number ', () => {
    return extractSerialChangeNumber(noSerialNumber).catch(err =>
      expect(err.message).toBe('failed to extract PDS serial change number')
    );
  });
});
