import { getPracticeAsid } from '../../../services/fhir/sds-fhir-client';
import { updateExtractForSending } from '../../../services/parser/message/update-extract-for-sending';
import request from 'supertest';
import { v4 } from '../../../__mocks__/uuid';
import app from '../../../app';
import { sendMessage } from '../../../services/mhs/mhs-outbound-client';
import {
  removeTitleFromExternalAttachments,
  wrangleAttachments
} from '../../../services/mhs/mhs-attachments-wrangler';

jest.mock('../../../services/fhir/sds-fhir-client');
jest.mock('../../../services/parser/message/update-extract-for-sending');
jest.mock('../../../services/mhs/mhs-outbound-client');
jest.mock('../../../services/mhs/mhs-attachments-wrangler');
jest.mock('../../../config', () => ({
  initializeConfig: jest.fn().mockReturnValue({
    consumerApiKeys: { TEST_USER: 'correct-key' },
    repoOdsCode: 'repo_ods_code'
  })
}));

const authKey = 'correct-key';
const conversationId = v4();
const destinationOdsCode = 'destination_ods_code';

const externalAttachmentWithTitle = {
  message_id: '5678',
  document_id: 'CA0F6E92-592E-4491-9ECC-2E0C77C90BE4',
  title: 'title',
  description: 'description'
};

const externalAttachmentWithoutTitle = {
  message_id: '5678',
  document_id: 'CA0F6E92-592E-4491-9ECC-2E0C77C90BE4',
  description: 'description'
};

const mockRequestBodyWithMissingPayload = {
  conversationId: conversationId,
  odsCode: destinationOdsCode,
  ehrRequestId: v4(),
  messageId: v4(),
  coreEhr: 'core ehr stored in ehr repository'
};

const mockRequestBody = {
  conversationId: conversationId,
  odsCode: destinationOdsCode,
  ehrRequestId: v4(),
  messageId: v4(),
  coreEhr: {
    ebXML: 'ebxml',
    payload: 'payload',
    attachments: [[{ message_id: '1234' }]],
    external_attachments: [externalAttachmentWithTitle, externalAttachmentWithTitle]
  }
};

const invalidConversationId = {
  conversationId: 'not-uuid',
  ehrRequestId: v4(),
  messageId: v4(),
  odsCode: destinationOdsCode,
  coreEhr: {
    ebXML: 'ebxml',
    payload: 'payload',
    attachments: [[{ message_id: '1234' }]],
    external_attachments: [externalAttachmentWithTitle]
  }
};
const invalidEhrRequestId = {
  conversationId: v4(),
  ehrRequestId: 'ehrRequestId',
  messageId: v4(),
  odsCode: destinationOdsCode,
  coreEhr: {
    ebXML: 'ebxml',
    payload: 'payload',
    attachments: [[{ message_id: '1234' }]],
    external_attachments: [{ message_id: '5678' }]
  }
};
const invalidMessageId = {
  conversationId: v4(),
  ehrRequestId: v4(),
  messageId: 'INVALID-MESSAGE-ID',
  odsCode: destinationOdsCode,
  coreEhr: {
    ebXML: 'ebxml',
    payload: 'payload',
    attachments: [[{ message_id: '1234' }]],
    external_attachments: [{ message_id: '5678' }]
  }
};

const missingCoreEhr = {
  conversationId: v4(),
  ehrRequestId: v4(),
  messageId: v4(),
  odsCode: destinationOdsCode
};

const missingOdsCode = {
  conversationId: v4(),
  ehrRequestId: v4(),
  messageId: v4(),
  coreEhr: {
    ebXML: 'ebxml',
    payload: 'payload',
    attachments: [[{ message_id: '1234' }]],
    external_attachments: [{ message_id: '5678' }]
  }
};

const missingExternalAttachmentsInCore = {
  conversationId: v4(),
  ehrRequestId: v4(),
  messageId: v4(),
  odsCode: destinationOdsCode,
  coreEhr: {
    ebXML: 'ebxml',
    payload: 'payload',
    attachments: [[{ message_id: '1234' }]]
  }
};

