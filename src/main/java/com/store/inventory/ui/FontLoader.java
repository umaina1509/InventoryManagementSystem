package com.store.inventory.ui;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads the bundled "Manrope" font files so the stylesheet can use them.
 *
 * <p>Manrope is distributed under the SIL Open Font License; the licence text
 * is stored next to the font files in {@code resources/fonts/OFL.txt}. If a
 * file cannot be loaded, the caller should switch to the fallback stylesheet,
 * which uses "Segoe UI" instead.</p>
 *
 * @author Mashal and Umaina
 */
public final class FontLoader {

    /** Font files bundled on the classpath (regular and semibold weights). */
    private static final String[] FONT_FILES = {
        "/fonts/Manrope-Regular.ttf",
        "/fonts/Manrope-SemiBold.ttf"
    };

    /** Default size used when registering a font; CSS decides the real size. */
    private static final double DEFAULT_SIZE = 13;

    /**
     * Prevents instantiation; this class only offers a static method.
     *
     * @author Mashal and Umaina
     */
    private FontLoader() {
        // utility class
    }

    /**
     * Registers all bundled Manrope font files with JavaFX.
     *
     * @return {@code true} if every font file was loaded, {@code false} if at
     *         least one was missing or unreadable
     * @author Mashal and Umaina
     */
    public static boolean loadManrope() {
        boolean allLoaded = true;
        for (String path : FONT_FILES) {
            try (InputStream in = FontLoader.class.getResourceAsStream(path)) {
                if (in == null || Font.loadFont(in, DEFAULT_SIZE) == null) {
                    allLoaded = false;
                }
            } catch (IOException e) {
                allLoaded = false;
            }
        }
        return allLoaded;
    }
}
