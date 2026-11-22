package com.store.inventory.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Left navigation bar: the application wordmark, the navigation items and a
 * footer area (used for the light/dark theme toggle).
 *
 * <p>Exactly one navigation item is "active" at a time; it is highlighted
 * with the {@code active} style class.</p>
 *
 * @author Mashal and Umaina
 */
public class Sidebar extends VBox {

    /** Container for the navigation buttons. */
    private final VBox navItems = new VBox(4);

    /** Container at the bottom of the sidebar. */
    private final VBox footer = new VBox(8);

    /** All navigation buttons, used to move the "active" highlight. */
    private final List<Button> buttons = new ArrayList<>();

    /**
     * Creates a sidebar with a plain-text wordmark and an empty navigation list.
     *
     * @param appName the application name shown at the top
     * @param tagline a short line shown under the name
     * @author Mashal and Umaina
     */
    public Sidebar(String appName, String tagline) {
        getStyleClass().add("sidebar");

        Label wordmark = new Label(appName);
        wordmark.getStyleClass().add("wordmark");
        Label taglineLabel = new Label(tagline);
        taglineLabel.getStyleClass().add("wordmark-tagline");
        VBox brand = new VBox(2, wordmark, taglineLabel);

        Label menuLabel = UiKit.sectionLabel("Menu");
        VBox menu = new VBox(8, menuLabel, navItems);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        setSpacing(32);
        getChildren().addAll(brand, menu, spacer, footer);
    }

    /**
     * Adds a navigation item. Clicking it makes it the active item and runs the action.
     *
     * @param text   the item text, e.g. {@code "Inventory"}
     * @param action what to do when the item is clicked
     * @return the created navigation button
     * @author Mashal and Umaina
     */
    public Button addNavItem(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-item");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            setActive(button);
            action.run();
        });
        buttons.add(button);
        navItems.getChildren().add(button);
        return button;
    }

    /**
     * Highlights the given navigation item and removes the highlight from all others.
     *
     * @param active the item to highlight; must have been created by {@link #addNavItem}
     * @author Mashal and Umaina
     */
    public void setActive(Button active) {
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    /**
     * Places a node at the bottom of the sidebar, replacing any previous footer content.
     *
     * @param content the node to show in the footer
     * @author Mashal and Umaina
     */
    public void setFooter(Node content) {
        footer.getChildren().setAll(content);
    }
}
