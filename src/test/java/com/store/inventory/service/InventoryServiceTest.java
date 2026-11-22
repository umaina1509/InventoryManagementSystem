package com.store.inventory.service;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;
import com.store.inventory.storage.InMemoryInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link InventoryService}. Test names follow the OEL test-case table.
 *
 * @author Mashal and Umaina
 */
class InventoryServiceTest {

    private InMemoryInventoryRepository repository;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryInventoryRepository();
        service = new InventoryService(repository);
    }

    @Test
    @DisplayName("TC1: Add a valid item -> item is added")
    void addValidItem() {
        Item item = service.addItem("Notebook", "20", null);
        assertEquals(1, service.getAllItems().size());
        assertEquals("Notebook", item.getName());
        assertEquals(20, item.getQuantity());
        assertEquals(StockStatus.IN_STOCK, item.getStatus());
    }

    @Test
    @DisplayName("TC2: Add an item with an empty name -> validation message")
    void addItemWithEmptyName() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> service.addItem("   ", "5", null));
        assertEquals("Item name must not be empty.", e.getMessage());
        assertTrue(service.getAllItems().isEmpty());
    }

    @Test
    @DisplayName("TC3a: Add an item with a negative quantity -> error message")
    void addItemWithNegativeQuantity() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> service.addItem("Pen", "-4", null));
        assertEquals("Quantity must not be negative.", e.getMessage());
        assertTrue(service.getAllItems().isEmpty());
    }

    @Test
    @DisplayName("TC3b: Add an item with a non-numeric quantity -> error message")
    void addItemWithNonNumericQuantity() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> service.addItem("Pen", "abc", null));
        assertEquals("Quantity must be a whole number (digits only).", e.getMessage());
        assertTrue(service.getAllItems().isEmpty());
    }

    @Test
    @DisplayName("TC4: View all items -> all items returned")
    void viewAllItems() {
        service.addItem("Pen", "10", null);
        service.addItem("Pencil", "3", null);
        service.addItem("Eraser", "0", null);
        assertEquals(3, service.getAllItems().size());
    }

    @Test
    @DisplayName("TC5: View an empty inventory -> empty list (UI shows a message)")
    void viewEmptyInventory() {
        assertTrue(service.getAllItems().isEmpty());
    }

    @Test
    @DisplayName("TC6: Update an existing item -> details updated")
    void updateExistingItem() {
        Item item = service.addItem("Pen", "10", null);
        Item updated = service.updateItem(item.getId(), "Gel Pen", "2", null);
        assertEquals("Gel Pen", updated.getName());
        assertEquals(2, updated.getQuantity());
        assertEquals(StockStatus.LOW_STOCK, updated.getStatus());
    }

    @Test
    @DisplayName("TC7: Update a non-existing item -> handled gracefully")
    void updateNonExistingItem() {
        ItemNotFoundException e = assertThrows(ItemNotFoundException.class,
                () -> service.updateItem(999, "Ghost", "1", null));
        assertTrue(e.getMessage().contains("999"));
    }

    @Test
    @DisplayName("TC8: Delete an existing item -> item removed")
    void deleteExistingItem() {
        Item item = service.addItem("Pen", "10", null);
        service.deleteItem(item.getId());
        assertTrue(service.getAllItems().isEmpty());
    }

    @Test
    @DisplayName("Extra: Delete a non-existing item -> handled gracefully")
    void deleteNonExistingItem() {
        assertThrows(ItemNotFoundException.class, () -> service.deleteItem(42));
    }

    @Test
    @DisplayName("Extra: Status is derived from quantity (0 / 1-5 / 6+)")
    void statusDerivedFromQuantity() {
        assertEquals(StockStatus.OUT_OF_STOCK, service.addItem("A", "0", null).getStatus());
        assertEquals(StockStatus.LOW_STOCK, service.addItem("B", "5", null).getStatus());
        assertEquals(StockStatus.IN_STOCK, service.addItem("C", "6", null).getStatus());
    }

    @Test
    @DisplayName("Extra: Status can be set manually")
    void setStatusManually() {
        Item item = service.addItem("Pen", "10", null);
        service.setStatus(item.getId(), StockStatus.LOW_STOCK);
        assertEquals(StockStatus.LOW_STOCK, service.findById(item.getId()).getStatus());
        assertEquals(StockStatus.OUT_OF_STOCK,
                service.addItem("Ruler", "10", StockStatus.OUT_OF_STOCK).getStatus());
    }

    @Test
    @DisplayName("Extra: Duplicate names (case-insensitive) are rejected")
    void duplicateNameRejected() {
        service.addItem("Pen", "10", null);
        assertThrows(ValidationException.class, () -> service.addItem("pen", "5", null));
    }

    @Test
    @DisplayName("Extra: Failed update leaves the item unchanged")
    void failedUpdateKeepsOldValues() {
        Item item = service.addItem("Pen", "10", null);
        assertThrows(ValidationException.class,
                () -> service.updateItem(item.getId(), "Pen", "oops", null));
        assertEquals(10, service.findById(item.getId()).getQuantity());
    }

    @Test
    @DisplayName("Extra: Changes are written to the repository")
    void changesArePersisted() {
        service.addItem("Pen", "10", null);
        InventoryService reloaded = new InventoryService(repository);
        assertEquals(1, reloaded.getAllItems().size());
        assertEquals("Pen", reloaded.getAllItems().get(0).getName());
    }

    @Test
    @DisplayName("Extra: New ids continue after reload and are never reused")
    void idsContinueAfterReload() {
        Item first = service.addItem("Pen", "10", null);
        InventoryService reloaded = new InventoryService(repository);
        Item second = reloaded.addItem("Pencil", "4", null);
        assertEquals(first.getId() + 1, second.getId());
    }
}
