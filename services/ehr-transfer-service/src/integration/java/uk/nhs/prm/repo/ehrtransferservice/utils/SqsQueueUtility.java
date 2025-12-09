package uk.nhs.prm.repo.ehrtransferservice.utils;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class SqsQueueUtility {
    private final AmazonSQSAsync amazonSQSAsync;
    private static final Logger LOGGER = LogManager.getLogger(SqsQueueUtility.class);

    @Autowired
    public SqsQueueUtility(AmazonSQSAsync amazonSQSAsync) {
        this.amazonSQSAsync = amazonSQSAsync;
    }

    public void purgeQueue(String queueName) {
        final String queueUrl = getQueueUrl(queueName);
        final PurgeQueueRequest request = new PurgeQueueRequest(queueUrl);

        amazonSQSAsync.purgeQueue(request);
        LOGGER.info("Successfully purged queue - {}", queueUrl);
    }

    public void sendSqsMessage(String message, String queueName) {
        final String queueUrl = getQueueUrl(queueName);
        amazonSQSAsync.sendMessage(queueUrl, message);

        LOGGER.info("Message sent successfully to {} SQS queue", queueName);
    }

    private String getQueueUrl(String queueName) {
        return amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();
    }
}