package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.NHS_NUMBER;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.UNESCAPED_HTML;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.getRandomTimestamp;

class ActiveSuspensionsMessageTest {
    @Test
    void Given_ActiveSuspensionsMessageContainingUnescapedHtml_When_ToJsonStringIsCalled_Then_ReturnValueShouldNotContainUnescapedHtml() {
        // Given
        final String timestamp = getRandomTimestamp();
        final ActiveSuspensionsMessage activeSuspensionsMessage = new ActiveSuspensionsMessage(
                NHS_NUMBER,
                UNESCAPED_HTML,
                timestamp
        );

        // When
        final String json = activeSuspensionsMessage.toJsonString();

        // Then
        assertFalse(json.contains(UNESCAPED_HTML));
    }
}