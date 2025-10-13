package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeEhrFragmentMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.LargeSqsMessage;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LargeMessageFragmentHandlerTest {
    @Mock
    LargeSqsMessage largeSqsMessage;

    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    ConversationActivityService conversationActivityService;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @InjectMocks
    LargeMessageFragmentHandler largeMessageFragmentHandler;

    @Captor
    ArgumentCaptor<LargeEhrFragmentMessage> largeMessageFragmentsArgumentCaptor;

    private static final UUID INBOUND_CONVERSATION_ID =
        UUID.fromString("e979906a-31ce-43f4-a7ce-af93ebb659dc");

    private static final StoreMessageResult COMPLETE_RESULT;
    private static final StoreMessageResult INCOMPLETE_RESULT;

    static  {
        COMPLETE_RESULT = getStoreMessageResult(Type.COMPLETE);
        INCOMPLETE_RESULT = getStoreMessageResult(Type.INCOMPLETE);
    }

    @Test
    void handleMessage_StoreMessageResultIsComplete_StoreFragmentMessageAndEhrCompletePositiveAcknowledgement() throws Exception {
        // when
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture()))
            .thenReturn(COMPLETE_RESULT);
        when(largeSqsMessage.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);
        doNothing()
            .when(conversationActivityService)
            .concludeConversationActivity(INBOUND_CONVERSATION_ID);

        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        // then
        verify(ehrRepoService).storeMessage(largeMessageFragmentsArgumentCaptor.capture());
        verify(conversationActivityService).concludeConversationActivity(INBOUND_CONVERSATION_ID);
        verify(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);
    }

    @Test
    void handleMessage_StoreMessageResultIsNotComplete_StoreFragmentMessage() throws Exception {
        // when
        when(ehrRepoService.storeMessage(largeMessageFragmentsArgumentCaptor.capture()))
            .thenReturn(INCOMPLETE_RESULT);

        largeMessageFragmentHandler.handleMessage(largeSqsMessage);

        // then
        verify(ehrRepoService).storeMessage(largeMessageFragmentsArgumentCaptor.capture());
        verify(conversationActivityService, never()).concludeConversationActivity(any(UUID.class));
        verify(gp2gpMessengerService, never()).sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);
    }

    @Test
    void handleMessage_StoreMessageThrowsException_ShouldNotSendEhrCompletePositiveAcknowledgement() throws Exception {
        // given
        final Exception exception = new Exception();

        // when
        doThrow(exception)
            .when(ehrRepoService)
            .storeMessage(largeMessageFragmentsArgumentCaptor.capture());

        // then
        assertThrows(Exception.class, () -> largeMessageFragmentHandler.handleMessage(largeSqsMessage));
        verify(conversationActivityService, never()).concludeConversationActivity(any(UUID.class));
        verify(gp2gpMessengerService, never()).sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);
    }

    // Helper Methods & Utilities
    private static StoreMessageResult getStoreMessageResult(Type type) {
        StoreMessageResponseBody responseBody;

        switch (type) {
            case COMPLETE -> responseBody = new StoreMessageResponseBody("complete");
            case INCOMPLETE -> responseBody = new StoreMessageResponseBody("incomplete");
            default -> throw new JUnitException("Invalid type provided for getResponseBody(String type)");
        }

        return new StoreMessageResult(responseBody);
    }

    private enum Type {
        COMPLETE,
        INCOMPLETE
    }
}