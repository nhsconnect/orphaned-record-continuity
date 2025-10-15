package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.prm.repo.suspension.service.pds.IntermittentErrorPdsException;

import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuspensionMessageProcessor {
    private static final Class RETRYABLE_EXCEPTION_CLASS = IntermittentErrorPdsException.class;

    @Value("${suspension.processor.retry.max.attempts}")
    private int maxAttempts;

    @Value("${suspension.processor.initial.interval.millisecond}")
    private int initialIntervalMillis;

    @Value("${suspension.processor.initial.interval.multiplier}")
    private double multiplier;

    final private MessageProcessExecution messageProcessExecution;

    public void process(String message) {
        Function<String, Void> retryableProcessEvent = Retry
                .decorateFunction(Retry.of("retryableSuspension", createRetryConfig()), this::processOnce);
        retryableProcessEvent.apply(message);
    }

    private Void processOnce(String message) {
        try {
            messageProcessExecution.run(message);
        }
        catch (Exception e) {
            if (RETRYABLE_EXCEPTION_CLASS.isInstance(e)) {
                log.info("Caught retryable exception in SuspensionMessageProcessor.processOnce", e);
            }
            else {
                log.error("Uncaught exception in SuspensionMessageProcessor.processOnce", e);
            }
            throw e;
        }
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
