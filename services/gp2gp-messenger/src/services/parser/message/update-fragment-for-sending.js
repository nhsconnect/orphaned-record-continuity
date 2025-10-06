import { initializeConfig } from '../../../config';
import {
  xmlStringToJsObject,
  jsObjectToXmlString,
  updateIdExtension,
  updateIdRoot
} from './utilities/message-utilities';

export async function updateFragmentForSending(
  fragmentPayload,
  messageId,
  recipientAsid,
  recipientOdsCode
) {
  const config = initializeConfig();
  const sendingAsid = config.repoAsid;
  const sendingOdsCode = config.repoOdsCode;

  const payloadXml = xmlStringToJsObject(fragmentPayload);

  const copcUK01 = payloadXml.COPC_IN000001UK01;
  updateIdExtension(copcUK01.communicationFunctionRcv.device, recipientAsid);
  updateIdExtension(copcUK01.communicationFunctionSnd.device, sendingAsid);
  updateIdRoot(copcUK01, messageId);

  updateIdExtension(copcUK01.ControlActEvent.author1.AgentSystemSDS.agentSystemSDS, recipientAsid);

  let payloadInformation = copcUK01.ControlActEvent.subject.PayloadInformation;
  updateIdRoot(payloadInformation, messageId);
  updateIdRoot(payloadInformation.pertinentInformation.pertinentPayloadBody, messageId);

  let gp2gpfragment = payloadInformation.value.Gp2gpfragment;
  gp2gpfragment['message-id'] = messageId;
  gp2gpfragment.From = sendingOdsCode;
  gp2gpfragment.Recipients.Recipient = recipientOdsCode;

  return jsObjectToXmlString(payloadXml);
}
