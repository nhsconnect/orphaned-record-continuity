package uk.nhs.prm.deductions.pdsadaptor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.paginators.GetParametersByPathIterable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadSSMParameterTest {

    private ReadSSMParameter ssmParamService;
    private SsmClient ssmClient;
    private GetParametersByPathRequest getParametersByPathRequestForService;
    private GetParametersByPathRequest getParametersByPathRequestForUser;
    private Parameter user1;
    private Parameter user2;
    private Parameter service1;


    @BeforeEach
    void setup() {
        ssmClient = Mockito.mock(SsmClient.class);
        ssmParamService = new ReadSSMParameter(ssmClient);

        getParametersByPathRequestForService = GetParametersByPathRequest.builder()
                .withDecryption(Boolean.TRUE)
                .path("/repo/" + "local" + "/user-input/api-keys/pds-adaptor/")
                .build();

        getParametersByPathRequestForUser = GetParametersByPathRequest.builder()
                .withDecryption(Boolean.TRUE)
                .path("/repo/" + "local" + "/user-input/api-keys/pds-adaptor/api-key-user")
                .build();

        user1 = Parameter.builder().name("user1").type(ParameterType.SECURE_STRING).value("12345").build();
        user2 = Parameter.builder().name("user2").type(ParameterType.SECURE_STRING).value("56789").build();
        service1 = Parameter.builder().name("service1").type(ParameterType.SECURE_STRING).value("54321").build();
    }

    @Test
    void shouldCallGetParameterByPathAndReturnMapOfApiKeys(){
        // setup
        GetParametersByPathResponse userApiKeyParameters = GetParametersByPathResponse.builder().parameters(Arrays.asList(user1, user2)).build();
        GetParametersByPathResponse serviceApiKeyParameters = GetParametersByPathResponse.builder().parameters(List.of(service1)).build();
        GetParametersByPathIterable iterableService = new GetParametersByPathIterable(ssmClient, getParametersByPathRequestForService);
        GetParametersByPathIterable iterableUser = new GetParametersByPathIterable(ssmClient, getParametersByPathRequestForUser);

        when(ssmClient.getParametersByPath(getParametersByPathRequestForService)).thenReturn(userApiKeyParameters);
        when(ssmClient.getParametersByPath(getParametersByPathRequestForUser)).thenReturn(serviceApiKeyParameters);
        when(ssmClient.getParametersByPathPaginator(getParametersByPathRequestForService)).thenReturn(iterableService);
        when(ssmClient.getParametersByPathPaginator(getParametersByPathRequestForUser)).thenReturn(iterableUser);

        // action
        Map<String, String> apiKeys = ssmParamService.getApiKeys("local");

        // assertions
        verify(ssmClient).getParametersByPath(getParametersByPathRequestForService);
        verify(ssmClient).getParametersByPath(getParametersByPathRequestForUser);

        assertTrue(apiKeys.containsKey("user1") && apiKeys.containsValue("12345"));
        assertTrue(apiKeys.containsKey("user2") && apiKeys.containsValue("56789"));
        assertTrue(apiKeys.containsKey("service1") && apiKeys.containsValue("54321"));
    }

}
