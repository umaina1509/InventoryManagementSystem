package com.store.inventory.ui;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;
import com.store.inventory.service.InventoryException;
import com.store.inventory.service.InventoryService;
import com.store.inventory.service.ItemNotFoundException;
import com.store.inventory.storage.CsvInventoryRepository;
import com.store.inventory.storage.InMemoryInventoryRepository;
import com.store.inventory.storage.InventoryRepository;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Main window of the Inventory Management System (JavaFX).
 *
 * <p>The window shows the inventory in a table on the left and an item form on
 * the right. All business rules live in {@link InventoryService}; this class
 * only collects input and displays results.</p>
 *
 * @author Mashal and Umaina
 */
public class MainApp extends Application {

    private static final String AUTO_STATUS = "Auto (from quantity)";
    private static final Path DATA_FILE = Paths.get("data", "inventory.csv");

    private InventoryService service;
    private final ObservableList<Item> tableData = FXCollections.observableArrayList();
    private FilteredList<Item> filteredData;

    private TableView<Item> table;
    private Label tablePlaceholder;
    private TextField searchField;
    private TextField nameField;
    private TextField quantityField;
    private ComboBox<String> statusBox;
    private Label feedbackLabel;
    private Label summaryLabel;

    /**
     * Builds the main window and shows it.
     *
     * @param stage the primary stage supplied by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        service = createService();

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        root.setBottom(buildStatusBar());

        refreshTable();

        Scene scene = new Scene(root, 980, 620);
        scene.getStylesheets().add(getClass().getResource("/styles/inventory.css").toExternalForm());

        stage.setTitle("Inventory Management System");
        stage.setMinWidth(860);
        stage.setMinHeight(540);
        stage.setScene(scene);
        stage.show();
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    /**
     * Creates the service, falling back to memory-only storage if the data file is unreadable.
     */
    private InventoryService createService() {
        try {
            InventoryRepository repository = new CsvInventoryRepository(DATA_FILE);
            return new InventoryService(repository);
        } catch (InventoryException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Storage problem");
            alert.setHeaderText("Saved inventory could not be loaded");
            alert.setContentText(e.getMessage() + "\n\nThe application will start with an empty, "
                    + "temporary inventory that is not saved to disk.");
            alert.showAndWait();
            return new InventoryService(new InMemoryInventoryRepository());
        }
    }

    private Region buildHeader() {
        Label title = new Label("Inventory Management");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Store stock control");
        subtitle.getStyleClass().add("app-subtitle");

        VBox box = new VBox(2, title, subtitle);
        box.getStyleClass().add("header");
        return box;
    }

    private Region buildContent() {
        HBox content = new HBox(16, buildTablePane(), buildFormPane());
        content.setPadding(new Insets(16));
        return content;
    }

