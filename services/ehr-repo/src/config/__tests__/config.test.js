import { initializeConfig } from '../index';

describe('config', () => {
  it('should correctly load consumer api keys', () => {
    process.env.API_KEY_FOR_E2E_TEST = 'xyz';
    process.env.API_KEY_FOR_GP_TO_REPO = 'abc';
    process.env.API_KEY_FOR_USER_FOO = 'tuv';
    process.env.USER_BAR = 'bar';
    process.env.NOT_AN_API_KEY_FOR_A_CONSUMER = 'not-a-key';

    const config = initializeConfig();
    const expectedConsumerApiKeys = { E2E_TEST: 'xyz', GP_TO_REPO: 'abc', USER_FOO: 'tuv' };
    expect(config.consumerApiKeys).toStrictEqual(expectedConsumerApiKeys);
  });
});
