package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.PresignedUrl;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EhrRepoServiceTest {
    @Mock
    private EhrRepoClient mockEhrRepoClient;
    @Mock
    private ConversationActivityService activityService;
    @InjectMocks
    private EhrRepoService ehrRepoService;

    @Test
    void shouldCoordinateEhrRepoClientCalls() throws Exception {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        PresignedUrl mockPresignedUrl = mock(PresignedUrl.class);
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockEhrRepoClient.fetchStorageUrl(conversationId, messageId)).thenReturn(mockPresignedUrl);
        when(mockEhrRepoClient.confirmMessageStored(mockParsedMessage)).thenReturn(new StoreMessageResponseBody("complete"));

        var result = ehrRepoService.storeMessage(mockParsedMessage);

        Assertions.assertThat(result.isEhrComplete()).isTrue();

        verify(activityService).captureConversationActivity(conversationId);
        verify(mockEhrRepoClient, times(1)).fetchStorageUrl(conversationId, messageId);
        verify(mockPresignedUrl, times(1)).uploadMessage(mockParsedMessage);
        verify(mockEhrRepoClient, times(1)).confirmMessageStored(mockParsedMessage);
    }

    @Test
    void shouldThrowStorageFailureExceptionWhenPresignedUrlCannotBeRetrieved() throws Exception {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockEhrRepoClient.fetchStorageUrl(conversationId, messageId)).thenThrow(new RuntimeException());

        Exception expected = assertThrows(Exception.class, () ->
                ehrRepoService.storeMessage(mockParsedMessage)
        );
        assertThat(expected, notNullValue());
    }

    @Test
    void shouldThrowStorageFailureExceptionWhenCannotStoreMessage() throws Exception {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        ParsedMessage mockParsedMessage = mock(ParsedMessage.class);
        PresignedUrl mockPresignedUrl = mock(PresignedUrl.class);
        when(mockParsedMessage.getConversationId()).thenReturn(conversationId);
        when(mockParsedMessage.getMessageId()).thenReturn(messageId);
        when(mockEhrRepoClient.fetchStorageUrl(conversationId, messageId)).thenReturn(mockPresignedUrl);
        doThrow(new HttpException()).when(mockEhrRepoClient).confirmMessageStored(any());

        Exception expected = assertThrows(Exception.class, () ->
                ehrRepoService.storeMessage(mock(ParsedMessage.class))
        );
        assertThat(expected, notNullValue());
    }
}
