import { v4 as uuid } from 'uuid';
import nock from 'nock';
import request from 'supertest';
import app from '../app';
import { pdsRetrivealQueryResponseSuccess } from '../services/pds/__tests__/data/pds-retrieval-query-response-success';

const DEFAULT_SENDING_ASID = '200000001161';
describe('app integration', () => {
  const authKey = 'correct-key';
  const host = 'http://localhost';

  beforeEach(() => {
    process.env.API_KEY_FOR_TEST_USER = authKey;
    process.env.GP2GP_MESSENGER_MHS_OUTBOUND_URL = 'http://localhost/mhs-outbound';
    process.env.SDS_FHIR_URL = 'http://localhost/sds-fhir';
    process.env.SDS_FHIR_API_KEY = 'key';
    process.env.SPINE_ORG_CODE = 'YES';
    process.env.PDS_ASID = '928942012545';
  });

  describe('GET /patient-demographics', () => {
    it('should return a 200 status code for /patient-demographics/:nhsNumber', done => {
      const mhsOutboundHeaders = {
        'Content-Type': 'application/json',
        'Interaction-ID': 'QUPA_IN000008UK02',
        'Ods-Code': 'YES',
        'from-asid': DEFAULT_SENDING_ASID,
        'wait-for-response': 'false'
      };

      const mhsOutboundScope = nock(host, { reqheaders: mhsOutboundHeaders })
        .post('/mhs-outbound')
        .reply(200, pdsRetrivealQueryResponseSuccess);

      request(app)
        .get('/patient-demographics/9999999999')
        .set('Authorization', authKey)
        .expect(200)
        .expect(res => {
          expect(mhsOutboundScope.isDone()).toBe(true);
          expect(res.body).toEqual(
            expect.objectContaining({
              data: {
                odsCode: 'B86041',
                patientPdsId: 'cppz',
                serialChangeNumber: '138'
              }
            })
          );
        })
        .end(done);
    });
  });

  describe('POST /health-record-requests/:nhsNumber/acknowledgement', () => {
    const practiceAsid = '200007389';
    const conversationId = uuid();
    const odsCode = 'B1234';
    const mockBody = {
      conversationId,
      messageId: uuid(),
      odsCode: odsCode,
      repositoryAsid: '20000018274'
    };

    it('should successfully send acknowledgement and return 204', done => {
      const mhsOutboundHeaders = {
        'Content-Type': 'application/json',
        'Interaction-ID': 'MCCI_IN010000UK13',
        'Correlation-ID': conversationId,
        'Ods-Code': odsCode,
        'from-asid': DEFAULT_SENDING_ASID,
        'wait-for-response': 'false'
      };

      const mockFhirResponse = {
        entry: [
          {
            resource: {
              identifier: [
                {
                  system: 'https://fhir.nhs.uk/Id/nhsSpineASID',
                  value: practiceAsid
                },
                {
                  system: 'https://fake-fhir',
                  value: 'B12345-836483'
                }
              ]
            }
          }
        ]
      };

      const fhirScope = nock(`${host}/sds-fhir`, {
        reqheaders: {
          apiKey: 'key'
        }
      })
        .get(`/Device`)
        .query({
          organization: `https://fhir.nhs.uk/Id/ods-organization-code|${odsCode}`,
          identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|urn:nhs:names:services:gp2gp:MCCI_IN010000UK13`
        })
        .reply(200, mockFhirResponse);

      const mhsOutboundScope = nock(host, mhsOutboundHeaders).post('/mhs-outbound').reply(204);

      request(app)
        .post('/health-record-requests/9999999999/acknowledgement')
        .set('Authorization', authKey)
        .send(mockBody)
        .expect(204)
        .expect(() => {
          expect(fhirScope.isDone()).toBe(true);
          expect(mhsOutboundScope.isDone()).toBe(true);
        })
        .end(done);
    });
  });

  describe('POST /health-record-requests/continue-message', () => {
    const practiceAsid = '200007389';
    const interactionId = 'COPC_IN000001UK01';
    const conversationId = uuid();
    const gpOdsCode = 'B12345';
    const ehrExtractMessageId = uuid();
    const body = {
      conversationId,
      gpOdsCode,
      ehrExtractMessageId
    };

    it('should successfully send continue message and return 204', async () => {
      const mhsOutboundHeaders = {
        'Content-Type': 'application/json',
        'Interaction-ID': interactionId,
        'Correlation-ID': conversationId,
        'Ods-Code': gpOdsCode,
        'from-asid': DEFAULT_SENDING_ASID,
        'wait-for-response': 'false'
      };

      const mockFhirResponse = {
        entry: [
          {
            resource: {
              identifier: [
                {
                  system: 'https://fhir.nhs.uk/Id/nhsSpineASID',
                  value: practiceAsid
                },
                {
                  system: 'https://fake-fhir',
                  value: 'B12345-836483'
                }
              ]
            }
          }
        ]
      };

      const fhirScope = nock(`${host}/sds-fhir`, {
        reqheaders: {
          apiKey: 'key'
        }
      })
        .get(`/Device`)
        .query({
          organization: `https://fhir.nhs.uk/Id/ods-organization-code|${gpOdsCode}`,
          identifier: `https://fhir.nhs.uk/Id/nhsServiceInteractionId|urn:nhs:names:services:gp2gp:${interactionId}`
        })
        .reply(200, mockFhirResponse);

      const mhsOutboundScope = nock(host, mhsOutboundHeaders).post('/mhs-outbound').reply(204);

      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .set('Authorization', authKey)
        .send(body);

      expect(res.status).toEqual(204);
      expect(fhirScope.isDone()).toBe(true);
      expect(mhsOutboundScope.isDone()).toBe(true);
    });
  });
});
