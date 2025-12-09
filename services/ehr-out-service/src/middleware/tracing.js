import { setCurrentSpanAttributes } from '../config/tracing';
import { v4 as uuid } from 'uuid';

export const middleware = (req, res, next) => {
  let traceId = req.headers.traceid;
  if (!traceId) {
    traceId = uuid();
  }
  setCurrentSpanAttributes({ traceId });
  next();
};
