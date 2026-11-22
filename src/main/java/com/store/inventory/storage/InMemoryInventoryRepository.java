package com.store.inventory.storage;

import com.store.inventory.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * A repository that keeps data only in memory.
 *
 * <p>Used as a fallback when the data file cannot be opened, and in unit tests.</p>
 *
 * @author umaina & mashal
 */
public class InMemoryInventoryRepository implements InventoryRepository {

    private final List<Item> stored = new ArrayList<>();

    @Override
    public List<Item> load() {
        List<Item> copy = new ArrayList<>();
        for (Item item : stored) {
            copy.add(new Item(item.getId(), item.getName(), item.getQuantity(), item.getStatus()));
        }
        return copy;
    }

    @Override
    public void save(List<Item> items) {
        stored.clear();
        for (Item item : items) {
            stored.add(new Item(item.getId(), item.getName(), item.getQuantity(), item.getStatus()));
        }
    }
}
