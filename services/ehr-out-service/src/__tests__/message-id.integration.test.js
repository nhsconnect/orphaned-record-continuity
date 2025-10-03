import {
  extractReferencedFragmentMessageIds,
  parseMessageId
} from '../services/parser/parsing-utilities';
import { transferOutFragmentsForNewContinueRequest } from '../services/transfer/transfer-out-fragments';
import { transferOutEhrCore } from '../services/transfer/transfer-out-ehr-core';
import { v4 as uuidv4 } from 'uuid';
import { readFileSync } from 'fs';
import nock from 'nock';
import { sortBy } from 'lodash';
import { replaceMessageIdsInObject } from '../services/transfer/transfer-out-util';
import {
  buildMessageIdReplacement,
  cleanupRecordsForTestByNhsNumber,
  createInboundCompleteRecordForTest
} from '../utilities/integration-test-utilities';
import { storeOutboundMessageIds } from '../services/database/dynamodb/store-outbound-message-ids';
import { getAllMessageIdPairs } from '../services/database/dynamodb/ehr-fragment-repository';

describe('Replacement of message IDs', () => {
  const uuidRegexPattern =
    /^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$/;

  const gp2gpUrl = 'http://fake-gp2gpmessenger-url';
  const gp2gpAuth = 'gp2gp-auth';
  const gp2gpHeaders = { reqheaders: { authorization: auth => auth === gp2gpAuth } };
  const ehrRepoUrl = 'http://fake-ehr-repo-url';
  const ehrRepoAuth = 'ehr-repo-auth';
  const ehrRepoHeaders = { reqheaders: { authorization: auth => auth === ehrRepoAuth } };

  const conversationId = uuidv4().toUpperCase();
  const nhsNumber = '1234567890';
  const odsCode = 'fake-ods-code';
  const ehrRequestId = uuidv4();

  const inboundFragmentMessageIds = [
    'DFBA6AC0-DDC7-41ED-808B-AC162D1F16F0',
    'DFEC7740-DDC7-41ED-808B-AC162D1F16F0',
    'DFEC7741-DDC7-41ED-808B-AC162D1F16F0',
    'DFF61430-DDC7-41ED-808B-AC162D1F16F0'
  ];
  const inboundConversationId = '5037AE3A-0CE2-4F47-ADB5-8FDD92E6F5A4';
  const ehrCorePresignedUrl = 'http://fake-core-presigned-url';

  beforeAll(async () => {
    process.env.GP2GP_MESSENGER_SERVICE_URL = gp2gpUrl;
    process.env.GP2GP_MESSENGER_AUTHORIZATION_KEYS = gp2gpAuth;
    process.env.EHR_REPO_SERVICE_URL = ehrRepoUrl;
    process.env.EHR_REPO_AUTHORIZATION_KEYS = ehrRepoAuth;
  });

  afterAll(async () => {
    await cleanupRecordsForTestByNhsNumber(nhsNumber);
    delete process.env.GP2GP_MESSENGER_SERVICE_URL;
    delete process.env.GP2GP_MESSENGER_AUTHORIZATION_KEYS;
    delete process.env.EHR_REPO_SERVICE_URL;
    delete process.env.EHR_REPO_AUTHORIZATION_KEYS;
  });

  describe('EHR core uses new message IDs', () => {
    it('should update the message IDs of the EHR core and referenced fragments', async () => {
      // given
      const ehrCore = readFileSync('src/__tests__/data/ehr_with_fragments/ehr-core', 'utf8');
      const inboundCoreMessageId = await parseMessageId(ehrCore);
      const oldReferencedFragmentIds = await extractReferencedFragmentMessageIds(ehrCore);

      // set up mocks
      await createInboundCompleteRecordForTest(
        inboundConversationId,
        nhsNumber,
        inboundCoreMessageId,
        inboundFragmentMessageIds
      );
      const ehrRepoScope = createNockForEhrRepoCoreMessage();
      const gp2gpMessengerGetODSScope = createNockForGp2gpGetOdsCode();
      const s3Scope = nock(ehrCorePresignedUrl).get('/').reply(200, ehrCore);

      let gp2gpMessengerPostBody;

      const storePostBody = body => {
        gp2gpMessengerPostBody = body;
        return true;
      };

      const gp2gpMessengerSendCoreScope = nock(gp2gpUrl, gp2gpHeaders)
        .post('/ehr-out-transfers/core', body => storePostBody(body))
        .reply(204);

      // when
      await transferOutEhrCore({
        conversationId,
        nhsNumber,
        odsCode,
        ehrRequestId
      });

      // then
      expect(ehrRepoScope.isDone()).toBe(true);
      expect(gp2gpMessengerGetODSScope.isDone()).toBe(true);
      expect(s3Scope.isDone()).toBe(true);
      expect(gp2gpMessengerSendCoreScope.isDone()).toBe(true);

      expect(gp2gpMessengerPostBody).toEqual({
        conversationId: conversationId,
        odsCode: odsCode,
        ehrRequestId: ehrRequestId,
        coreEhr: expect.anything(),
        messageId: expect.stringMatching(uuidRegexPattern)
      });

      const outboundEhrCore = gp2gpMessengerPostBody.coreEhr;

      const outboundEhrCoreAsString = JSON.stringify(outboundEhrCore);

      expect(outboundEhrCoreAsString.includes(inboundCoreMessageId)).toBe(false);
      for (let oldFragmentId of oldReferencedFragmentIds) {
        expect(outboundEhrCoreAsString.includes(oldFragmentId)).toBe(false);
      }
    });
  });

  describe('Message fragments use new message IDs', () => {
    function readMessageFragmentFile(fragmentFilename) {
      return readFileSync(`src/__tests__/data/ehr_with_fragments/${fragmentFilename}`, 'utf8');
    }

    const getPresignedUrlForFragment = messageId =>
      `http://fake-fragment-presigned-url/${messageId}`;

    function createNockForEhrRepoFragmentMessage(messageId) {
      const ehrFragmentPresignedUrl = getPresignedUrlForFragment(messageId);

      return nock(ehrRepoUrl, ehrRepoHeaders)
        .get(`/fragments/${inboundConversationId}/${messageId}`)
        .reply(200, ehrFragmentPresignedUrl);
    }

    function createNockForS3BucketFragmentMessage(filename, messageId) {
      const fragmentFile = readMessageFragmentFile(filename);
      const ehrFragmentPresignedUrl = getPresignedUrlForFragment(messageId);
      const { hostname, pathname } = new URL(ehrFragmentPresignedUrl);

      return nock(`http://${hostname}`).get(pathname).reply(200, fragmentFile);
    }

    it('should update the message IDs of the message fragment and nested fragments within the fragment', async () => {
      // given
      const fragmentFilenamesAndOldMessageIds = {
        'fragment-1': 'DFBA6AC0-DDC7-41ED-808B-AC162D1F16F0',
        'fragment-2': 'DFEC7740-DDC7-41ED-808B-AC162D1F16F0',
        'fragment-2-1': 'DFEC7741-DDC7-41ED-808B-AC162D1F16F0',
        'fragment-2-2': 'DFF61430-DDC7-41ED-808B-AC162D1F16F0'
      };
      const fragmentFilenames = Object.keys(fragmentFilenamesAndOldMessageIds);
      const fragmentOldMessageIds = Object.values(fragmentFilenamesAndOldMessageIds);
      const inboundCoreMessageId = 'DF91D420-DDC7-41ED-808B-AC162D1F16F0';
      const outboundFragmentMessageIds = inboundFragmentMessageIds.map(() =>
        uuidv4().toUpperCase()
      );
      const messageIdReplacement = buildMessageIdReplacement(
        [inboundCoreMessageId, ...inboundFragmentMessageIds],
        [uuidv4().toUpperCase(), ...outboundFragmentMessageIds]
      );

      // when
      await createInboundCompleteRecordForTest(
        inboundConversationId,
        nhsNumber,
        inboundCoreMessageId,
        inboundFragmentMessageIds
      );
      await storeOutboundMessageIds(messageIdReplacement, inboundConversationId);

      const ehrRepoPatientRecordScope = createNockForEhrRepoCoreMessage();
      const s3Scopes = Object.entries(fragmentFilenamesAndOldMessageIds).map(
        ([filename, messageId]) => {
          return createNockForS3BucketFragmentMessage(filename, messageId);
        }
      );
      const ehrRepoFragmentScopes = fragmentOldMessageIds.map(messageId =>
        createNockForEhrRepoFragmentMessage(messageId)
      );

      const gp2gpMessengerPostRequestBodies = [];
      const storePostBody = body => {
        gp2gpMessengerPostRequestBodies.push(body);
        return true;
      };

      const gp2gpMessengerSendFragmentScope = nock(gp2gpUrl, gp2gpHeaders)
        .persist()
        .post('/ehr-out-transfers/fragment', body => storePostBody(body))
        .reply(204);

      await transferOutFragmentsForNewContinueRequest({ conversationId, nhsNumber, odsCode });

      // then
      // Ensure the Nock scopes have completed
      expect(ehrRepoPatientRecordScope.isDone()).toBe(true);
      s3Scopes.forEach(scope => {
        expect(scope.isDone()).toBe(true);
      });
      ehrRepoFragmentScopes.forEach(scope => {
        expect(scope.isDone()).toBe(true);
      });
      expect(gp2gpMessengerSendFragmentScope.isDone()).toBe(true);

      // Compare the POST request bodies which GP2GP Messenger was sent.
      gp2gpMessengerPostRequestBodies.forEach(postRequestBody => {
        expect(postRequestBody.conversationId).toEqual(conversationId);
        expect(postRequestBody.odsCode).toEqual(odsCode);
        expect(postRequestBody.messageId).toEqual(postRequestBody.messageId.toUpperCase());
        expect(fragmentOldMessageIds).not.toContain(postRequestBody.messageId);
      });

      // Get the new Message IDs from the database, compare with the
      // Message IDs within the POST Request bodies.
      const messageIdReplacements = await getAllMessageIdPairs(
        fragmentOldMessageIds,
        inboundConversationId
      );
      const newFragmentMessageIds = messageIdReplacements
        .map(replacement => replacement.newMessageId)
        .sort();

      const newMessageIdsInPostRequests = gp2gpMessengerPostRequestBodies
        .map(body => body.messageId)
        .sort();

      expect(newMessageIdsInPostRequests).toEqual(newFragmentMessageIds);

      // manually replace all message ids in copied ver of original messages,
      // and compare those with the actual outbound fragment messages
      let actualOutboundFragmentMessages = gp2gpMessengerPostRequestBodies.map(
        body => body.fragmentMessage
      );
      actualOutboundFragmentMessages = sortBy(
        actualOutboundFragmentMessages,
        getMessageIdFromMessage
      );

      let expectedOutboundFragmentMessages = fragmentFilenames.map(messageFileName => {
        const messageFragmentFile = JSON.parse(readMessageFragmentFile(messageFileName));
        return replaceMessageIdsInObject(messageFragmentFile, messageIdReplacements);
      });

      expectedOutboundFragmentMessages = sortBy(
        expectedOutboundFragmentMessages,
        getMessageIdFromMessage
      );

      expect(actualOutboundFragmentMessages).toEqual(expectedOutboundFragmentMessages);
    });
  });

  // HELPER FUNCTIONS
  const createNockForGp2gpGetOdsCode = () =>
    nock(gp2gpUrl, gp2gpHeaders)
      .persist()
      .get(`/patient-demographics/${nhsNumber}`)
      .reply(200, { data: { odsCode } });

  const createNockForEhrRepoCoreMessage = () =>
    nock(ehrRepoUrl, ehrRepoHeaders).get(`/patients/${nhsNumber}`).reply(200, {
      coreMessageUrl: ehrCorePresignedUrl,
      fragmentMessageIds: inboundFragmentMessageIds,
      inboundConversationId: inboundConversationId
    });

  const getMessageIdFromMessage = message => {
    const messageIdRegex = /<eb:MessageId>([A-F0-9-]+)<\/eb:MessageId>/;
    const match = message.ebXML.match(messageIdRegex);
    if (!match) throw new Error('Failed to extract messageId from message');
    return match[1];
  };
});
