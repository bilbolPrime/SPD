package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;

public final class SupporterEntitlements {
    public static final int SUBSCRIBER_ICON = 5;
    private static boolean runAurasUnlocked;
    private static int runAura;

    private SupporterEntitlements() { }

    public static boolean ownsAura(int style) {
        return Game.isFreeDesktop() && runAurasUnlocked && style >= 1 && style <= SUBSCRIBER_ICON;
    }

    public static int selectedAura() {
        return ownsAura(runAura) ? runAura : 0;
    }

    public static void selectAura(int style) {
        if (style != 0 && !ownsAura(style)) throw new IllegalArgumentException("Unavailable aura");
        runAura = style;
    }

    public static boolean unlockRunAuras() {
        if (!Game.isFreeDesktop() || !(Game.scene() instanceof GameScene)
                || Dungeon.hero == null || Dungeon.level == null || !Dungeon.hero.isAlive()) return false;
        runAurasUnlocked = true;
        return true;
    }

    public static void resetRunAura() {
        runAurasUnlocked = false;
        runAura = 0;
    }
}
