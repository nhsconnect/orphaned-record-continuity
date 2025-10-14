package uk.nhs.prm.deductions.nemseventprocessor.nemsevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NemsEventValidatorTest {

    NemsEventValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NemsEventValidator();
    }

    @Test
    void shouldThrowAnErrorWhenNhsNumberIsNot10DigitsLong() {
        NemsEventValidationException nemsEventValidationException = assertThrows(NemsEventValidationException.class, () -> {
            validator.validate("123", "01", "B12345");
        });

        assertThat(nemsEventValidationException.getMessage()).contains("NemsEventValidationException: NHS Number is not 10 digits");
    }

    @Test
    void shouldThrowAnErrorWhenNhsNumberHasNotGot01ValidationCode() {
        NemsEventValidationException nemsEventValidationException = assertThrows(NemsEventValidationException.class, () -> {
            validator.validate("1234567890", "02", "B12345");
        });

        assertThat(nemsEventValidationException.getMessage()).contains("NemsEventValidationException: NHS Number verification code does not equal 01");
    }

    @Test
    void shouldThrowAnErrorWhenPreviousGpOdsCodeIsMoreThan10Digits() {
        NemsEventValidationException nemsEventValidationException = assertThrows(NemsEventValidationException.class, () -> {
            validator.validate("1234567890", "01", "B12345C23456");
        });

        assertThat(nemsEventValidationException.getMessage()).contains("NemsEventValidationException: Previous GP ODS Code is more than 10 characters");
    }

    @Test
    void shouldNotThrowErrorWhenAllValuesAreValid() {
        assertDoesNotThrow(() -> {
            validator.validate("1234567890", "01", "B12345");
        });
    }
}