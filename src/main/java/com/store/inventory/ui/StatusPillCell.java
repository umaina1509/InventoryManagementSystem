package com.store.inventory.ui;

import com.store.inventory.model.Item;
import com.store.inventory.model.StockStatus;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

/**
 * Table cell that shows a {@link StockStatus} as a small coloured pill.
 *
 * <p>The pill colour comes from the stylesheet: {@code pill-in} (sage),
 * {@code pill-low} (sand) or {@code pill-out} (terracotta).</p>
 *
 * @author Mashal and Umaina
 */
public class StatusPillCell extends TableCell<Item, StockStatus> {

    private final Label pill = new Label();

    /**
     * Creates an empty status cell that displays its content as a graphic only.
     *
     * @author Mashal and Umaina
     */
    public StatusPillCell() {
        pill.getStyleClass().add("pill");
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * Updates the pill text and colour whenever the cell is reused for another row.
     *
     * @param status the status to show, or {@code null} for an empty row
     * @param empty  {@code true} if the cell has no row to display
     * @author Mashal and Umaina
     */
    @Override
    protected void updateItem(StockStatus status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) {
            setGraphic(null);
            return;
        }
        pill.setText(status.getLabel());
        pill.getStyleClass().removeAll("pill-in", "pill-low", "pill-out");
        pill.getStyleClass().add(styleClassFor(status));
        setGraphic(pill);
    }

    /**
     * Returns the style class that colours the pill for a status.
     *
     * @param status the stock status
     * @return {@code "pill-in"}, {@code "pill-low"} or {@code "pill-out"}
     * @author Mashal and Umaina
     */
    private static String styleClassFor(StockStatus status) {
        return switch (status) {
            case IN_STOCK -> "pill-in";
            case LOW_STOCK -> "pill-low";
            case OUT_OF_STOCK -> "pill-out";
        };
    }
}
