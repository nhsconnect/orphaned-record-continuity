package uk.nhs.prm.repo.re_registration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.NHS_NUMBER;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.UNESCAPED_HTML;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.getRandomOdsCode;
import static uk.nhs.prm.repo.re_registration.utility.TestDataUtility.getRandomTimestamp;

class ReRegistrationEventTest {
    @Test
    void Given_ReRegistrationEventContainingUnescapedHtml_When_ToJsonStringIsCalled_Then_ReturnValueShouldNotContainUnescapedHtml() {
        // Given
        final String timestamp = getRandomTimestamp();
        final String odsCode = getRandomOdsCode();
        final ReRegistrationEvent reRegistrationEvent = new ReRegistrationEvent(
                NHS_NUMBER,
                odsCode,
                UNESCAPED_HTML,
                timestamp
        );

        // When
        final String json = reRegistrationEvent.toJsonString();

        // Then
        assertFalse(json.contains(UNESCAPED_HTML));
    }
}