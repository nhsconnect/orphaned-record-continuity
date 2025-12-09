import { extractOdsCode } from '../extract-ods-code';

describe('extractOdsCode', () => {
  const testOdsCode = 'B23731';

  const pdsResponseWithOdsCode = `
    <patientCareProvisionEvent classCode="PCPR" moodCode="EVN">
      <code codeSystem="2.16.840.1.113883.2.1.3.2.4.17.37" code="1"/>
      <effectiveTime>
        <low value="20140212"/>
      </effectiveTime>
      <performer typeCode="PRF">
         <assignedEntity classCode="ASSIGNED">
            <id root="2.16.840.1.113883.2.1.4.3" extension="${testOdsCode}"/>
          </assignedEntity>
      </performer>
    </patientCareProvisionEvent>
    `;

  const multiplePatientCareProvisionEventsWithNoneGP = `
    <playedOtherProviderPatient classCode="PAT">
      <patientCareProvisionEvent classCode="PCPR" moodCode="EVN">
        <code codeSystem="2.16.840.1.113883.2.1.3.2.4.17.37" code="P1"/>
        <effectiveTime>
          <low value="20140212"/>
        </effectiveTime>
        <performer typeCode="PRF">
         <assignedEntity classCode="ASSIGNED">
            <id root="2.16.840.1.113883.2.1.4.3" extension="12345"/>
          </assignedEntity>
        </performer>
      </patientCareProvisionEvent>
      <patientCareProvisionEvent classCode="PCPR" moodCode="EVN">
        <code codeSystem="2.16.840.1.113883.2.1.3.2.4.17.37" code="1"/>
        <effectiveTime>
          <low value="20140212"/>
        </effectiveTime>
        <performer typeCode="PRF">
         <assignedEntity classCode="ASSIGNED">
            <id root="2.16.840.1.113883.2.1.4.3" extension="${testOdsCode}"/>
          </assignedEntity>
        </performer>
      </patientCareProvisionEvent>
    </playedOtherProviderPatient>
    `;

  const noPatientCareProvisionEvent = `
      <playedOtherProviderPatient classCode="PAT">
      </playedOtherProviderPatient>
  `;

  it('should extract the pdsId from XML body', () => {
    return expect(extractOdsCode(pdsResponseWithOdsCode)).resolves.toBe(testOdsCode);
  });

  it('should extract the pdsId from XML body if the body contains patientCareProvisionEvent is not GP', () => {
    return expect(extractOdsCode(multiplePatientCareProvisionEventsWithNoneGP)).resolves.toBe(
      testOdsCode
    );
  });

  it('should throw error if it could not extract the ods code ', () => {
    return expect(extractOdsCode(noPatientCareProvisionEvent)).rejects.toEqual(
      Error('Failed to extract ODS Code')
    );
  });
});
