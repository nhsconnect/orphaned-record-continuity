package uk.nhs.prm.repo.re_registration.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.prm.repo.re_registration.model.ReRegistrationEvent;
import uk.nhs.prm.repo.re_registration.pds.IntermittentErrorPdsException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReRegistrationsRetryHandlerTest {

    @Mock
    private ReRegistrationsHandler reRegistrationsHandler;

    private ReRegistrationsRetryHandler retryHandler;


    @BeforeEach
    public void setUp() {
        retryHandler = new ReRegistrationsRetryHandler(reRegistrationsHandler, 3, 1, 2.0);
    }

    @Test
    public void shouldRetryUpToThreeTimesWhenIntermittentErrorPdsExceptionIsThrown() {
        doThrow(IntermittentErrorPdsException.class).when(reRegistrationsHandler).process(any());
        assertThrows(IntermittentErrorPdsException.class, () -> retryHandler.handle(getParsedMessage().toJsonString()));
        verify(reRegistrationsHandler, times(3)).process(any());
    }

    private ReRegistrationEvent getParsedMessage() {
        return new ReRegistrationEvent("1234567890", "ABC123", UUID.randomUUID().toString(), "2017-11-01T15:00:33+00:00");
    }
}