package com.store.inventory.ui;

import javafx.application.Application;

/**
 * Plain entry point that starts {@link MainApp}.
 *
 * <p>Running a class that extends {@code Application} directly from some IDE
 * setups fails with "JavaFX runtime components are missing". Starting from
 * this class, which does not extend {@code Application}, avoids that problem.</p>
 *
 * @author Mashal and Umaina
 */
public final class Launcher {

    private Launcher() {
        // not instantiable
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
