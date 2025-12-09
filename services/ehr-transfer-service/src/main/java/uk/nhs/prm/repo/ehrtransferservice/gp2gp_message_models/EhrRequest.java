package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrRequest {
    @JacksonXmlProperty(localName = "recordTarget")
    public RecordTarget recordTarget;

    @JacksonXmlProperty(localName = "author")
    public Author author;

    @JacksonXmlProperty(localName = "id")
    public Identifier id;

    public Patient getPatient() {
        return this.recordTarget.patient;
    }

    public RequestingPractice getRequestingPractice() {
        return this.author.requestingPractice;
    }

    public String getId() {
        return this.id.root;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecordTarget {
        @JacksonXmlProperty(localName = "patient")
        public Patient patient;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        @JacksonXmlProperty(localName = "AgentOrgSDS")
        public RequestingPractice requestingPractice;
    }
}