describe('ehr out transfers', () => {
  const odsCode = destinationOdsCode;
  const interactionId = 'RCMR_IN030000UK06';
  const serviceId = `urn:nhs:names:services:gp2gp:${interactionId}`;
  it('should call getPracticeAsid', async () => {
    wrangleAttachments.mockReturnValue({
      attachments: [[{ message_id: '1234' }]],
      external_attachments: [{ message_id: '5678' }]
    });

    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(mockRequestBody);
    expect(res.status).toBe(204);
    expect(getPracticeAsid).toHaveBeenCalledWith(odsCode, serviceId);
  });

  it('should throw an error when there is no payload in the ehr core message', async () => {
    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(mockRequestBodyWithMissingPayload);

    expect(res.status).toBe(503);
    expect(res.body).toEqual({
      errors: [
        'Sending EHR Extract failed',
        'Could not extract payload from the JSON message stored in EHR Repo'
      ]
    });
  });

  it('should invoke updateExtractForSending, wrangleAttachments and sendMessage', async () => {
    getPracticeAsid.mockReturnValue('mockAsid');
    updateExtractForSending.mockReturnValue('payload');
    wrangleAttachments.mockReturnValue({
      attachments: [[{ message_id: '1234' }]],
      external_attachments: [{ message_id: '5678' }]
    });
    removeTitleFromExternalAttachments.mockReturnValue([
      externalAttachmentWithoutTitle,
      externalAttachmentWithoutTitle
    ]);

    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(mockRequestBody);

    expect(updateExtractForSending).toHaveBeenCalledWith(
      mockRequestBody.coreEhr.payload,
      mockRequestBody.ehrRequestId,
      'mockAsid',
      'repo_ods_code',
      destinationOdsCode
    );

    expect(wrangleAttachments).toHaveBeenCalledWith(mockRequestBody.coreEhr);

    expect(sendMessage).toHaveBeenCalledWith({
      conversationId: '00000000-0000-4000-a000-000000000000',
      interactionId: 'RCMR_IN030000UK06',
      messageId: mockRequestBody.messageId,
      message: 'payload',
      odsCode: destinationOdsCode,
      attachments: mockRequestBody.coreEhr.attachments,
      external_attachments: [externalAttachmentWithoutTitle, externalAttachmentWithoutTitle]
    });
    expect(res.status).toBe(204);
  });

  it('should remove title field from all external attachments', async () => {
    getPracticeAsid.mockReturnValue('mockAsid');
    updateExtractForSending.mockReturnValue('payload');
    removeTitleFromExternalAttachments.mockReturnValueOnce([
      externalAttachmentWithoutTitle,
      externalAttachmentWithoutTitle
    ]);

    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(mockRequestBody);

    expect(sendMessage).toHaveBeenCalledWith({
      conversationId: '00000000-0000-4000-a000-000000000000',
      interactionId: 'RCMR_IN030000UK06',
      message: 'payload',
      messageId: mockRequestBody.messageId,
      odsCode: destinationOdsCode,
      attachments: mockRequestBody.coreEhr.attachments,
      external_attachments: [externalAttachmentWithoutTitle, externalAttachmentWithoutTitle]
    });
    expect(res.status).toBe(204);
  });

  it('should return an error when conversationId is not a uuid', async () => {
    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(invalidConversationId);
    expect(res.status).toEqual(422);
    expect(res.body).toEqual({
      errors: [
        {
          conversationId: "'conversationId' provided is not of type UUID"
        }
      ]
    });
  });

  it('should return an error when ehrRequestId is not a uuid', async () => {
    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(invalidEhrRequestId);
    expect(res.status).toEqual(422);
    expect(res.body).toEqual({
      errors: [
        {
          ehrRequestId: 'Provided value is not of type UUID'
        }
      ]
    });
  });

  it('should return an error when messageId is not a uuid', async () => {
    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(invalidMessageId);
    expect(res.status).toEqual(422);
    expect(res.body).toEqual({
      errors: [
        {
          messageId: 'Provided value is not of type UUID'
        }
      ]
    });
  });

  it('should return an error when request body has no odsCode', async () => {
    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(missingOdsCode);
    expect(res.status).toEqual(422);
    expect(res.body).toEqual({
      errors: [
        {
          odsCode: 'Value has not been provided'
        }
      ]
    });
  });

  it('should return an error when request body has no coreEhr', async () => {
    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(missingCoreEhr);
    expect(res.status).toEqual(422);
    expect(res.body).toEqual({
      errors: [
        {
          coreEhr: 'Value has not been provided'
        }
      ]
    });
  });

  it('should be able to send core message without external attachments', async () => {
    // given
    const expectedMessage = {
      conversationId: missingExternalAttachmentsInCore.conversationId,
      interactionId: 'RCMR_IN030000UK06',
      message: 'payload',
      messageId: missingExternalAttachmentsInCore.messageId,
      odsCode: missingExternalAttachmentsInCore.odsCode,
      attachments: missingExternalAttachmentsInCore.coreEhr.attachments,
      external_attachments: null
    };

    // when
    updateExtractForSending.mockReturnValue('payload');
    wrangleAttachments.mockReturnValue({
      attachments: missingExternalAttachmentsInCore.coreEhr.attachments,
      external_attachments: null
    });

    const res = await request(app)
      .post('/ehr-out-transfers/core')
      .set('Authorization', authKey)
      .send(missingExternalAttachmentsInCore);

    // then
    expect(res.status).toEqual(204);
    expect(sendMessage).toHaveBeenCalledWith(expectedMessage);
    expect(removeTitleFromExternalAttachments).not.toHaveBeenCalled();
  });
});
