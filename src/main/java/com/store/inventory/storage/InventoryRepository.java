package com.store.inventory.storage;

import com.store.inventory.model.Item;

import java.util.List;

/**
 * Abstraction over where inventory data is persisted.
 *
 * @author YOUR_NAME
 */
public interface InventoryRepository {

    /**
     * Loads all stored items.
     *
     * @return the stored items, or an empty list if nothing has been stored yet
     */
    List<Item> load();

    /**
     * Replaces the stored data with the given items.
     *
     * @param items the complete list of items to store
     */
    void save(List<Item> items);
}
