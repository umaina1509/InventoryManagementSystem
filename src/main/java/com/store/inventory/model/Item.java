package com.store.inventory.model;

/**
 * An item held in the store's inventory.
 *
 * <p>Each item has a unique identifier, a name, a quantity on hand and a
 * {@link StockStatus}.</p>
 *
 * @author YOUR_NAME
 */
public class Item {

    private final int id;
    private String name;
    private int quantity;
    private StockStatus status;

    /**
     * Creates a new item.
     *
     * @param id       the unique identifier of the item
     * @param name     the item name
     * @param quantity the quantity on hand
     * @param status   the stock status
     */
    public Item(int id, String name, int quantity, StockStatus status) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.status = status;
    }

    /**
     * Returns the unique identifier of this item.
     *
     * @return the item id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the item name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the item name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the quantity on hand.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Changes the quantity on hand.
     *
     * @param quantity the new quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the stock status.
     *
     * @return the status
     */
    public StockStatus getStatus() {
        return status;
    }

    /**
     * Changes the stock status.
     *
     * @param status the new status
     */
    public void setStatus(StockStatus status) {
        this.status = status;
    }

    /**
     * Returns a readable description of this item.
     *
     * @return the item as text
     */
    @Override
    public String toString() {
        return "Item{id=" + id + ", name='" + name + "', quantity=" + quantity
                + ", status=" + status + '}';
    }
}
