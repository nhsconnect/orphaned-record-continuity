// this is gonna take ebXML parameter, thanks lint
import { XmlParser } from '../parser/xml-parser/xml-parser';
import { logInfo } from '../../middleware/logging';

function findAttachmentReferenceByHref(attachmentReferences, content_id) {
  return attachmentReferences.find(a => a.href == content_id);
}

export const wrangleAttachments = async mhsJsonMessage => {
  if (mhsJsonMessage.attachments === undefined) {
    logInfo('No MHS json attachments []');
    return {};
  }

  if (mhsJsonMessage.ebXML === undefined) {
    logInfo('No MHS json ebXML');
    return {};
  }

  logInfo('Got MHS json with ebXML and attachments[] length: ' + mhsJsonMessage.attachments.length);

  const references = await extractManifestReferences(mhsJsonMessage);

  if (references == null) {
    logInfo('No references');
    return {};
  }

  const outboundAttachments = mhsJsonMessage.attachments.map(a => {
    let attachmentReference = findAttachmentReferenceByHref(references, 'cid:' + a.content_id);

    const description = extractDescription(attachmentReference);

    const document_id = extractDocumentId(attachmentReference);

    return composeOutboundAttachment(description, document_id, a);
  });

  const MID_PREFIX = 'mid:';
  const outboundExternalAttachments = references
    .filter(ref => ref.href.startsWith(MID_PREFIX))
    .map(ref => {
      return {
        description: ref.Description.innerText,
        document_id: ref.id,
        message_id: ref.href.substring(MID_PREFIX.length)
      };
    });

  const attachmentsInfo = {
    attachments: outboundAttachments,
    external_attachments: outboundExternalAttachments
  };

  logInfo('Attachments wrangled: ' + attachmentsInfo.attachments.length);
  logInfo('External attachments wrangled: ' + attachmentsInfo.external_attachments.length);
  return attachmentsInfo;
};

function composeOutboundAttachment(description, document_id, attachment) {
  const outboundAttachment = { ...attachment };

  delete outboundAttachment.content_id;

  outboundAttachment.description = description;
  outboundAttachment.document_id = document_id;

  if (!attachment.is_base64) {
    logInfo('Encoding / forcing to base64');
    outboundAttachment.is_base64 = true;
    outboundAttachment.payload = base64Encoded(attachment.payload);
  }
  return outboundAttachment;
}

async function extractManifestReferences(mhsJsonMessage) {
  return await new XmlParser()
    .parse(mhsJsonMessage.ebXML)
    .then(jsObject => jsObject.findAll('Reference'))
    .then(references => {
      logInfo('Total number of references including hl7 core: ' + references.length);
      if (references.length < 2) {
        logInfo('No attachments to wrangle');
        return null;
      }
      return references;
    });
}

function extractDescription(attachmentReference) {
  const description = attachmentReference.Description.innerText;
  return description;
}

function extractDocumentId(attachmentReference) {
  const eb_id = attachmentReference.id;
  const document_id = eb_id.substring(1);
  return document_id;
}

function base64Encoded(raw) {
  let buff = new Buffer(raw);
  return buff.toString('base64');
}

export function removeTitleFromExternalAttachments(externalAttachments) {
  return externalAttachments.map(externalAttachment => {
    delete externalAttachment.title;
    return externalAttachment;
  });
}
