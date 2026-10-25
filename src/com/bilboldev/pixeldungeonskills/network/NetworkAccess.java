package com.bilboldev.pixeldungeonskills.network;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class NetworkAccess {
    public interface Result {
        void complete(boolean granted);
    }

    public interface Backend {
        void request(Result result);
    }

    private static final ConcurrentLinkedQueue<Runnable> results = new ConcurrentLinkedQueue<>();
    private static Backend backend = result -> result.complete(true);

    private NetworkAccess() { }

    public static void install(Backend access) {
        backend = access;
    }

    public static void uninstall(Backend access) {
        if (backend != access) return;
        backend = result -> result.complete(false);
        results.clear();
    }

    public static void request(Result result) {
        backend.request(granted -> results.add(() -> result.complete(granted)));
    }

    public static void poll() {
        Runnable result;
        while ((result = results.poll()) != null) result.run();
        PartyPrompts.poll();
    }
}
