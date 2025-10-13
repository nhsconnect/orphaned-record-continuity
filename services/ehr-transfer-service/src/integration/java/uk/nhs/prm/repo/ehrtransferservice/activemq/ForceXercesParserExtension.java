package uk.nhs.prm.repo.ehrtransferservice.activemq;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This temporary extension has been created so that
 * logback does not blow up when using the Swift MQ Client.
 */
public class ForceXercesParserExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
    }
}