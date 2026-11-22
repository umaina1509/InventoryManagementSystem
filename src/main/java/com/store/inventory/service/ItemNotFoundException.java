package com.store.inventory.service;

/**
 * Thrown when an operation refers to an item id that does not exist.
 *
 * @author umaina & mashal
 */
public class ItemNotFoundException extends InventoryException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception for a missing item.
     *
     * @param id the id that could not be found
     */
    public ItemNotFoundException(int id) {
        super("No item with ID " + id + " exists. It may have been deleted.");
    }
}
