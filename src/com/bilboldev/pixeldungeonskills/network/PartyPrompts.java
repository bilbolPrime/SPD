package com.bilboldev.pixeldungeonskills.network;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class PartyPrompts {
    public interface Result {
        void complete(String value);
    }

    public interface Backend {
        default void edit(Input input) { }
        default void endEdit() { }
        void copy(String value);
        void paste(Result result);
    }

    public enum Kind { ADDRESS, PORT, CODE }

    public abstract static class Input {
        public final Kind kind;
        public final int limit;
        private String value;
        private int cursor, anchor;

        protected Input(Kind kind, String value, int limit) {
            this.kind = kind;
            this.value = value;
            this.limit = limit;
            cursor = anchor = value.length();
        }

        public String value() { return value; }
        public int cursor() { return cursor; }
        public int anchor() { return anchor; }

        public void set(String text, int cursor, int anchor) {
            value = text.substring(0, Math.min(text.length(), limit));
            this.cursor = Math.max(0, Math.min(cursor, value.length()));
            this.anchor = Math.max(0, Math.min(anchor, value.length()));
            changed();
        }

        public void insert(String text) {
            StringBuilder clean = new StringBuilder();
            int room = limit - value.length() + Math.abs(cursor - anchor);
            for (int i = 0; i < text.length() && clean.length() < room; i++) {
                char c = text.charAt(i);
                if (c >= ' ' && c <= '~') clean.append(c);
            }
            int first = Math.min(cursor, anchor), last = Math.max(cursor, anchor);
            set(value.substring(0, first) + clean + value.substring(last), first + clean.length(), first + clean.length());
        }

        public void erase(boolean backwards) {
            if (cursor == anchor) {
                if (backwards && cursor > 0) anchor--;
                else if (!backwards && cursor < value.length()) anchor++;
            }
            insert("");
        }

        public void move(int position, boolean select) {
            set(value, position, select ? anchor : position);
        }

        public void selectAll() {
            set(value, value.length(), 0);
        }

        public String selection() {
            return value.substring(Math.min(cursor, anchor), Math.max(cursor, anchor));
        }

        protected abstract void changed();
        public abstract void next(boolean backwards);
        public abstract void accept();
        public abstract void cancel();
    }

    private static Backend backend;
    private static volatile Input editing;
    public static volatile int keyboardTop;
    private static final ConcurrentLinkedQueue<Runnable> results = new ConcurrentLinkedQueue<>();

    private PartyPrompts() { }

    public static void install(Backend platform) {
        backend = platform;
    }

    public static void uninstall(Backend platform) {
        if (backend != platform) return;
        backend = null;
        editing = null;
        keyboardTop = 0;
        results.clear();
    }

    public static Input editing() {
        return editing;
    }

    public static void edit(Input input) {
        editing = input;
        if (backend != null) backend.edit(input);
    }

    public static void endEdit(Input input) {
        if (editing != input) return;
        editing = null;
        keyboardTop = 0;
        if (backend != null) backend.endEdit();
    }

    public static void changed(Input input, String value, int cursor, int anchor) {
        results.add(() -> {
            if (editing == input) input.set(value, cursor, anchor);
        });
    }

    public static void next(Input input, boolean backwards) {
        results.add(() -> {
            if (editing == input) input.next(backwards);
        });
    }

    public static void accept(Input input) {
        results.add(() -> {
            if (editing == input) input.accept();
        });
    }

    public static void cancel(Input input) {
        results.add(() -> {
            if (editing == input) input.cancel();
        });
    }

    public static void paste() {
        Input input = editing;
        if (input == null || backend == null) return;
        backend.paste(value -> results.add(() -> {
            if (editing == input && value != null) {
                input.insert(value);
                backend.edit(input);
            }
        }));
    }

    public static void copy(String value) {
        if (backend == null) throw new IllegalStateException("Party clipboard is not installed");
        backend.copy(value);
    }

    public static void poll() {
        Runnable result;
        while ((result = results.poll()) != null) result.run();
    }
}
