package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageHeader {
    @JacksonXmlProperty(localName = "Action", namespace = "eb")
    public String action;

    @JacksonXmlProperty(localName = "ConversationId", namespace = "eb")
    public UUID conversationId;

    @JacksonXmlProperty(localName = "MessageData", namespace = "eb")
    public MessageData messageData;
}
