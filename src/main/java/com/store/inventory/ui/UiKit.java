package com.store.inventory.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Small factory helpers shared by the user-interface classes.
 *
 * <p>Keeps recurring pieces, such as the small uppercase section labels, in
 * one place so they look the same everywhere.</p>
 *
 * @author Mashal and Umaina
 */
public final class UiKit {

    /** Hair space placed between letters to imitate CSS letter-spacing. */
    private static final char HAIR_SPACE = ' ';

    /**
     * Prevents instantiation; this class only offers static helpers.
     *
     * @author Mashal and Umaina
     */
    private UiKit() {
        // utility class
    }

    /**
     * Converts text to upper case with a hair space between letters.
     *
     * <p>JavaFX CSS has no {@code letter-spacing} property, so this is used to
     * give small labels a slightly spaced-out look.</p>
     *
     * @param text the text to convert
     * @return the upper-case, letter-spaced text
     * @author Mashal and Umaina
     */
    public static String letterSpaced(String text) {
        String upper = text.toUpperCase();
        StringBuilder result = new StringBuilder(upper.length() * 2);
        for (int i = 0; i < upper.length(); i++) {
            if (i > 0) {
                result.append(HAIR_SPACE);
            }
            result.append(upper.charAt(i));
        }
        return result.toString();
    }

    /**
     * Creates a small, upper-case, letter-spaced section label.
     *
     * @param text the label text, e.g. {@code "Item details"}
     * @return the styled label
     * @author Mashal and Umaina
     */
    public static Label sectionLabel(String text) {
        Label label = new Label(letterSpaced(text));
        label.getStyleClass().add("section-label");
        return label;
    }

    /**
     * Creates an empty region that grows horizontally, pushing the nodes
     * after it to the right side of an {@link HBox}.
     *
     * @return the spacer region
     * @author Mashal and Umaina
     */
    public static Region horizontalSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
}
