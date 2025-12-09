package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {
    @JacksonXmlProperty(localName = "id")
    public Identifier id;

    public String getNhsNumber() {
        return this.id.extension;
    }
}
