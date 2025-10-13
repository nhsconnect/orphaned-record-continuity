package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("unit")
class EhrExtractMessageWrapperTest {
    @Test
    void shouldRetrieveNHSNumberFromEHRExtract() throws JsonProcessingException {
        String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<RCMR_IN030000UK06 xmlns=\"urn:hl7-org:v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:hl7-org:v3 ..SchemasRCMR_IN030000UK06.xsd\">\n" +
                "    <ControlActEvent classCode=\"CACT\" moodCode=\"EVN\">\n" +
                "        <subject typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
                "            <EhrExtract classCode=\"EXTRACT\" moodCode=\"EVN\">\n" +
                "                <recordTarget typeCode=\"RCT\">\n" +
                "                    <patient classCode=\"PAT\">\n" +
                "                        <id root=\"2.16.840.1.113883.2.1.4.1\" extension=\"9442964410\" />\n" +
                "                    </patient>" +
                "                </recordTarget>" +
                "             </EhrExtract>" +
                "         </subject>" +
                "    </ControlActEvent>" +
                "</RCMR_IN030000UK06>";

        XmlMapper xmlMapper = new XmlMapper();
        EhrExtractMessageWrapper ehrExtractMessageWrapper = xmlMapper.readValue(extract, EhrExtractMessageWrapper.class);
        EhrExtract ehrExtract = ehrExtractMessageWrapper.getEhrExtract();
        Patient patient = ehrExtract.getPatient();
        assertThat(patient.getNhsNumber(), equalTo("9442964410"));
    }
}
