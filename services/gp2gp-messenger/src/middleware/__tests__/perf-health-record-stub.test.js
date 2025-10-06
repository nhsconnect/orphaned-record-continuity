import { perfTestStep } from '../perf-health-record-stub';

jest.mock('../logging');

describe('perf health record stub', () => {
  afterEach(() => {
    delete process.env.NHS_ENVIRONMENT;
  });

  it('should return status 204 when we are in perf environment', async () => {
    const mockRes = { status: jest.fn(() => ({ send: jest.fn() })) };
    const mockedNext = jest.fn();
    process.env.NHS_ENVIRONMENT = 'perf';
    perfTestStep({}, mockRes, mockedNext);

    expect(mockRes.status).toHaveBeenLastCalledWith(204);
    expect(mockedNext).not.toHaveBeenCalled();
  });

  it('should call next middleware when environment is not perf', async () => {
    const mockRes = { status: jest.fn() };
    const mockedNext = jest.fn();
    process.env.NHS_ENVIRONMENT = 'local';

    perfTestStep({}, mockRes, mockedNext);

    expect(mockRes.status).not.toHaveBeenCalled();
    expect(mockedNext).toHaveBeenCalled();
  });
});
