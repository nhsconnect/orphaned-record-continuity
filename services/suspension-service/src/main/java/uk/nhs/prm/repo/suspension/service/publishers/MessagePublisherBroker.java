package uk.nhs.prm.repo.suspension.service.publishers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.suspension.service.config.Tracer;
import uk.nhs.prm.repo.suspension.service.model.*;
import uk.nhs.prm.repo.suspension.service.suspensionsevents.SuspensionEvent;

@Component
@RequiredArgsConstructor
public class MessagePublisherBroker {
    public final NotSuspendedEventPublisher notSuspendedEventPublisher;
    public final MofUpdatedEventPublisher mofUpdatedEventPublisher;
    public final MofNotUpdatedEventPublisher mofNotUpdatedEventPublisher;
    public final InvalidSuspensionPublisher invalidSuspensionPublisher;
    public final EventOutOfOrderPublisher eventOutOfOrderPublisher;
    public final DeceasedPatientEventPublisher deceasedPatientEventPublisher;
    public final RepoIncomingEventPublisher repoIncomingEventPublisher;
    public final ActiveSuspensionsEventPublisher activeSuspensionsEventPublisher;
    public final Tracer tracer;

    public void notSuspendedMessage(String nemsMessageId) {
        var notSuspendedMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:NO_LONGER_SUSPENDED_ON_PDS");
        notSuspendedEventPublisher.sendMessage(notSuspendedMessage);
    }

    public void notSyntheticMessage(String nemsMessageId) {
        var notSyntheticMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:NOT_SYNTHETIC");
        mofNotUpdatedEventPublisher.sendMessage(notSyntheticMessage);
    }

    public void odsCodeNotSafeListedMessage(String nemsMessageId) {
        var notSyntheticMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:ODS_CODE_NOT_SAFE_LISTED");
        mofNotUpdatedEventPublisher.sendMessage(notSyntheticMessage);
    }

    public void deceasedPatientMessage(String nemsMessageId) {
        var deceasedPatientMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:DECEASED_PATIENT");
        deceasedPatientEventPublisher.sendMessage(deceasedPatientMessage);
    }

    public void eventOutOfOrderMessage(String nemsMessageId) {
        var eventOutOfOrderMessage = new NonSensitiveDataMessage(nemsMessageId, "NO_ACTION:EVENT_PROCESSED_OUT_OF_ORDER");
        eventOutOfOrderPublisher.sendMessage(eventOutOfOrderMessage);
    }

    public void invalidMessage(String sensitiveMessage, String nemsMessageId) {
        invalidSuspensionPublisher.sendInvalidMessageAndAuditMessage(sensitiveMessage, nemsMessageId);
    }

    public void mofNotUpdatedMessage(String nemsMessageId, boolean toRepoOdsCode) {
        var status = toRepoOdsCode ? "NO_ACTION:MOF_SAME_AS_REPO" : "NO_ACTION:MOF_SAME_AS_PREVIOUS_GP";
        var mofSameAsPreviousGp = new NonSensitiveDataMessage(nemsMessageId, status);
        mofNotUpdatedEventPublisher.sendMessage(mofSameAsPreviousGp);
    }

    public void mofUpdatedMessage(String nemsMessageId, String previousOdsCode, boolean isSuperseded) {
        var status = isSuperseded ? "ACTION:UPDATED_MANAGING_ORGANISATION_FOR_SUPERSEDED_PATIENT" : "ACTION:UPDATED_MANAGING_ORGANISATION";
        var mofUpdatedMessage = new ManagingOrganisationUpdatedMessage(nemsMessageId, previousOdsCode, status);
        mofUpdatedEventPublisher.sendMessage(mofUpdatedMessage);
    }

    public void activeSuspensionMessage(SuspensionEvent suspensionEvent) {
        var activeSuspensionsMessage = new ActiveSuspensionsMessage(suspensionEvent);
        activeSuspensionsEventPublisher.sendMessage(activeSuspensionsMessage, suspensionEvent.getNemsMessageId());
    }

    public void repoIncomingMessage(PdsAdaptorSuspensionStatusResponse pdsAdaptorSuspensionStatusResponse, SuspensionEvent suspensionEvent) {
        var repoIncomingEvent = new RepoIncomingEvent(pdsAdaptorSuspensionStatusResponse, suspensionEvent, tracer.getTraceId());
        repoIncomingEventPublisher.sendMessage(repoIncomingEvent);
    }

}