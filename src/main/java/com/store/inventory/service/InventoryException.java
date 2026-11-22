package com.store.inventory.service;

/**
 * Base class for all errors raised by the inventory business logic.
 *
 * <p>The message of every subclass is written to be shown directly to the user.</p>
 *
 * @author Mashal and Umaina
 */
public class InventoryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.
     *
     * @param message a user-readable description of the problem
     */
    public InventoryException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with an underlying cause.
     *
     * @param message a user-readable description of the problem
     * @param cause   the underlying cause
     */
    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
