package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.UNESCAPED_HTML;

class AuditMessageTest {
    @Test
    void Given_AuditMessageContainingUnescapedHtml_When_ToJsonStringIsCalled_Then_ReturnValueShouldNotContainUnescapedHtml() {
        // Given
        final String nemsMessageId = UUID.randomUUID().toString();
        final AuditMessage auditMessage = new AuditMessage(
                nemsMessageId,
                UNESCAPED_HTML
        );

        // When
        final String json = auditMessage.toJsonString();

        // Then
        assertFalse(json.contains(UNESCAPED_HTML));
    }
}