package com.store.inventory.ui;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;
import com.store.inventory.service.InputValidator;
import com.store.inventory.service.InventoryException;
import com.store.inventory.service.InventoryService;
import com.store.inventory.service.ItemNotFoundException;
import com.store.inventory.service.ValidationException;
import com.store.inventory.storage.CsvInventoryRepository;
import com.store.inventory.storage.InMemoryInventoryRepository;
import com.store.inventory.storage.InventoryRepository;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Main window of the Inventory Management System (JavaFX), branded "StockRoom".
 *
 * <p>The window has a navigation sidebar on the left (Inventory, About and a
 * light/dark theme toggle) and the current page on the right. The inventory
 * page shows a header with search, live stat cards, the sortable items table
 * with status filter chips, and an item form. All business rules live in
 * {@link InventoryService}; this class only collects input and displays
 * results.</p>
 *
 * @author Mashal and Umaina
 */
public class MainApp extends Application {

    private static final String APP_NAME = "StockRoom";
    private static final String AUTO_STATUS = "Auto (from quantity)";
    private static final Path DATA_FILE = Paths.get("data", "inventory.csv");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

    private static final String EMPTY_INVENTORY_MESSAGE =
            "The inventory is empty. Add an item using the form on the right.";
    private static final String NO_RESULTS_MESSAGE = "No items match your search.";
    private static final String OUT_OF_STOCK_NOTE = "Quantity set to 0 because status is Out of Stock";
    private static final String NO_FILTER_RESULTS_MESSAGE = "No items match the selected filter.";
    private static final String NO_COMBINED_RESULTS_MESSAGE =
            "No items match your search and the selected filter.";

    /** Stylesheets of the main window, also applied to dialogs. */
    private final List<String> stylesheets = new ArrayList<>();

    private InventoryService service;
    private final ObservableList<Item> tableData = FXCollections.observableArrayList();
    private FilteredList<Item> filteredData;

    /** Status chosen with the filter chips, or {@code null} for "All". */
    private StockStatus statusFilter;
    private final ToggleGroup filterGroup = new ToggleGroup();

    private ThemeToggle themeToggle;
    private StatCard totalCard;
    private StatCard inStockCard;
    private StatCard lowStockCard;
    private StatCard outOfStockCard;

    private TableView<Item> table;
    private Label placeholderTitle;
    private Label placeholderBody;
    private Label countLabel;
    private TextField searchField;
    private TextField nameField;
    private TextField quantityField;
    private ComboBox<String> statusBox;
    private Label nameError;
    private Label quantityError;

    /** Muted note under Quantity, shown while the status is Out of Stock. */
    private Label quantityNote;
    private Label feedbackLabel;

    /**
     * Creates the application. JavaFX calls this constructor itself; the
     * window is built later in {@link #start(Stage)}.
     *
     * @author Mashal and Umaina
     */
    public MainApp() {
        // nothing to set up before start()
    }

