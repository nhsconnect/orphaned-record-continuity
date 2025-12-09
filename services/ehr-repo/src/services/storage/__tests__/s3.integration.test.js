import { generateMultipleUUID } from '../../../utilities/integration-test-utilities';
import getSignedUrl from '../get-signed-url';
import { v4 as uuidv4 } from 'uuid';
import S3Service from '../s3';
import axios from 'axios';

jest.mock('../../../middleware/logging');

describe('S3Service integration test with localstack', () => {
  const S3CLIENT = new S3Service();

  // ===== HELPER METHODS =====
  const getObjectByName = (filename) => {
    const getObjectParams = {
      Bucket: S3CLIENT.Bucket,
      Key: filename
    };
    return S3CLIENT.s3.getObject(getObjectParams).promise();
  };

  // ===== TESTS START FROM HERE =====
  describe('saveObjectWithName', () => {
    it('can save an object in the S3 bucket', async () => {
      // given
      const testFileName = uuidv4() + '/' + uuidv4();
      const testData = 'Lorem ipsum dolor sit amet';

      // when
      await S3CLIENT.saveObjectWithName(testFileName, testData);

      // then
      const objectInBucket = await getObjectByName(testFileName);
      const objectContent = objectInBucket.Body.toString();

      expect(objectContent).toEqual(testData);
    });
  });

  describe('getSignedUrl', () => {
    const testEhrCore = {
      ebXML: '<soap:Envelope><content>ebXML</content></soap:Envelope>',
      payload: '<RCMR_IN030000UK06>payload</<RCMR_IN030000UK06>',
      attachments: [],
      external_attachments: []
    };

    it('return a presigned url for upload when operation = putObject', async () => {
      // make a presigned url for putObject, upload an object with that url, then verify that the object is in bucket
      // given
      const [conversationId, messageId] = generateMultipleUUID(2);
      const testFileName = `${conversationId.toLowerCase()}/${messageId.toLowerCase()}`;
      const operation = 'putObject';

      // when
      const presignedUrl = await getSignedUrl(conversationId, messageId, operation);
      const response = await axios.put(presignedUrl, testEhrCore);

      // then
      expect(response.status).toBe(200);

      const objectInBucket = await getObjectByName(testFileName);
      const objectContent = objectInBucket.Body.toString();

      expect(objectContent).toEqual(JSON.stringify(testEhrCore));
    });

    it('return a presigned url for download when operation = getObject', async () => {
      // store an object to the S3 bucket, then download it with presigned url
      // given
      const [conversationId, messageId] = generateMultipleUUID(2);
      const testFileName = `${conversationId.toLowerCase()}/${messageId.toLowerCase()}`;
      const operation = 'getObject';

      await S3CLIENT.saveObjectWithName(testFileName, JSON.stringify(testEhrCore));

      // when
      const presignedUrl = await getSignedUrl(conversationId, messageId, operation);
      const response = await axios.get(presignedUrl);

      // then
      expect(response.status).toBe(200);

      // verify that the file we download from presignedUrl is same as the file we stored in bucket
      expect(response.data).toEqual(testEhrCore);
    });
  });
});
