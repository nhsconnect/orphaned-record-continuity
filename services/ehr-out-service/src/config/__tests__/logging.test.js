import { obfuscateSecrets, options } from '../logging';

describe('logging', () => {
  describe('obfuscateSecrets', () => {
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
  describe('combined formatter', () => {
    it('should replace secret values with obfuscated value', () => {
      const formatter = options.format;
      const result = formatter.transform({
        level: `INFO`,
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
