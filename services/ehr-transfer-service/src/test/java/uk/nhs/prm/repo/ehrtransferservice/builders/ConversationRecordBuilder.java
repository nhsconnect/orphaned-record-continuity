package uk.nhs.prm.repo.ehrtransferservice.builders;

import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_STARTED;
import static uk.nhs.prm.repo.ehrtransferservice.utility.DateUtility.ZONE_ID;

public class ConversationRecordBuilder {

    private UUID inboundConversationId;

    private Optional<UUID> outboundConversationId;

    private String nhsNumber;

    private String sourceGp;

    private Optional<String> destinationGp;

    private ConversationTransferStatus transferStatus;

    private Optional<String> failureCode;

    private Optional<UUID> nemsMessageId;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    private Optional<Instant> deletedAt;

    public ConversationRecordBuilder withDefaults() {
        inboundConversationId = UUID.fromString("9b1bf808-353f-4459-a325-2ded5c576e63");
        outboundConversationId = Optional.empty();
        nhsNumber = "1234567890";
        sourceGp = "ODSCODE";
        destinationGp = Optional.empty();
        transferStatus = INBOUND_STARTED;
        failureCode = Optional.empty();
        nemsMessageId = Optional.of(UUID.fromString("d221cbca-7e76-4eac-83b6-98dcc615dd60"));
        createdAt = ZonedDateTime.of(
            LocalDateTime.of(2024,1, 1, 1, 1, 1),
            ZoneId.of(ZONE_ID)
        );
        updatedAt = ZonedDateTime.of(
            LocalDateTime.of(2024,1, 1, 1, 1, 1),
            ZoneId.of(ZONE_ID)
        );
        deletedAt = Optional.empty();
        return this;
    }

    public ConversationRecordBuilder withInboundConversationId(UUID inboundConversationId) {
        this.inboundConversationId = inboundConversationId;
        return this;
    }

    public ConversationRecordBuilder withTransferStatus(ConversationTransferStatus transferStatus) {
        this.transferStatus = transferStatus;
        return this;
    }

    public ConversationRecord build() {
        return new ConversationRecord(
            inboundConversationId,
            outboundConversationId,
            nhsNumber,
            sourceGp,
            destinationGp,
            transferStatus,
            failureCode,
            nemsMessageId,
            createdAt,
            updatedAt,
            deletedAt
        );
    }
}
