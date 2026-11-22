package com.store.inventory.ui;

import com.store.inventory.model.StockStatus;
import com.store.inventory.service.InventoryService;

/**
 * Sample stock for a small general store, added on the very first start.
 *
 * <p>Quantities are chosen around {@link StockStatus#LOW_STOCK_THRESHOLD}
 * (currently 5) so the mix is 9 In Stock (more than 5), 4 Low Stock (1 to 5)
 * and 2 Out of Stock (0). Every item is added with the "Auto" status, so the
 * status is derived from the quantity by the normal service rules.</p>
 *
 * @author Mashal and Umaina
 */
public final class SampleData {

    /** Item names and quantities, in the order they are added. */
    private static final Object[][] ITEMS = {
        // In Stock (more than LOW_STOCK_THRESHOLD)
        {"Notebook A4", 120},
        {"Ballpoint Pen (Blue)", 250},
        {"A4 Paper Ream", 40},
        {"Glue Stick", 35},
        {"Highlighter Set", 18},
        {"Whiteboard Marker", 24},
        {"Sticky Notes", 60},
        {"Envelopes (Pack of 50)", 12},
        {"Paper Clips (Box)", 30},
        // Low Stock (1 to LOW_STOCK_THRESHOLD)
        {"Stapler", 4},
        {"Scissors", 3},
        {"Calculator", 2},
        {"Correction Tape", 5},
        // Out of Stock
        {"Ruler 30cm", 0},
        {"Printer Ink Cartridge", 0}
    };

    /**
     * Prevents instantiation; this class only offers a static method.
     *
     * @author Mashal and Umaina
     */
    private SampleData() {
        // utility class
    }

    /**
     * Adds the sample items to a service through {@link InventoryService#addItem},
     * so they pass the normal validation and are saved by the service's repository.
     *
     * @param service the service to fill, normally one with an empty inventory
     * @throws com.store.inventory.service.InventoryException if an item is rejected
     *         or cannot be saved
     * @author Mashal and Umaina
     */
    public static void seed(InventoryService service) {
        for (Object[] item : ITEMS) {
            service.addItem((String) item[0], String.valueOf(item[1]), null);
        }
    }
}
