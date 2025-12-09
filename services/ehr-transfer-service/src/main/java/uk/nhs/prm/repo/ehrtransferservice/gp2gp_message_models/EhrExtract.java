package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrExtract {
    @JacksonXmlProperty(localName = "recordTarget")
    public RecordTarget recordTarget;

    public Patient getPatient() {
        return this.recordTarget.patient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordTarget {
        @JacksonXmlProperty(localName = "patient")
        public Patient patient;
    }
}
