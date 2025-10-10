package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        LocalStackAwsConfig.class
})
class MessageAcknowledgementTest {

    @Autowired
    private SqsClient sqsClient;

    @MockBean
    private NemsEventHandler nemsEventHandler;

    @Value("${aws.nemsEventsQueueName}")
    private String nemsEventQueueName;

    private StubbedNemsEventHandler stubbedNemsEventHandler;

    @BeforeEach
    public void setUpStubbedHandler() {
        stubbedNemsEventHandler = new StubbedNemsEventHandler();
        doAnswer(invocation -> {
            stubbedNemsEventHandler.processNemsEvent(invocation.getArgument(0));
            return null;
        }).when(nemsEventHandler).processNemsEvent(anyString());
    }

    @Test
    void shouldNotImplicitlyAcknowledgeAFailedMessageWhenTheNextMessageIsProcessedOk_SoThatItIsThereToBeReprocessedAfterVisibilityTimeout() {

        sendMessage(nemsEventQueueName, "throw me");
        stubbedNemsEventHandler.waitUntilProcessed("throw me", 10);

        sendMessage(nemsEventQueueName, "process me ok");
        stubbedNemsEventHandler.waitUntilProcessed("process me ok", 10);

        assertThat(getIncomingNemsMessagesCount("ApproximateNumberOfMessagesNotVisible")).isEqualTo(1);
    }

    private int getIncomingNemsMessagesCount(String countAttributeName) {
        var countAttribute = QueueAttributeName.fromValue(countAttributeName);
        var attributesRequest = GetQueueAttributesRequest.builder()
                .attributeNames(countAttribute)
                .queueUrl(getQueueUrl(nemsEventQueueName))
                .build();
        var attributes = sqsClient.getQueueAttributes(attributesRequest).attributes();
        return Integer.parseInt(attributes.get(countAttribute));
    }

    private void sendMessage(String queueName, String message) {
        sqsClient.sendMessage(SendMessageRequest.builder().queueUrl(getQueueUrl(queueName)).messageBody(message).build());
    }

    private String getQueueUrl(String queueName) {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
    }
}
