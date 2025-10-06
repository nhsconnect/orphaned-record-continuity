package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("unit")
class EhrRequestMessageWrapperTest {
    @Test
    void shouldRetrieveNHSNumberFromEhrRequest() throws JsonProcessingException {
        String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<RCMR_IN010000UK05 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" type=\"Message\" xmlns=\"urn:hl7-org:v3\">\n" +
                "<ControlActEvent type=\"ControlAct\" classCode=\"CACT\" moodCode=\"EVN\">\n" +
                "<subject type=\"ActRelationship\" typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
                "<EhrRequest type=\"ActHeir\" classCode=\"EXTRACT\" moodCode=\"RQO\">\n" +
                "<id root=\"041CA2AE-3EC6-4AC9-942F-0F6621CC0BFC\"/>\n" +
                "<recordTarget type=\"Participation\" typeCode=\"RCT\">\n" +
                "<patient type=\"Patient\" classCode=\"PAT\">\n" +
                "<id root=\"2.16.840.1.113883.2.1.4.1\" extension=\"9692294935\"/>\n" +
                "</patient>\n" +
                "</recordTarget>\n" +
                "</EhrRequest>\n" +
                "</subject>\n" +
                "</ControlActEvent>\n" +
                "</RCMR_IN010000UK05>";
        XmlMapper xmlMapper = new XmlMapper();
        EhrRequestMessageWrapper ehrRequestMessageWrapper = xmlMapper.readValue(extract, EhrRequestMessageWrapper.class);
        EhrRequest ehrRequest = ehrRequestMessageWrapper.getEhrRequest();
        Patient patient = ehrRequest.getPatient();
        assertThat(patient.getNhsNumber(), equalTo("9692294935"));
    }

    @Test
    void shouldRetrieveOdsCodeFromEhrRequest() throws JsonProcessingException {
        String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<RCMR_IN010000UK05 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" type=\"Message\" xmlns=\"urn:hl7-org:v3\">\n" +
                "<ControlActEvent type=\"ControlAct\" classCode=\"CACT\" moodCode=\"EVN\">\n" +
                "<subject type=\"ActRelationship\" typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
                "<EhrRequest type=\"ActHeir\" classCode=\"EXTRACT\" moodCode=\"RQO\">\n" +
                "<author type=\"Participation\" typeCode=\"AUT\">\n" +
                "<AgentOrgSDS type=\"RoleHeir\" classCode=\"AGNT\">\n" +
                "<agentOrganizationSDS type=\"Organization\" classCode=\"ORG\" determinerCode=\"INSTANCE\">\n" +
                "<id root=\"1.2.826.0.1285.0.1.10\" extension=\"N82668\"/>\n" +
                "</agentOrganizationSDS>\n" +
                "</AgentOrgSDS>\n" +
                "</author>\n" +
                "</EhrRequest>\n" +
                "</subject>\n" +
                "</ControlActEvent>\n" +
                "</RCMR_IN010000UK05>";
        XmlMapper xmlMapper = new XmlMapper();
        EhrRequestMessageWrapper ehrRequestMessageWrapper = xmlMapper.readValue(extract, EhrRequestMessageWrapper.class);
        EhrRequest ehrRequest = ehrRequestMessageWrapper.getEhrRequest();
        RequestingPractice requestingPractice = ehrRequest.getRequestingPractice();
        assertThat(requestingPractice.getOdsCode(), equalTo("N82668"));
    }

    @Test
    void shouldRetrieveEhrRequestId() throws JsonProcessingException {
        String extract = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<RCMR_IN010000UK05 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" type=\"Message\" xmlns=\"urn:hl7-org:v3\">\n" +
                "<ControlActEvent type=\"ControlAct\" classCode=\"CACT\" moodCode=\"EVN\">\n" +
                "<subject type=\"ActRelationship\" typeCode=\"SUBJ\" contextConductionInd=\"false\">\n" +
                "<EhrRequest type=\"ActHeir\" classCode=\"EXTRACT\" moodCode=\"RQO\">\n" +
                "<id root=\"041CA2AE-3EC6-4AC9-942F-0F6621CC0BFC\"/>\n" +
                "</EhrRequest>\n" +
                "</subject>\n" +
                "</ControlActEvent>\n" +
                "</RCMR_IN010000UK05>";
        XmlMapper xmlMapper = new XmlMapper();
        EhrRequestMessageWrapper ehrRequestMessageWrapper = xmlMapper.readValue(extract, EhrRequestMessageWrapper.class);
        EhrRequest ehrRequest = ehrRequestMessageWrapper.getEhrRequest();
        assertThat(ehrRequest.getId(), equalTo("041CA2AE-3EC6-4AC9-942F-0F6621CC0BFC"));
    }
}
