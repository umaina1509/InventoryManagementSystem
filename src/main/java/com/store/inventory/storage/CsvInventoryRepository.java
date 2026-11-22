package com.store.inventory.storage;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the inventory in a CSV file so data survives application restarts.
 *
 * <p>File format: a header line followed by one line per item with the columns
 * {@code id,name,quantity,status}. Names are quoted and embedded quotes are doubled.</p>
 *
 * @author YOUR_NAME
 */
public class CsvInventoryRepository implements InventoryRepository {

    private static final String HEADER = "id,name,quantity,status";

    private final Path file;

    /**
     * Creates a repository backed by the given file.
     *
     * @param file the CSV file; it is created on the first save if it does not exist
     */
    public CsvInventoryRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Item> load() {
        List<Item> items = new ArrayList<>();
        if (!Files.exists(file)) {
            return items;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) { // line 0 is the header
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                items.add(parseLine(line, i + 1));
            }
        } catch (IOException e) {
            throw new StorageException("Could not read inventory file: " + file, e);
        }
        return items;
    }

    @Override
    public void save(List<Item> items) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Item item : items) {
            lines.add(item.getId() + "," + quote(item.getName()) + ","
                    + item.getQuantity() + "," + item.getStatus().name());
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not write inventory file: " + file, e);
        }
    }

    private static String quote(String text) {
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private Item parseLine(String line, int lineNumber) {
        try {
            int firstComma = line.indexOf(',');
            int lastComma = line.lastIndexOf(',');
            int secondLastComma = line.lastIndexOf(',', lastComma - 1);
            if (firstComma < 0 || secondLastComma <= firstComma) {
                throw new IllegalArgumentException("wrong number of columns");
            }
            int id = Integer.parseInt(line.substring(0, firstComma).trim());
            String rawName = line.substring(firstComma + 1, secondLastComma).trim();
            int quantity = Integer.parseInt(line.substring(secondLastComma + 1, lastComma).trim());
            StockStatus status = StockStatus.valueOf(line.substring(lastComma + 1).trim());
            return new Item(id, unquote(rawName), quantity, status);
        } catch (IllegalArgumentException e) {
            throw new StorageException(
                    "Inventory file is corrupted at line " + lineNumber + ": " + file, e);
        }
    }

    private static String unquote(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1).replace("\"\"", "\"");
        }
        return text;
    }
}
