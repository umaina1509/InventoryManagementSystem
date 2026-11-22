package com.store.inventory.ui;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "About" page listing the project name, course, authors and technology stack.
 *
 * @author Mashal and Umaina
 */
public class AboutView extends VBox {

    /** Two-column grid holding the "key: value" rows. */
    private final GridPane details = new GridPane();

    /** Number of rows added to {@link #details} so far. */
    private int rowCount;

    /**
     * Builds the About page with its title and a card of project details.
     *
     * @author Mashal and Umaina
     */
    public AboutView() {
        getStyleClass().add("page");
        setSpacing(24);

        Label title = new Label("About");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Project information");
        subtitle.getStyleClass().add("page-subtitle");
        VBox header = new VBox(2, title, subtitle);

        ColumnConstraints keyColumn = new ColumnConstraints(140);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);
        details.getColumnConstraints().addAll(keyColumn, valueColumn);
        details.setHgap(24);
        details.setVgap(16);

        addRow("Project", "StockRoom, an Inventory Management System for a small store");
        addRow("Course", "Software Construction (OEL)");
        addRow("Authors", "Mashal and Umaina");
        addRow("Tech stack", "Java 17, JavaFX 21, Maven, JUnit 5");
        addRow("Storage", "CSV file (data/inventory.csv)");
        addRow("Typeface", "Manrope, SIL Open Font License 1.1");

        VBox card = new VBox(16, UiKit.sectionLabel("Details"), details);
        card.getStyleClass().addAll("card", "about-card");
        card.setMaxWidth(640);

        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(header, card);
    }

    /**
     * Adds one "key: value" row to the details grid.
     *
     * @param key   the row label, e.g. {@code "Authors"}
     * @param value the row value
     * @author Mashal and Umaina
     */
    private void addRow(String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("about-key");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("about-value");
        valueLabel.setWrapText(true);
        GridPane.setValignment(keyLabel, VPos.TOP);
        details.addRow(rowCount++, keyLabel, valueLabel);
    }
}
