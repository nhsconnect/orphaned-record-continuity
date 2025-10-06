export const pdsQueryActFailed = `<?xml version='1.0' encoding='UTF-8'?>
<SOAP-ENV:Envelope xmlns:crs="http://national.carerecords.nhs.uk/schema/crs/" 
xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" 
xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" 
xmlns="urn:hl7-org:v3" 
xmlns:hl7="urn:hl7-org:v3">
<SOAP-ENV:Header>
    <wsa:MessageID>uuid:DC50384E-5E2A-41EA-A673-F40343488B16</wsa:MessageID>
    <wsa:Action>urn:nhs:names:services:pdsquery/QUQI_IN010000UK14</wsa:Action>
    <wsa:To/>
    <wsa:From>
        <wsa:Address>urn:nhs:names:services:pdsquery</wsa:Address>
    </wsa:From>
    <communicationFunctionRcv typeCode="RCV">
        <device classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.826.0.1285.0.2.0.107" extension="200000001161"/>
        </device>
    </communicationFunctionRcv>
    <communicationFunctionSnd typeCode="SND">
        <device classCode="DEV" determinerCode="INSTANCE">
            <id root="1.2.826.0.1285.0.2.0.107" extension="928942012545"/>
        </device>
    </communicationFunctionSnd>
    <wsa:RelatesTo>uuid:B6C48C50-95F0-42C5-A9F3-6FCD92540237</wsa:RelatesTo>
</SOAP-ENV:Header>
<SOAP-ENV:Body>
    <retrievalQueryResponse>
        <QUQI_IN010000UK14>
            <id root="DC50384E-5E2A-41EA-A673-F40343488B16"/>
            <creationTime value="20200304151436"/>
            <versionCode code="3NPfIT6.3.01"/>
            <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="QUQI_IN010000UK14"/>
            <processingCode code="P"/>
            <processingModeCode code="T"/>
            <acceptAckCode code="NE"/>
            <acknowledgement typeCode="AE">
                <messageRef>
                    <id root="91048916-01F1-4333-BEC8-BC0E7A576815"/>
                </messageRef>
            </acknowledgement>
            <communicationFunctionRcv typeCode="RCV">
                <device classCode="DEV" determinerCode="INSTANCE">
                    <id root="1.2.826.0.1285.0.2.0.107" extension="200000001161"/>
                </device>
            </communicationFunctionRcv>
            <communicationFunctionSnd typeCode="SND">
                <device classCode="DEV" determinerCode="INSTANCE">
                    <id root="1.2.826.0.1285.0.2.0.107" extension="928942012545"/>
                </device>
            </communicationFunctionSnd>
            <ControlActEvent classCode="CACT" moodCode="EVN">
                <author1 typeCode="AUT">
                    <AgentSystemSDS classCode="AGNT">
                        <agentSystemSDS classCode="DEV" determinerCode="INSTANCE">
                            <id root="1.2.826.0.1285.0.2.0.107" extension="928942012545"/>
                        </agentSystemSDS>
                    </AgentSystemSDS>
                </author1>
                <reason typeCode="RSON">
                    <justifyingDetectedIssueEvent classCode="ALRT" moodCode="EVN">
                        <code code="1" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.42" displayName="No match to a Service User record">
                            <qualifier code="ER"/>
                        </code>
                    </justifyingDetectedIssueEvent>
                </reason>
                <reason typeCode="RSON">
                    <justifyingDetectedIssueEvent classCode="ALRT" moodCode="EVN">
                        <code code="RT003" codeSystem="2.16.840.1.113883.2.1.3.2.4.17.42" displayName="No match to a Service User record">
                            <qualifier code="ER"/>
                        </code>
                    </justifyingDetectedIssueEvent>
                </reason>
                <queryAck type="QueryAck">
                    <queryResponseCode code="ID"/>
                </queryAck>
            </ControlActEvent>
        </QUQI_IN010000UK14>
    </retrievalQueryResponse>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>`;