    private Region buildTablePane() {
        Label heading = new Label("Items");
        heading.getStyleClass().add("section-title");

        searchField = new TextField();
        searchField.setPromptText("Search by name");
        searchField.setPrefWidth(240);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(8, heading, spacer, searchField);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Item, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        idColumn.setPrefWidth(60);

        TableColumn<Item, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        nameColumn.setPrefWidth(280);

        TableColumn<Item, Number> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        quantityColumn.setPrefWidth(100);
        quantityColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Item, StockStatus> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(StockStatus status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-in", "status-low", "status-out");
                if (empty || status == null) {
                    setText(null);
                    return;
                }
                setText(status.getLabel());
                switch (status) {
                    case IN_STOCK -> getStyleClass().add("status-in");
                    case LOW_STOCK -> getStyleClass().add("status-low");
                    case OUT_OF_STOCK -> getStyleClass().add("status-out");
                }
            }
        });
        statusColumn.setPrefWidth(140);

        table.getColumns().add(idColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(quantityColumn);
        table.getColumns().add(statusColumn);

        tablePlaceholder = new Label();
        table.setPlaceholder(tablePlaceholder);

        filteredData = new FilteredList<>(tableData, item -> true);
        table.setItems(filteredData);
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();
            filteredData.setPredicate(item -> query.isEmpty()
                    || item.getName().toLowerCase().contains(query));
            updatePlaceholder();
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                populateForm(newItem);
            }
        });

        VBox pane = new VBox(10, toolbar, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(pane, Priority.ALWAYS);
        return pane;
    }

    private Region buildFormPane() {
        Label heading = new Label("Item details");
        heading.getStyleClass().add("section-title");

        nameField = new TextField();
        nameField.setPromptText("e.g. Notebook A4");

        quantityField = new TextField();
        quantityField.setPromptText("0 or more");

        statusBox = new ComboBox<>();
        statusBox.getItems().add(AUTO_STATUS);
        for (StockStatus status : StockStatus.values()) {
            statusBox.getItems().add(status.getLabel());
        }
        statusBox.setValue(AUTO_STATUS);
        statusBox.setMaxWidth(Double.MAX_VALUE);

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(10);
        fields.add(new Label("Name"), 0, 0);
        fields.add(nameField, 1, 0);
        fields.add(new Label("Quantity"), 0, 1);
        fields.add(quantityField, 1, 1);
        fields.add(new Label("Status"), 0, 2);
        fields.add(statusBox, 1, 2);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(quantityField, Priority.ALWAYS);
        GridPane.setHgrow(statusBox, Priority.ALWAYS);

        Button addButton = new Button("Add Item");
        addButton.getStyleClass().add("primary");
        addButton.setDefaultButton(true);
        addButton.setOnAction(e -> onAdd());

        Button updateButton = new Button("Update Item");
        updateButton.setOnAction(e -> onUpdate());

        Button deleteButton = new Button("Delete Item");
        deleteButton.setOnAction(e -> onDelete());

        Button clearButton = new Button("Clear Form");
        clearButton.setOnAction(e -> {
            clearForm();
            table.getSelectionModel().clearSelection();
        });

        GridPane buttons = new GridPane();
        buttons.setHgap(8);
        buttons.setVgap(8);
        buttons.add(addButton, 0, 0);
        buttons.add(updateButton, 1, 0);
        buttons.add(deleteButton, 0, 1);
        buttons.add(clearButton, 1, 1);
        for (Button b : new Button[] {addButton, updateButton, deleteButton, clearButton}) {
            b.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(b, Priority.ALWAYS);
        }

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMinHeight(40);
        feedbackLabel.setAlignment(Pos.TOP_LEFT);

        Label hint = new Label("Select a row in the table to edit or delete it. "
                + "With \"Auto\", the status follows the quantity: 0 is Out of Stock, "
                + "1 to " + StockStatus.LOW_STOCK_THRESHOLD + " is Low Stock.");
        hint.getStyleClass().add("hint");
        hint.setWrapText(true);

        VBox pane = new VBox(12, heading, fields, buttons, feedbackLabel, hint);
        pane.getStyleClass().add("form-pane");
        pane.setPadding(new Insets(16));
        pane.setPrefWidth(310);
        pane.setMinWidth(310);
        pane.setMaxWidth(310);
        return pane;
    }

    private Region buildStatusBar() {
        summaryLabel = new Label();
        HBox bar = new HBox(summaryLabel);
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void onAdd() {
        try {
            Item added = service.addItem(nameField.getText(), quantityField.getText(), selectedStatus());
            clearForm();
            refreshTable();
            table.getSelectionModel().clearSelection();
            showSuccess("Added \"" + added.getName() + "\" (ID " + added.getId() + ").");
        } catch (InventoryException e) {
            showError(e.getMessage());
        }
    }

    private void onUpdate() {
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an item in the table before updating.");
            return;
        }
        try {
            Item updated = service.updateItem(selected.getId(), nameField.getText(),
                    quantityField.getText(), selectedStatus());
            refreshTable();
            selectById(updated.getId());
            showSuccess("Updated \"" + updated.getName() + "\".");
        } catch (ItemNotFoundException e) {
            showError(e.getMessage());
            refreshTable(); // the item no longer exists; keep the table in sync
        } catch (InventoryException e) {
            showError(e.getMessage()); // validation error: keep the selection so the user can retry
        }
    }

    private void onDelete() {
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an item in the table before deleting.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getName() + "\"? This cannot be undone.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm delete");
        confirm.setHeaderText(null);
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }
        try {
            Item removed = service.deleteItem(selected.getId());
            clearForm();
            refreshTable();
            table.getSelectionModel().clearSelection();
            showSuccess("Deleted \"" + removed.getName() + "\".");
        } catch (InventoryException e) {
            showError(e.getMessage());
            refreshTable(); // the item no longer exists; keep the table in sync
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Returns the status chosen in the form, or {@code null} for "Auto". */
    private StockStatus selectedStatus() {
        return StockStatus.fromLabel(statusBox.getValue());
    }

    private void populateForm(Item item) {
        nameField.setText(item.getName());
        quantityField.setText(String.valueOf(item.getQuantity()));
        boolean followsQuantity = item.getStatus() == StockStatus.fromQuantity(item.getQuantity());
        statusBox.setValue(followsQuantity ? AUTO_STATUS : item.getStatus().getLabel());
        feedbackLabel.setText("");
    }

    private void clearForm() {
        nameField.clear();
        quantityField.clear();
        statusBox.setValue(AUTO_STATUS);
    }

    private void refreshTable() {
        tableData.setAll(service.getAllItems());
        updatePlaceholder();
        summaryLabel.setText(String.format("Total items: %d     In Stock: %d     Low Stock: %d     Out of Stock: %d",
                service.getAllItems().size(),
                service.countByStatus(StockStatus.IN_STOCK),
                service.countByStatus(StockStatus.LOW_STOCK),
                service.countByStatus(StockStatus.OUT_OF_STOCK)));
    }

    private void updatePlaceholder() {
        if (service.getAllItems().isEmpty()) {
            tablePlaceholder.setText("The inventory is empty. Add an item using the form on the right.");
        } else {
            tablePlaceholder.setText("No items match your search.");
        }
    }

    private void selectById(int id) {
        for (Item item : table.getItems()) {
            if (item.getId() == id) {
                table.getSelectionModel().select(item);
                return;
            }
        }
    }

    private void showError(String message) {
        feedbackLabel.getStyleClass().setAll("label", "feedback-error");
        feedbackLabel.setText(message);
    }

    private void showSuccess(String message) {
        feedbackLabel.getStyleClass().setAll("label", "feedback-ok");
        feedbackLabel.setText(message);
    }
}
