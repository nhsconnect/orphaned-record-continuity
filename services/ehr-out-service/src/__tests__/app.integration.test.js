import { createOutboundConversation } from '../services/database/dynamodb/outbound-conversation-repository';
import { expectStructuredLogToContain, transportSpy } from '../__builders__/logging-helper';
import { readFile, validateMessageEquality } from './utilities/integration-test.utilities';
import { transferOutFragmentsForNewContinueRequest } from '../services/transfer/transfer-out-fragments';
import { transferOutEhrCore } from '../services/transfer/transfer-out-ehr-core';
import { getEhrCoreAndFragmentIdsFromRepo } from '../services/ehr-repo/get-ehr';
import { sendFragment } from '../services/gp2gp/send-fragment';
import { sendCore } from '../services/gp2gp/send-core';
import { agent as request } from 'supertest';
import { logger } from '../config/logging';
import { config } from '../config';
import expect from 'expect';
import { v4 } from 'uuid';
import app from '../app';
import { patientAndPracticeOdsCodesMatch } from '../services/transfer/transfer-out-util';
import {
  getFragment,
  getFragmentConversationAndMessageIdsFromEhrRepo
} from '../services/ehr-repo/get-fragment';
import nock from 'nock';
import { ConversationStatus } from '../constants/enums';
import {
  cleanupRecordsForTest,
  createInboundCompleteRecordForTest
} from '../utilities/integration-test-utilities';
import { storeOutboundMessageIds } from '../services/database/dynamodb/store-outbound-message-ids';
import { parseMessageId } from '../services/parser/parsing-utilities';

const fakeAuth = 'fake-keys';

// Setup mocking
jest.mock('../services/ehr-repo/get-ehr');
jest.mock('../services/transfer/transfer-out-util', () => {
  const originalModule = jest.requireActual('../services/transfer/transfer-out-util');

  return {
    __esModule: true,
    ...originalModule,
    patientAndPracticeOdsCodesMatch: jest.fn()
  };
});

jest.mock('../services/gp2gp/send-core');
jest.mock('../services/ehr-repo/get-fragment');
jest.mock('../services/gp2gp/send-fragment');

describe('GET /health', () => {
  // ============ COMMON PROPERTIES ============
  const HEALTH_ENDPOINT = '/health';
  const { nhsEnvironment } = config();
  // =================== END ===================

  it('should return 200 and the response from getHealthCheck', async () => {
    const expectedHealthCheckResponse = {
      version: '1',
      description: 'Health of ehr-out-service',
      nhsEnvironment: nhsEnvironment,
      details: {
        database: {
          type: 'dynamodb'
        }
      }
    };

    const response = await request(app).get(HEALTH_ENDPOINT);

    expect(response.statusCode).toBe(200);
    expect(response.body).toMatchObject(expectedHealthCheckResponse);
  });
});

describe('Swagger Documentation', () => {
  // ============ COMMON PROPERTIES ============
  const SWAGGER_ENDPOINT_1 = '/swagger';
  const SWAGGER_ENDPOINT_2 = '/swagger/index.html';
  // =================== END ===================

  it('GET /swagger - should return a redirect 301 status code and text/html content type response', async () => {
    const res = await request(app).get(SWAGGER_ENDPOINT_1);
    expect(res.statusCode).toBe(301);
  });

  it('GET /swagger/index.html - should return a 200 status code and text/html content type response', async () => {
    const res = await request(app).get(SWAGGER_ENDPOINT_2);
    expect(res.statusCode).toBe(200);
  });
});

