package uk.nhs.prm.repo.re_registration.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import uk.nhs.prm.repo.re_registration.metrics.AppConfig;
import uk.nhs.prm.repo.re_registration.model.ActiveSuspensionsMessage;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSuspensionsDb {
    private final DynamoDbClient dynamoDbClient;
    private final AppConfig config;

    public ActiveSuspensionsMessage getByNhsNumber(String nhsNumber) {
        log.info("Trying to fetch the record by nhsNumber from the active suspensions db.");

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("nhs_number", AttributeValue.builder().s(nhsNumber).build());
        var getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(config.activeSuspensionsDynamoDbTableName())
                .key(key)
                .build());

        return fromDbItem(getItemResponse);
    }

    public void save(ActiveSuspensionsMessage activeSuspensionMessage) {

        log.info("Trying to save active suspensions message in db.");

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("nhs_number", AttributeValue.builder().s(activeSuspensionMessage.getNhsNumber()).build());
        item.put("previous_gp", AttributeValue.builder().s(activeSuspensionMessage.getPreviousOdsCode()).build());
        item.put("nems_last_updated_date", AttributeValue.builder().s(activeSuspensionMessage.getNemsLastUpdatedDate()).build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(config.activeSuspensionsDynamoDbTableName())
                .item(item)
                .build());

    }


    public void deleteByNhsNumber(String nhsNumber) {
        log.info("Trying to delete the record by nhsNumber from the active suspensions db.");

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("nhs_number", AttributeValue.builder().s(nhsNumber).build());

        try{
            dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                    .tableName(config.activeSuspensionsDynamoDbTableName())
                    .key(key)
                    .build());
        }catch (DynamoDbException exception){
            log.error("Error while deleting active-suspensions record from the DB.", exception);
        }
    }

    private ActiveSuspensionsMessage fromDbItem(GetItemResponse itemResponse) {
        if (!itemResponse.hasItem()) {
            return null;
        }
        var nhsNumber = itemResponse.item().get("nhs_number").s();
        var previousOdsCode = itemResponse.item().get("previous_gp").s();
        var nemsLastUpdatedDate = itemResponse.item().get("nems_last_updated_date").s();
        return new ActiveSuspensionsMessage(nhsNumber, previousOdsCode, nemsLastUpdatedDate);
    }
}
