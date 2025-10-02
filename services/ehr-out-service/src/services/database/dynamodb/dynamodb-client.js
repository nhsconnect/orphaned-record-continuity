import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { DynamoDBDocumentClient } from '@aws-sdk/lib-dynamodb';
import { IS_IN_LOCAL } from '../../../utilities/integration-test-utilities';

export const getDynamodbClient = () => {
  const clientConfig = {
    region: process.env.AWS_DEFAULT_REGION ?? 'eu-west-2'
  };

  if (IS_IN_LOCAL) {
    // for running integration test in dojo or IDE
    clientConfig.endpoint = process.env.DYNAMODB_LOCAL_ENDPOINT ?? 'http://localhost:4573';
  }

  const baseClient = new DynamoDBClient(clientConfig);
  return DynamoDBDocumentClient.from(baseClient);
};
