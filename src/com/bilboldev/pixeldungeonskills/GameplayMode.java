package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.messages.Messages;

public enum GameplayMode {
    CLASSIC("classic"), REBORN("reborn");

    public final String id;

    GameplayMode(String id) {
        this.id = id;
    }

    public String title() {
        return Messages.get("custom.gameplay." + id);
    }

    public static GameplayMode fromId(String id) {
        for (GameplayMode mode : values()) if (mode.id.equals(id)) return mode;
        throw new IllegalArgumentException("Unknown gameplay mode: " + id);
    }
}
