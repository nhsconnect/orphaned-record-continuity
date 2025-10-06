package uk.nhs.prm.repo.ehrtransferservice.models.ack;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.nhs.prm.repo.ehrtransferservice.gp2gp_message_models.AcknowledgementMessageWrapper;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AcknowledgementTest {
    @ParameterizedTest
    @MethodSource("provideArgumentsForAcknowledgementType")
    void acknowledgementTypeCodeShouldBeNegativeAcknowledgement(String typeCode, boolean expected) {
        // given
        AcknowledgementMessageWrapper acknowledgementMessageWrapper = new AcknowledgementMessageWrapper();
        acknowledgementMessageWrapper.acknowledgement = new AcknowledgementMessageWrapper.Acknowledgement();
        acknowledgementMessageWrapper.acknowledgement.typeCode = typeCode;
        var ack = new Acknowledgement(null, acknowledgementMessageWrapper, "bob");

        // then
        assertThat(ack.isNegativeAcknowledgement()).isEqualTo(expected);
    }

    private static Stream<Arguments> provideArgumentsForAcknowledgementType() {
        return Stream.of(
                Arguments.of("AA", false),
                Arguments.of("AE", true),
                Arguments.of("AR", true),
                Arguments.of("something that should never be set", true));
    }
}
