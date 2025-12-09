package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.ehrtransferservice.activemq.ForceXercesParserExtension;
import uk.nhs.prm.repo.ehrtransferservice.configuration.LocalStackAwsConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.ConversationIneligibleForRetryException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.QueryReturnedNoItemsException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;
import uk.nhs.prm.repo.ehrtransferservice.services.ConversationActivityService;
import uk.nhs.prm.repo.ehrtransferservice.utils.TransferTrackerDbUtility;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalStackAwsConfig.class })
@ExtendWith(ForceXercesParserExtension.class)
public class TransferServiceTest {
    @Autowired
    TransferService transferService;

    @Autowired
    TransferTrackerDbUtility transferTrackerDbUtility;

    @Autowired
    ConversationActivityService conversationActivityService;

    @Autowired
    private TransferRepository transferRepository;

    private static final String NHS_NUMBER = "9798547485";
    private static final String SOURCE_GP = "B45744";
    private static final String NEMS_MESSAGE_ID = "2d74a113-1076-4c63-91bc-e50d232b6a79";
    private static final String EHR_CORE_MESSAGE_ID = "13CD1199-4B3A-44DC-9A60-6ABCC22B8A44";

    @Test
    void createConversation_ValidNewConversationOrResetForRetryRequest_Ok() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);

        // when
        try {
            transferService.createConversationOrResetForRetry(repoIncomingEvent);
        } catch (ConversationIneligibleForRetryException e) {
            fail("Conversation should be new and eligible.");
        }

        ConversationRecord record = transferService
                .getConversationByInboundConversationId(inboundConversationId);

        String nemsMessageIdResult = record.nemsMessageId()
                .orElseThrow()
                .toString();

        // then
        assertEquals(record.inboundConversationId().toString(), inboundConversationId.toString());
        assertEquals(record.nhsNumber(), NHS_NUMBER);
        assertEquals(record.sourceGp(), SOURCE_GP);
        assertEquals(record.transferStatus(), INBOUND_STARTED);
        assertEquals(record.failureCode(), Optional.empty());
        assertEquals(nemsMessageIdResult, NEMS_MESSAGE_ID);
        assertNotNull(record.createdAt());
        assertNotNull(record.updatedAt());
    }

    @Test
    void createConversation_ValidRetriedConversationOrResetForRetryRequest_Ok() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent repoIncomingEvent = createRepoIncomingEvent(inboundConversationId);
        // create an already existing conversation that we can retry
        transferRepository.createConversation(repoIncomingEvent);

        // when
        try {
            transferService.createConversationOrResetForRetry(repoIncomingEvent);
        } catch (ConversationIneligibleForRetryException e) {
            fail("Conversation should be eligible for retry");
        }

        ConversationRecord record = transferService
                .getConversationByInboundConversationId(inboundConversationId);

        String nemsMessageIdResult = record.nemsMessageId()
                .orElseThrow()
                .toString();

        // then
        assertEquals(record.inboundConversationId().toString(), inboundConversationId.toString());
        assertEquals(record.nhsNumber(), NHS_NUMBER);
        assertEquals(record.sourceGp(), SOURCE_GP);
        assertEquals(record.transferStatus(), INBOUND_STARTED);
        assertEquals(record.failureCode(), Optional.empty());
        assertEquals(nemsMessageIdResult, NEMS_MESSAGE_ID);
        assertNotNull(record.createdAt());
        assertNotNull(record.updatedAt());
    }

    @Test
    void isInboundConversationPresent_ValidInboundConversationId_ShouldReturnTrue() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);

        // when
        try {
            transferService.createConversationOrResetForRetry(event);
        } catch (ConversationIneligibleForRetryException e) {
            fail("Conversation should be new and eligible.");
        }

        boolean isConversationPresent = transferService
            .isInboundConversationPresent(inboundConversationId);

        // then
        assertTrue(isConversationPresent);
    }

    @Test
    void isInboundConversationPresent_NonExistingInboundConversationId_ShouldReturnFalse() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        boolean isConversationPresent = transferService
            .isInboundConversationPresent(inboundConversationId);

        // then
        assertFalse(isConversationPresent);
    }

    @Test
    void getConversationByInboundConversationId_NonExistingInboundConversationId_ShouldThrowConversationNotPresentException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        assertThrows(ConversationNotPresentException.class,
            () -> transferService.getConversationByInboundConversationId(inboundConversationId));
    }

    @Test
    void updateConversationTransferStatus_ValidInboundConversationIdAndConversationTransferStatus_ShouldUpdateStatus() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);

        try {
            transferService.createConversationOrResetForRetry(event);
        } catch (ConversationIneligibleForRetryException e) {
            fail("Conversation should be new and eligible.");
        }

        // when
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_COMPLETE);
        ConversationRecord record = transferService
            .getConversationByInboundConversationId(inboundConversationId);

        // then
        assertEquals(record.transferStatus(), INBOUND_COMPLETE);
    }

    @Test
    void updateConversationTransferStatus_ConversationIsAlreadyComplete_ShouldNotUpdateTransferStatus() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);

        try {
            transferService.createConversationOrResetForRetry(event);
        } catch (ConversationIneligibleForRetryException e) {
            fail("Conversation should be new and eligible.");
        }

        // when
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_COMPLETE);

        // then
        transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_CONTINUE_REQUEST_SENT);

        ConversationRecord record = transferService
                .getConversationByInboundConversationId(inboundConversationId);

        assertEquals(record.transferStatus(), INBOUND_COMPLETE);
    }

    @Test
    void updateConversationTransferStatus_NonExistingInboundConversationIdAndExistingConversationTransferStatus_ShouldThrowConversationNotPresentException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // then
        assertThrows(ConversationNotPresentException.class, () ->
                transferService.updateConversationTransferStatus(inboundConversationId, INBOUND_FAILED));
    }

    @Test
    void updateConversationTransferStatusWithFailure_ValidInboundConversationIdAndFailureCode_ShouldUpdateFailureCode() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final RepoIncomingEvent event = createRepoIncomingEvent(inboundConversationId);
        final String failureCode = "19";

        try {
            transferService.createConversationOrResetForRetry(event);
        } catch (ConversationIneligibleForRetryException e) {
            fail("Conversation should be new and eligible.");
        }

        // when
        transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode);
        final ConversationRecord record = transferService
            .getConversationByInboundConversationId(inboundConversationId);

        final String failureCodeResult = record.failureCode().orElseThrow();

        // then
        assertEquals(record.transferStatus(), INBOUND_FAILED);
        assertEquals(failureCodeResult, failureCode);
    }

    @Test
    void updateConversationTransferStatusWithFailure_NonExistingInboundConversationIdAndFailureCode_ShouldThrowConversationNotPresentException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final String failureCode = "19";

        // when
        assertThrows(ConversationNotPresentException.class, () ->
            transferService.updateConversationTransferStatusWithFailure(inboundConversationId, failureCode));
    }

    @Test
    void getEhrCoreInboundMessageIdForInboundConversationId_ValidInboundConversationId_ShouldReturnMessageId() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();
        final UUID ehrCoreMessageId = UUID.fromString(EHR_CORE_MESSAGE_ID);

        // when
        transferTrackerDbUtility.createCore(inboundConversationId, ehrCoreMessageId);

        final UUID result =
            transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId);

        // then
        assertEquals(result, ehrCoreMessageId);
    }

    @Test
    void getEhrCoreInboundMessageIdForInboundConversationId_NonExistingInboundConversationId_ShouldThrowQueryReturnedNoItemsException() {
        // given
        final UUID inboundConversationId = UUID.randomUUID();

        // when
        assertThrows(QueryReturnedNoItemsException.class,
            () -> transferService.getEhrCoreInboundMessageIdForInboundConversationId(inboundConversationId));
    }

    // Helper Methods
    private RepoIncomingEvent createRepoIncomingEvent(UUID inboundConversationId) {
        return RepoIncomingEvent.builder()
            .nhsNumber(NHS_NUMBER)
            .sourceGp(SOURCE_GP)
            .nemsMessageId(NEMS_MESSAGE_ID)
            .conversationId(inboundConversationId.toString().toUpperCase())
            .build();
    }
}