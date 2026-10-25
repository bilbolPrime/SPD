package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.messages.Message;

public class PersistentWndOptions extends WndOptions {

    public PersistentWndOptions(String title, String message, String... options) {
        super(title, message, options);
    }

    public PersistentWndOptions(Message title, Message message, Message... options) {
        super(title, message, options);
    }

    @Override
    public void onBackPressed() {

    }
}
