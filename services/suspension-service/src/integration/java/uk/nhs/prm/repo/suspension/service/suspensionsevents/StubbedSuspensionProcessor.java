package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import java.time.LocalTime;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.time.LocalTime.now;

public class StubbedSuspensionProcessor  {

    private ConcurrentLinkedQueue processedMessages = new ConcurrentLinkedQueue();


    public void process(String message) {
        processedMessages.add(message);
        if (message.startsWith("throw")) {
            System.out.println("throwing from stub for: " + message);
            throw new RuntimeException("boom from " + getClass());
        }
        System.out.println("processed normally in stub: " + message);
    }

    public void waitUntilProcessed(String message, int timeoutSeconds) {
        LocalTime timeoutTime = now().plusSeconds(timeoutSeconds);
        do {
            if (now().isAfter(timeoutTime)) {
                throw new RuntimeException("Did not process message before timeout in " + getClass());
            }
            System.out.println("waiting process");
            waitABit();
        } while (!isProcessed(message));
        waitABit();
    }

    private boolean isProcessed(String message) {
        return processedMessages.contains(message);
    }

    private void waitABit() {
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            // nop
        }
    }
}
