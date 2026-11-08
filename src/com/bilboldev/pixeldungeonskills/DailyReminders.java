package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndDaily;
import com.bilboldev.pixeldungeonskills.windows.WndDailyReminders;
import java.io.IOException;

public final class DailyReminders {
    public interface Settings {
        boolean enabled();
        boolean requested();
        int minute();
        void save(int minute, boolean enabled);
        void openSettings();
        default void played(long day) {}
        default void configure() { configureRequested = true; }
    }

    public static Settings settings;
    public static volatile boolean requested;
    public static volatile boolean configureRequested;

    private DailyReminders() {}

    public static void showRequested(boolean inRun) {
        if (configureRequested && settings != null) {
            configureRequested = false;
            if (!(Window.topWindow() instanceof WndDailyReminders))
                Game.scene().add(new WndDailyReminders());
        }
        if (!requested || Window.topWindow() != null) return;
        if (GauntletRun.active() || DailyRun.active()) return;
        if (inRun && (Dungeon.hero == null || !Dungeon.hero.ready || LocalCoop.active())) return;
        requested = false;
        if (inRun) {
            try { Dungeon.saveAll(); }
            catch (IOException failure) { failure.printStackTrace(); return; }
        }
        Game.scene().add(new WndDaily());
    }
}
