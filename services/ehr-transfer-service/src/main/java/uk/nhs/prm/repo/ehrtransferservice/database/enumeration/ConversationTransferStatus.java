package uk.nhs.prm.repo.ehrtransferservice.database.enumeration;

public enum ConversationTransferStatus {
    INBOUND_STARTED(false, true),
    INBOUND_REQUEST_SENT(false, true),
    INBOUND_CORE_RECEIVED(false, true),
    INBOUND_CONTINUE_REQUEST_SENT(false, true),
    INBOUND_COMPLETE(true, false),
    INBOUND_FAILED(true, false),
    INBOUND_TIMEOUT(true, true),

    OUTBOUND_PENDING(true, false),
    OUTBOUND_STARTED(true, false),
    OUTBOUND_SENT_CORE(true, false),
    OUTBOUND_CONTINUE_REQUEST_RECEIVED(true, false),
    OUTBOUND_FRAGMENTS_SENDING_FAILED(true, false),
    OUTBOUND_SENT_FRAGMENTS(true, false),
    OUTBOUND_COMPLETE(true, false),
    OUTBOUND_FAILED(true, false);

    public final boolean isInboundTerminating;
    public final boolean isInboundRetryable;

    ConversationTransferStatus(boolean isInboundTerminating, boolean isInboundRetryable) {
        this.isInboundTerminating = isInboundTerminating;
        this.isInboundRetryable = isInboundRetryable;
    }
}