describe('GET /registration-requests/:conversationId', () => {
  const outboundConversationId = v4().toUpperCase();
  const nhsNumber = '1234567891';
  const odsCode = 'B12345';
  const status = ConversationStatus.OUTBOUND_STARTED;

  beforeEach(() => {
    logger.add(transportSpy);
    process.env.API_KEY_FOR_TEST = fakeAuth;
  });

  afterEach(() => {
    delete process.env.API_KEY_FOR_TEST;
  });

  it('should return registration request info', async () => {
    const retrievalResponse = {
      data: {
        type: 'registration-requests',
        id: outboundConversationId,
        attributes: {
          nhsNumber,
          odsCode,
          status
        }
      }
    };

    await createInboundCompleteRecordForTest(v4().toUpperCase(), nhsNumber, v4().toUpperCase(), []);
    await createOutboundConversation(outboundConversationId, nhsNumber, odsCode);

    const res = await request(app)
      .get(`/registration-requests/${outboundConversationId}`)
      .set('Authorization', fakeAuth);

    expect(res.statusCode).toBe(200);
    expect(res.body).toMatchObject(retrievalResponse);
    expectStructuredLogToContain(transportSpy, {
      conversationId: outboundConversationId
    });
  });

  it('should return 404 when registration request cannot be found', async () => {
    const nonExistentConversationId = '941ca257-f88f-499b-8e13-f8a62e7fea7a';
    const res = await request(app)
      .get(`/registration-requests/${nonExistentConversationId}`)
      .set('Authorization', fakeAuth);

    expect(res.statusCode).toBe(404);
    expectStructuredLogToContain(transportSpy, {
      conversationId: nonExistentConversationId
    });
  });
});

