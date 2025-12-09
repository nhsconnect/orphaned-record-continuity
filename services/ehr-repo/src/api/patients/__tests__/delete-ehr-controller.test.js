import request from 'supertest';
import app from '../../../app';
import { markRecordAsSoftDeleteForPatient } from '../../../services/database/ehr-conversation-repository';
import { initializeConfig } from '../../../config';
import { logError, logWarning } from '../../../middleware/logging';
import { v4 as uuid } from 'uuid';

jest.mock('../../../services/database/ehr-conversation-repository');
jest.mock('../../../middleware/logging');
jest.mock('../../../services/storage/get-signed-url');
jest.mock('../../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({})
}));

describe('deleteEhrController', () => {
  initializeConfig.mockReturnValue({
    consumerApiKeys: { TEST_USER: 'correct-key' }
  });

  const authorizationKeys = 'correct-key';
  const nhsNumber = '1234567890';

  describe('success', () => {
    it('should return 200 when controller invoked correctly', async () => {
      const conversationIds = [uuid().toUpperCase(), uuid().toUpperCase(), uuid().toUpperCase()];
      markRecordAsSoftDeleteForPatient.mockResolvedValue(conversationIds);

      const res = await request(app)
        .delete(`/patients/${nhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(200);
      expect(markRecordAsSoftDeleteForPatient).toHaveBeenCalledWith(nhsNumber);
      expect(res.body.data.id).toEqual(nhsNumber);
      expect(res.body.data.type).toEqual('patients');
      expect(res.body.data.conversationIds).toEqual(conversationIds);
    });
  });

  describe('failure', () => {
    it('should return a 503 when an unexpected server error occurs', async () => {
      markRecordAsSoftDeleteForPatient.mockRejectedValue({});
      const res = await request(app)
        .delete(`/patients/${nhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(503);
      expect(logError).toHaveBeenCalledWith('Could not mark as deleted EHR record', {});
    });

    it('should return a 404 when record is not found', async () => {
      markRecordAsSoftDeleteForPatient.mockResolvedValue(undefined);
      const res = await request(app)
        .delete(`/patients/${nhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toEqual(404);
      expect(logWarning).toHaveBeenCalledWith('Could not find EHR record');
    });
  });

  describe('authentication', () => {
    it('should return 401 when authentication keys are missing', async () => {
      const res = await request(app).delete(`/patients/${nhsNumber}`);

      expect(res.status).toBe(401);
    });

    it('should return 403 when authentication keys are incorrect', async () => {
      const res = await request(app)
        .delete(`/patients/${nhsNumber}`)
        .set('Authorization', 'incorrect');

      expect(res.status).toBe(403);
    });
  });

  describe('validation', () => {
    it('should return 422 and an error message when nhsNumber is not numeric', async () => {
      const invalidNhsNumber = 'not-valid';
      const errorMessage = { nhsNumber: "'nhsNumber' provided is not numeric" };

      const res = await request(app)
        .delete(`/patients/${invalidNhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });

    it('should return 422 and an error message when nhsNumber is not 10 characters', async () => {
      const invalidNhsNumber = '123';
      const errorMessage = { nhsNumber: "'nhsNumber' provided is not 10 characters" };

      const res = await request(app)
        .delete(`/patients/${invalidNhsNumber}`)
        .set('Authorization', authorizationKeys);

      expect(res.status).toBe(422);
      expect(res.body.errors).toContainEqual(errorMessage);
    });
  });
});
