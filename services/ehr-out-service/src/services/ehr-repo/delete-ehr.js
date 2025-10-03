import { PresignedUrlNotFoundWhileDeletingEhrError } from '../../errors/errors';
import { logError } from '../../middleware/logging';
import { config } from '../../config';
import axios from 'axios';

export const sendDeleteRequestToEhrRepo = async (nhsNumber, conversationId) => {
  const { ehrRepoAuthKeys, ehrRepoServiceUrl } = config();
  const repoUrl = `${ehrRepoServiceUrl}/patients/${nhsNumber}`;

  return await axios
    .delete(repoUrl, {
      headers: { Authorization: ehrRepoAuthKeys, conversationId }
    })
    .then(response => {
      return {
        data: {
          type: response.data.data.type,
          id: response.data.data.id,
          conversationIds: response.data.data.conversationIds
        }
      };
    })
    .catch(error => {
      if (error?.response?.status === 404) {
        throw new PresignedUrlNotFoundWhileDeletingEhrError(error);
      } else {
        logError('Error deleting health record', error);
        throw error;
      }
    });
};
