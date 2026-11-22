package com.store.inventory.storage;

import com.store.inventory.service.InventoryException;

/**
 * Thrown when inventory data cannot be read from or written to storage.
 *
 * @author YOUR_NAME
 */
public class StorageException extends InventoryException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new storage exception.
     *
     * @param message a description of the problem
     * @param cause   the underlying I/O or parsing error
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
