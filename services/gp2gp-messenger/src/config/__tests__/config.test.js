import { initializeConfig } from '../';

const originalEnv = process.env;

describe('config', () => {
  describe('NHS_ENVIRONMENT', () => {
    afterEach(() => {
      process.env.NHS_ENVIRONMENT = originalEnv.NHS_ENVIRONMENT;
    });

    it('should get NHS_ENVIRONMENT = local when environment variable not defined', () => {
      if (process.env.NHS_ENVIRONMENT) delete process.env.NHS_ENVIRONMENT;
      expect(initializeConfig().nhsEnvironment).toEqual('local');
    });
  });

  describe('repoAsid', () => {
    afterEach(() => {
      process.env.GP2GP_MESSENGER_REPOSITORY_ASID = originalEnv.GP2GP_MESSENGER_REPOSITORY_ASID;
    });

    it('should return 200000001161 when GP2GP_MESSENGER_REPOSITORY_ASID is not set', () => {
      if (process.env.GP2GP_MESSENGER_REPOSITORY_ASID)
        delete process.env.GP2GP_MESSENGER_REPOSITORY_ASID;
      expect(initializeConfig().repoAsid).toEqual('200000001161');
    });
  });

  describe('repoOdsCode', () => {
    afterEach(() => {
      process.env.GP2GP_MESSENGER_REPOSITORY_ODS_CODE =
        originalEnv.GP2GP_MESSENGER_REPOSITORY_ODS_CODE;
    });

    it('should return B86041 when GP2GP_MESSENGER_REPOSITORY_ODS_CODE is not set', () => {
      if (process.env.GP2GP_MESSENGER_REPOSITORY_ODS_CODE)
        delete process.env.GP2GP_MESSENGER_REPOSITORY_ODS_CODE;
      expect(initializeConfig().repoOdsCode).toEqual('B86041');
    });
  });

  describe('wholesome config test', () => {
    afterEach(() => {
      process.env = originalEnv;
    });

    describe('wholesome config test', () => {
      afterEach(() => {
        process.env = originalEnv;
      });

      it('should map config with process.env values if set', () => {
        process.env.GP2GP_MESSENGER_REPOSITORY_ASID = 'repoAsid';
        process.env.GP2GP_MESSENGER_REPOSITORY_ODS_CODE = 'REPO_ODS_CODE';
        process.env.PDS_ASID = 'pdsAsid';
        process.env.GP2GP_MESSENGER_MHS_OUTBOUND_URL = 'mhsOutboundUrl';
        process.env.NHS_ENVIRONMENT = 'nhsEnvironment';
        process.env.SERVICE_URL = 'url';

        expect(initializeConfig()).toEqual(
          expect.objectContaining({
            repoAsid: 'repoAsid',
            repoOdsCode: 'REPO_ODS_CODE',
            pdsAsid: 'pdsAsid',
            mhsOutboundUrl: 'mhsOutboundUrl',
            nhsEnvironment: 'nhsEnvironment'
          })
        );
      });
    });
  });

  describe('api keys', () => {
    it('should correctly load consumer api keys', () => {
      process.env.API_KEY_FOR_E2E_TEST = 'xyz';
      process.env.API_KEY_FOR_GP_TO_REPO = 'abc';
      process.env.API_KEY_FOR_USER_FOO = 'tuv';
      process.env.USER_BAR = 'bar';
      process.env.NOT_AN_API_KEY_FOR_A_CONSUMER = 'not-a-key';

      const expectedConsumerApiKeys = { E2E_TEST: 'xyz', GP_TO_REPO: 'abc', USER_FOO: 'tuv' };
      expect(initializeConfig().consumerApiKeys).toStrictEqual(expectedConsumerApiKeys);
    });
  });
});
