import { config } from '../../config';
import { EhrTransferTracker } from '../database/dynamodb/dynamo-ehr-transfer-tracker';

export const getHealthCheck = async () => {
  const { nhsEnvironment } = config();
  const db = EhrTransferTracker.getInstance();

  return {
    version: '1',
    description: 'Health of ehr-out-service',
    nhsEnvironment: nhsEnvironment,
    details: {
      database: {
        type: 'dynamodb',
        status: `is tableName configured: ${db.tableName !== undefined}`
      }
    }
  };
};
