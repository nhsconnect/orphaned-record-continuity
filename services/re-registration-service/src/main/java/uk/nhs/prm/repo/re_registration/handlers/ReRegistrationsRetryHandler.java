package uk.nhs.prm.repo.re_registration.handlers;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.prm.repo.re_registration.pds.IntermittentErrorPdsException;

import java.util.function.Function;

@Slf4j
@Component
public class ReRegistrationsRetryHandler {

    private static final Class RETRYABLE_EXCEPTION_CLASS = IntermittentErrorPdsException.class;

    private final ReRegistrationsHandler reRegistrationsHandler;
    private final int maxAttempts;
    private final int initialIntervalMillis;
    private final double multiplier;

    public ReRegistrationsRetryHandler(ReRegistrationsHandler reRegistrationsHandler,
                                       @Value("${pdsIntermittentError.retry.max.attempts}")
                                               int maxAttempts,
                                       @Value("${pdsIntermittentError.initial.interval.millisecond}")
                                               int initialIntervalMillis,
                                       @Value("${pdsIntermittentError.initial.interval.multiplier}")
                                               double multiplier) {
        this.reRegistrationsHandler = reRegistrationsHandler;
        this.maxAttempts = maxAttempts;
        this.initialIntervalMillis = initialIntervalMillis;
        this.multiplier = multiplier;
    }

    public void handle(String message) {
        Function<String, Void> retryableProcessEvent = Retry
                .decorateFunction(Retry.of("retryablePdsIntermittentError", createRetryConfig()), this::processOnce);
        retryableProcessEvent.apply(message);
    }

    public Void processOnce(String payload) {
        reRegistrationsHandler.process(payload);
        return null;
    }

    private RetryConfig createRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialIntervalMillis, multiplier))
                .retryExceptions(RETRYABLE_EXCEPTION_CLASS)
                .build();
    }
}