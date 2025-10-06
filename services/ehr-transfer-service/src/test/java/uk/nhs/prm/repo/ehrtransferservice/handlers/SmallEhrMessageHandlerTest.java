package uk.nhs.prm.repo.ehrtransferservice.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement.EhrCompleteAcknowledgementFailedException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.base.AcknowledgementException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.EhrRepoService;
import uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo.StoreMessageResult;
import uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger.Gp2gpMessengerService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmallEhrMessageHandlerTest {
    @Mock
    EhrRepoService ehrRepoService;

    @Mock
    ConversationActivityService conversationActivityService;

    @Mock
    Gp2gpMessengerService gp2gpMessengerService;

    @Mock
    ParsedMessage parsedMessage;

    @Mock
    StoreMessageResult storeMessageResult;

    @InjectMocks
    SmallEhrMessageHandler smallEhrMessageHandler;

    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("3f77be78-4b48-4409-bbed-370a5cf4e35f");

    @Test
    void handleMessage_EhrComplete_StoreMessageAndAcknowledgementSent() throws Exception {
        // when
        when(ehrRepoService.storeMessage(parsedMessage))
            .thenReturn(storeMessageResult);
        when(parsedMessage.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);
        when(storeMessageResult.isEhrComplete())
            .thenReturn(true);
        doNothing()
            .when(conversationActivityService)
            .concludeConversationActivity(INBOUND_CONVERSATION_ID);

        smallEhrMessageHandler.handleMessage(parsedMessage);

        // then
        verify(ehrRepoService).storeMessage(parsedMessage);
        verify(conversationActivityService).concludeConversationActivity(INBOUND_CONVERSATION_ID);
        verify(gp2gpMessengerService).sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);
    }

    @Test
    void handleMessage_EhrNotComplete_NoAcknowledgementSent() throws Exception {
        // when
        when(ehrRepoService.storeMessage(parsedMessage))
            .thenReturn(storeMessageResult);
        when(storeMessageResult.isEhrComplete())
            .thenReturn(false);

        smallEhrMessageHandler.handleMessage(parsedMessage);

        // then
        verify(ehrRepoService).storeMessage(parsedMessage);
        verify(conversationActivityService, never()).concludeConversationActivity(any(UUID.class));
        verify(gp2gpMessengerService, never()).sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);
    }

    @Test
    void handleMessage_ExceptionThrownWhileStoringMessage_ExceptionPropagated() throws Exception {
        // when
        when(ehrRepoService.storeMessage(parsedMessage))
            .thenThrow(Exception.class);

        // then
        assertThrows(Exception.class, () -> smallEhrMessageHandler.handleMessage(parsedMessage));
        verify(storeMessageResult, never()).isEhrComplete();
        verify(conversationActivityService, never()).concludeConversationActivity(any(UUID.class));
        verify(gp2gpMessengerService, never()).sendEhrCompletePositiveAcknowledgement(any(UUID.class));
        verify(parsedMessage, never()).getConversationId();
    }

    @Test
    void handleMessage_ExceptionThrownWhileSendingAcknowledgement_ExceptionPropagated() throws Exception {
        // given
        final AcknowledgementException exception =
            new EhrCompleteAcknowledgementFailedException(INBOUND_CONVERSATION_ID, new Throwable());
        final String exceptionMessage =
            "Failed to send an EHR Complete Acknowledgement for Inbound Conversation ID %s";

        // when
        when(ehrRepoService.storeMessage(parsedMessage))
            .thenReturn(storeMessageResult);
        when(parsedMessage.getConversationId())
            .thenReturn(INBOUND_CONVERSATION_ID);
        when(storeMessageResult.isEhrComplete())
            .thenReturn(true);
        doNothing()
            .when(conversationActivityService)
            .concludeConversationActivity(INBOUND_CONVERSATION_ID);
        doThrow(exception)
            .when(gp2gpMessengerService)
            .sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);

        final AcknowledgementException thrown =
            assertThrows(AcknowledgementException.class, () -> smallEhrMessageHandler.handleMessage(parsedMessage));

        // then
        assertEquals(thrown.getMessage(), exceptionMessage.formatted(INBOUND_CONVERSATION_ID.toString().toUpperCase()));
    }
}