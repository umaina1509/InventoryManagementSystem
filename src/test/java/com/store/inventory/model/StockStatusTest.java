package com.store.inventory.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link StockStatus}.
 *
 * @author Mashal and Umaina
 */
class StockStatusTest {

    @Test
    void derivesStatusFromQuantity() {
        assertEquals(StockStatus.OUT_OF_STOCK, StockStatus.fromQuantity(0));
        assertEquals(StockStatus.LOW_STOCK, StockStatus.fromQuantity(1));
        assertEquals(StockStatus.LOW_STOCK, StockStatus.fromQuantity(StockStatus.LOW_STOCK_THRESHOLD));
        assertEquals(StockStatus.IN_STOCK, StockStatus.fromQuantity(StockStatus.LOW_STOCK_THRESHOLD + 1));
    }

    @Test
    void looksUpByLabel() {
        assertEquals(StockStatus.LOW_STOCK, StockStatus.fromLabel("low stock"));
        assertNull(StockStatus.fromLabel("Auto (from quantity)"));
        assertNull(StockStatus.fromLabel(null));
    }
}
