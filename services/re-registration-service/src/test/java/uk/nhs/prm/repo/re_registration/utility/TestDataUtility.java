package uk.nhs.prm.repo.re_registration.utility;


import org.joda.time.DateTime;
import wiremock.org.apache.commons.lang3.RandomStringUtils;

public final class TestDataUtility {
    public static String UNESCAPED_HTML = "<!DOCTYPE html><html lang='en'><head></head><body></body></html>";

    public static String NHS_NUMBER = "9745812541";

    public static String getRandomTimestamp() {
        return DateTime.now().toDateTimeISO().toString();
    }

    public static String getRandomOdsCode() {
        return RandomStringUtils.randomAlphanumeric(6);
    }

    private TestDataUtility() { }
}
