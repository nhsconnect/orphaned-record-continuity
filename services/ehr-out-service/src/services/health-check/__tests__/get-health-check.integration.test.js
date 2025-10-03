import { getHealthCheck } from '../get-health-check';
import { config } from '../../../config';

describe('getHealthCheck', () => {
  const { nhsEnvironment } = config();

  it('should return static health check object', async () => {
    const expected = {
      version: '1',
      description: 'Health of ehr-out-service',
      nhsEnvironment: nhsEnvironment,
      details: {
        database: {
          type: 'dynamodb'
        }
      }
    };

    const actual = await getHealthCheck();

    expect(actual).toMatchObject(expected);
  });
});