describe('Ensure health record outbound XML is unchanged', () => {
  // ============ COMMON PROPERTIES ============
  // EHR Data
  const nhsNumber = '9693643038';
  const odsCode = 'B85002';
  const inboundConversationId = '9D83A41F-9C65-40B7-B573-AD49C04CCC93';
  const outboundConversationId = '05E36C93-2DEF-4586-B842-127C534FB8B7';
  const ehrRequestMessageId = '7670F731-FE63-41FC-B238-975C31AFF913';
  const { gp2gpMessengerAuthKeys, gp2gpMessengerServiceUrl } = config();

  const originalFragments = {
    'DD92589A-B5B4-4492-AADD-51534821F07B': readFile(
      'COPC_IN000001UK01_01',
      'equality-test',
      'large-ehr-no-external-attachments',
      'original'
    ),
    'DE4A9436-FFA3-49B1-8180-5570510F0C11': readFile(
      'COPC_IN000001UK01_02',
      'equality-test',
      'large-ehr-no-external-attachments',
      'original'
    ),
    '62330782-1C8B-45CD-95E3-4FC624091C61': readFile(
      'COPC_IN000001UK01_03',
      'equality-test',
      'large-ehr-no-external-attachments',
      'original'
    ),
    '770C42DD-301B-4177-A78E-0E9E62F3FDA1': readFile(
      'COPC_IN000001UK01_04',
      'equality-test',
      'large-ehr-no-external-attachments',
      'original'
    )
  };

  const fragmentMessageIds = Object.keys(originalFragments);

  const ehrRepoMessageIdResponse = {
    inboundConversationId: inboundConversationId,
    messageIds: fragmentMessageIds
  };

  // Nock
  const gp2gpMessengerEndpointUrl = `${gp2gpMessengerServiceUrl}/ehr-out-transfers/fragment`;
  const gp2gpMessengerHeaders = { headers: { Authorization: gp2gpMessengerAuthKeys } };

  // =================== END ===================

  beforeEach(async () => {
    await cleanupRecordsForTest(inboundConversationId).catch(() => {});
  });

  afterAll(async () => {
    await cleanupRecordsForTest(inboundConversationId);
  });

  it('should verify that a small EHR core is unchanged by XML changes', async () => {
    // given
    const originalEhrCore = readFile('RCMR_IN030000UK06', 'equality-test', 'small-ehr', 'original');
    const inboundCoreMessageId = await parseMessageId(originalEhrCore);
    const responseFromEhrRepo = {
      inboundConversationId: inboundConversationId,
      ehrCore: JSON.parse(originalEhrCore),
      fragmentMessageIds: []
    };
    await createInboundCompleteRecordForTest(
      inboundConversationId,
      nhsNumber,
      inboundCoreMessageId,
      []
    );

    // when
    getEhrCoreAndFragmentIdsFromRepo.mockReturnValueOnce(responseFromEhrRepo);
    patientAndPracticeOdsCodesMatch.mockReturnValue(true);
    sendCore.mockReturnValueOnce(undefined);

    await transferOutEhrCore({
      conversationId: outboundConversationId,
      nhsNumber: nhsNumber,
      odsCode: odsCode,
      ehrRequestId: ehrRequestMessageId
    });

    expect(sendCore).toHaveBeenCalled();
    const modifiedEhrCore = JSON.stringify(sendCore.mock.calls[0][2]);

    // then
    expect(validateMessageEquality(originalEhrCore, modifiedEhrCore)).toBe(true);
  });

  it('should verify that a fragment with no external attachments is unchanged by xml changes', async () => {
    // given
    const fragmentMessageIds = Object.keys(originalFragments);
    const inboundCoreMessageId = 'B1321D36-B5B0-43D1-A372-CAFDA8811258';
    const allInboundMessageIds = [inboundCoreMessageId, ...fragmentMessageIds];
    const messageIdReplacements = allInboundMessageIds.map(oldMessageId => {
      return {
        oldMessageId,
        newMessageId: oldMessageId.slice(0, 35) + '0'
      };
    });
    await createInboundCompleteRecordForTest(
      inboundConversationId,
      nhsNumber,
      inboundCoreMessageId,
      fragmentMessageIds
    );

    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepoMessageIdResponse);
    for (let messageId of fragmentMessageIds) {
      getFragment.mockReturnValueOnce(JSON.parse(originalFragments[messageId]));
    }

    await createOutboundConversation(outboundConversationId, nhsNumber, odsCode);
    await storeOutboundMessageIds(messageIdReplacements, inboundConversationId);

    nock(gp2gpMessengerEndpointUrl, gp2gpMessengerHeaders).post('/').reply(204); // This 'nock' is for sendFragment()

    await transferOutFragmentsForNewContinueRequest({
      conversationId: outboundConversationId,
      nhsNumber: nhsNumber,
      odsCode: odsCode
    });

    const originalFragmentsSorted = Object.values(originalFragments).sort();
    const receivedArguments = [0, 1, 2, 3]
      .map(i => JSON.stringify(sendFragment.mock.calls[i][3])) // 3 = fragmentMessage
      .sort();

    // then
    expect(sendFragment).toBeCalledTimes(4);
    expect(validateMessageEquality(originalFragmentsSorted[0], receivedArguments[0])).toBe(true);
    expect(validateMessageEquality(originalFragmentsSorted[1], receivedArguments[1])).toBe(true);
    expect(validateMessageEquality(originalFragmentsSorted[2], receivedArguments[2])).toBe(true);
    expect(validateMessageEquality(originalFragmentsSorted[3], receivedArguments[3])).toBe(true);
  });

  it('should verify that a fragment with external attachments is unchanged by xml changes', async () => {
    // given
    const fragmentMessageIds = Object.keys(originalFragments);
    const inboundCoreMessageId = 'B1321D36-B5B0-43D1-A372-CAFDA8811258';
    const allInboundMessageIds = [inboundCoreMessageId, ...fragmentMessageIds];
    const messageIdReplacements = allInboundMessageIds.map(oldMessageId => {
      return {
        oldMessageId,
        newMessageId: oldMessageId.slice(0, 35) + '0'
      };
    });
    await createInboundCompleteRecordForTest(
      inboundConversationId,
      nhsNumber,
      inboundCoreMessageId,
      fragmentMessageIds
    );

    // when
    getFragmentConversationAndMessageIdsFromEhrRepo.mockResolvedValueOnce(ehrRepoMessageIdResponse);
    for (let messageId of fragmentMessageIds) {
      getFragment.mockReturnValueOnce(JSON.parse(originalFragments[messageId]));
    }

    await createOutboundConversation(outboundConversationId, nhsNumber, odsCode);
    await storeOutboundMessageIds(messageIdReplacements, inboundConversationId);

    nock(gp2gpMessengerEndpointUrl, gp2gpMessengerHeaders).post('/').reply(204); // This 'nock' is for sendFragment()

    await transferOutFragmentsForNewContinueRequest({
      conversationId: outboundConversationId,
      nhsNumber: nhsNumber,
      odsCode: odsCode
    });

    const originalFragmentsSorted = Object.values(originalFragments).sort();
    const receivedArguments = [0, 1, 2, 3]
      .map(i => JSON.stringify(sendFragment.mock.calls[i][3])) // 3 = fragmentMessage
      .sort();

    expect(sendFragment).toBeCalledTimes(4);
    expect(validateMessageEquality(originalFragmentsSorted[0], receivedArguments[0])).toBe(true);
    expect(validateMessageEquality(originalFragmentsSorted[1], receivedArguments[1])).toBe(true);
    expect(validateMessageEquality(originalFragmentsSorted[2], receivedArguments[2])).toBe(true);
    expect(validateMessageEquality(originalFragmentsSorted[3], receivedArguments[3])).toBe(true);
  });
});
