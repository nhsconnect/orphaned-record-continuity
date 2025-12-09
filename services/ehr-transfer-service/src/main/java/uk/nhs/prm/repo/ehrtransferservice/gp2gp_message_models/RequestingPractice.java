package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestingPractice {
    @JacksonXmlProperty(localName = "agentOrganizationSDS")
    public AgentOrganizationSDS agentOrganizationSDS;

    public String getOdsCode() {
        return this.agentOrganizationSDS.id.extension;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentOrganizationSDS {
        @JacksonXmlProperty(localName = "id")
        public Identifier id;
    }
}
