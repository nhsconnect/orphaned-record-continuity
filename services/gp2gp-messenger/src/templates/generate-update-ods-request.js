const checkTemplateArguments = require('./utils/check_params');

const generateUpdateOdsRequest = ({
  id,
  timestamp,
  receivingService,
  sendingService,
  newOdsCode,
  patient
}) => {
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
    newOdsCode: newOdsCode,
    patient: {
      nhsNumber: undefined,
      pdsId: undefined,
      pdsUpdateChangeNumber: undefined,
      ...(patient || {})
    }
  };

  checkTemplateArguments(inputObject);
  return template(inputObject);
};

const template = ({
  id,
  timestamp,
  receivingService: { asid: receivingAsid },
  sendingService: { asid: sendingAsid },
  newOdsCode: newOdsCode,
  patient: { nhsNumber, pdsId, pdsUpdateChangeNumber }
}) =>
  ` 
<PRPA_IN000203UK03 xmlns="urn:hl7-org:v3" xmlns:hl7="urn:hl7-org:v3">
    <id root="${id}" />
    <creationTime value="${timestamp}" />
    <versionCode code="3NPfIT6.3.01" />
    <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="PRPA_IN000203UK03" />
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
        <subject typeCode="SUBJ">
          <PdsUpdateRequest classCode="REG" moodCode="RQO">
            <!--
             To indicate that all updates are "Change to existing data" 
            -->
            <code code="1" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.40"/>
            
            <!-- IMPORTANT???            -->
            <pertinentInformation typeCode="PERT">
               <pertinentSerialChangeNumber classCode="OBS" moodCode="EVN">
                  <code code="2" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.35" />
                  <value value="${pdsUpdateChangeNumber}" />
               </pertinentSerialChangeNumber>
            </pertinentInformation>
            
            
            <subject typeCode="SBJ">
              <patientRole classCode="PAT">
              
                <!--
                 NHS number (identifies patient - not an updated item of data) 
                -->
                <id root="2.16.840.1.113883.2.1.4.1" extension="${nhsNumber}"/>
                
                <!--
                To indicate a change to primary care provision
                -->
                <patientPerson classCode="PSN" determinerCode="INSTANCE">
                  <playedOtherProviderPatient classCode="PAT">
                    <subjectOf typeCode="SBJ">
                      <patientCareProvision classCode="PCPR" moodCode="EVN" updateMode="altered">
                        <id root="2.16.840.1.113883.2.1.3.2.4.18.1" extension="${pdsId}"/>
                        <!--  To indicate primary care provision  -->
                        <code code="1" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.37"/>
                        <performer typeCode="PRF">
                          <assignedOrganization classCode="ASSIGNED">
                          <!--  National GP Practice code of primary care provider  -->
                          <id root="2.16.840.1.113883.2.1.4.3" extension="${newOdsCode.toUpperCase()}"/>
                          </assignedOrganization>
                        </performer>
                      </patientCareProvision>
                    </subjectOf>
                  </playedOtherProviderPatient>
                </patientPerson>
              </patientRole>
            </subject>
          </PdsUpdateRequest>
        </subject>
    </ControlActEvent>
</PRPA_IN000203UK03>
`;

module.exports = generateUpdateOdsRequest;
