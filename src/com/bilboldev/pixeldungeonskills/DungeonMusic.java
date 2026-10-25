package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.audio.LayeredMusic;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;

public final class DungeonMusic {
    private DungeonMusic() {}
    public static final LayeredMusic.Track REBORN = new LayeredMusic.Track("action/sewers",
            "music/action/sewers/beneath-the-city-action-loop.ogg",
            "music/action/sewers/blackwater-rising-boss-layer.ogg", 8131765, 384);

    public static boolean hasBoss() {
        if (!Dungeon.bossLevel() || Dungeon.level == null) return false;
        for (Mob mob : Dungeon.level.mobs) {
            if (mob.isAlive() && (mob instanceof RebornBoss || mob instanceof Goo || mob instanceof Tengu
                    || mob instanceof DM300 || mob instanceof King || mob instanceof Yog)) return true;
        }
        return false;
    }

    public static void enter() {
        Music.INSTANCE.volume(1);
        if (Presentation.rebornMusic() && Dungeon.depth >= 1 && Dungeon.depth <= 26) {
            Music.INSTANCE.playLayered(REBORN, hasBoss());
        } else {
            Music.INSTANCE.play(Assets.TUNE, true);
        }
    }
}
