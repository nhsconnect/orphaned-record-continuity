package uk.nhs.prm.repo.ehrtransferservice.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.database.model.ConversationRecord;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.FailedToPersistException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationAlreadyPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationNotPresentException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationUpdateException;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.QueryReturnedNoItemsException;
import uk.nhs.prm.repo.ehrtransferservice.repo_incoming.RepoIncomingEvent;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_FAILED;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus.INBOUND_STARTED;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.Layer.CONVERSATION;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.Layer.CORE;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.*;
import static uk.nhs.prm.repo.ehrtransferservice.utility.DateUtility.getIsoTimestamp;

@Log4j2
@Component
@RequiredArgsConstructor
public class TransferRepository {
    private final AppConfig config;
    private final DynamoDbClient dynamoDbClient;

    void createConversation(RepoIncomingEvent event) {
        final Map<String, AttributeValue> tableItem = new HashMap<>();
        final String timestamp = getIsoTimestamp();
        final UUID inboundConversationId = UUID.fromString(event.getConversationId());

        if (isInboundConversationPresent(inboundConversationId)) {
            throw new ConversationAlreadyPresentException(inboundConversationId);
        }

        tableItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString().toUpperCase())
            .build());

        tableItem.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION.name()).build());

        tableItem.put(NHS_NUMBER.name, AttributeValue.builder()
            .s(event.getNhsNumber())
            .build());

        tableItem.put(SOURCE_GP.name, AttributeValue.builder()
            .s(event.getSourceGp())
            .build());

        tableItem.put(TRANSFER_STATUS.name, AttributeValue.builder()
            .s(INBOUND_STARTED.name())
            .build());

        tableItem.put(NEMS_MESSAGE_ID.name, AttributeValue.builder()
            .s(event.getNemsMessageId())
            .build());

        tableItem.put(CREATED_AT.name, AttributeValue.builder()
            .s(timestamp)
            .build());

        tableItem.put(UPDATED_AT.name, AttributeValue.builder()
            .s(timestamp)
            .build());

        final PutItemRequest itemRequest = PutItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .item(tableItem)
            .build();

        try {
            dynamoDbClient.putItem(itemRequest);
            log.info("Initial conversation record created for Inbound Conversation ID {}", event.getConversationId());
        } catch (SdkException exception) {
            throw new FailedToPersistException(inboundConversationId, exception);
        }
    }

    boolean isInboundConversationPresent(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString().toUpperCase())
                .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION.name())
            .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(keyItems)
                .build();

        return dynamoDbClient.getItem(itemRequest).hasItem();
    }

    ConversationRecord findConversationByInboundConversationId(UUID inboundConversationId) {
        final Map<String, AttributeValue> keyItem = new HashMap<>();

        keyItem.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString().toUpperCase())
            .build());

        keyItem.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION.name())
            .build());

        final GetItemRequest itemRequest = GetItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .key(keyItem)
            .build();

        final GetItemResponse response = dynamoDbClient.getItem(itemRequest);

        if (!response.hasItem()) {
            throw new ConversationNotPresentException(inboundConversationId);
        }

        return mapGetItemResponseToConversationRecord(response);
    }

    void updateConversationStatus(UUID inboundConversationId, ConversationTransferStatus conversationTransferStatus) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
            .s(inboundConversationId.toString().toUpperCase())
            .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
            .s(CONVERSATION.name())
            .build());

        final UpdateItemRequest itemRequest = UpdateItemRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .key(keyItems)
            .updateExpression("SET #TransferStatus = :tsValue, #UpdatedAt = :uaValue")
            .expressionAttributeNames(Map.of(
                    "#TransferStatus", TRANSFER_STATUS.name,
                    "#UpdatedAt", UPDATED_AT.name
            ))
            .expressionAttributeValues(Map.of(
                    ":tsValue", AttributeValue.builder()
                            .s(conversationTransferStatus.name())
                            .build(),
                    ":uaValue", AttributeValue.builder()
                            .s(getIsoTimestamp())
                            .build()
            ))
            .conditionExpression("attribute_exists(CreatedAt)")
            .build();

        try {
            dynamoDbClient.updateItem(itemRequest);
        } catch (SdkException exception) {
            throw new ConversationUpdateException(inboundConversationId, exception);
        }
    }

    void updateConversationStatusWithFailure(UUID inboundConversationId, String failureCode) {
        final Map<String, AttributeValue> keyItems = new HashMap<>();

        keyItems.put(INBOUND_CONVERSATION_ID.name, AttributeValue.builder()
                .s(inboundConversationId.toString().toUpperCase())
                .build());

        keyItems.put(LAYER.name, AttributeValue.builder()
                .s(CONVERSATION.name())
                .build());

        final UpdateItemRequest itemRequest = UpdateItemRequest.builder()
                .tableName(config.transferTrackerDbTableName())
                .key(keyItems)
                .updateExpression("SET #TransferStatus = :tsValue, #FailureCode = :fcValue, #UpdatedAt = :uaValue")
                .expressionAttributeNames(Map.of(
                        "#TransferStatus", TRANSFER_STATUS.name,
                        "#UpdatedAt", UPDATED_AT.name,
                        "#FailureCode", FAILURE_CODE.name
                ))
                .expressionAttributeValues(Map.of(
                        ":tsValue", AttributeValue.builder()
                                .s(INBOUND_FAILED.name())
                                .build(),
                        ":fcValue", AttributeValue.builder()
                                .s(failureCode)
                                .build(),
                        ":uaValue", AttributeValue.builder()
                                .s(getIsoTimestamp())
                                .build()
                ))
                .conditionExpression("attribute_exists(CreatedAt)")
                .build();

        try {
            dynamoDbClient.updateItem(itemRequest);
        } catch (SdkException exception) {
            throw new ConversationUpdateException(inboundConversationId, exception);
        }
    }

    UUID getEhrCoreInboundMessageIdForInboundConversationId(UUID inboundConversationId) {
        final QueryRequest queryRequest = QueryRequest.builder()
            .tableName(config.transferTrackerDbTableName())
            .keyConditionExpression("%s = :inboundConversationId AND begins_with(%s, :layer)".formatted(
                INBOUND_CONVERSATION_ID.name,
                LAYER.name
            ))
            .expressionAttributeValues(Map.of(
                ":inboundConversationId", AttributeValue.builder()
                    .s(inboundConversationId.toString().toUpperCase())
                    .build(),
                ":layer", AttributeValue.builder()
                    .s(CORE.name())
                    .build()
            ))
            .build();

        final QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        if(!queryResponse.hasItems() || queryResponse.items().isEmpty()) {
            throw new QueryReturnedNoItemsException(inboundConversationId);
        }

        return UUID.fromString(queryResponse.items().get(0).get(INBOUND_MESSAGE_ID.name).s());
    }

    private ConversationRecord mapGetItemResponseToConversationRecord(GetItemResponse itemResponse) {
        Map<String, AttributeValue> item = itemResponse.item();

        return new ConversationRecord(
            UUID.fromString(item.get(INBOUND_CONVERSATION_ID.name).s()),
            Optional.ofNullable(item.get(OUTBOUND_CONVERSATION_ID.name))
                .map(AttributeValue::s)
                .map(UUID::fromString),
            item.get(NHS_NUMBER.name).s(),
            item.get(SOURCE_GP.name).s(),
            Optional.ofNullable(item.get(DESTINATION_GP.name))
                .map(AttributeValue::s),
            ConversationTransferStatus.valueOf(item.get(TRANSFER_STATUS.name).s()),
            Optional.ofNullable(item.get(FAILURE_CODE.name))
                .map(AttributeValue::s),
            Optional.ofNullable(item.get(NEMS_MESSAGE_ID.name))
                .map(AttributeValue::s)
                .map(UUID::fromString),
            ZonedDateTime.parse(item.get(CREATED_AT.name).s()),
            ZonedDateTime.parse(item.get(UPDATED_AT.name).s()),
            Optional.ofNullable(item.get(DELETED_AT.name))
                .map(AttributeValue::s)
                .map(Instant::parse)
        );
    }
}