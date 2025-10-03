import cloneDeep from 'lodash.clonedeep';
import { LOG_SIZE_LIMIT, checkSizeAndLogMessage, removeBase64Payloads } from '../logging-utils';
import { logInfo } from '../../../middleware/logging';
import { loadTestData } from './test-utils';

jest.mock('../../../middleware/logging');

describe('logging-utils', () => {
  describe('removeBase64Payloads', () => {
    it('should take a javascript object and redact any base64 encoded contents', () => {
      // given
      const inputObject = loadTestData('inputMessageContainingBase64.json');
      const inputObjectClone = cloneDeep(inputObject);

      const expectedOutputObject = loadTestData('expectedOutputWithBase64PayloadsRemoved.json');

      // when
      const output = removeBase64Payloads(inputObject);

      // then
      expect(output).toEqual(expectedOutputObject);

      // verify that input object is not mutated in the process
      expect(inputObject).toEqual(inputObjectClone);
    });

    it('should leave the input unchanged if it is not a JS object or an array', () => {
      // given
      const testInputs = [1234, 'a test string', true];

      testInputs.forEach(input => {
        const output = removeBase64Payloads(input);
        expect(output).toBe(input);
      });
    });
  });

  describe('checkSizeAndLogMessage', () => {
    it('should log a message of < 256 KB normally', () => {
      // given
      const inputMessage = {
        payload: '<COPC_IN000001UK01><content></content></COPC_IN000001UK01>',
        attachments: [
          {
            payload: '[REMOVED]',
            is_base64: true,
            content_id: 'Attachment1@e-mis.com/EMISWeb/GP2GP2.2A',
            content_type: 'text/plain'
          }
        ],
        external_attachments: [
          {
            document_id: '_D695F543-7F04-4B27-81DC-AA238831DEA9',
            message_id: '55146806-4E3B-4EFB-92BF-66556846FF2F',
            description:
              'Filename="D695F543-7F04-4B27-81DC-AA238831DEA9_IMG_01.jpg" ContentType=image/jpeg Compressed=No LargeAttachment=Yes OriginalBase64=No Length=6038724',
            title: '"D695F543-7F04-4B27-81DC-AA238831DEA9_IMG_01.jpg"'
          }
        ]
      };

      // when
      checkSizeAndLogMessage(inputMessage);

      // then
      expect(logInfo).toHaveBeenCalledWith(inputMessage);
    });

    // prettier-ignore
    it('should log a message of > 256 KB in multiple parts', () => {
      // given
      const veryLongString = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.'
        .repeat(25000);
      const inputMessage = {
        payload: `<COPC_IN000001UK01><content>${veryLongString}</content></COPC_IN000001UK01>`,
        attachments: [
          {
            payload: 'some_non_base64_payload',
            is_base64: false,
            content_id: 'Attachment1@e-mis.com/EMISWeb/GP2GP2.2A',
            content_type: 'text/plain'
          }
        ],
        external_attachments: [
          {
            document_id: '_D695F543-7F04-4B27-81DC-AA238831DEA9',
            message_id: '55146806-4E3B-4EFB-92BF-66556846FF2F',
            description:
              'Filename="D695F543-7F04-4B27-81DC-AA238831DEA9_IMG_01.jpg" ContentType=image/jpeg Compressed=No LargeAttachment=Yes OriginalBase64=No Length=6038724',
            title: '"D695F543-7F04-4B27-81DC-AA238831DEA9_IMG_01.jpg"'
          }
        ]
      };

      // when
      checkSizeAndLogMessage(inputMessage);

      // then
      const expectedNumberOfParts = Math.ceil(veryLongString.length / LOG_SIZE_LIMIT);
      expect(logInfo).toHaveBeenCalledTimes(expectedNumberOfParts + 2); // including every part and the start and end logs

      const loggedMessageParts = logInfo.mock.calls.slice(1, -1).map(array => array[0]);
      expect(loggedMessageParts[0]).toContain(`"payload":"<COPC_IN000001UK01><content>Lorem ipsum`);

      // verify that we can get back the original message by combining every part of the logs
      const combinedParts = loggedMessageParts
        .map(str => str.replace(/Part \d+ of \d+: /, ''))
        .join('');
      const recoveredMessage = JSON.parse(combinedParts);
      expect(recoveredMessage).toEqual(inputMessage);
    });
  });
});