    /**
     * Builds the main window and shows it.
     *
     * @param stage the primary stage supplied by the JavaFX runtime
     * @author Mashal and Umaina
     */
    @Override
    public void start(Stage stage) {
        boolean fontsLoaded = FontLoader.loadManrope();
        stylesheets.add(getClass().getResource("/styles/inventory.css").toExternalForm());
        if (!fontsLoaded) {
            stylesheets.add(getClass().getResource("/styles/fallback-fonts.css").toExternalForm());
        }
        service = createService();

        Region inventoryView = buildInventoryView();
        Region aboutView = new AboutView();
        StackPane contentArea = new StackPane(inventoryView);

        Sidebar sidebar = new Sidebar(APP_NAME, "Inventory management");
        Button inventoryNav = sidebar.addNavItem("Inventory",
                () -> contentArea.getChildren().setAll(inventoryView));
        sidebar.addNavItem("About", () -> contentArea.getChildren().setAll(aboutView));
        sidebar.setActive(inventoryNav);

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        themeToggle = new ThemeToggle(root);
        sidebar.setFooter(themeToggle);

        refreshTable();

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().addAll(stylesheets);

        stage.setTitle(APP_NAME + " - Inventory Management System");
        stage.setMinWidth(1060);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    /**
     * Creates the service, falling back to memory-only storage if the data file is unreadable.
     *
     * <p>On the very first start (no data file yet) the inventory is filled
     * with {@link SampleData}. If the file exists, even when it is empty, it
     * is loaded as it is and nothing is added, so an inventory the user has
     * emptied stays empty.</p>
     *
     * @return the inventory service used by the window
     * @author Mashal and Umaina
     */
    private InventoryService createService() {
        boolean firstRun = !Files.exists(DATA_FILE);
        try {
            InventoryRepository repository = new CsvInventoryRepository(DATA_FILE);
            InventoryService created = new InventoryService(repository);
            if (firstRun) {
                SampleData.seed(created);
            }
            return created;
        } catch (InventoryException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Storage problem");
            alert.setHeaderText("Saved inventory could not be loaded");
            alert.setContentText(e.getMessage() + "\n\nThe application will start with an empty, "
                    + "temporary inventory that is not saved to disk.");
            styleDialog(alert);
            alert.showAndWait();
            return new InventoryService(new InMemoryInventoryRepository());
        }
    }

    /**
     * Builds the inventory page: header, stat cards, then the table and the
     * item form side by side.
     *
     * @return the inventory page
     * @author Mashal and Umaina
     */
    private Region buildInventoryView() {
        Region header = buildHeader(); // creates the search field used by the table filter
        HBox body = new HBox(16, buildTableCard(), buildFormCard());
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox page = new VBox(24, header, buildStatRow(), body);
        page.getStyleClass().add("page");
        return page;
    }

    /**
     * Builds the page header: title, today's date and the search field.
     *
     * @return the header row
     * @author Mashal and Umaina
     */
    private Region buildHeader() {
        Label title = new Label("Inventory");
        title.getStyleClass().add("page-title");
        Label date = new Label(LocalDate.now().format(DATE_FORMAT));
        date.getStyleClass().add("page-subtitle");
        VBox titles = new VBox(2, title, date);

        searchField = new TextField();
        searchField.setPromptText("Search items by name");
        SVGPath searchIcon = Icons.search();
        searchIcon.setMouseTransparent(true);
        StackPane searchBox = new StackPane(searchField, searchIcon);
        searchBox.getStyleClass().add("search-box");
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 11));

