package uk.nhs.prm.repo.ehrtransferservice.services.ehr_repo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.nhs.prm.repo.ehrtransferservice.models.confirmmessagestored.StoreMessageResponseBody;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StoreMessageResultTest {
    @ParameterizedTest
    @MethodSource("provideArgumentsForHealthRecordStatus")
    void ehrIsCompleteIfHealthRecordStatusIsComplete(String healthRecordStatus, boolean expected) {
        var result = new StoreMessageResult(new StoreMessageResponseBody(healthRecordStatus));
        assertThat(result.isEhrComplete()).isEqualTo(expected);
    }

    private static Stream<Arguments> provideArgumentsForHealthRecordStatus() {
        return Stream.of(
                Arguments.of("complete", true),
                Arguments.of("not complete", false),
                Arguments.of("", false),
                Arguments.of(null, false));
    }
}