import { PresignedUrlNotFoundError, PatientRecordNotFoundError } from '../../errors/errors';
import { downloadFromUrl } from '../transfer/transfer-out-util';
import { logInfo, logError } from '../../middleware/logging';
import { config } from '../../config';
import axios from 'axios';
import { AcknowledgementErrorCode } from '../../constants/enums';

export const getFragmentConversationAndMessageIdsFromEhrRepo = async nhsNumber => {
  const { ehrRepoServiceUrl, ehrRepoAuthKeys } = config();
  const repoUrl = `${ehrRepoServiceUrl}/patients/${nhsNumber}`;

  return await axios
    .get(repoUrl, {
      headers: { Authorization: ehrRepoAuthKeys }
    })
    .then(extractIdsFromEhrRepoResponse)
    .catch(handleErrorWhileRetrievingIds);
};

const extractIdsFromEhrRepoResponse = response => {
  return {
    inboundConversationId: response.data?.inboundConversationId,
    messageIds: response.data?.fragmentMessageIds
  };
};

const handleErrorWhileRetrievingIds = error => {
  if (error?.response?.status === 404) {
    throw new PatientRecordNotFoundError(); // TODO THIS SHOULD BE A DIFFERENT, POSSIBLY NEW ERROR
  } else {
    logError('Failed to retrieve inboundConversationId from ehr-repo');
    throw error;
  }
};

export const getFragment = async (inboundConversationId, messageId) => {
  const fragmentMessageUrl = await retrieveFragmentPresignedUrlFromRepo(
    inboundConversationId,
    messageId
  );
  logInfo(`Successfully retrieved fragment presigned url with messageId: ${messageId}`);
  const fragment = await downloadFromUrl(fragmentMessageUrl);
  logInfo(`Successfully retrieved fragment with messageId: ${messageId}`);

  return fragment;
};

const retrieveFragmentPresignedUrlFromRepo = async (inboundConversationId, messageId) => {
  const { ehrRepoServiceUrl, ehrRepoAuthKeys } = config();
  const repoUrl = `${ehrRepoServiceUrl}/fragments/${inboundConversationId}/${messageId}`;

  return await axios
    .get(repoUrl, {
      headers: { Authorization: ehrRepoAuthKeys }
    })
    .then(response => response.data)
    .catch(error => {
      throw new PresignedUrlNotFoundError(error, AcknowledgementErrorCode.ERROR_CODE_10_B);
    });
};
