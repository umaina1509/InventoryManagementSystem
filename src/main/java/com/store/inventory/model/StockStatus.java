package com.store.inventory.model;

/**
 * Stock status of an inventory item.
 *
 * <p>The status is normally derived from the quantity on hand (see
 * {@link #fromQuantity(int)}), but the user may also set it manually.</p>
 *
 * @author YOUR_NAME
 */
public enum StockStatus {

    /** Plenty of stock available. */
    IN_STOCK("In Stock"),

    /** Stock is running low and should be reordered. */
    LOW_STOCK("Low Stock"),

    /** No units left. */
    OUT_OF_STOCK("Out of Stock");

    /** Quantities from 1 up to and including this value are considered "Low Stock". */
    public static final int LOW_STOCK_THRESHOLD = 5;

    private final String label;

    StockStatus(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label of this status.
     *
     * @return the display label, e.g. {@code "Low Stock"}
     */
    public String getLabel() {
        return label;
    }

    /**
     * Derives the status that matches a given quantity.
     *
     * @param quantity the quantity on hand (must not be negative)
     * @return {@link #OUT_OF_STOCK} for 0, {@link #LOW_STOCK} for 1 to
     *         {@value #LOW_STOCK_THRESHOLD}, otherwise {@link #IN_STOCK}
     */
    public static StockStatus fromQuantity(int quantity) {
        if (quantity <= 0) {
            return OUT_OF_STOCK;
        }
        if (quantity <= LOW_STOCK_THRESHOLD) {
            return LOW_STOCK;
        }
        return IN_STOCK;
    }

    /**
     * Finds the status whose label matches the given text (case-insensitive).
     *
     * @param text the label to look up
     * @return the matching status, or {@code null} if none matches
     */
    public static StockStatus fromLabel(String text) {
        if (text == null) {
            return null;
        }
        for (StockStatus status : values()) {
            if (status.label.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * Returns the display label, so the status prints nicely in the UI.
     *
     * @return the label
     */
    @Override
    public String toString() {
        return label;
    }
}
