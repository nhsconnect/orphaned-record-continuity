import request from 'supertest';
import { v1 as uuidv1, v4 as uuidv4 } from 'uuid';
import app from '../../../app';
import { buildEhrAcknowledgementPayload } from '../../../templates/generate-ehr-acknowledgement';
import { getPracticeAsid } from '../../../services/fhir/sds-fhir-client';
import { sendMessage } from '../../../services/mhs/mhs-outbound-client';
import { logError, logInfo } from '../../../middleware/logging';
import { AcknowledgementErrorCode } from '../../../constants/enums';

jest.mock('../../../middleware/logging');
jest.mock('../../../middleware/auth');
jest.mock('../../../templates/generate-ehr-acknowledgement');
jest.mock('../../../services/fhir/sds-fhir-client');
jest.mock('../../../services/mhs/mhs-outbound-client');

function expectValidationErrors(
  nhsNumber,
  conversationId,
  ehrCoreMessageId,
  odsCode,
  repositoryAsid,
  errors
) {
  return request(app)
    .post(`/health-record-requests/${nhsNumber}/acknowledgement`)
    .send({
      conversationId,
      messageId: ehrCoreMessageId,
      odsCode,
      repositoryAsid
    })
    .expect(422)
    .expect(res => {
      expect(res.body).toEqual({
        errors: errors
      });
    });
}

