import dateFormat from 'dateformat';
import { logInfo } from '../middleware/logging';
import { AcknowledgementErrorCode } from '../constants/enums';

export const buildEhrAcknowledgementPayload = ({
  acknowledgementMessageId,
  receivingAsid,
  sendingAsid,
  acknowledgedMessageId,
  errorCode,
  errorDisplayName
}) => {
  switch (errorCode) {
    case null:
    case undefined:
      logInfo('Building positive acknowledgement message with typeCode: AA');

      return positiveAckMessageTemplate({
        acknowledgementMessageId: acknowledgementMessageId.toUpperCase(),
        timestamp: dateFormat(Date.now(), 'yyyymmddHHMMss'),
        acknowledgementTypeCode: 'AA', // positive ACK
        acknowledgedMessageId: acknowledgedMessageId.toUpperCase(),
        receivingAsid,
        sendingAsid
      });
    case AcknowledgementErrorCode.ERROR_CODE_30.errorCode: // negative ACK, large message general failure, unknown failure reason
    case AcknowledgementErrorCode.ERROR_CODE_99.errorCode: // negative ACK, unknown failure reason
      logInfo(
        `Building negative acknowledgement message with typeCode: AE, errorCode: ${errorCode} and errorDisplayName: ${errorDisplayName}`
      );

      return negativeAckMessageTemplate({
        acknowledgementMessageId: acknowledgementMessageId.toUpperCase(),
        timestamp: dateFormat(Date.now(), 'yyyymmddHHMMss'),
        acknowledgementTypeCode: 'AE', // negative ACK, unknown failure reason
        acknowledgedMessageId: acknowledgedMessageId.toUpperCase(),
        receivingAsid,
        sendingAsid,
        errorCode,
        errorDisplayName
      });
    default: // negative ACK, known failure reason
      logInfo(
        `Building negative acknowledgement message with typeCode: AR and errorCode: ${errorCode} and errorDisplayName: ${errorDisplayName}`
      );

      return negativeAckMessageTemplate({
        acknowledgementMessageId: acknowledgementMessageId.toUpperCase(),
        timestamp: dateFormat(Date.now(), 'yyyymmddHHMMss'),
        acknowledgementTypeCode: 'AR', // negative ACK, known failure reason
        acknowledgedMessageId: acknowledgedMessageId.toUpperCase(),
        receivingAsid,
        sendingAsid,
        errorCode,
        errorDisplayName
      });
  }
};

const positiveAckMessageTemplate = ({
  acknowledgementMessageId,
  timestamp,
  acknowledgementTypeCode,
  acknowledgedMessageId,
  receivingAsid,
  sendingAsid
}) =>
  `<MCCI_IN010000UK13 xmlns="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:hl7-org:v3 ..SchemasMCCI_IN010000UK13.xsd">
      <id root="${acknowledgementMessageId}" />
      <creationTime value="${timestamp}" />
      <versionCode code="V3NPfIT4.2.00" />
      <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="MCCI_IN010000UK13" />
      <processingCode code="P" />
      <processingModeCode code="T" />
      <acceptAckCode code="NE" />
      <acknowledgement typeCode="${acknowledgementTypeCode}">
         <messageRef>
            <id root="${acknowledgedMessageId}" />
         </messageRef>
      </acknowledgement>
      <communicationFunctionRcv typeCode="RCV">
         <device classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.826.0.1285.0.2.0.107" extension="${receivingAsid}" />
         </device>
      </communicationFunctionRcv>
      <communicationFunctionSnd typeCode="SND">
         <device classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}" />
         </device>
      </communicationFunctionSnd>
      <ControlActEvent classCode="CACT" moodCode="EVN">
         <author1 typeCode="AUT">
            <AgentSystemSDS classCode="AGNT">
               <agentSystemSDS classCode="DEV" determinerCode="INSTANCE">
                  <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}" />
               </agentSystemSDS>
            </AgentSystemSDS>
         </author1>
      </ControlActEvent>
   </MCCI_IN010000UK13>`;

const negativeAckMessageTemplate = ({
  acknowledgementMessageId,
  timestamp,
  acknowledgementTypeCode,
  acknowledgedMessageId,
  receivingAsid,
  sendingAsid,
  errorCode, // not in a positive ack
  errorDisplayName // not in a positive ack
}) =>
  `<MCCI_IN010000UK13 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema" type="Message" xmlns="urn:hl7-org:v3">
      <id root="${acknowledgementMessageId}" />
      <creationTime value="${timestamp}" />
      <versionCode code="V3NPfIT3.1.10" />
      <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="MCCI_IN010000UK13" />
      <processingCode code="P" />
      <processingModeCode code="T" />
      <acceptAckCode code="NE" />
      <acknowledgement type="Acknowledgement" typeCode="${acknowledgementTypeCode}">
         <acknowledgementDetail type="AcknowledgementDetail" typeCode="${acknowledgementTypeCode}">
            <code code="${errorCode}" displayName="${errorDisplayName}"/>
         </acknowledgementDetail>
         <messageRef type="Message">
            <id root="${acknowledgedMessageId}" />
         </messageRef>
      </acknowledgement>
      <communicationFunctionRcv type="CommunicationFunction" typeCode="RCV">
         <device type="Device" classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.826.0.1285.0.2.0.107" extension="${receivingAsid}" />
         </device>
      </communicationFunctionRcv>
      <communicationFunctionSnd type="CommunicationFunction" typeCode="SND">
         <device type="Device" classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}" />
         </device>
      </communicationFunctionSnd>
      <ControlActEvent type="ControlAct" classCode="CACT" moodCode="EVN">
         <author1 type="Participation" typeCode="AUT">
            <AgentSystemSDS type="RoleHeir" classCode="AGNT">
               <agentSystemSDS type="Device" classCode="DEV" determinerCode="INSTANCE">
                  <id root="1.2.826.0.1285.0.2.0.107" extension="${sendingAsid}" />
               </agentSystemSDS>
            </AgentSystemSDS>
         </author1>
         <reason type="ActRelationship" typeCode="RSON">
            <justifyingDetectedIssueEvent type="Observation" classCode="ALRT" moodCode="EVN">
               <code code="${errorCode}" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.101" displayName="${errorDisplayName}">
                  <qualifier inverted="false">
                     <value code="ER" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.104"/>
                  </qualifier>
               </code>
            </justifyingDetectedIssueEvent>
         </reason>
      </ControlActEvent>
   </MCCI_IN010000UK13>`;
