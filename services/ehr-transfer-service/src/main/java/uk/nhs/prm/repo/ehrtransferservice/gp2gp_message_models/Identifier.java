package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAttribute;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Identifier {
    @XmlAttribute(name = "extension")
    public String extension;

    @XmlAttribute(name = "root")
    public String root;
}
