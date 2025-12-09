class MhsError extends Error {
  constructor(message) {
    super(message);

    Error.captureStackTrace(this, MhsError);
  }
}

export default MhsError;
