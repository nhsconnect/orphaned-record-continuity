import express from 'express';

export const buildTestApp = (endpoint, ...handlers) => {
  const testApp = express();
  testApp.use(express.json());
  testApp.use(endpoint, ...handlers);

  return testApp;
};
