const checkTemplateArguments = require('./utils/check_params');

const generateEhrRequestQuery = ({
  id,
  timestamp,
  receivingService,
  sendingService,
  patient,
  ehrRequestId
}) => {
  const inputObject = {
    id,
    timestamp,
    receivingService: {
      asid: undefined,
      odsCode: undefined,
      ...(receivingService || {})
    },
    sendingService: {
      asid: undefined,
      odsCode: undefined,
      ...(sendingService || {})
    },
    patient: {
      nhsNumber: undefined,
      ...(patient || {})
    },
    ehrRequestId
  };

  checkTemplateArguments(inputObject);
  return template(inputObject);
};

const template = ({
  id,
  timestamp,
  receivingService: { asid: receivingAsid, odsCode: receivingOdsCode },
  sendingService: { asid: sendingAsid, odsCode: sendingOdsCode },
  patient: { nhsNumber },
  ehrRequestId
}) =>
  `<RCMR_IN010000UK05 xmlns="urn:hl7-org:v3" 
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
        xsi:schemaLocation="urn:hl7-org:v3 RCMR_IN010000UK05.xsd">
    <id root="${id}" />
    <creationTime value="${timestamp}" />
    <versionCode code="V3NPfIT3.1.10"/>
    <interactionId extension="RCMR_IN010000UK05" root="2.16.840.1.113883.2.1.3.2.4.12"/>
    <processingCode code="P"/>
    <processingModeCode code="T"/>
    <acceptAckCode code="NE"/>
    <communicationFunctionRcv typeCode="RCV">
        <device classCode="DEV" determinerCode="INSTANCE">
            <id extension="${receivingAsid}" root="1.2.826.0.1285.0.2.0.107"/>
        </device>
    </communicationFunctionRcv>
    <communicationFunctionSnd typeCode="SND">
        <device classCode="DEV" determinerCode="INSTANCE">
            <id extension="${sendingAsid}" root="1.2.826.0.1285.0.2.0.107"/>
        </device>
    </communicationFunctionSnd>
    <ControlActEvent classCode="CACT" moodCode="EVN">
        <author1 typeCode="AUT">
            <AgentSystemSDS classCode="AGNT">
                <agentSystemSDS classCode="DEV" determinerCode="INSTANCE">
                    <id extension="${sendingAsid}" root="1.2.826.0.1285.0.2.0.107"/>
                </agentSystemSDS>
            </AgentSystemSDS>
        </author1>
        <subject contextConductionInd="false" typeCode="SUBJ">
            <EhrRequest classCode="EXTRACT" moodCode="RQO">
              <id root="${ehrRequestId}"/>
              <recordTarget typeCode="RCT">
                  <patient classCode="PAT">
                      <id root="2.16.840.1.113883.2.1.4.1" extension="${nhsNumber}"/>
                  </patient>
              </recordTarget>
              <author typeCode="AUT">
                  <AgentOrgSDS classCode="AGNT">
                      <agentOrganizationSDS classCode="ORG" determinerCode="INSTANCE">
                          <id root="1.2.826.0.1285.0.1.10" extension="${sendingOdsCode.toUpperCase()}"/>
                      </agentOrganizationSDS>
                  </AgentOrgSDS>
              </author>
              <destination typeCode="DST">
                  <AgentOrgSDS classCode="AGNT">
                      <agentOrganizationSDS classCode="ORG" determinerCode="INSTANCE">
                          <id root="1.2.826.0.1285.0.1.10" extension="${receivingOdsCode.toUpperCase()}"/>
                      </agentOrganizationSDS>
                  </AgentOrgSDS>
              </destination>
          </EhrRequest>
        </subject>
    </ControlActEvent>
  </RCMR_IN010000UK05>`;

module.exports = generateEhrRequestQuery;
