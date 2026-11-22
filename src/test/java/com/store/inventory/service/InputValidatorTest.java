package com.store.inventory.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link InputValidator}.
 *
 * @author Mashal and Umaina
 */
class InputValidatorTest {

    @Test
    void nameIsTrimmed() {
        assertEquals("Stapler", InputValidator.validateName("  Stapler  "));
    }

    @Test
    void nullAndBlankNamesAreRejected() {
        assertThrows(ValidationException.class, () -> InputValidator.validateName(null));
        assertThrows(ValidationException.class, () -> InputValidator.validateName(""));
        assertThrows(ValidationException.class, () -> InputValidator.validateName("   "));
    }

    @Test
    void tooLongNameIsRejected() {
        String longName = "x".repeat(InputValidator.MAX_NAME_LENGTH + 1);
        assertThrows(ValidationException.class, () -> InputValidator.validateName(longName));
    }

    @Test
    void validQuantitiesAreParsed() {
        assertEquals(0, InputValidator.parseQuantity("0"));
        assertEquals(15, InputValidator.parseQuantity(" 15 "));
        assertEquals(InputValidator.MAX_QUANTITY,
                InputValidator.parseQuantity(String.valueOf(InputValidator.MAX_QUANTITY)));
    }

    @Test
    void invalidQuantitiesAreRejected() {
        assertThrows(ValidationException.class, () -> InputValidator.parseQuantity(null));
        assertThrows(ValidationException.class, () -> InputValidator.parseQuantity(""));
        assertThrows(ValidationException.class, () -> InputValidator.parseQuantity("abc"));
        assertThrows(ValidationException.class, () -> InputValidator.parseQuantity("3.5"));
        assertThrows(ValidationException.class, () -> InputValidator.parseQuantity("-1"));
        assertThrows(ValidationException.class, () -> InputValidator.parseQuantity("99999999999999999999"));
        assertThrows(ValidationException.class,
                () -> InputValidator.parseQuantity(String.valueOf(InputValidator.MAX_QUANTITY + 1)));
    }
}
