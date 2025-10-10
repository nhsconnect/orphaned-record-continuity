package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import java.time.LocalTime;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.time.LocalTime.now;

public class StubbedNemsEventHandler implements NemsEventHandler {

    private ConcurrentLinkedQueue processedMessages = new ConcurrentLinkedQueue();

    public StubbedNemsEventHandler() {
        System.out.println("stub nems event handler created");
    }

    @Override
    public void processNemsEvent(String message) {
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
