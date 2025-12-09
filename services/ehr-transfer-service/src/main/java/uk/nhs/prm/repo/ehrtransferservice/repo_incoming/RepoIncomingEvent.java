package uk.nhs.prm.repo.ehrtransferservice.repo_incoming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoIncomingEvent {
    private String nhsNumber;
    private String sourceGp;
    private String nemsMessageId;
    private String conversationId;
}
