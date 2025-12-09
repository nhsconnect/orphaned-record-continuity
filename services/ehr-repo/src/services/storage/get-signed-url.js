import S3Service from './s3';
import { logInfo } from '../../middleware/logging';

const getSignedUrl = (conversationId, messageId, operation) => {
  logInfo('Retrieving pre-signed URL');
  conversationId = conversationId.toLowerCase();
  messageId = messageId.toLowerCase();
  const fileName = `${conversationId}/${messageId}`.toLowerCase();
  return new S3Service().getPresignedUrlWithFilename(fileName, operation);
};

export default getSignedUrl;
