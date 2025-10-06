PDS query sent to PDS in order to obtain some patient data, particular points of interest are:
 * serial number of the patient data record
 * PDS ID

```xml
<QUPA_IN000008UK02 xmlns="urn:hl7-org:v3" xmlns:hl7="urn:hl7-org:v3">
    <id root="B4AABF1A-D1EF-46B2-B754-1726B2BE90E3" />
    <creationTime value="20200218155832" />
    <versionCode code="V3NPfIT3.0" />
    <interactionId root="2.16.840.1.113883.2.1.3.2.4.12" extension="QUPA_IN000008UK02" />
    <processingCode code="P" />
    <processingModeCode code="T" />
    <acceptAckCode code="NE" />
    <communicationFunctionRcv>
        <device classCode="DEV" determinerCode="INSTANCE">
            <hl7:id xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/" extension="928942012545"
                    root="1.2.826.0.1285.0.2.0.107" />
        </device>
    </communicationFunctionRcv>
    <communicationFunctionSnd>
        <device classCode="DEV" determinerCode="INSTANCE">
            <hl7:id xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/" extension="200000001161"
                    root="1.2.826.0.1285.0.2.0.107" />
        </device>
    </communicationFunctionSnd>
    <ControlActEvent classCode="CACT" moodCode="EVN">
        <author1 typeCode="AUT">
            <AgentSystemSDS classCode="AGNT">
                <agentSystemSDS classCode="DEV" determinerCode="INSTANCE">
                    <hl7:id xmlns:SOAP="http://schemas.xmlsoap.org/soap/envelope/" extension="200000001161"
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
                <value root="2.16.840.1.113883.2.1.4.1" extension="9442964410" />
                <semanticsText>person.id</semanticsText>
            </person.id>
            <retrievalItem>
                <semanticsText>person.allData</semanticsText>
            </retrievalItem>
        </query>
    </ControlActEvent>
</QUPA_IN000008UK02>
```
