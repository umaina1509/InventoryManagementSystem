package com.store.inventory.storage;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link CsvInventoryRepository}.
 *
 * @author Mashal Areej
 */
class CsvInventoryRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void missingFileLoadsAsEmpty() {
        CsvInventoryRepository repo = new CsvInventoryRepository(tempDir.resolve("none.csv"));
        assertTrue(repo.load().isEmpty());
    }

    @Test
    void saveThenLoadRoundTrip() {
        CsvInventoryRepository repo = new CsvInventoryRepository(tempDir.resolve("data/inv.csv"));
        repo.save(List.of(
                new Item(1, "Pen", 10, StockStatus.IN_STOCK),
                new Item(2, "Paper, A4 \"80gsm\"", 0, StockStatus.OUT_OF_STOCK)));

        List<Item> loaded = repo.load();
        assertEquals(2, loaded.size());
        assertEquals("Paper, A4 \"80gsm\"", loaded.get(1).getName());
        assertEquals(StockStatus.OUT_OF_STOCK, loaded.get(1).getStatus());
        assertEquals(10, loaded.get(0).getQuantity());
    }

    @Test
    void corruptedFileThrowsStorageException() throws IOException {
        Path file = tempDir.resolve("bad.csv");
        Files.writeString(file, "id,name,quantity,status\nnot-a-number,\"Pen\",3,IN_STOCK\n");
        assertThrows(StorageException.class, () -> new CsvInventoryRepository(file).load());
    }
}
