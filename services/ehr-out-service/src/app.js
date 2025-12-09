import express from 'express';
import swaggerUi from 'swagger-ui-express';

import { middleware } from './middleware/logging';
import { registrationRequests } from './api/registration-request';
import swaggerDocument from './swagger.json';

const app = express();
app.use(express.json());

app.use('/swagger', swaggerUi.serve, swaggerUi.setup(swaggerDocument));
app.use('/registration-requests', middleware, registrationRequests);

// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  res.status(500).json({ error: err.message });
});

export default app;
