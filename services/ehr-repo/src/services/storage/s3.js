import { initializeConfig } from '../../config';
import { Endpoint, S3 } from 'aws-sdk';

const URL_EXPIRY_TIME = 60;
const CONTENT_TYPE = 'text/xml';
const config = initializeConfig();

export default class S3Service {
  constructor() {
    this.s3 = new S3(this._get_config());
    this.Bucket = config.awsS3BucketName;
  }

  saveObjectWithName(filename, data) {
    const params = {
      Bucket: config.awsS3BucketName,
      Key: filename,
      Body: data
    };
    return this.s3.putObject(params).promise();
  }

  getPresignedUrlWithFilename(filename, operation) {
    const params = {
      Bucket: this.Bucket,
      Key: filename,
      Expires: URL_EXPIRY_TIME
    };

    if (operation === 'putObject') {
      params.ContentType = CONTENT_TYPE;
    }

    return this.s3.getSignedUrlPromise(operation, params);
  }

  _get_config() {
    if (config.nhsEnvironment === 'local') {
      return {
        accessKeyId: 'test-access-key',
        secretAccessKey: 'test-secret-key',
        endpoint: new Endpoint(config.localstackUrl),
        s3ForcePathStyle: true
      };
    }

    return {};
  }
}
