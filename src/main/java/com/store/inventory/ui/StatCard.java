package com.store.inventory.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Small summary card showing one number, such as the count of low-stock items.
 *
 * <p>The card has a small upper-case title, optionally preceded by a coloured
 * dot that matches a stock status, and a large number below it.</p>
 *
 * @author Mashal and Umaina
 */
public class StatCard extends VBox {

    /** Large label showing the number. */
    private final Label valueLabel = new Label("0");

    /**
     * Creates a stat card showing the value 0.
     *
     * @param title         the card title, e.g. {@code "Low Stock"}
     * @param dotStyleClass style class that colours the status dot (e.g. {@code "dot-low"}),
     *                      or {@code null} for a card without a dot
     * @author Mashal and Umaina
     */
    public StatCard(String title, String dotStyleClass) {
        getStyleClass().addAll("card", "stat-card");

        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        if (dotStyleClass != null) {
            Region dot = new Region();
            dot.getStyleClass().addAll("stat-dot", dotStyleClass);
            titleRow.getChildren().add(dot);
        }
        titleRow.getChildren().add(UiKit.sectionLabel(title));

        valueLabel.getStyleClass().add("stat-value");

        setSpacing(8);
        setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(titleRow, valueLabel);
    }

    /**
     * Changes the number shown on the card.
     *
     * @param value the new value
     * @author Mashal and Umaina
     */
    public void setValue(long value) {
        valueLabel.setText(String.valueOf(value));
    }
}
