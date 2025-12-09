package uk.nhs.prm.repo.ehrtransferservice.utils;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageParsingUtility {
    private static final String MESSAGE_ID_REGEX = "<eb:MessageId>([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})</eb:MessageId>";

    private MessageParsingUtility() { }

    public static Optional<UUID> getMessageId(String message) {
        final Pattern pattern = Pattern.compile(MESSAGE_ID_REGEX);
        final Matcher matcher = pattern.matcher(message);

        if(matcher.find()) {
            return Optional.of(UUID.fromString(matcher.group(1)));
        }

        return Optional.empty();
    }
}