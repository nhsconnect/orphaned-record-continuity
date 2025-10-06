export const generateAcknowledgementResponse = () =>
  `<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" 
    xmlns:eb="http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd"
    xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header>
        <eb:MessageHeader eb:version="2.0" soap-env:mustUnderstand="1">
            <eb:From>
                <eb:PartyId eb:type="urn:nhs:names:partyType:ocs+serviceInstance">A28009-821605</eb:PartyId>
            </eb:From>
            <eb:To>
                <eb:PartyId eb:type="urn:nhs:names:partyType:ocs+serviceInstance">5EP-807264</eb:PartyId>
            </eb:To>
            <eb:CPAId>8fff3d7101f23f9c61bd</eb:CPAId>
            <eb:ConversationId>922CBB28-B342-4217-8432-73779A678555</eb:ConversationId>
            <eb:Service>urn:oasis:names:tc:ebxml-msg:service</eb:Service>
            <eb:Action>Acknowledgment</eb:Action>
            <eb:MessageData>
                <eb:MessageId>5075C56F-8E91-4AB7-9C01-6B7D1245553C</eb:MessageId>
                <eb:Timestamp>2018-06-12T08:29:20Z</eb:Timestamp>
                <eb:RefToMessageId>BC35C075-A538-4B5F-965D-F8B8E3D582F5</eb:RefToMessageId>
            </eb:MessageData>
            <eb:DuplicateElimination>always</eb:DuplicateElimination>
        </eb:MessageHeader>
       <eb:acknowledgement typeCode="AA">
        <eb:acknowledgementDetail typeCode="ER">
            <eb:code codeSystem="2.16.840.1.113883.2.1.3.2.4.17.32" code="519"
                  displayName="hl7:{interactionId}/hl7:communicationFunctionRcv/hl7:device/hl7:id[@root=2.16.840.1.113883.2.1.3.2.4.10] is not [1..1], or is inconsistent with the SOAP:Header"/>
        </eb:acknowledgementDetail>
        <eb:messageRef>
            <eb:id root="kjhidsfg-fdgdfg-dfgdg"/>
        </eb:messageRef>
    </eb:acknowledgement>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body/>
</SOAP-ENV:Envelope>`;
