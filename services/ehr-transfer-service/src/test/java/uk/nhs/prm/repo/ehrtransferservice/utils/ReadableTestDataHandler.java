package uk.nhs.prm.repo.ehrtransferservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.MhsJsonMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ReadableTestDataHandler {

    private TestDataLoader rawLoader = new TestDataLoader();

    public String readMessage(String interactionId, String variant) {
        Path messageFolder = readableMessageDirPath(interactionId, variant);

        Path envelopeXmlFile = messageFolder.resolve("envelope.xml");
        Path contentXmlFile = messageFolder.resolve("payload.xml");

        var mhsMessageData = new LinkedHashMap<String, Object>();
        mhsMessageData.put("ebXML", readFile(envelopeXmlFile));
        mhsMessageData.put("payload", readFile(contentXmlFile));
        mhsMessageData.put("attachments", Collections.emptyList());

        return asJson(mhsMessageData);
    }

    public void splitMessage(String unreadableFilename) {
        var messageBody = readRawDataResource(unreadableFilename);

        var interactionId = unreadableFilename.substring(0, 17);
        var variant = unreadableFilename.substring(17);

        Path messageFolder = readableMessageDirPath(interactionId, variant);

        Path envelopeXmlFile = messageFolder.resolve("envelope.xml");
        Path contentXmlFile = messageFolder.resolve("payload.xml");

        var parsedJson = parseMhsMessage(messageBody);

        writeToFile(envelopeXmlFile, parsedJson.ebXML);
        writeToFile(contentXmlFile, parsedJson.payload);
    }

    private Path readableMessageDirPath(String interactionId, String variant) {
        return Paths.get(dataDirPath().toAbsolutePath().toString(), "readable", interactionId, variant);
    }

    private Path dataDirPath() {
        return getPath("data/");
    }

    private MhsJsonMessage parseMhsMessage(String messageBody) {
        try {
            return new ObjectMapper().readValue(messageBody, MhsJsonMessage.class);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readRawDataResource(String unreadableFilename) {
        try {
            return rawLoader.getDataAsString(unreadableFilename);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(Path filepath, String content) {
        try {
            System.out.println("writing to " + filepath);
            Files.createDirectories(filepath.getParent());
            Files.write(filepath, content.getBytes());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readFile(Path filepath) {
        try {
            return Files.readString(filepath, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getPath(String path) {
        try {
            return ResourceUtils.getFile("classpath:" + path).toPath();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String asJson(HashMap<String, Object> mhsMessageData) {
        String json;
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(mhsMessageData);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}
