import request from 'supertest';
import { logError } from '../../../middleware/logging';
import { buildTestApp } from '../../../__builders__/test-app';
import { getOutboundConversationById } from '../../../services/database/dynamodb/outbound-conversation-repository';
import { ConversationStatus } from '../../../constants/enums';
import { registrationRequests } from '../index';

jest.mock('../../../middleware/logging');
jest.mock('../../../services/database/dynamodb/outbound-conversation-repository');
jest.mock('../../../config', () => ({
  config: jest.fn().mockReturnValue({ consumerApiKeys: { TEST: 'valid-key' } })
}));

describe('GET /registration-requests/', () => {
  const nhsNumber = '1234567890';
  const conversationId = '3A3EE007-1188-4978-8122-C1E2596F29C6';
  const odsCode = 'A12345';
  const invalidConversationId = 'de78578799';
  const status = ConversationStatus.OUTBOUND_STARTED;
  const testApp = buildTestApp('/registration-requests', registrationRequests);

  it('should return 200 and registration request information if :conversationId is uuidv4 and Authorization Header provided', async () => {
    getOutboundConversationById.mockResolvedValue({
      OutboundConversationId: conversationId,
      NhsNumber: nhsNumber,
      DestinationGp: odsCode,
      TransferStatus: status
    });

    const res = await request(testApp)
      .get(`/registration-requests/${conversationId}`)
      .set('Authorization', 'valid-key');

    const mockBody = {
      data: {
        type: 'registration-requests',
        id: conversationId,
        attributes: {
          nhsNumber,
          odsCode,
          status
        }
      }
    };

    expect(res.statusCode).toBe(200);
    expect(getOutboundConversationById).toHaveBeenCalledWith(conversationId);
    expect(res.body).toEqual(mockBody);
  });

  it('should return 200 and registration request information if :conversationId is uuidv1 and Authorization Header provided', async () => {
    const conversationIdUuidv1 = 'EBC252CA-3ADF-11EB-ADC1-0242AC120002';
    getOutboundConversationById.mockResolvedValue({
      OutboundConversationId: conversationIdUuidv1,
      NhsNumber: nhsNumber,
      DestinationGp: odsCode,
      TransferStatus: status
    });

    const res = await request(testApp)
      .get(`/registration-requests/${conversationIdUuidv1}`)
      .set('Authorization', 'valid-key');

    const mockBody = {
      data: {
        type: 'registration-requests',
        id: conversationIdUuidv1,
        attributes: {
          nhsNumber,
          odsCode,
          status
        }
      }
    };

    expect(res.statusCode).toBe(200);
    expect(getOutboundConversationById).toHaveBeenCalledWith(conversationIdUuidv1);
    expect(res.body).toEqual(mockBody);
  });

  it('should return an error if :conversationId is not valid', async () => {
    const errorMessage = [{ conversationId: "'conversationId' provided is not of type UUID" }];
    const res = await request(testApp)
      .get(`/registration-requests/${invalidConversationId}`)
      .set('Authorization', 'valid-key');

    expect(res.statusCode).toBe(422);
    expect(res.body).toEqual({
      errors: errorMessage
    });
  });

  it('should return 404 when conversation id cannot be found', async () => {
    const nonExistentConversationId = conversationId;
    getOutboundConversationById.mockResolvedValue(null);

    const res = await request(testApp)
      .get(`/registration-requests/${nonExistentConversationId}`)
      .set('Authorization', 'valid-key');

    expect(res.statusCode).toBe(404);
  });

  it('should return 503 when getRegistrationRequestStatusByConversationId returns rejected Promise', async () => {
    getOutboundConversationById.mockRejectedValue({});

    const res = await request(testApp)
      .get(`/registration-requests/${conversationId}`)
      .set('Authorization', 'valid-key');

    expect(logError).toHaveBeenCalledWith('Registration request status call failed', {});
    expect(res.statusCode).toBe(503);
  });
});
