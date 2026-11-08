package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.ui.Window;
import java.util.concurrent.TimeUnit;

public abstract class GameUpdates {
    private static volatile GameUpdates source;
    private static final long CHECK_INTERVAL = TimeUnit.MINUTES.toNanos(30);

    private final String store;
    private final String url;
    private long lastCheck;
    private boolean checked;
    private boolean checking;
    protected volatile boolean available;

    protected GameUpdates(String store, String url) {
        this.store = store;
        this.url = url;
    }

    public static void install(GameUpdates updates) {
        source = updates;
    }

    public static void check() {
        GameUpdates updates = source;
        if (updates != null) updates.startCheck();
    }

    private synchronized void startCheck() {
        long now = System.nanoTime();
        if (checking || checked && now - lastCheck < CHECK_INTERVAL) return;
        checked = true;
        checking = true;
        lastCheck = now;
        try {
            Thread worker = new Thread(() -> {
                try {
                    fetch();
                } catch (RuntimeException | LinkageError unavailable) {

                } finally {
                    synchronized (GameUpdates.this) { checking = false; }
                }
            }, "Game update check");
            worker.setDaemon(true);
            worker.start();
        } catch (RuntimeException unavailable) {
            checking = false;
        }
    }

    public static boolean available() {
        return source != null && source.available;
    }

    public static String store() {
        return source == null ? "github" : source.store;
    }

    public static boolean open() {
        return source != null && source.openStore();
    }

    protected boolean openStore() {
        return Window.openUrl(url);
    }

    protected abstract void fetch();
}