describe('POST /health-record-requests/{conversation-id}/acknowledgement', () => {
  const conversationId = uuidv4();
  const ehrCoreMessageId = uuidv1();
  const nhsNumber = '1234567890';
  const odsCode = 'B1234';
  const interactionId = 'MCCI_IN010000UK13';
  const serviceId = 'urn:nhs:names:services:gp2gp:MCCI_IN010000UK13';
  const message = 'fake-acknowledgement-message';
  const repositoryAsid = '200000001162';
  const practiceAsid = '200000001163';
  const errorCode = AcknowledgementErrorCode.ERROR_CODE_06.errorCode;
  const errorDisplayName = AcknowledgementErrorCode.ERROR_CODE_06.errorDisplayName;
  const ackParametersUsingConversationIdAsAckMessageId = {
    acknowledgementMessageId: conversationId,
    receivingAsid: practiceAsid,
    sendingAsid: repositoryAsid,
    acknowledgedMessageId: ehrCoreMessageId
  };

  describe('sendEhrAcknowledgement', () => {
    getPracticeAsid.mockReturnValue(practiceAsid);

    it('should return a 204 status code and build positive ack with necessary params including using conversationID as unique ackMessageId and sent EHR core messageId as acknowledged message id', done => {
      request(app)
        .post(`/health-record-requests/${nhsNumber}/acknowledgement`)
        .send({
          conversationId,
          messageId: ehrCoreMessageId,
          odsCode,
          repositoryAsid
        })
        .expect(204)
        .expect(() => {
          expect(getPracticeAsid).toHaveBeenCalledWith(odsCode, serviceId);
          expect(buildEhrAcknowledgementPayload).toHaveBeenCalledWith({
            acknowledgementMessageId: conversationId,
            receivingAsid: practiceAsid,
            sendingAsid: repositoryAsid,
            acknowledgedMessageId: ehrCoreMessageId
          });
        })
        .end(done);
    });

    it('should return a 204 status code and build negative ack with necessary params including using conversationID as unique ackMessageId and sent EHR core messageId as acknowledged message id', done => {
      request(app)
        .post(`/health-record-requests/${nhsNumber}/acknowledgement`)
        .send({
          conversationId,
          messageId: ehrCoreMessageId,
          odsCode,
          repositoryAsid,
          errorCode,
          errorDisplayName
        })
        .expect(204)
        .expect(() => {
          expect(getPracticeAsid).toHaveBeenCalledWith(odsCode, serviceId);
          expect(buildEhrAcknowledgementPayload).toHaveBeenCalledWith({
            acknowledgementMessageId: conversationId,
            receivingAsid: practiceAsid,
            sendingAsid: repositoryAsid,
            acknowledgedMessageId: ehrCoreMessageId,
            errorCode,
            errorDisplayName
          });
        })
        .end(done);
    });

    it('should accept a messageId with uuidv4', done => {
      request(app)
        .post(`/health-record-requests/${nhsNumber}/acknowledgement`)
        .send({ conversationId, messageId: uuidv4(), odsCode, repositoryAsid })
        .expect(204)
        .end(done);
    });

    it('should send acknowledgement to mhs with correct values', done => {
      buildEhrAcknowledgementPayload.mockReturnValue(message);
      request(app)
        .post(`/health-record-requests/${nhsNumber}/acknowledgement`)
        .send({
          conversationId,
          messageId: ehrCoreMessageId,
          odsCode,
          repositoryAsid
        })
        .expect(() => {
          expect(buildEhrAcknowledgementPayload).toHaveBeenCalledWith(
            ackParametersUsingConversationIdAsAckMessageId
          );
          expect(sendMessage).toHaveBeenCalledWith({
            interactionId,
            conversationId,
            odsCode,
            message
          });
          expect(logInfo).toHaveBeenCalledWith('Acknowledgement sent to MHS');
        })
        .end(done);
    });

    it('should return a 503 when cannot send acknowledgement to mhs', async () => {
      buildEhrAcknowledgementPayload.mockReturnValue(message);
      sendMessage.mockRejectedValue('cannot send acknowledgement to mhs');
      await request(app)
        .post(`/health-record-requests/${nhsNumber}/acknowledgement`)
        .send({
          conversationId,
          messageId: ehrCoreMessageId,
          odsCode,
          repositoryAsid
        })
        .expect(503)
        .expect(() => {
          expect(buildEhrAcknowledgementPayload).toHaveBeenCalledWith(
            ackParametersUsingConversationIdAsAckMessageId
          );
          expect(sendMessage).toHaveBeenCalledWith({
            interactionId,
            conversationId,
            odsCode,
            message
          });
          expect(logError).toHaveBeenCalled();
        });
    });
  });

  describe('acknowledgementValidation', () => {
    it('should return a 422 status code when nhsNumber is not 10 digits', done => {
      const invalidNhsNumber = '123';
      expectValidationErrors(
        invalidNhsNumber,
        conversationId,
        ehrCoreMessageId,
        odsCode,
        repositoryAsid,
        [{ nhsNumber: "'nhsNumber' provided is not 10 digits" }]
      ).end(done);
    });

    it('should return a 422 status code when nhsNumber is not numeric', done => {
      const invalidNhsNumber = 'notNumeric';
      expectValidationErrors(
        invalidNhsNumber,
        conversationId,
        ehrCoreMessageId,
        odsCode,
        repositoryAsid,
        [{ nhsNumber: "'nhsNumber' provided is not numeric" }]
      ).end(done);
    });

    it('should return a 422 status code when conversationId is not type uuid', done => {
      const invalidConversationId = '123';
      expectValidationErrors(
        nhsNumber,
        invalidConversationId,
        ehrCoreMessageId,
        odsCode,
        repositoryAsid,
        [{ conversationId: "'conversationId' provided is not of type UUIDv4" }]
      ).end(done);
    });

    it('should return a 422 status code when conversationId is not provided', done => {
      expectValidationErrors(nhsNumber, null, ehrCoreMessageId, odsCode, repositoryAsid, [
        { conversationId: "'conversationId' provided is not of type UUIDv4" },
        { conversationId: "'conversationId' is not configured" }
      ]).end(done);
    });

    it('should return a 422 status code when messageId is not type uuid', done => {
      const invalidMessageId = 'invalid';
      expectValidationErrors(nhsNumber, conversationId, invalidMessageId, odsCode, repositoryAsid, [
        { messageId: "'messageId' provided is not of type UUID" }
      ]).end(done);
    });

    it('should return a 422 status code when messageId is not provided', done => {
      expectValidationErrors(nhsNumber, conversationId, null, odsCode, repositoryAsid, [
        { messageId: "'messageId' provided is not of type UUID" },
        { messageId: "'messageId' is not configured" }
      ]).end(done);
    });

    it('should return a 422 status code when odsCode is not provided', done => {
      expectValidationErrors(nhsNumber, conversationId, ehrCoreMessageId, null, repositoryAsid, [
        { odsCode: "'odsCode' is not configured" }
      ]).end(done);
    });

    it('should return a 422 status code when repositoryAsid is not provided', done => {
      expectValidationErrors(nhsNumber, conversationId, ehrCoreMessageId, odsCode, null, [
        { repositoryAsid: "'repositoryAsid' is not configured" }
      ]).end(done);
    });
  });
});
