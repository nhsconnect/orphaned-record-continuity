package uk.nhs.prm.repo.suspension.service.publishers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.prm.repo.suspension.service.model.NonSensitiveDataMessage;
import uk.nhs.prm.repo.suspension.service.publishers.MessagePublisher;
import uk.nhs.prm.repo.suspension.service.publishers.MofNotUpdatedEventPublisher;

import static org.mockito.Mockito.verify;

class MofNotUpdatedEventPublisherTest {

    @Test
    void shouldCallSendMessageWithCorrectStringWhenEventPublisherIsInvoked(){
        var mockMessagePublisher = Mockito.mock(MessagePublisher.class);
        var nonSensitiveDataMessage = new NonSensitiveDataMessage("nemsMessageId","status");
        String NonSensitiveMessageBody = "{\"nemsMessageId\":\"nemsMessageId\",\"status\":\"status\"}";
        MofNotUpdatedEventPublisher mofNotUpdatedEventPublisher = new MofNotUpdatedEventPublisher(mockMessagePublisher,"arn");
        mofNotUpdatedEventPublisher.sendMessage(nonSensitiveDataMessage);
        verify(mockMessagePublisher).sendMessage("arn",NonSensitiveMessageBody);

    }

}