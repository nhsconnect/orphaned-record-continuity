import { obfuscateSecrets, options } from '../logging';

jest.mock('winston', () => ({
  ...jest.requireActual('winston'),
  createLogger: jest.fn().mockImplementation(() => ({
    log: jest.fn(),
    error: jest.fn(),
    debug: jest.fn(),
    warn: jest.fn()
  }))
}));

describe('logging', () => {
  describe('obfuscateSecrets', () => {
    const formatter = obfuscateSecrets();
    const obfuscatedString = '********';

    it('should replace top level data key value with obfuscated values', () => {
      const obfuscatedObject = formatter.transform({
        data: 'secret-payload'
      });

      expect(obfuscatedObject).toStrictEqual(
        expect.objectContaining({
          data: obfuscatedString
        })
      );
    });

    it('should replace top level passcode key value with obfuscated values', () => {
      const obfuscatedObject = formatter.transform({
        passcode: '123456789'
      });

      expect(obfuscatedObject).toStrictEqual(
        expect.objectContaining({
          passcode: obfuscatedString
        })
      );
    });

    it('should replace nested passcode key value with obfuscated values', () => {
      const obfuscatedObject = formatter.transform({
        nested: {
          passcode: '123456789'
        }
      });

      expect(obfuscatedObject).toStrictEqual(
        expect.objectContaining({
          nested: {
            passcode: obfuscatedString
          }
        })
      );
    });

    it('should replace nested passcode key value with obfuscated values', () => {
      const obfuscatedObject = formatter.transform({
        nested: {
          data: 'some-secret-data'
        }
      });

      expect(obfuscatedObject).toStrictEqual(
        expect.objectContaining({
          nested: {
            data: obfuscatedString
          }
        })
      );
    });

    it('should replace secret values with obfuscated value', () => {
      const formatter = obfuscateSecrets();

      const messageSymbol = Symbol('message');
      const result = formatter.transform({
        message: `some-message`,
        data: 'secret-payload',
        error: {
          port: 61614,
          connectArgs: {
            ssl: true,
            connectHeaders: {
              login: 'abcdefg',
              authorization: '1234567',
              Authorization: '1234567',
              apiKey: '1234567'
            }
          }
        },
        [messageSymbol]: 'some-symbol-message'
      });

      expect(result).toEqual({
        message: `some-message`,
        data: '********',
        error: {
          port: 61614,
          connectArgs: {
            ssl: true,
            connectHeaders: {
              login: 'abcdefg',
              authorization: '********',
              Authorization: '********',
              apiKey: '********'
            }
          }
        },
        [messageSymbol]: 'some-symbol-message'
      });
    });
  });

  describe('options', () => {
    it('should contain one transport', () => {
      expect(options.transports.length).toBe(1);
    });

    it('should have a formatter', () => {
      expect(options.format).not.toBeNull();
    });
  });

  describe('combined formatter', () => {
    it('should replace secret values with obfuscated value', () => {
      const formatter = options.format;
      const result = formatter.transform({
        level: 'INFO',
        message: `some-message`,
        data: 'secret-payload',
        error: {
          port: 61614,
          connectArgs: {
            ssl: true,
            connectHeaders: {
              login: 'abcdefg',
              authorization: '1234567',
              Authorization: '1234567',
              apiKey: '1234567'
            }
          }
        }
      });
      const messageSymbol = Object.getOwnPropertySymbols(result)[0];

      expect(result[messageSymbol]).not.toContain('1234567');
    });
  });
});
