package uk.nhs.prm.repo.ehrtransferservice.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(locations = "classpath:application.properties")
@ExtendWith(MockitoExtension.class)
class ConversationActivityServiceTest {
    private static final UUID INBOUND_CONVERSATION_ID = UUID.randomUUID();

    @InjectMocks
    private final ConversationActivityService activityService;

    public ConversationActivityServiceTest() {
        this.activityService = new ConversationActivityService();
    }

    @AfterEach
    void afterEach() {
        activityService.concludeConversationActivity(INBOUND_CONVERSATION_ID);
    }

    @Test
    void captureConversationActivity_ValidInboundConversationId_Ok() {
        // when
        activityService.captureConversationActivity(INBOUND_CONVERSATION_ID);

        // then
        assertTrue(activityService.isConversationActive(INBOUND_CONVERSATION_ID));
    }

    @Test
    void concludeConversationActivity_ValidInboundConversationId_Ok() {
        // when
        activityService.captureConversationActivity(INBOUND_CONVERSATION_ID);
        activityService.concludeConversationActivity(INBOUND_CONVERSATION_ID);

        // then
        assertFalse(activityService.isConversationActive(INBOUND_CONVERSATION_ID));
    }

    @Test
    void isConversationActive_ActiveInboundConversationId_ReturnTrue() {
        // when
        activityService.captureConversationActivity(INBOUND_CONVERSATION_ID);
        final boolean result = activityService.isConversationActive(INBOUND_CONVERSATION_ID);

        // then
        assertTrue(result);
    }

    @Test
    void isConversationActive_InactiveInboundConversationId_ReturnFalse() {
        // when
        final boolean result = activityService.isConversationActive(INBOUND_CONVERSATION_ID);

        // then
        assertFalse(result);
    }

    @Test
    void isConversationTimedOut_ActiveInboundConversationId_ReturnTrue() throws NoSuchFieldException, IllegalAccessException {
        // given
        configureTimeout(5);

        // when
        activityService.captureConversationActivity(INBOUND_CONVERSATION_ID);

        // then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            final boolean result = activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID);
            assertTrue(result);
        });
    }

    @Test
    void isConversationTimedOut_ActiveInboundConversationId_ReturnFalse() throws NoSuchFieldException, IllegalAccessException {
        // given
        configureTimeout(5);

        // when
        activityService.captureConversationActivity(INBOUND_CONVERSATION_ID);
        final boolean result = activityService.isConversationTimedOut(INBOUND_CONVERSATION_ID);

        // then
        assertFalse(result);
    }

    private void configureTimeout(int timeoutSeconds) throws NoSuchFieldException, IllegalAccessException {
        final Field inboundTimeoutSecondsField = ConversationActivityService.class.getDeclaredField("inboundTimeoutSeconds");
        inboundTimeoutSecondsField.setAccessible(true);
        inboundTimeoutSecondsField.set(this.activityService, timeoutSeconds);
    }
}