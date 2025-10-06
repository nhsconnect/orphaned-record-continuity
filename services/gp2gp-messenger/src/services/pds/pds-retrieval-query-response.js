import { handlePdsResponse } from './';
import { logInfo } from '../../middleware/logging';

const PDS_RETRIEVAL_QUERY_RESPONSE = 'QUPA_IN000009UK03';

class PDSRetrievalQueryResponse {
  constructor() {
    this.name = 'PDS Retrieval Query Response';
    this.interactionId = PDS_RETRIEVAL_QUERY_RESPONSE;
  }

  handleMessage(message) {
    logInfo('Handling PDS Retrieval Query Response');
    return handlePdsResponse(message);
  }
}

export { PDSRetrievalQueryResponse, PDS_RETRIEVAL_QUERY_RESPONSE };
