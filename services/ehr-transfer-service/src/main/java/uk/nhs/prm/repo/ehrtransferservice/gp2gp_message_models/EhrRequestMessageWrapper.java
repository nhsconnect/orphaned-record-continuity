package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrRequestMessageWrapper implements MessageContent {
    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public EhrRequest getEhrRequest() {
        return this.controlActEvent.subject.ehrRequest;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlProperty(localName = "subject")
        public Subject subject;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Subject {

            @JacksonXmlProperty(localName = "EhrRequest")
            public EhrRequest ehrRequest;
        }
    }
}
