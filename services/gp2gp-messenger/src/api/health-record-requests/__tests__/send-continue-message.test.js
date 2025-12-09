import request from 'supertest';
import app from '../../../app';
import { v4 as uuid } from 'uuid';
import { initializeConfig } from '../../../config';
import { getPracticeAsid } from '../../../services/fhir/sds-fhir-client';
import { logError, logInfo } from '../../../middleware/logging';
import { generateContinueRequest } from '../../../templates/generate-continue-request';
import { sendMessage } from '../../../services/mhs/mhs-outbound-client';

jest.mock('../../../config');
jest.mock('../../../middleware/logging');
jest.mock('../../../services/fhir/sds-fhir-client');
jest.mock('../../../templates/generate-continue-request');
jest.mock('../../../services/mhs/mhs-outbound-client');

describe('sendContinueMessage', () => {
  const authorizationKeys = 'correct-key';
  const repoAsid = '20000000890';
  initializeConfig.mockReturnValue({
    consumerApiKeys: { TEST_USER: authorizationKeys },
    repoAsid: repoAsid
  });

  const serviceId = 'urn:nhs:names:services:gp2gp:COPC_IN000001UK01';
  const interactionId = 'COPC_IN000001UK01';
  const gpReceivingAsid = '20000000678';
  const message = 'fake-continue-message';
  const conversationId = uuid().toUpperCase();
  const gpOdsCode = 'B12345';
  const ehrExtractMessageId = uuid().toUpperCase();
  const messageId = uuid().toUpperCase();
  const body = {
    conversationId,
    gpOdsCode,
    ehrExtractMessageId
  };

  describe('success', () => {
    const generateContinueRequestInputValues = {
      messageId,
      receivingAsid: gpReceivingAsid,
      sendingAsid: repoAsid,
      ehrExtractMessageId,
      gpOdsCode
    };

    it('should return a 204 when continue message has been created and sent', async () => {
      getPracticeAsid.mockResolvedValue(gpReceivingAsid);
      generateContinueRequest.mockReturnValueOnce(message);
      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .send(body)
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(204);
      expect(getPracticeAsid).toHaveBeenCalledWith(gpOdsCode, serviceId);
      expect(generateContinueRequest).toHaveBeenCalledWith(generateContinueRequestInputValues);
      expect(sendMessage).toHaveBeenCalledWith({
        interactionId,
        conversationId,
        odsCode: gpOdsCode,
        message,
        messageId
      });
      expect(logInfo).toHaveBeenCalledWith('Continue message sent to MHS');
    });
  });

  describe('failure', () => {
    it('should return a 503 if cannot get practice asid', async () => {
      getPracticeAsid.mockRejectedValueOnce({});
      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .send(body)
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(503);
      expect(logError).toHaveBeenCalledWith('Could not send continue message', {});
    });

    it('should return a 503 when continue message cannot be sent', async () => {
      getPracticeAsid.mockResolvedValue(gpReceivingAsid);
      generateContinueRequest.mockReturnValueOnce(message);
      sendMessage.mockRejectedValueOnce({});

      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .send(body)
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(503);
      expect(logError).toHaveBeenCalledWith('Could not send continue message', {});
    });
  });

  describe('validation', () => {
    it('should return an error if conversationId is not valid', async () => {
      const invalidConversationId = '12345';
      const errorMessage = [{ conversationId: "'conversationId' provided is not of type UUID" }];

      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .send({ conversationId: invalidConversationId, gpOdsCode, ehrExtractMessageId })
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(422);
      expect(res.body).toEqual({
        errors: errorMessage
      });
    });

    it('should return an error if gpOdsCode is not valid', async () => {
      const errorMessage = [{ gpOdsCode: "'gpOdsCode' has not been provided" }];

      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .send({ conversationId, ehrExtractMessageId })
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(422);
      expect(res.body).toEqual({
        errors: errorMessage
      });
    });

    it('should return an error if ehrExtractMessageId is not a uuid', async () => {
      const invalidEhrExtractMessageId = '1234';
      const errorMessage = [
        { ehrExtractMessageId: "'ehrExtractMessageId' provided is not of type UUID" }
      ];

      const res = await request(app)
        .post('/health-record-requests/continue-message')
        .send({ conversationId, gpOdsCode, ehrExtractMessageId: invalidEhrExtractMessageId })
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(422);
      expect(res.body).toEqual({
        errors: errorMessage
      });
    });
  });
});
