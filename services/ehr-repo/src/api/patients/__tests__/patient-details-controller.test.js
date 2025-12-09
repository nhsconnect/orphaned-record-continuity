import request from 'supertest';
import { when } from 'jest-when';
import { v4 as uuid } from 'uuid';
import app from '../../../app';
import { initializeConfig } from '../../../config';
import { logError, logInfo, logWarning } from '../../../middleware/logging';
import getSignedUrl from '../../../services/storage/get-signed-url';
import {
  getCurrentConversationIdForPatient,
  getMessageIdsForConversation
} from '../../../services/database/ehr-conversation-repository';
import { HealthRecordNotFoundError } from '../../../errors/errors';

jest.mock('../../../services/database/ehr-conversation-repository');
jest.mock('../../../middleware/logging');
jest.mock('../../../services/storage/get-signed-url');
jest.mock('../../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({})
}));

describe('patientDetailsController', () => {
  initializeConfig.mockReturnValue({
    consumerApiKeys: { TEST_USER: 'correct-key' }
  });

  const authorizationKeys = 'correct-key';

  describe('success', () => {
    it('should return 200 and correct link to health record extract given a small record', async () => {
      // given
      const nhsNumber = '1234567890';
      const conversationId = uuid().toUpperCase();
      const messageId = uuid().toUpperCase();
      const presignedUrl = 'test-url';

      getCurrentConversationIdForPatient.mockResolvedValue(conversationId);
      getMessageIdsForConversation.mockResolvedValue({
        coreMessageId: messageId,
        fragmentMessageIds: []
      });
      getSignedUrl.mockResolvedValue(presignedUrl);

      // when
      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set({ Authorization: authorizationKeys, conversationId: conversationId });

      // then
      expect(res.status).toBe(200);
      expect(getCurrentConversationIdForPatient).toHaveBeenCalledWith(nhsNumber);
      expect(getSignedUrl).toHaveBeenCalledWith(conversationId, messageId, 'getObject');
      expect(res.body.coreMessageUrl).toEqual(presignedUrl);
      expect(res.body.fragmentMessageIds).toEqual([]);
      expect(res.body.conversationIdFromEhrIn).toEqual(conversationId);
    });

    it('should return 200 and correct link to health record extract and fragment message IDs', async () => {
      // given
      const nhsNumber = '1234567890';
      const conversationId = uuid().toUpperCase();
      const healthRecordExtractId = uuid();
      const fragmentMessageId = uuid().toUpperCase();
      const extractPresignedUrl = 'extract-url';

      getCurrentConversationIdForPatient.mockResolvedValue(conversationId);
      getMessageIdsForConversation.mockResolvedValue({
        coreMessageId: healthRecordExtractId,
        fragmentMessageIds: [fragmentMessageId]
      });
      when(getSignedUrl)
        .calledWith(conversationId, healthRecordExtractId, 'getObject')
        .mockResolvedValue(extractPresignedUrl);

      // when
      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set({ Authorization: authorizationKeys, conversationId: conversationId });

      // then
      expect(res.status).toBe(200);
      expect(getCurrentConversationIdForPatient).toHaveBeenCalledWith(nhsNumber);
      expect(getSignedUrl).toHaveBeenCalledWith(conversationId, healthRecordExtractId, 'getObject');
      expect(getSignedUrl).not.toHaveBeenCalledWith(conversationId, fragmentMessageId, 'getObject');
      expect(res.body.coreMessageUrl).toEqual(extractPresignedUrl);
      expect(res.body.fragmentMessageIds).toEqual([fragmentMessageId]);
      expect(res.body.conversationIdFromEhrIn).toEqual(conversationId);
    });

    it('should return 200 but log a warning when conversation id is not passed as header', async () => {
      // given
      const nhsNumber = '1234567890';
      const conversationId = uuid().toUpperCase();
      const healthRecordExtractId = uuid();
      const fragmentMessageId = uuid().toUpperCase();
      const extractPresignedUrl = 'extract-url';

      getCurrentConversationIdForPatient.mockResolvedValue(conversationId);
      getMessageIdsForConversation.mockResolvedValue({
        coreMessageId: healthRecordExtractId,
        fragmentMessageIds: [fragmentMessageId]
      });
      when(getSignedUrl)
        .calledWith(conversationId, healthRecordExtractId, 'getObject')
        .mockResolvedValue(extractPresignedUrl);

      // when
      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set({ Authorization: authorizationKeys });

      // then
      expect(logWarning).toHaveBeenCalledWith('conversationId not passed as header');
      expect(res.status).toEqual(200);
    });
  });

  describe('failure', () => {
    const nhsNumber = '1234567890';
    const conversationId = 'fake-conversationId';

    it('should return a 404 when no complete health record is found', async () => {
      // given
      getCurrentConversationIdForPatient.mockRejectedValue(new HealthRecordNotFoundError());

      // when
      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set({ Authorization: authorizationKeys, conversationId: conversationId });

      // then
      expect(res.status).toEqual(404);
      expect(logInfo).toHaveBeenCalledWith('Did not find a complete patient health record');
    });

    it('should return a 503 when cannot get patient health record from database', async () => {
      // given
      getCurrentConversationIdForPatient.mockRejectedValue({ bob: 'cheese' });

      // when
      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set({ Authorization: authorizationKeys, conversationId: conversationId });

      // then
      expect(res.status).toEqual(503);
      expect(logError).toHaveBeenCalledWith('Could not retrieve patient health record', {
        bob: 'cheese'
      });
    });
  });

  describe('authentication', () => {
    it('should return 401 when authentication keys are missing', async () => {
      const nhsNumber = '1234567890';
      const res = await request(app).get(`/patients/${nhsNumber}`);

      expect(res.status).toBe(401);
    });

    it('should return 403 when authentication keys are incorrect', async () => {
      const nhsNumber = '1234567890';
      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set('Authorization', 'incorrect');

      expect(res.status).toBe(403);
    });
  });

  describe('validation', () => {
    it('should return 422 and an error message when nhsNumber is not numeric', async () => {
      const nhsNumber = 'not-valid';
      const errorMessage = { nhsNumber: "'nhsNumber' provided is not numeric" };

      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when nhsNumber is not 10 characters', async () => {
      const nhsNumber = '123';
      const errorMessage = { nhsNumber: "'nhsNumber' provided is not 10 characters" };

      const res = await request(app)
        .get(`/patients/${nhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });
  });
});
