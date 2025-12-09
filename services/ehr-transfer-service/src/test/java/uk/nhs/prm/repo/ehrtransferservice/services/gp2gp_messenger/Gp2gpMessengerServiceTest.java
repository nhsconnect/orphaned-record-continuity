package uk.nhs.prm.repo.ehrtransferservice.services.gp2gp_messenger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.nhs.prm.repo.ehrtransferservice.database.TransferService;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.HttpException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.acknowledgement.EhrCompleteAcknowledgementFailedException;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerContinueMessageRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerEhrRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.Gp2gpMessengerPositiveAcknowledgementRequestBody;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.ParsedMessage;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.ehrtransferservice.utils.TestDataUtility.*;

@ExtendWith(MockitoExtension.class)
class Gp2gpMessengerServiceTest {
    @Mock
    private Gp2gpMessengerClient gp2gpMessengerClient;

    @Mock
    private TransferService transferService;

    @Mock
    private ParsedMessage parsedMessage;

    @Mock
    private ConversationRecord conversationRecord;

    @Captor
    private ArgumentCaptor<Gp2gpMessengerEhrRequestBody> gp2gpMessengerEhrRequestBodyArgumentCaptor;

    @Captor
    private ArgumentCaptor<Gp2gpMessengerContinueMessageRequestBody> gp2gpMessengerContinueMessageRequestBodyArgumentCaptor;

    @Captor
    private ArgumentCaptor<Gp2gpMessengerPositiveAcknowledgementRequestBody> gp2gpMessengerPositiveAcknowledgementRequestBodyArgumentCaptor;

    @InjectMocks
    private Gp2gpMessengerService gp2gpMessengerService;

    private static final UUID INBOUND_CONVERSATION_ID = UUID.fromString("9ac92880-bcff-4ed2-ac42-d944b783d90f");
    private static final UUID INBOUND_MESSAGE_ID = UUID.fromString("4fa2b27b-dc5b-4779-8e2c-c2c3a816d9b4");
    private static final String SOURCE_GP = "B14758";
    private static final String NHS_NUMBER = "9798547854";

    @Test
    void sendEhrRequest_ValidRepoIncomingEvent_SendGp2gpMessengerEhrRequest() throws Exception {
        // given
        final RepoIncomingEvent event = createRepoIncomingEvent(INBOUND_CONVERSATION_ID);

        // when
        ReflectionTestUtils.setField(gp2gpMessengerService, "repositoryOdsCode", REPOSITORY_ODS_CODE);
        ReflectionTestUtils.setField(gp2gpMessengerService, "repositoryAsid", REPOSITORY_ASID);
        gp2gpMessengerService.sendEhrRequest(event);

        // then
        verify(gp2gpMessengerClient).sendGp2gpMessengerEhrRequest(
            eq(event.getNhsNumber()),
            gp2gpMessengerEhrRequestBodyArgumentCaptor.capture()
        );
    }

    @Test
    void sendEhrRequest_SendGp2gpMessengerEhrRequestThrowsHttpException_ThrowNewException() throws HttpException, URISyntaxException, IOException, InterruptedException {
        // given
        final RepoIncomingEvent event = createRepoIncomingEvent(INBOUND_CONVERSATION_ID);
        final Exception exception = new HttpException();

        // when
        ReflectionTestUtils.setField(gp2gpMessengerService, "repositoryOdsCode", REPOSITORY_ODS_CODE);
        ReflectionTestUtils.setField(gp2gpMessengerService, "repositoryAsid", REPOSITORY_ASID);
        doThrow(exception)
            .when(gp2gpMessengerClient)
            .sendGp2gpMessengerEhrRequest(any(String.class), gp2gpMessengerEhrRequestBodyArgumentCaptor.capture());

        // then
        assertThrows(Exception.class, () -> gp2gpMessengerService.sendEhrRequest(event));
    }

    @Test
    void sendContinueMessage_ValidParsedMessageAndSourceGp_SendContinueMessage() throws HttpException, IOException, URISyntaxException, InterruptedException {
        // when
        when(parsedMessage.getConversationId()).thenReturn(INBOUND_CONVERSATION_ID);
        when(parsedMessage.getMessageId()).thenReturn(INBOUND_MESSAGE_ID);

        gp2gpMessengerService.sendContinueMessage(parsedMessage, SOURCE_GP);

        // then
        verify(gp2gpMessengerClient)
            .sendContinueMessage(gp2gpMessengerContinueMessageRequestBodyArgumentCaptor.capture());
    }

    @Test
    void sendEhrCompletePositiveAcknowledgement_ExistingInboundConversationId_SendGp2gpMessengerPositiveAcknowledgement() throws HttpException, IOException, URISyntaxException, InterruptedException {
        // when
        when(transferService.getConversationByInboundConversationId(INBOUND_CONVERSATION_ID))
            .thenReturn(conversationRecord);
        when(transferService.getEhrCoreInboundMessageIdForInboundConversationId(INBOUND_CONVERSATION_ID))
            .thenReturn(INBOUND_MESSAGE_ID);
        when(conversationRecord.sourceGp()).thenReturn(SOURCE_GP);
        when(conversationRecord.nhsNumber()).thenReturn(NHS_NUMBER);

        gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID);

        // then
        verify(gp2gpMessengerClient).sendGp2gpMessengerAcknowledgement(
            eq(NHS_NUMBER),
            gp2gpMessengerPositiveAcknowledgementRequestBodyArgumentCaptor.capture()
        );
    }

    @Test
    void sendEhrCompletePositiveAcknowledgement_SendGp2gpMessengerPositiveAcknowledgementThrowsHttpException_ThrowNewEhrCompleteAcknowledgementFailedException() throws HttpException, IOException, URISyntaxException, InterruptedException {
        // given
        final Exception exception = new HttpException();

        // when
        ReflectionTestUtils.setField(gp2gpMessengerService, "repositoryOdsCode", REPOSITORY_ODS_CODE);
        ReflectionTestUtils.setField(gp2gpMessengerService, "repositoryAsid", REPOSITORY_ASID);
        when(transferService.getConversationByInboundConversationId(INBOUND_CONVERSATION_ID))
            .thenReturn(conversationRecord);
        when(transferService.getEhrCoreInboundMessageIdForInboundConversationId(INBOUND_CONVERSATION_ID))
            .thenReturn(INBOUND_MESSAGE_ID);
        when(conversationRecord.sourceGp()).thenReturn(SOURCE_GP);
        when(conversationRecord.nhsNumber()).thenReturn(NHS_NUMBER);
        doThrow(exception)
            .when(gp2gpMessengerClient)
            .sendGp2gpMessengerAcknowledgement(
                any(String.class),
                gp2gpMessengerPositiveAcknowledgementRequestBodyArgumentCaptor.capture()
            );

        // then
        assertThrows(EhrCompleteAcknowledgementFailedException.class,
            () -> gp2gpMessengerService.sendEhrCompletePositiveAcknowledgement(INBOUND_CONVERSATION_ID));
    }
}