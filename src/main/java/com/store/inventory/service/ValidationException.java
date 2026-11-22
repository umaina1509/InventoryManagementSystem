package com.store.inventory.service;

/**
 * Thrown when user input fails validation (empty name, invalid quantity, duplicate name).
 *
 * @author Mashal and Umaina
 */
public class ValidationException extends InventoryException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new validation exception.
     *
     * @param message the message to show to the user
     */
    public ValidationException(String message) {
        super(message);
    }
}
