import { handlePdsResponse } from '../pds-response-handler';
import {
  PDSRetrievalQueryResponse,
  PDS_RETRIEVAL_QUERY_RESPONSE
} from '../pds-retrieval-query-response';

jest.mock('../pds-response-handler');

describe('PDSRetrievalQueryResponse', () => {
  it('should return "PDS Retrieval Query Response" when calling name', () => {
    expect(new PDSRetrievalQueryResponse().name).toBe('PDS Retrieval Query Response');
  });

  it('should return PDS_RETRIEVAL_QUERY_RESPONSE when calling interactionId', () => {
    expect(new PDSRetrievalQueryResponse().interactionId).toBe(PDS_RETRIEVAL_QUERY_RESPONSE);
  });

  it('should call handlePdsResponse with message', async () => {
    const message = 'message';
    await new PDSRetrievalQueryResponse().handleMessage(message);
    expect(handlePdsResponse).toHaveBeenCalledTimes(1);
    expect(handlePdsResponse).toHaveBeenCalledWith(message);
  });
});
