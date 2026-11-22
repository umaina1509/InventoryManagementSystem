package com.store.inventory.service;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;
import com.store.inventory.storage.InventoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Business logic of the inventory: add, view, update, delete and set status.
 *
 * <p>Every change is validated and then written to the {@link InventoryRepository}.
 * The class contains no user-interface code so it can be tested on its own.</p>
 *
 * @author umaina & mashal
 */
public class InventoryService {

    private final InventoryRepository repository;
    private final List<Item> items;
    private int nextId;

    /**
     * Creates a service and loads existing data from the repository.
     *
     * @param repository where the inventory is stored
     */
    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
        this.items = new ArrayList<>(repository.load());
        int maxId = 0;
        for (Item item : items) {
            maxId = Math.max(maxId, item.getId());
        }
        this.nextId = maxId + 1;
    }

    /**
     * Returns all items in the order they were added.
     *
     * @return a read-only view of the items; empty if the inventory is empty
     */
    public List<Item> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Finds an item by id.
     *
     * @param id the item id
     * @return the item
     * @throws ItemNotFoundException if no item has this id
     */
    public Item findById(int id) {
        for (Item item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new ItemNotFoundException(id);
    }

    /**
     * Adds a new item after validating the input.
     *
     * @param name         the raw name text
     * @param quantityText the raw quantity text
     * @param status       the status to assign, or {@code null} to derive it from the quantity
     * @return the item that was added
     * @throws ValidationException if the name is empty or duplicated, or the quantity is invalid
     */
    public Item addItem(String name, String quantityText, StockStatus status) {
        String cleanName = InputValidator.validateName(name);
        int quantity = InputValidator.parseQuantity(quantityText);
        ensureNameIsUnique(cleanName, -1);

        StockStatus finalStatus = (status != null) ? status : StockStatus.fromQuantity(quantity);
        Item item = new Item(nextId++, cleanName, quantity, finalStatus);
        items.add(item);
        repository.save(items);
        return item;
    }

    /**
     * Updates the name, quantity and status of an existing item.
     *
     * @param id           the id of the item to update
     * @param name         the new raw name text
     * @param quantityText the new raw quantity text
     * @param status       the status to assign, or {@code null} to derive it from the quantity
     * @return the updated item
     * @throws ItemNotFoundException if no item has this id
     * @throws ValidationException   if the input is invalid
     */
    public Item updateItem(int id, String name, String quantityText, StockStatus status) {
        Item item = findById(id);
        String cleanName = InputValidator.validateName(name);
        int quantity = InputValidator.parseQuantity(quantityText);
        ensureNameIsUnique(cleanName, id);

        item.setName(cleanName);
        item.setQuantity(quantity);
        item.setStatus(status != null ? status : StockStatus.fromQuantity(quantity));
        repository.save(items);
        return item;
    }

    /**
     * Sets only the stock status of an item.
     *
     * @param id     the id of the item
     * @param status the new status
     * @return the updated item
     * @throws ItemNotFoundException if no item has this id
     */
    public Item setStatus(int id, StockStatus status) {
        Item item = findById(id);
        item.setStatus(status);
        repository.save(items);
        return item;
    }

    /**
     * Deletes an item.
     *
     * @param id the id of the item to delete
     * @return the item that was removed
     * @throws ItemNotFoundException if no item has this id
     */
    public Item deleteItem(int id) {
        Item item = findById(id);
        items.remove(item);
        repository.save(items);
        return item;
    }

    /**
     * Counts the items that currently have the given status.
     *
     * @param status the status to count
     * @return the number of matching items
     */
    public long countByStatus(StockStatus status) {
        return items.stream().filter(i -> i.getStatus() == status).count();
    }

    private void ensureNameIsUnique(String name, int ignoreId) {
        for (Item item : items) {
            if (item.getId() != ignoreId && item.getName().equalsIgnoreCase(name)) {
                throw new ValidationException("An item named \"" + item.getName() + "\" already exists.");
            }
        }
    }
}
