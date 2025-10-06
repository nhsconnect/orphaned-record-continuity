package uk.nhs.prm.repo.ehrtransferservice.database.model;

import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public record ConversationRecord(
    UUID inboundConversationId,
    Optional<UUID> outboundConversationId,
    String nhsNumber,
    String sourceGp,
    Optional<String> destinationGp,
    ConversationTransferStatus transferStatus,
    Optional<String> failureCode,
    Optional<UUID> nemsMessageId,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt,
    Optional<Instant> deletedAt
) { }