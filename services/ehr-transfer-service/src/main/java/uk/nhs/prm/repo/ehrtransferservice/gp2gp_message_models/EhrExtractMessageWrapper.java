package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrExtractMessageWrapper implements MessageContent {
    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public EhrExtract getEhrExtract() {
        return this.controlActEvent.subject.ehrExtract;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlProperty(localName = "subject")
        public Subject subject;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Subject {

            @JacksonXmlProperty(localName = "EhrExtract")
            public EhrExtract ehrExtract;
        }
    }
}
