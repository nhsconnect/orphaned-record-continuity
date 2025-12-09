import { initializeConfig } from '../../../config';
import {
  xmlStringToJsObject,
  jsObjectToXmlString,
  updateIdExtension
} from './utilities/message-utilities';

export const updateExtractForSending = async (
  ehrExtract,
  ehrRequestId,
  receivingAsid,
  sendingOdsCode,
  destinationOdsCode
) => {
  const config = initializeConfig();
  const parsedEhr = await xmlStringToJsObject(ehrExtract);

  const sendingAsid = config.repoAsid;

  const rcmrUK06 = parsedEhr.RCMR_IN030000UK06;
  const controlActEvent = rcmrUK06.ControlActEvent;

  updateIdExtension(rcmrUK06.communicationFunctionRcv.device, receivingAsid);
  updateIdExtension(rcmrUK06.communicationFunctionSnd.device, sendingAsid);
  updateIdExtension(controlActEvent.author1.AgentSystemSDS.agentSystemSDS, sendingAsid);
  controlActEvent.subject.EhrExtract.inFulfillmentOf.priorEhrRequest.id['@_root'] = ehrRequestId;

  updateAuthorOdsCode(controlActEvent.subject.EhrExtract, sendingOdsCode);
  updateAuthorOdsCode(controlActEvent.subject.EhrExtract.component.ehrFolder, sendingOdsCode);

  updateIdExtension(
    controlActEvent.subject.EhrExtract.destination.AgentOrgSDS.agentOrganizationSDS,
    destinationOdsCode
  );

  return jsObjectToXmlString(parsedEhr);
};

const updateAuthorOdsCode = (authorParent, sendingOdsCode) => {
  const field = authorParent.author.AgentOrgSDS.agentOrganizationSDS;
  updateIdExtension(field, sendingOdsCode);
};
