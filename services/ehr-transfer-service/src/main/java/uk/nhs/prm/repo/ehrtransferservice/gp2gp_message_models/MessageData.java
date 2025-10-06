package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageData {
    @JacksonXmlProperty(localName = "MessageId", namespace = "eb")
    public UUID messageId;
}
