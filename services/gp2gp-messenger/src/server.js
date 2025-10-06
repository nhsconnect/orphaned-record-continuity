import app from './app';
import { logInfo } from './middleware/logging';

app.listen(3000, () => {
  logInfo('Listening on port 3000');
  logInfo(`Running with nodejs version: ${process.versions.node}`);
});
