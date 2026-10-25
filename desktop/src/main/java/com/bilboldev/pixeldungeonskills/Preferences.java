package com.bilboldev.pixeldungeonskills;

import com.badlogic.gdx.Gdx;

enum Preferences {

    INSTANCE;

    private static final java.util.prefs.Preferences BOOTSTRAP_PREFS = java.util.prefs.Preferences.userRoot().node("the-spd-3d");

    public static final String KEY_LANDSCAPE = "landscape";
    public static final String KEY_IMMERSIVE = "immersive";
    public static final String KEY_MUSIC = "music";
    public static final String KEY_SOUND_FX = "soundfx";
    public static final String KEY_ZOOM = "zoom";
    public static final String KEY_LAST_CLASS = "last_class";
    public static final String KEY_CHALLENGES = "challenges";
    public static final String KEY_DONATED = "donated";
    public static final String KEY_INTRO = "intro";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_BRIGHTNESS = "brightness";
    public static final String KEY_BRIGHTNESS_NEW = "brightness_new";
    public static final String KEY_ENABLED_3D = "enabled3D";
    public static final String KEY_DEGRADATION = "nodegradation";
    public static final String KEY_ARCHER_MAIDEN = "archermaiden";
    public static final String KEY_DISABLE_CHAMPIONS = "disablechampions";
    public static final String KEY_SECOND_QUICK_SLOT = "secondquickslot";
    public static final String KEY_SKILL_FONT_SIZE = "skillfontsize";
    public static final String KEY_WINDOWED_MODE = "windowedmode";
    public static final String KEY_BORDERLESS_WINDOWED_MODE = "borderlesswindowedmode";
    public static final String KEY_WINDOW_WIDTH = "windowwidth";
    public static final String KEY_WINDOW_HEIGHT = "windowheight";

    private com.badlogic.gdx.Preferences prefs;

    private com.badlogic.gdx.Preferences get() {
        if (prefs == null && Gdx.app != null) {
            prefs = Gdx.app.getPreferences("the-spd-3d");
        }
        return prefs;
    }

    int getInt(String key, int defValue) {
        com.badlogic.gdx.Preferences preferences = get();
        if (preferences != null) {
            return preferences.getInteger(key, defValue);
        }
        return BOOTSTRAP_PREFS.getInt(key, defValue);
    }

    boolean getBoolean(String key, boolean defValue) {
        com.badlogic.gdx.Preferences preferences = get();
        if (preferences != null) {
            return preferences.getBoolean(key, defValue);
        }
        return BOOTSTRAP_PREFS.getBoolean(key, defValue);
    }

    String getString(String key, String defValue) {
        com.badlogic.gdx.Preferences preferences = get();
        if (preferences != null) {
            return preferences.getString(key, defValue);
        }
        return BOOTSTRAP_PREFS.get(key, defValue);
    }

    void put(String key, int value) {
        BOOTSTRAP_PREFS.putInt(key, value);
        flushBootstrap();

        com.badlogic.gdx.Preferences preferences = get();
        if (preferences != null) {
            preferences.putInteger(key, value);
            preferences.flush();
        }
    }

    void put(String key, boolean value) {
        BOOTSTRAP_PREFS.putBoolean(key, value);
        flushBootstrap();

        com.badlogic.gdx.Preferences preferences = get();
        if (preferences != null) {
            preferences.putBoolean(key, value);
            preferences.flush();
        }
    }

    void put(String key, String value) {
        BOOTSTRAP_PREFS.put(key, value);
        flushBootstrap();

        com.badlogic.gdx.Preferences preferences = get();
        if (preferences != null) {
            preferences.putString(key, value);
            preferences.flush();
        }
    }

    private void flushBootstrap() {
        try {
            BOOTSTRAP_PREFS.flush();
        } catch (Exception ignored) {
        }
    }
}
