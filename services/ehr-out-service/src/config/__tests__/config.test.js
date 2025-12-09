import { config } from '..';

describe('config', () => {
  let serviceUrl;

  beforeEach(() => {
    serviceUrl = process.env.SERVICE_URL;
  });

  afterEach(() => {
    process.env.SERVICE_URL = serviceUrl;
  });

  it('service url defaults to the correct value when environment variables not set', () => {
    if (process.env.SERVICE_URL) delete process.env.SERVICE_URL;
    expect(config().repoToGpServiceUrl).toEqual(`http://127.0.0.1:3000`);
  });

  it('service url is the correct value when environment variables are set', () => {
    process.env.SERVICE_URL = 'url';
    expect(config().repoToGpServiceUrl).toEqual(`url`);
  });

  it('nhs Environment is the correct value when environment variables are set', () => {
    process.env.NHS_ENVIRONMENT = 'local';
    expect(config().nhsEnvironment).toEqual('local');
  });

  describe('api keys', () => {
    it('should correctly load consumer api keys', () => {
      process.env.API_KEY_FOR_E2E_TEST = 'xyz';
      process.env.API_KEY_FOR_GP_TO_REPO = 'abc';
      process.env.API_KEY_FOR_USER_FOO = 'tuv';
      process.env.USER_BAR = 'bar';
      process.env.NOT_AN_API_KEY_FOR_A_CONSUMER = 'not-a-key';

      const expectedConsumerApiKeys = { E2E_TEST: 'xyz', GP_TO_REPO: 'abc', USER_FOO: 'tuv' };
      expect(config().consumerApiKeys).toStrictEqual(expectedConsumerApiKeys);
    });
  });
});
