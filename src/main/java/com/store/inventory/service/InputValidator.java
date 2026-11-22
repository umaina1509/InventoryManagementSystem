package com.store.inventory.service;

/**
 * Validates raw text entered by the user.
 *
 * <p>All methods are static and throw {@link ValidationException} with a
 * message suitable for display when the input is not acceptable.</p>
 *
 * @author Mashal and Umaina
 */
public final class InputValidator {

    /** Maximum number of characters allowed in an item name. */
    public static final int MAX_NAME_LENGTH = 60;

    /** Maximum quantity accepted for a single item. */
    public static final int MAX_QUANTITY = 1_000_000;

    private InputValidator() {
        // utility class
    }

    /**
     * Validates an item name and returns it with surrounding whitespace removed.
     *
     * @param name the raw name text, may be {@code null}
     * @return the trimmed name
     * @throws ValidationException if the name is empty or too long
     */
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Item name must not be empty.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new ValidationException(
                    "Item name must not exceed " + MAX_NAME_LENGTH + " characters.");
        }
        return trimmed;
    }

    /**
     * Parses and validates a quantity.
     *
     * @param text the raw quantity text, may be {@code null}
     * @return the quantity as a non-negative integer
     * @throws ValidationException if the text is empty, not a whole number,
     *                             negative, or larger than {@link #MAX_QUANTITY}
     */
    public static int parseQuantity(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Quantity must not be empty.");
        }
        long value;
        try {
            value = Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Quantity must be a whole number (digits only).");
        }
        if (value < 0) {
            throw new ValidationException("Quantity must not be negative.");
        }
        if (value > MAX_QUANTITY) {
            throw new ValidationException("Quantity must not exceed " + MAX_QUANTITY + ".");
        }
        return (int) value;
    }
}