        HBox header = new HBox(16, titles, UiKit.horizontalSpacer(), searchBox);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /**
     * Builds the row of four stat cards (total and one per stock status).
     *
     * @return the stat card row
     * @author Mashal and Umaina
     */
    private Region buildStatRow() {
        totalCard = new StatCard("Total items", null);
        inStockCard = new StatCard(StockStatus.IN_STOCK.getLabel(), "dot-in");
        lowStockCard = new StatCard(StockStatus.LOW_STOCK.getLabel(), "dot-low");
        outOfStockCard = new StatCard(StockStatus.OUT_OF_STOCK.getLabel(), "dot-out");

        HBox row = new HBox(16, totalCard, inStockCard, lowStockCard, outOfStockCard);
        for (StatCard card : new StatCard[] {totalCard, inStockCard, lowStockCard, outOfStockCard}) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }
        return row;
    }

    /**
     * Builds the status filter chips: All, In Stock, Low Stock and Out of Stock.
     *
     * @return the row of chips
     * @author Mashal and Umaina
     */
    private HBox buildFilterChips() {
        HBox chips = new HBox(8, createChip("All", null));
        for (StockStatus status : StockStatus.values()) {
            chips.getChildren().add(createChip(status.getLabel(), status));
        }
        filterGroup.selectToggle(filterGroup.getToggles().get(0));
        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                filterGroup.selectToggle(oldToggle); // one chip is always selected
                return;
            }
            statusFilter = (StockStatus) newToggle.getUserData();
            applyFilter();
        });
        chips.setAlignment(Pos.CENTER_LEFT);
        return chips;
    }

    /**
     * Creates one filter chip.
     *
     * @param text   the chip text
     * @param status the status the chip filters on, or {@code null} for "All"
     * @return the chip
     * @author Mashal and Umaina
     */
    private ToggleButton createChip(String text, StockStatus status) {
        ToggleButton chip = new ToggleButton(text);
        chip.getStyleClass().add("chip");
        chip.setUserData(status);
        chip.setToggleGroup(filterGroup);
        return chip;
    }

    /**
     * Builds the card that holds the filter chips and the items table.
     *
     * @return the table card
     * @author Mashal and Umaina
     */
    private Region buildTableCard() {
        countLabel = new Label();
        countLabel.getStyleClass().add("count-label");
        HBox cardHeader = new HBox(8, buildFilterChips(), UiKit.horizontalSpacer(), countLabel);
        cardHeader.getStyleClass().add("card-header");
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        table = buildTable();

        VBox card = new VBox(8, cardHeader, table);
        card.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /**
     * Builds the items table with its columns, placeholder, filtering and sorting.
     * Clicking a column header sorts by that column.
     *
     * @return the configured table
     * @author Mashal and Umaina
     */
    private TableView<Item> buildTable() {
        TableView<Item> itemTable = new TableView<>();
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Item, Number> idColumn = new TableColumn<>(UiKit.letterSpaced("ID"));
        idColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        idColumn.getStyleClass().add("id-cell");
        idColumn.setPrefWidth(70);

        TableColumn<Item, String> nameColumn = new TableColumn<>(UiKit.letterSpaced("Name"));
        nameColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        nameColumn.setPrefWidth(320);

        TableColumn<Item, Number> quantityColumn = new TableColumn<>(UiKit.letterSpaced("Quantity"));
        quantityColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQuantity()));
        quantityColumn.getStyleClass().add("numeric");
        quantityColumn.setPrefWidth(120);

        TableColumn<Item, StockStatus> statusColumn = new TableColumn<>(UiKit.letterSpaced("Status"));
        statusColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new StatusPillCell());
        statusColumn.setPrefWidth(160);

        itemTable.getColumns().add(idColumn);
        itemTable.getColumns().add(nameColumn);
        itemTable.getColumns().add(quantityColumn);
        itemTable.getColumns().add(statusColumn);

        placeholderTitle = new Label();
        placeholderTitle.getStyleClass().add("placeholder-title");
        placeholderBody = new Label();
        placeholderBody.getStyleClass().add("placeholder-body");
        VBox placeholder = new VBox(4, placeholderTitle, placeholderBody);
        placeholder.setAlignment(Pos.CENTER);
        itemTable.setPlaceholder(placeholder);

        filteredData = new FilteredList<>(tableData, item -> true);
        SortedList<Item> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(itemTable.comparatorProperty());
        itemTable.setItems(sortedData);
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilter());

        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                populateForm(newItem);
            }
        });
        return itemTable;
    }

    /**
     * Builds the "Item details" form card with its fields, buttons and messages.
     *
     * @return the form card
     * @author Mashal and Umaina
     */
    private Region buildFormCard() {
        nameField = new TextField();
        nameField.setPromptText("e.g. Notebook A4");
        nameError = createErrorLabel();

        quantityField = new TextField();
        quantityField.setPromptText("0 or more");
        quantityError = createErrorLabel();
        quantityNote = new Label(OUT_OF_STOCK_NOTE);
        quantityNote.getStyleClass().add("field-note");
        quantityNote.setWrapText(true);
        quantityNote.setMinHeight(Region.USE_PREF_SIZE);
        quantityNote.managedProperty().bind(quantityNote.visibleProperty());
        quantityNote.setVisible(false);

        statusBox = new ComboBox<>();
        statusBox.getItems().add(AUTO_STATUS);
        for (StockStatus status : StockStatus.values()) {
            statusBox.getItems().add(status.getLabel());
        }
        statusBox.setValue(AUTO_STATUS);
        statusBox.setMaxWidth(Double.MAX_VALUE);

        nameField.textProperty().addListener((obs, o, n) -> clearFieldError(nameField, nameError));
        quantityField.textProperty().addListener((obs, o, n) -> clearFieldError(quantityField, quantityError));
        statusBox.valueProperty().addListener((obs, o, n) -> applyOutOfStockRule());

        VBox fields = new VBox(16,
                createField("Name", nameField, nameError),
                createField("Quantity", quantityField, quantityError, quantityNote),
                createField("Status", statusBox));

        Button addButton = new Button("Add item");
        addButton.getStyleClass().add("primary");
        addButton.setDefaultButton(true);
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(e -> onAdd());

        Button updateButton = new Button("Update");
        updateButton.setMaxWidth(Double.MAX_VALUE);
        updateButton.setOnAction(e -> onUpdate());

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("danger");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setOnAction(e -> onDelete());

        HBox.setHgrow(updateButton, Priority.ALWAYS);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);
        HBox secondaryButtons = new HBox(8, updateButton, deleteButton);

        Button clearButton = new Button("Clear form");
        clearButton.getStyleClass().add("text-button");
        clearButton.setOnAction(e -> {
            clearForm();
            feedbackLabel.setText("");
            table.getSelectionModel().clearSelection();
        });

        VBox buttons = new VBox(8, addButton, secondaryButtons, clearButton);

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMinHeight(Region.USE_PREF_SIZE);
        // Takes no space (and adds no gap) while there is no message.
        feedbackLabel.managedProperty().bind(feedbackLabel.textProperty().isNotEmpty());

        Label hint = new Label("Select a row in the table to edit or delete it. "
                + "With \"Auto\", the status follows the quantity: 0 is Out of Stock, "
                + "1 to " + StockStatus.LOW_STOCK_THRESHOLD + " is Low Stock.");
        hint.getStyleClass().add("hint");
        hint.setWrapText(true);
        hint.setMinHeight(Region.USE_PREF_SIZE); // never cut the hint down to "..."
        VBox hintBox = new VBox(hint); // grows to push the hint to the bottom of the card
        hintBox.setAlignment(Pos.BOTTOM_LEFT);
        VBox.setVgrow(hintBox, Priority.ALWAYS);

        VBox content = new VBox(16, UiKit.sectionLabel("Item details"), fields, buttons,
                feedbackLabel, hintBox);
        content.getStyleClass().add("form-content");

        // Scrolls only when the window is too short to show the whole form.
        ScrollPane scroller = new ScrollPane(content);
        scroller.getStyleClass().add("form-scroll");
        scroller.setFitToWidth(true);
        scroller.setFitToHeight(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        StackPane card = new StackPane(scroller);
        card.getStyleClass().addAll("card", "form-card");
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        return card;
    }

    /**
     * Groups a field label, an input control and any message labels shown under it.
     *
     * @param labelText the field label text
     * @param input     the input control
     * @param messages  labels placed under the input, such as the inline error
     *                  and the Out of Stock note; may be empty
     * @return the field group
     * @author Mashal and Umaina
     */
    private VBox createField(String labelText, Control input, Label... messages) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        label.setLabelFor(input);
        VBox group = new VBox(6, label, input);
        group.getChildren().addAll(messages);
        return group;
    }

    /**
     * Creates a hidden label for inline validation messages under a field.
     *
     * @return the error label; it only takes up space while it is visible
     * @author Mashal and Umaina
     */
    private Label createErrorLabel() {
        Label error = new Label();
        error.getStyleClass().add("field-error");
        error.setWrapText(true);
        error.setMinHeight(Region.USE_PREF_SIZE);
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);
        return error;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    /**
     * Adds a new item from the form after checking the fields. An item added
     * with status Out of Stock is always saved with quantity 0.
     *
     * @author Mashal and Umaina
     */
    private void onAdd() {
        feedbackLabel.setText("");
        applyOutOfStockRule(); // overrides a number typed after choosing Out of Stock
        if (!validateFields()) {
            return;
        }
        try {
            Item added = service.addItem(nameField.getText(), quantityField.getText(), selectedStatus());
            clearForm();
            refreshTable();
            table.getSelectionModel().clearSelection();
            showSuccess("Added \"" + added.getName() + "\" (ID " + added.getId() + ").");
        } catch (ValidationException e) {
            // Name and quantity were already checked, so this is the duplicate-name rule.
            showFieldError(nameField, nameError, e.getMessage());
        } catch (InventoryException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Saves the form values to the item selected in the table. An item saved
     * with status Out of Stock always gets quantity 0.
     *
     * @author Mashal and Umaina
     */
    private void onUpdate() {
        feedbackLabel.setText("");
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an item in the table before updating.");
            return;
        }
        applyOutOfStockRule(); // overrides a number typed after choosing Out of Stock
        if (!validateFields()) {
            return; // keep the selection so the user can correct the input
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
        } catch (ValidationException e) {
            showFieldError(nameField, nameError, e.getMessage());
        } catch (InventoryException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Deletes the item selected in the table after the user confirms.
     *
     * @author Mashal and Umaina
     */
    private void onDelete() {
        feedbackLabel.setText("");
        Item selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an item in the table before deleting.");
            return;
        }
        ButtonType deleteType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getName() + "\"? This cannot be undone.",
                deleteType, ButtonType.CANCEL);
        confirm.setTitle("Confirm delete");
        confirm.setHeaderText(null);
        confirm.setGraphic(null);
        styleDialog(confirm);
        confirm.getDialogPane().lookupButton(deleteType).getStyleClass().add("danger");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != deleteType) {
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

    /**
     * Checks the name and quantity fields with {@link InputValidator} and shows
     * an inline message under every field that is invalid.
     *
     * @return {@code true} if both fields are valid
     * @author Mashal and Umaina
     */
    private boolean validateFields() {
        boolean valid = true;
        try {
            InputValidator.validateName(nameField.getText());
        } catch (ValidationException e) {
            showFieldError(nameField, nameError, e.getMessage());
            valid = false;
        }
        try {
            InputValidator.parseQuantity(quantityField.getText());
        } catch (ValidationException e) {
            showFieldError(quantityField, quantityError, e.getMessage());
            valid = false;
        }
        return valid;
    }

    /**
     * Shows a validation message under a field and marks the field as invalid.
     *
     * @param field   the invalid input field
     * @param error   the label under the field
     * @param message the message to show
     * @author Mashal and Umaina
     */
    private void showFieldError(TextField field, Label error, String message) {
        if (!field.getStyleClass().contains("invalid")) {
            field.getStyleClass().add("invalid");
        }
        error.setText(message);
        error.setVisible(true);
    }

    /**
     * Removes the validation message and invalid marking from a field.
     *
     * @param field the input field
     * @param error the label under the field
     * @author Mashal and Umaina
     */
    private void clearFieldError(TextField field, Label error) {
        field.getStyleClass().remove("invalid");
        error.setText("");
        error.setVisible(false);
    }

    /**
     * Returns the status chosen in the form.
     *
     * @return the chosen status, or {@code null} for "Auto"
     * @author Mashal and Umaina
     */
    private StockStatus selectedStatus() {
        return StockStatus.fromLabel(statusBox.getValue());
    }

    /**
     * Applies the rule "Out of Stock means quantity 0" to the form.
     *
     * <p>If the chosen status is Out of Stock, the Quantity field is set to
     * "0" and a short note explains why. For any other status the note is
     * hidden and the quantity is left as it is (an earlier quantity is not
     * restored), so Auto, In Stock and Low Stock behave as before.</p>
     *
     * @author Mashal and Umaina
     */
    private void applyOutOfStockRule() {
        boolean outOfStock = selectedStatus() == StockStatus.OUT_OF_STOCK;
        if (outOfStock && !"0".equals(quantityField.getText())) {
            quantityField.setText("0");
        }
        quantityNote.setVisible(outOfStock);
    }

    /**
     * Copies an item's values into the form.
     *
     * @param item the item to show
     * @author Mashal and Umaina
     */
    private void populateForm(Item item) {
        nameField.setText(item.getName());
        quantityField.setText(String.valueOf(item.getQuantity()));
        boolean followsQuantity = item.getStatus() == StockStatus.fromQuantity(item.getQuantity());
        statusBox.setValue(followsQuantity ? AUTO_STATUS : item.getStatus().getLabel());
        applyOutOfStockRule(); // also needed when the status value did not change
        clearFieldError(nameField, nameError);
        clearFieldError(quantityField, quantityError);
        feedbackLabel.setText("");
    }

    /**
     * Empties the form fields and removes any inline validation messages.
     *
     * @author Mashal and Umaina
     */
    private void clearForm() {
        nameField.clear();
        quantityField.clear();
        statusBox.setValue(AUTO_STATUS);
        clearFieldError(nameField, nameError);
        clearFieldError(quantityField, quantityError);
    }

    /**
     * Reloads the table from the service and updates the stat cards, counts
     * and placeholder.
     *
     * @author Mashal and Umaina
     */
    private void refreshTable() {
        tableData.setAll(service.getAllItems());
        totalCard.setValue(service.getAllItems().size());
        inStockCard.setValue(service.countByStatus(StockStatus.IN_STOCK));
        lowStockCard.setValue(service.countByStatus(StockStatus.LOW_STOCK));
        outOfStockCard.setValue(service.countByStatus(StockStatus.OUT_OF_STOCK));
        updateCountsAndPlaceholder();
    }

    /**
     * Filters the table by the search text combined with the selected status chip.
     *
     * @author Mashal and Umaina
     */
    private void applyFilter() {
        String query = currentQuery();
        StockStatus status = statusFilter;
        filteredData.setPredicate(item ->
                (query.isEmpty() || item.getName().toLowerCase().contains(query))
                && (status == null || item.getStatus() == status));
        updateCountsAndPlaceholder();
    }

    /**
     * Returns the search text, trimmed and in lower case.
     *
     * @return the search query; empty if the search field is empty
     * @author Mashal and Umaina
     */
    private String currentQuery() {
        String text = searchField.getText();
        return text == null ? "" : text.trim().toLowerCase();
    }

    /**
     * Updates the "x of y items" label and the message shown when the table is empty.
     *
     * @author Mashal and Umaina
     */
    private void updateCountsAndPlaceholder() {
        int total = service.getAllItems().size();
        int shown = filteredData.size();
        String noun = total == 1 ? " item" : " items";
        countLabel.setText(shown == total ? total + noun : shown + " of " + total + noun);

        if (total == 0) {
            placeholderTitle.setText("No items yet");
            placeholderBody.setText(EMPTY_INVENTORY_MESSAGE);
        } else {
            boolean searching = !currentQuery().isEmpty();
            boolean filtering = statusFilter != null;
            placeholderTitle.setText("Nothing found");
            if (searching && filtering) {
                placeholderBody.setText(NO_COMBINED_RESULTS_MESSAGE);
            } else if (filtering) {
                placeholderBody.setText(NO_FILTER_RESULTS_MESSAGE);
            } else {
                placeholderBody.setText(NO_RESULTS_MESSAGE);
            }
        }
    }

    /**
     * Selects the table row of the item with the given id, if it is visible.
     *
     * @param id the item id
     * @author Mashal and Umaina
     */
    private void selectById(int id) {
        for (Item item : table.getItems()) {
            if (item.getId() == id) {
                table.getSelectionModel().select(item);
                return;
            }
        }
    }

    /**
     * Gives a dialog the same stylesheets and current theme as the main window.
     *
     * @param alert the dialog to style before it is shown
     * @author Mashal and Umaina
     */
    private void styleDialog(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().addAll(stylesheets);
        if (themeToggle != null && themeToggle.isDark()) {
            pane.getStyleClass().add(ThemeToggle.DARK_CLASS);
        }
    }

    /**
     * Shows an error message under the form buttons.
     *
     * @param message the message to show
     * @author Mashal and Umaina
     */
    private void showError(String message) {
        feedbackLabel.getStyleClass().setAll("label", "feedback-error");
        feedbackLabel.setText(message);
    }

    /**
     * Shows a success message under the form buttons.
     *
     * @param message the message to show
     * @author Mashal and Umaina
     */
    private void showSuccess(String message) {
        feedbackLabel.getStyleClass().setAll("label", "feedback-ok");
        feedbackLabel.setText(message);
    }
}
