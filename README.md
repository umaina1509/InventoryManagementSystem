# Inventory Management System

A JavaFX desktop application for managing the stock of a small store. Each item has a name, a quantity and a stock status (In Stock, Low Stock or Out of Stock).

Developed for the Software Construction Open-Ended Lab (CLO-4 and CLO-5) at Military College of Signals, NUST.

**Authors:** Mashal Areej (506018) and Umaina Tanveer (507732)

## Features

- Add, view, update and delete items
- Set stock status manually, or leave it on Auto so it follows the quantity
- Input validation: empty names, negative quantities and non-numeric quantities are rejected with a clear message
- Duplicate item names are rejected (case-insensitive)
- Search by name, filter by status and sort by any column
- Summary cards showing total, in stock, low stock and out of stock counts
- Light and dark theme
- Data is saved automatically to a CSV file and loaded on the next start

## Stock status rules

| Quantity | Status (Auto) |
|----------|---------------|
| 0        | Out of Stock  |
| 1 to 5   | Low Stock     |
| 6 or more| In Stock      |

Setting an item to Out of Stock manually sets its quantity to 0.

## Requirements

- JDK 17 or newer
- Apache Maven 3.8 or newer
- An internet connection on the first build, to download dependencies

## How to run

```
git clone https://github.com/umaina1509/InventoryManagementSystem.git
cd InventoryManagementSystem
mvn javafx:run
```

The first run creates a `data` folder containing `inventory.csv`. On the first launch the application is filled with sample items.

## How to run the tests

```
mvn test
```

## How to generate the Javadoc

```
mvn javadoc:javadoc
```

The HTML documentation is written to `docs/javadoc`. Open `docs/javadoc/index.html` in a browser.

## Project structure

```
src/main/java/com/store/inventory/
    model/      Item, StockStatus
    service/    InventoryService, InputValidator, custom exceptions
    storage/    InventoryRepository, CsvInventoryRepository, InMemoryInventoryRepository
    ui/         Launcher, MainApp and interface components
src/main/resources/   stylesheet and font
src/test/java/        JUnit 5 tests
docs/javadoc/         generated API documentation
pom.xml               Maven build file
```

## Design overview

The application has four layers. The user interface calls the service, the service applies the business rules and calls the repository interface, and the repository reads and writes the CSV file. The model classes are shared by all layers.

```
MainApp -> InventoryService -> InventoryRepository -> data/inventory.csv
```

## Technologies

Java, JavaFX 21, Apache Maven, JUnit 5, Git and GitHub, Javadoc, Visual Studio Code