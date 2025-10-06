import { createLogger, format, transports } from 'winston';
import traverse from 'traverse';
import cloneDeep from 'lodash.clonedeep';
import { initializeConfig } from './index';
import { getCurrentSpanAttributes } from './tracing';

export const obfuscateSecrets = format(info => {
  const OBFUSCATED_VALUE = '********';
  const SECRET_KEYS = ['passcode', 'data', 'authorization', 'Authorization', 'apiKey'];
  const updated = cloneDeep(info);
  traverse(updated).forEach(function () {
    if (SECRET_KEYS.includes(this.key)) this.update(OBFUSCATED_VALUE);
  });
  return updated;
});

export const addCommonFields = format(info => {
  const { nhsEnvironment } = initializeConfig();
  const updated = cloneDeep(info);
  let attributes = getCurrentSpanAttributes();
  if (attributes) {
    updated['traceId'] = attributes.traceId;
    updated['conversationId'] = attributes.conversationId;
    updated['messageId'] = attributes.messageId;
  }

  updated.level = updated.level.toUpperCase();
  updated['service'] = 'gp2gp-messenger';
  updated['environment'] = nhsEnvironment;
  return updated;
});

export const options = {
  format: format.combine(
    format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    obfuscateSecrets(),
    addCommonFields(),
    format.json()
  ),
  level: process.env.LOG_LEVEL,
  transports: [new transports.Console({ handleExceptions: true })]
};

export const logger = createLogger(options);
