import dateFormat from 'dateformat';
import { initializeConfig } from '../config';
import checkTemplateArguments from './utils/check_params';

export const generateContinueRequest = ({
  messageId,
  receivingAsid,
  sendingAsid,
  ehrExtractMessageId,
  gpOdsCode
}) => {
  const { repoOdsCode } = initializeConfig();
  const timestamp = dateFormat(Date.now(), 'yyyymmddHHMMss');
  const inputObject = {
    id: messageId,
    timestamp,
    receivingAsid,
    sendingAsid,
    ehrExtractMessageId,
    gpOdsCode,
    repoOdsCode: repoOdsCode
  };
  checkTemplateArguments(inputObject);
  return continueRequest(inputObject);
};

const continueRequest = ({
  id,
  timestamp,
  receivingAsid,
  sendingAsid,
  ehrExtractMessageId,
  gpOdsCode,
  repoOdsCode
}) =>
  `<COPC_IN000001UK01 xmlns="urn:hl7-org:v3">
  <id root="${id}"/>
  <creationTime value="${timestamp}"/>
  <versionCode code="V3NPfIT3.0"/>
  <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="COPC_IN000001UK01"/>
  <processingCode code="P"/>
  <processingModeCode code="T"/>
  <acceptAckCode code="NE"/>
  <communicationFunctionRcv>
    <device>
      <id root="1.2.826.0.1285.0.2.0.107" extension="${receivingAsid}"/>
    </device>
  </communicationFunctionRcv>
  <communicationFunctionSnd>
    <device>
      <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}"/>
    </device>
  </communicationFunctionSnd>
  <ControlActEvent classCode="CACT" moodCode="EVN">
    <author1>
      <AgentSystemSDS>
        <agentSystemSDS>
          <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}"/>
        </agentSystemSDS>
      </AgentSystemSDS>
    </author1>
    <subject typeCode="SUBJ" contextConductionInd="false">
      <PayloadInformation xmlns:npfitlc="NPFIT:HL7:Localisation" classCode="OBS" moodCode="EVN">
        <code code="GP2GP_PI" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.202" displayName="GP2GP Payload Information"/>
        <id root="${id}"/>
        <npfitlc:messageType root="2.16.840.1.113883.2.1.3.2.4.18.17" extension="RCMR_MT000002GB01"/>
        <value>
          <Gp2gpfragment xmlns="urn:nhs:names:services:gp2gp">
            <Version>01</Version>
            <Recipients>
              <Recipient>${gpOdsCode.toUpperCase()}</Recipient>
            </Recipients>
            <From>${repoOdsCode}</From>
            <subject>Continue Acknowledgement</subject>
            <message-id>${id}</message-id>
          </Gp2gpfragment>
        </value>
        <pertinentInformation typeCode="PERT">
          <sequenceNumber value="1"/>
          <pertinentPayloadBody moodCode="EVN" classCode="OBS">
            <code code="GP2GP_PB" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.202" displayName="GP2GP Payload Body"/>
            <id root="${id}"/>
            <value>
              <Gp2gpfragment xmlns="urn:nhs:names:services:gp2gp">
                <Message xmlns="urn:hl7-org:v3" type="Message">
                  <id root="${id}"/>
                  <code code="0" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.101" displayName="Continue"/>
                  <creationTime value="201009201130"/>
                  <versionCode code="V3NPfIT3.1.09"/>
                  <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="MCCI_IN010000UK13"/>
                  <processingCode code="P"/>
                  <processingModeCode code="T"/>
                  <acceptAckCode code="NE"/>
                  <acknowledgement typeCode="AA">
                    <acknowledgementDetail typeCode="IF">
                      <code code="0" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.101" displayName="Continue"/>
                    </acknowledgementDetail>
                    <messageRef>
                      <id root="${ehrExtractMessageId}"/>
                    </messageRef>
                  </acknowledgement>
                  <communicationFunctionRcv>
                    <device>
                      <id root="1.2.826.0.1285.0.2.0.107" extension="${receivingAsid}"/>
                    </device>
                  </communicationFunctionRcv>
                  <communicationFunctionSnd>
                    <device>
                      <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}"/>
                    </device>
                  </communicationFunctionSnd>
                </Message>
                <acknowledgedMessage>
                  <id root="${ehrExtractMessageId}"/>
                </acknowledgedMessage>
              </Gp2gpfragment>
            </value>
          </pertinentPayloadBody>
        </pertinentInformation>
      </PayloadInformation>
    </subject>
  </ControlActEvent>
</COPC_IN000001UK01>`;
