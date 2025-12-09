import { readFile, validateMessageEquality } from '../integration-test.utilities';
import { FileReadError } from '../../../errors/errors';
import expect from 'expect';

describe('integration-test-utilities.js', () => {
  describe('readMessage', () => {
    it('should read a file successfully if the file name and folder path is correct', () => {
      // given
      const ehrCoreFileName = 'COPC_IN000001UK01_01';
      const folders = ['equality-test', 'large-ehr-with-external-attachments', 'modified'];

      // when
      const result = readFile(ehrCoreFileName, ...folders);

      // then
      expect(result).not.toBe(undefined);
    });

    it('should throw a FileReadError when the file name is incorrect', () => {
      // given
      const nonExistentFileName = 'RCMR_IN030000UK10';
      const folders = ['equality-test', 'large-ehr', 'original'];

      // then
      expect(() => readFile(nonExistentFileName, ...folders)).toThrow(FileReadError);
    });

    it('should throw a FileReadError when a folder name is incorrect', () => {
      // given
      const ehrCoreFileName = 'RCMR_IN030000UK06';
      const folders = ['equality-test', 'large-ehr', 'original', 'unknown'];

      // then
      expect(() => readFile(ehrCoreFileName, ...folders)).toThrow(FileReadError);
    });
  });

  describe('validateMessageEquality for large electronic health records without external attachments', () => {
    it('should be true when a large ehr EHR_CORE has no unexpected changes', () => {
      // given
      const message = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'large-ehr-no-external-attachments',
        'original'
      );

      // when
      const result = validateMessageEquality(message, message);

      // then
      expect(result).toBe(true);
    });

    it('should be false when a large ehr EHR_CORE has unexpected changes', () => {
      // given
      const originalMessage = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'large-ehr-no-external-attachments',
        'original'
      );
      const modifiedMessage = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'large-ehr-no-external-attachments',
        'modified'
      );

      // when
      const result = validateMessageEquality(originalMessage, modifiedMessage);

      // then
      expect(result).toBe(false);
    });

    it('should be true when a fragment CONTINUE has no unexpected changes', () => {
      // given
      const message = readFile(
        'COPC_IN000001UK01_01',
        'equality-test',
        'large-ehr-no-external-attachments',
        'original'
      );

      // when
      const result = validateMessageEquality(message, message);

      // then
      expect(result).toBe(true);
    });
  });

  describe('validateMessageEquality for large electronic health records with external attachments', () => {
    it('should be true when a large ehr EHR_CORE with external attachments has no unexpected changes', () => {
      // given
      const message = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'large-ehr-with-external-attachments',
        'original'
      );

      // when
      const result = validateMessageEquality(message, message);

      // then
      expect(result).toBe(true);
    });

    it('should be false when a large ehr EHR_CORE with external attachments has unexpected changes', () => {
      // given
      const originalMessage = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'large-ehr-with-external-attachments',
        'original'
      );
      const modifiedMessage = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'large-ehr-with-external-attachments',
        'modified'
      );

      // when
      const result = validateMessageEquality(originalMessage, modifiedMessage);

      // then
      expect(result).toBe(false);
    });

    it('should be true when a fragment CONTINUE with an empty array of external attachments has no unexpected changes', () => {
      // given
      const message = readFile(
        'COPC_IN000001UK01_01',
        'equality-test',
        'large-ehr-with-external-attachments',
        'original'
      );

      // when
      const result = validateMessageEquality(message, message);

      // then
      expect(result).toBe(true);
    });

    it('should be false when a fragment CONTINUE with an empty array of external attachments has unexpected changes', () => {
      // given
      const originalMessage = readFile(
        'COPC_IN000001UK01_04',
        'equality-test',
        'large-ehr-with-external-attachments',
        'original'
      );
      const modifiedMessage = readFile(
        'COPC_IN000001UK01_04',
        'equality-test',
        'large-ehr-with-external-attachments',
        'modified'
      );

      // when
      const result = validateMessageEquality(originalMessage, modifiedMessage);

      // then
      expect(result).toBe(false);
    });

    it('should be true when a fragment CONTINUE with external attachments has no unexpected changes', () => {
      // given
      const message = readFile(
        'COPC_IN000001UK01_03',
        'equality-test',
        'large-ehr-with-external-attachments',
        'original'
      );

      // when
      const result = validateMessageEquality(message, message);

      // then
      expect(result).toBe(true);
    });

    it('should be false when a fragment CONTINUE with external attachments has unexpected changes', () => {
      // given
      const originalMessage = readFile(
        'COPC_IN000001UK01_01',
        'equality-test',
        'large-ehr-with-external-attachments',
        'original'
      );
      const modifiedMessage = readFile(
        'COPC_IN000001UK01_01',
        'equality-test',
        'large-ehr-with-external-attachments',
        'modified'
      );

      // when
      const result = validateMessageEquality(originalMessage, modifiedMessage);

      // then
      expect(result).toBe(false);
    });
  });

  describe('validateMessageEquality for small electronic health records', () => {
    it('should be true when a small electronic health record EHR_CORE has no unexpected changes', () => {
      // given
      const message = readFile('RCMR_IN030000UK06', 'equality-test', 'small-ehr', 'original');

      // when
      const result = validateMessageEquality(message, message);

      // then
      expect(result).toBe(true);
    });

    it('should be false when a small electronic health record EHR_CORE has unexpected changes', () => {
      // given
      const originalMessage = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'small-ehr',
        'original'
      );
      const modifiedMessage = readFile(
        'RCMR_IN030000UK06',
        'equality-test',
        'small-ehr',
        'modified'
      );

      // when
      const result = validateMessageEquality(originalMessage, modifiedMessage);

      // then
      expect(result).toBe(false);
    });
  });
});
