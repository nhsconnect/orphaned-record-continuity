import path from 'path';
import { readFileSync } from 'fs';
import nock from 'nock';
import { v4 as uuidv4 } from 'uuid';

import { config } from '../../../config';

const GP2GP_MESSENGER_URL = 'http://localhost';
const GP2GP_MESSENGER_AUTH_KEY = 'fake-keys';
const REPOSITORY_ASID = 'fake-asid';
export const setupMockConfigForTest = () => {
  config.mockReturnValue({
    gp2gpMessengerAuthKeys: GP2GP_MESSENGER_AUTH_KEY,
    gp2gpMessengerServiceUrl: GP2GP_MESSENGER_URL,
    repositoryAsid: REPOSITORY_ASID
  });
};

export const createRandomUUID = number => {
  return Array(number)
    .fill('')
    .map(() => uuidv4().toUpperCase());
};

export const EhrMessageType = {
  core: 'Core',
  fragment: 'Fragment'
};

export const isSmallerThan256KB = input => {
  const jsObjectAsString = typeof input == 'string' ? input : JSON.stringify(input);
  return jsObjectAsString.length < 256 * 1024;
};

export const createMockGP2GPScope = messageType => {
  // helper function to create the nock scope of gp2gp messenger
  // ehr core & ehr fragment calls to different endpoints at gp2gp, so take that as an input param
  let outboundRequestBody = {};
  const endpoint =
    messageType === EhrMessageType.core ? '/ehr-out-transfers/core' : '/ehr-out-transfers/fragment';

  const scope = nock(GP2GP_MESSENGER_URL)
    .post(endpoint, requestBody => {
      Object.assign(outboundRequestBody, requestBody);
      return true;
    })
    .reply(() => [200, 'OK']);

  scope.outboundRequestBody = outboundRequestBody;

  return scope;
};

export const loadTestData = filename => {
  return JSON.parse(readFileSync(path.join(__dirname, 'data', filename), 'utf8'));
};
