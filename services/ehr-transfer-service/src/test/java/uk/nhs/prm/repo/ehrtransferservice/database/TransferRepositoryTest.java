package uk.nhs.prm.repo.ehrtransferservice.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import uk.nhs.prm.repo.ehrtransferservice.config.AppConfig;
import uk.nhs.prm.repo.ehrtransferservice.database.enumeration.ConversationTransferStatus;
import uk.nhs.prm.repo.ehrtransferservice.exceptions.database.ConversationUpdateException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.nhs.prm.repo.ehrtransferservice.database.enumeration.TransferTableAttribute.INBOUND_CONVERSATION_ID;

@ExtendWith(MockitoExtension.class)
class TransferRepositoryTest {
    @Mock
    private AppConfig appConfig;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @InjectMocks
    private TransferRepository transferRepository;

    private static final String TABLE_NAME = "ehr-transfer-tracker";
    private static final String CREATED_AT_CONDITION_EXPRESSION = "attribute_exists(CreatedAt)";
    private static final String UPDATE_STATUS_CODE_EXPRESSION = "SET #TransferStatus = :tsValue, #UpdatedAt = :uaValue";
    private static final String UPDATE_WITH_FAILURE_CODE_EXPRESSION = "SET #TransferStatus = :tsValue, #FailureCode = :fcValue, #UpdatedAt = :uaValue";
    private static final ArgumentCaptor<UpdateItemRequest> updateItemRequestCaptor = ArgumentCaptor.forClass(UpdateItemRequest.class);

    @BeforeEach
    void beforeEach() {
        doReturn(TABLE_NAME)
                .when(appConfig)
                .transferTrackerDbTableName();
    }

    @Test
    void updateConversationStatus_ValidInboundConversationIdAndStatus_ShouldCallUpdateItem() {
        // given
        final String inboundConversationId = "F68FE779-5A2A-490B-81FE-6976543EFC06";
        final ConversationTransferStatus status = ConversationTransferStatus.INBOUND_COMPLETE;

        // when
        transferRepository.updateConversationStatus(UUID.fromString(inboundConversationId), status);

        // then
        verify(appConfig).transferTrackerDbTableName();
        verify(dynamoDbClient).updateItem(updateItemRequestCaptor.capture());
        assertEquals(inboundConversationId, updateItemRequestCaptor.getValue().key().get(INBOUND_CONVERSATION_ID.name).s());
        assertEquals(status.name(), updateItemRequestCaptor.getValue().expressionAttributeValues().get(":tsValue").s());
        assertEquals(CREATED_AT_CONDITION_EXPRESSION, updateItemRequestCaptor.getValue().conditionExpression());
        assertEquals(UPDATE_STATUS_CODE_EXPRESSION, updateItemRequestCaptor.getValue().updateExpression());
    }

    @Test
    void updateConversationStatus_ValidInboundConversationIdAndStatusWithNoCreatedAtDate_ShouldThrowConversationUpdateException() {
        // given
        final String inboundConversationId = "C68082F9-EFB9-4144-BAA0-3A2F2E2A88B9";
        final ConversationTransferStatus status = ConversationTransferStatus.INBOUND_COMPLETE;

        // when
        doThrow(ConditionalCheckFailedException.class)
                .when(dynamoDbClient)
                .updateItem(any(UpdateItemRequest.class));

        // then
        assertThrows(
                ConversationUpdateException.class,
                () -> transferRepository.updateConversationStatus(UUID.fromString(inboundConversationId), status)
        );

        verify(dynamoDbClient).updateItem(updateItemRequestCaptor.capture());
        assertEquals(inboundConversationId, updateItemRequestCaptor.getValue().key().get(INBOUND_CONVERSATION_ID.name).s());
    }

    @Test
    void updateConversationStatusWithFailure_ValidInboundConversationIdAndFailureCode_ShouldCallUpdateItem() {
        // given
        final String inboundConversationId = "0A1B2C3D-4E5F-6789-ABCD-EF0123456789";
        final String failureCode = "19";

        // when
        transferRepository.updateConversationStatusWithFailure(UUID.fromString(inboundConversationId), failureCode);

        // then
        verify(appConfig).transferTrackerDbTableName();
        verify(dynamoDbClient).updateItem(updateItemRequestCaptor.capture());
        assertEquals(inboundConversationId, updateItemRequestCaptor.getValue().key().get(INBOUND_CONVERSATION_ID.name).s());
        assertEquals(failureCode, updateItemRequestCaptor.getValue().expressionAttributeValues().get(":fcValue").s());
        assertEquals(UPDATE_WITH_FAILURE_CODE_EXPRESSION, updateItemRequestCaptor.getValue().updateExpression());
        assertEquals(CREATED_AT_CONDITION_EXPRESSION, updateItemRequestCaptor.getValue().conditionExpression());
    }

    @Test
    void updateConversationStatusWithFailure_ValidInboundConversationIdAndFailureCodeWithNoCreatedAtDate_ShouldThrowConversationUpdateException() {
        // given
        final String inboundConversationId = "8A9B0CAD-2E3F-4567-ABCD-EF8901234567";
        final String failureCode = "19";

        // when
        doThrow(ConditionalCheckFailedException.class)
                .when(dynamoDbClient)
                .updateItem(any(UpdateItemRequest.class));

        // then
        assertThrows(
                ConversationUpdateException.class,
                () -> transferRepository.updateConversationStatusWithFailure(UUID.fromString(inboundConversationId), failureCode)
        );

        verify(dynamoDbClient).updateItem(updateItemRequestCaptor.capture());
        assertEquals(inboundConversationId, updateItemRequestCaptor.getValue().key().get(INBOUND_CONVERSATION_ID.name).s());
    }
}