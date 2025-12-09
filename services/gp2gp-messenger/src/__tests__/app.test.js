import { when } from 'jest-when';
import request from 'supertest';
import { v4 as uuid } from 'uuid';
import app from '../app';
import { sendMessage } from '../services/mhs/mhs-outbound-client';
import generatePdsRetrievalQuery from '../templates/generate-pds-retrieval-request';
import generateUpdateOdsRequest from '../templates/generate-update-ods-request';
import { getPracticeAsid } from '../services/fhir/sds-fhir-client';

jest.mock('../config/logging');
jest.mock('../config/', () => ({
  initializeConfig: jest.fn().mockReturnValue({
    pdsAsid: 'pdsAsid',
    repoAsid: 'repoAsid',
    nhsNumberPrefix: '944',
    spineOrgCode: 'code'
  })
}));
jest.mock('../middleware/auth');
jest.mock('../services/mhs/mhs-outbound-client');
jest.mock('../services/fhir/sds-fhir-client');
jest.mock('../templates/generate-pds-retrieval-request');
jest.mock('../templates/generate-update-ods-request');
jest.mock('../templates/ehr-request-template');

describe('app', () => {
  const testSerialChangeNumber = '2';
  const testPatientPdsId = 'cppz';
  const testOdsCode = 'B12345';
  const interactionId = 'QUPA_IN000008UK02';
  const interactionIdUpdate = 'PRPA_IN000203UK03';
  const mockUUID = 'ebf6ee70-b9b7-44a6-8780-a386fccd759c';
  const fakerequest =
    '<QUPA_IN000008UK02 xmlns="urn:hl7-org:v3" xmlns:hl7="urn:hl7-org:v3"></QUPA_IN000008UK02>';
  const fakerequestUpdate =
    '<PRPA_IN000203UK03 xmlns="urn:hl7-org:v3" xmlns:hl7="urn:hl7-org:v3"></PRPA_IN000203UK03>';
  const message = `
  <PDSResponse xmlns="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" classCode="OBS" moodCode="EVN" xsi:schemaLocation="urn:hl7-org:v3 ../../Schemas/PRPA_MT000201UK03.xsd">
        <pertinentSerialChangeNumber classCode="OBS" moodCode="EVN">
          <code code="2" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.35"/>
          <value value="${testSerialChangeNumber}"/>
        </pertinentSerialChangeNumber>
      <patientCareProvisionEvent classCode="PCPR" moodCode="EVN">
        <code codeSystem="2.16.840.1.113883.2.1.3.2.4.17.37" code="1"/>
        <effectiveTime>
          <low value="20140212"/>
        </effectiveTime>
        <id root="2.16.840.1.113883.2.1.3.2.4.18.1" extension="${testPatientPdsId}"/>
        <performer typeCode="PRF">
         <assignedEntity classCode="ASSIGNED">
            <id root="2.16.840.1.113883.2.1.4.3" extension="${testOdsCode}"/>
          </assignedEntity>
        </performer>
      </patientCareProvisionEvent>
  </PDSResponse>`;

  describe('GET /patient-demographics/:nhsNumber', () => {
    beforeEach(() => {
      uuid.mockImplementation(() => mockUUID);
      when(sendMessage)
        .calledWith({
          interactionId,
          conversationId: mockUUID,
          odsCode: 'code',
          message: fakerequest
        })
        .mockResolvedValue({ status: 200, data: message });

      generatePdsRetrievalQuery.mockResolvedValue(fakerequest);
    });

    it('should return a 200', done => {
      request(app).get('/patient-demographics/9999999999').expect(200).end(done);
    });

    it('should return on object containing serialChangeNumber and patientPdsId', done => {
      request(app)
        .get('/patient-demographics/9999999999')
        .expect(200)
        .expect(res =>
          expect(res.body.data).toEqual({
            serialChangeNumber: testSerialChangeNumber,
            patientPdsId: testPatientPdsId,
            odsCode: testOdsCode
          })
        )
        .end(done);
    });
  });

  describe('PATCH /patient-demographics/:nhsNumber', () => {
    beforeEach(() => {
      when(sendMessage)
        .calledWith({
          interactionId: interactionIdUpdate,
          conversationId: mockUUID,
          odsCode: 'code',
          message: fakerequestUpdate
        })
        .mockResolvedValue({ status: 202, data: {} });

      generateUpdateOdsRequest.mockResolvedValue(fakerequestUpdate);
    });

    it('should return a 204 (no content) if :nhsNumber is numeric and 10 digits and Authorization Header provided', done => {
      request(app)
        .patch('/patient-demographics/9442964410')
        .send({
          serialChangeNumber: '123',
          pdsId: 'cppz',
          newOdsCode: '12345',
          conversationId: mockUUID
        })
        .expect(204)
        .expect(res => {
          expect(res.body).toEqual({});
        })
        .end(done);
    });
  });

  describe('POST /health-record-requests/:nhsNumber', () => {
    getPracticeAsid.mockResolvedValue('practice_asid');

    it('should return a 204 status code', done => {
      request(app)
        .post('/health-record-requests/9444567890')
        .send({
          repositoryOdsCode: 'repo_ods_code',
          repositoryAsid: 'repo_asid',
          practiceOdsCode: 'practice_ods_code',
          conversationId: mockUUID
        })
        .expect(204)
        .end(done);
    });
  });
});
