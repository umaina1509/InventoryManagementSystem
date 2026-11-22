package com.store.inventory.ui;

import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Button that switches the whole window between the light and dark theme.
 *
 * <p>The themes are defined in the stylesheet: the light palette on
 * {@code .root} and the dark palette on {@code .root.dark}. Switching only
 * adds or removes the {@code dark} style class on the scene root, so the
 * change is instant. The button shows a moon in light mode and a sun in
 * dark mode, both drawn with {@link Icons}.</p>
 *
 * @author Mashal and Umaina
 */
public class ThemeToggle extends Button {

    /** Style class that activates the dark palette. */
    public static final String DARK_CLASS = "dark";

    /** Scene root that receives or loses the {@link #DARK_CLASS} style class. */
    private final Parent root;

    /** Whether the dark theme is active. */
    private boolean dark;

    /**
     * Creates a toggle for the given scene root, starting in light mode.
     *
     * @param root the scene root whose style class decides the theme
     * @author Mashal and Umaina
     */
    public ThemeToggle(Parent root) {
        this.root = root;
        getStyleClass().add("theme-toggle");
        setMaxWidth(Double.MAX_VALUE);
        setGraphicTextGap(10);
        setOnAction(e -> toggle());
        updateAppearance();
    }

    /**
     * Reports whether the dark theme is currently active.
     *
     * @return {@code true} in dark mode, {@code false} in light mode
     * @author Mashal and Umaina
     */
    public boolean isDark() {
        return dark;
    }

    /**
     * Switches from light to dark mode or back.
     *
     * @author Mashal and Umaina
     */
    public void toggle() {
        dark = !dark;
        if (dark) {
            root.getStyleClass().add(DARK_CLASS);
        } else {
            root.getStyleClass().remove(DARK_CLASS);
        }
        updateAppearance();
    }

    /**
     * Updates the icon and text so the button describes the theme it switches to.
     *
     * @author Mashal and Umaina
     */
    private void updateAppearance() {
        setGraphic(dark ? Icons.sun() : Icons.moon());
        setText(dark ? "Light mode" : "Dark mode");
    }
}
