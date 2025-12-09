package uk.nhs.prm.repo.suspension.service.pds;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnexpectedPdsAdaptorResponseExceptionTest {

    @Test
    public void shouldExposeExceptionMessage() {
        assertThat(new UnexpectedPdsAdaptorResponseException("cheese").getMessage()).isEqualTo("cheese");
    }
}