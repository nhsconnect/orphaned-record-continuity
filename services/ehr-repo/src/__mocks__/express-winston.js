const errorLogger = jest.fn().mockImplementation(() => (req, res, next) => next());
const logger = jest.fn().mockImplementation(() => (req, res, next) => next());

export { errorLogger, logger };
