const checkTemplateArguments = require('./utils/check_params');

const generatePdsRetrievalQuery = ({ id, timestamp, receivingService, sendingService, patient }) =>
  new Promise(resolve => {
    const inputObject = {
      id,
      timestamp,
      receivingService: {
        asid: undefined,
        ...(receivingService || {})
      },
      sendingService: {
        asid: undefined,
        ...(sendingService || {})
      },
      patient: {
        nhsNumber: undefined,
        ...(patient || {})
      }
    };

    checkTemplateArguments(inputObject);
    resolve(template(inputObject));
  });

const template = ({
  id,
  timestamp,
  receivingService: { asid: receivingAsid },
  sendingService: { asid: sendingAsid },
  patient: { nhsNumber }
}) =>
  `<QUPA_IN000008UK02 xmlns="urn:hl7-org:v3" xmlns:hl7="urn:hl7-org:v3">
    <id root="${id}" />
    <creationTime value="${timestamp}" />
    <versionCode code="V3NPfIT3.0" />
    <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="QUPA_IN000008UK02" />
    <processingCode code="P" />
    <processingModeCode code="T" />
    <acceptAckCode code="NE" />
    <communicationFunctionRcv>
        <device classCode="DEV" determinerCode="INSTANCE">
            <hl7:id xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/" extension="${receivingAsid}"
                    root="1.2.826.0.1285.0.2.0.107" />
        </device>
    </communicationFunctionRcv>
    <communicationFunctionSnd>
        <device classCode="DEV" determinerCode="INSTANCE">
            <hl7:id xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/" extension="${sendingAsid}"
                    root="1.2.826.0.1285.0.2.0.107" />
        </device>
    </communicationFunctionSnd>
    <ControlActEvent classCode="CACT" moodCode="EVN">
        <author1 typeCode="AUT">
            <AgentSystemSDS classCode="AGNT">
                <agentSystemSDS classCode="DEV" determinerCode="INSTANCE">
                    <hl7:id xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/" extension="${sendingAsid}"
                            root="1.2.826.0.1285.0.2.0.107" />
                </agentSystemSDS>
            </AgentSystemSDS>
        </author1>
        <query>
            <historicDataIndicator>
                <value code="1" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.36" />
                <semanticsText>historicDataIndicator</semanticsText>
            </historicDataIndicator>
            <person.id>
                <value root="2.16.840.1.113883.2.1.4.1" extension="${nhsNumber}" />
                <semanticsText>person.id</semanticsText>
            </person.id>
            <retrievalItem>
                <semanticsText>person.allData</semanticsText>
            </retrievalItem>
        </query>
    </ControlActEvent>
</QUPA_IN000008UK02>`;

module.exports = generatePdsRetrievalQuery;
