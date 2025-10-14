package uk.nhs.prm.repo.re_registration.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestLogAppender extends AppenderBase<ILoggingEvent> {
    ArrayList<ILoggingEvent> loggingEvents = new ArrayList<>();

    public static TestLogAppender addTestLogAppender() {
        var testLogAppender = new TestLogAppender();
        var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(testLogAppender);

        testLogAppender.start();
        return testLogAppender;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        loggingEvents.add(eventObject);
    }

    public ILoggingEvent findLoggedEvent(String subString) {
        for (ILoggingEvent event : loggingEvents) {
            System.out.println("logged event message: " + event.getMessage());
            if (event.getMessage().contains(subString)) {
                return event;
            }
        }
        return null;
    }

    public List<ILoggingEvent> allLogs() {
        return loggingEvents;
    }
}
