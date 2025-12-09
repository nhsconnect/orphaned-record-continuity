package uk.nhs.prm.repo.ehrtransferservice.utility;

import java.util.UUID;

public final class UuidUtility {
    private UuidUtility() {}

    /**
     * ParsedMessage getters can return null, wrapping as optional to avoid NullPointerException
     * @param id the UUID to uppercase
     * @return UUID as uppercased string
     */
    public static String getUuidAsUpperCasedStringIfNotNull(UUID id) {
        return id != null
                ? id.toString().toUpperCase()
                : null;
    }
}
