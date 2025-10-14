package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.prm.repo.suspension.service.infra.LocalStackAwsConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest()
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration( classes = {
        LocalStackAwsConfig.class
})
@TestPropertySource(properties = {
        "aws.incomingQueueName=test_ack_queue",
})
@DirtiesContext
public class MessageAcknowledgementTest {

    @Autowired
    private AmazonSQSAsync amazonSQSAsync;

    @MockBean
    private SuspensionMessageProcessor mockMessageProcessor;

    private StubbedSuspensionProcessor stubbedSuspensionProcessor;

    @Value("${aws.incomingQueueName}")
    private String suspensionsQueueName;

    private String queueUrl;

    @BeforeEach
    public void setUpStubbedHandler() {
        queueUrl = amazonSQSAsync.getQueueUrl(suspensionsQueueName).getQueueUrl();
        purgeQueue(queueUrl);
        stubbedSuspensionProcessor = new StubbedSuspensionProcessor();
        doAnswer(invocation -> {
            stubbedSuspensionProcessor.process(invocation.getArgument(0));
            return null;
        }).when(mockMessageProcessor).process(anyString());
    }

    @AfterEach
    public void tearDown(){
        queueUrl = amazonSQSAsync.getQueueUrl(suspensionsQueueName).getQueueUrl();
        purgeQueue(queueUrl);
    }

    @Test
    void shouldNotImplicitlyAcknowledgeAFailedMessageWhenTheNextMessageIsProcessedOk_SoThatItIsThereToBeReprocessedAfterVisibilityTimeout() {

        amazonSQSAsync.sendMessage(queueUrl, "throw me");
        stubbedSuspensionProcessor.waitUntilProcessed("throw me", 10);

        amazonSQSAsync.sendMessage(queueUrl, "process me ok");
        stubbedSuspensionProcessor.waitUntilProcessed("process me ok", 10);

        assertThat(getIncomingNemsMessagesCount("ApproximateNumberOfMessagesNotVisible")).isEqualTo(1);

    }

    private int getIncomingNemsMessagesCount(String countAttributeName) {
        var incomingQueue = amazonSQSAsync.getQueueUrl(suspensionsQueueName).getQueueUrl();
        var attributes = amazonSQSAsync.getQueueAttributes(new GetQueueAttributesRequest()
                .withAttributeNames(countAttributeName)
                .withQueueUrl(incomingQueue)).getAttributes();
        return Integer.parseInt(attributes.get(countAttributeName));
    }

    private void purgeQueue(String queueUrl) {
        System.out.println("Purging queue url: " + queueUrl);
        amazonSQSAsync.purgeQueue(new PurgeQueueRequest(queueUrl));
    }

}
