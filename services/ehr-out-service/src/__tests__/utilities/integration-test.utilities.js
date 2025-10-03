import { logError, logInfo } from '../../middleware/logging';
import { FileReadError } from '../../errors/errors';
import { XMLParser } from 'fast-xml-parser';
import isEqual from 'lodash.isequal';
import { readFileSync } from 'fs';
import * as path from 'path';

const INTERACTION_IDS = {
  EHR_CORE: 'RCMR_IN030000UK06',
  CONTINUE: 'COPC_IN000001UK01'
};

const parser = new XMLParser({
  ignoreAttributes: false,
  removeNSPrefix: true,
  ignoreDeclaration: true
});

export const readFile = (fileName, ...folderNames) => {
  try {
    return readFileSync(path.join(__dirname, '..', 'data', ...folderNames, fileName), 'utf-8');
  } catch (error) {
    throw new FileReadError(error);
  }
};

export const validateMessageEquality = (original, modified) => {
  const conditions = [
    validateEbXmlEquality(original, modified),
    validatePayloadEquality(original, modified),
    validateAttachmentEquality(original, modified),
    validateExternalAttachmentEquality(original, modified)
  ];

  return conditions.every(test => test === true);
};

const validateEbXmlEquality = (original, modified) => {
  const ebXMLs = {
    original: parser.parse(JSON.parse(original).ebXML),
    modified: parser.parse(JSON.parse(modified).ebXML)
  };

  // Check original vs modified ebXML
  for (const key in ebXMLs) {
    if (Object.hasOwn(ebXMLs, key)) {
      // Delete the Message ID attribute within MessageData -> MessageId
      delete ebXMLs[key]['Envelope']['Header']['MessageHeader']['MessageData']['MessageId'];

      // Delete inner manifest Message ID (i.e. mid) references
      if (Array.isArray(ebXMLs[key]['Envelope']['Body']['Manifest']['Reference'])) {
        ebXMLs[key]['Envelope']['Body']['Manifest']['Reference'].forEach(reference => {
          if (reference['@_href'].includes('mid')) {
            delete reference['@_href'];
          }
        });
      }
    }
  }

  return isEqual(ebXMLs.original, ebXMLs.modified);
};

const validatePayloadEquality = (original, modified) => {
  const payloads = {
    original: parser.parse(JSON.parse(original).payload),
    modified: parser.parse(JSON.parse(modified).payload)
  };

  for (const key in payloads) {
    if (Object.hasOwn(payloads, key)) {
      if (
        original.includes(INTERACTION_IDS.EHR_CORE) &&
        modified.includes(INTERACTION_IDS.EHR_CORE)
      ) {
        // Remove Message ID references within the EHR_CORE message
        delete payloads[key][INTERACTION_IDS.EHR_CORE]['id']['@_root'];
        delete payloads[key][INTERACTION_IDS.EHR_CORE]['ControlActEvent']['subject']['EhrExtract'][
          'id'
        ]['@_root'];
      } else if (
        original.includes(INTERACTION_IDS.CONTINUE) &&
        modified.includes(INTERACTION_IDS.CONTINUE)
      ) {
        // Remove Message ID references within the CONTINUE message
        delete payloads[key][INTERACTION_IDS.CONTINUE]['id']['@_root'];
        delete payloads[key][INTERACTION_IDS.CONTINUE]['ControlActEvent']['subject'][
          'PayloadInformation'
        ]['id']['@_root'];
        delete payloads[key][INTERACTION_IDS.CONTINUE]['ControlActEvent']['subject'][
          'PayloadInformation'
        ]['value']['Gp2gpfragment']['message-id'];
        delete payloads[key][INTERACTION_IDS.CONTINUE]['ControlActEvent']['subject'][
          'PayloadInformation'
        ]['pertinentInformation']['pertinentPayloadBody']['id']['@_root'];
      } else {
        logError('Unrecognised Interaction ID');
        return false;
      }
    }
  }

  return isEqual(payloads.original, payloads.modified);
};

const validateAttachmentEquality = (original, modified) => {
  const attachments = {
    original: JSON.parse(original).attachments,
    modified: JSON.parse(modified).attachments
  };

  return isEqual(attachments.original, attachments.modified);
};

const validateExternalAttachmentEquality = (original, modified) => {
  if (original.includes('external_attachments') || modified.includes('external_attachments')) {
    const externalAttachments = {
      original: JSON.parse(original).external_attachments,
      modified: JSON.parse(modified).external_attachments
    };

    if (externalAttachments.original.length === 0 && externalAttachments.modified.length === 0) {
      logInfo('No external attachments found in original and modified, skipping.');
      return true;
    }

    for (const key in externalAttachments) {
      if (Object.hasOwn(externalAttachments, key)) {
        for (const externalAttachment in externalAttachments[key]) {
          delete externalAttachments[key][externalAttachment]['message_id'];
        }
      }
    }

    return isEqual(externalAttachments.original, externalAttachments.modified);
  }

  return true;
};
