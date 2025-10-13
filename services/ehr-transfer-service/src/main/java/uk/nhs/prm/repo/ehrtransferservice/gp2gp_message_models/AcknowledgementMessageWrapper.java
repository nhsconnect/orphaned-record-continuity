package uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.AcknowledgementTypeCode;
import uk.nhs.prm.repo.ehrtransferservice.models.ack.FailureDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcknowledgementMessageWrapper implements MessageContent {

    public Acknowledgement acknowledgement;

    public AcknowledgementTypeCode getTypeCode() {
        return AcknowledgementTypeCode.parse(acknowledgement.typeCode);
    }

    @JacksonXmlProperty(localName = "ControlActEvent")
    public ControlActEvent controlActEvent;

    public List<FailureDetail> getFailureDetails() {
        List<FailureDetail> failureDetailList = new ArrayList<>();

        acknowledgementDetails().forEach(ad -> failureDetailList.add(new FailureDetail(ad)));
        reasons().forEach(reason -> failureDetailList.add(new FailureDetail(reason)));

        return failureDetailList;
    }

    public Stream<ControlActEvent.Reason> reasons() {
        if (controlActEvent == null || controlActEvent.reason == null) {
            return Stream.empty();
        }
        return controlActEvent.reason.stream();
    }

    public Stream<Acknowledgement.AcknowledgementDetail> acknowledgementDetails() {
        if (acknowledgement.acknowledgementDetail == null) {
            return Stream.empty();
        }
        return acknowledgement.acknowledgementDetail.stream();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlActEvent {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Reason> reason;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Reason {

            public JustifyingDetectedIssueEvent justifyingDetectedIssueEvent;

            public String getErrorDisplayName() {
                return hasEventCode() ? eventCode().displayName : null;
            }

            public String getCode() {
                return hasEventCode() ? eventCode().code : null;
            }

            public String getCodeSystem() {
                return hasEventCode() ? eventCode().codeSystem : null;
            }

            public String getQualifier() {
                if (!hasEventCode()) {
                    return null;
                }
                if (eventCode().qualifier == null) {
                    return null;
                }
                return eventCode().qualifier.code;
            }

            private boolean hasEventCode() {
                return justifyingDetectedIssueEvent != null && eventCode() != null;
            }

            private JustifyingDetectedIssueEvent.Code eventCode() {
                return justifyingDetectedIssueEvent.code;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class JustifyingDetectedIssueEvent {
                public Code code;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Code {

                    public String displayName;
                    public String code;
                    public String codeSystem;
                    public Qualifier qualifier;

                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public static class Qualifier {
                        public String code;
                    }
                }
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Acknowledgement {
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<AcknowledgementDetail> acknowledgementDetail;
        public String typeCode;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AcknowledgementDetail {
            public String typeCode;
            public AcknowledgementDetailCode code;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AcknowledgementDetailCode {
                public String displayName;
                public String code;
                public String codeSystem;
            }

            public String getDisplayName() {
                return code == null ? null : code.displayName;
            }

            public String getCode() {
                return code == null ? null : code.code;
            }

            public String getCodeSystem() {
                return code == null ? null : code.codeSystem;
            }

            public String getTypeCode() {
                return typeCode;
            }
        }
    }
}
