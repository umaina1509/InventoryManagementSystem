package com.store.inventory.ui;

import javafx.scene.shape.SVGPath;

/**
 * Simple line-art icons drawn with {@link SVGPath}, all on a 16 by 16 grid.
 *
 * <p>The icons are coloured through CSS (style class {@code icon}), so they
 * follow the light and dark themes automatically.</p>
 *
 * @author Mashal and Umaina
 */
public final class Icons {

    /** Magnifying glass: a ring with a short handle. */
    private static final String SEARCH =
            "M6.5 1a5.5 5.5 0 1 0 3.4 9.8l4 4 1.1-1.1-4-4A5.5 5.5 0 0 0 6.5 1z"
            + "m0 1.5a4 4 0 1 1 0 8a4 4 0 1 1 0-8z";

    /** Sun: a disc with eight short rays. */
    private static final String SUN =
            "M8 4.5a3.5 3.5 0 1 0 0 7a3.5 3.5 0 1 0 0-7z"
            + "M7.25 0h1.5v2.5h-1.5z M7.25 13.5h1.5V16h-1.5z"
            + "M0 7.25h2.5v1.5H0z M13.5 7.25H16v1.5h-2.5z"
            + "M2.8 3.73l.93-.93 1.4 1.4-.93.93z M10.87 11.8l.93-.93 1.4 1.4-.93.93z"
            + "M11.8 5.13l-.93-.93 1.4-1.4.93.93z M3.73 13.2l-.93-.93 1.4-1.4.93.93z";

    /** Moon: a crescent. */
    private static final String MOON =
            "M14 8.53A6 6 0 1 1 7.47 2a4.67 4.67 0 0 0 6.53 6.53z";

    /**
     * Prevents instantiation; this class only offers static factory methods.
     *
     * @author Mashal and Umaina
     */
    private Icons() {
        // utility class
    }

    /**
     * Creates a magnifying-glass icon for the search field.
     *
     * @return a new search icon
     * @author Mashal and Umaina
     */
    public static SVGPath search() {
        return create(SEARCH);
    }

    /**
     * Creates a sun icon (shown when switching to the light theme).
     *
     * @return a new sun icon
     * @author Mashal and Umaina
     */
    public static SVGPath sun() {
        return create(SUN);
    }

    /**
     * Creates a moon icon (shown when switching to the dark theme).
     *
     * @return a new moon icon
     * @author Mashal and Umaina
     */
    public static SVGPath moon() {
        return create(MOON);
    }

    /**
     * Builds an icon from SVG path data and gives it the {@code icon} style class.
     *
     * @param pathData the SVG path data on a 16 by 16 grid
     * @return the icon shape
     * @author Mashal and Umaina
     */
    private static SVGPath create(String pathData) {
        SVGPath path = new SVGPath();
        path.setContent(pathData);
        path.getStyleClass().add("icon");
        return path;
    }
}
