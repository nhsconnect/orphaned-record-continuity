import { logInfo } from './middleware/logging';
import { portNumber } from './config';
import app from './app';

const port = portNumber || 3000;

app.listen(port, () => {
  logInfo(`Listening on port ${port}`);
  logInfo(`Running with nodejs version: ${process.versions.node}`);
});
