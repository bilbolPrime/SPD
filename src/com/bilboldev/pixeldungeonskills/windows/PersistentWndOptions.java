package com.bilboldev.pixeldungeonskills.windows;

/**
 * Created by Moussa on 28-Jan-17.
 */
public class PersistentWndOptions extends WndOptions {

    public PersistentWndOptions(String title, String message, String... options) {
        super(title, message, options);
    }

    @Override
    public void onBackPressed() {

    }
}
