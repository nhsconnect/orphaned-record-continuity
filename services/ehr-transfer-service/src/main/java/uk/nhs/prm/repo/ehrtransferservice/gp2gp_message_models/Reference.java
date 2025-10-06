package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAttribute;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reference {
    @XmlAttribute(name = "xlink:href")
    public String href;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
