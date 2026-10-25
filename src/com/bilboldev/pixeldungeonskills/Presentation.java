package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.tweeners.Tweener;
import com.bilboldev.pixeldungeonskills.scenes.*;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.ui.Toolbar;

public final class Presentation {
    private static final String KEY_REBORN = "reborn_presentation";
    private static MusicFade musicFade;

    private Presentation() {}

    public static boolean reborn() {
        return Preferences.INSTANCE.getBoolean(KEY_REBORN, true);
    }

    public static void reborn(boolean enabled) {
        Preferences.INSTANCE.put(KEY_REBORN, enabled);
        applyAudio();
    }

    public static boolean rebornMusic() {
        return Preferences.INSTANCE.getBoolean("reborn_music", reborn());
    }

    public static void rebornMusic(boolean enabled) {
        boolean changed = rebornMusic() != enabled;
        Preferences.INSTANCE.put("reborn_music", enabled);
        if (!changed || Game.instance == null || Game.scene() == null) return;
        float gain = musicFade == null ? 1 : musicFade.gain;
        if (musicFade != null) musicFade.killAndErase();
        musicFade = new MusicFade(Game.scene(), gain);
        Game.scene().add(musicFade);
    }

    public static String titleMusic() {
        return rebornMusic() ? Assets.TITLE_AMBIENCE : Assets.THEME;
    }

    public static int quickSlots() {
        return Math.max(1, Math.min(QuickSlot.MAX_SLOTS, Preferences.INSTANCE.getInt("quick_slot_count", 2)));
    }

    public static void quickSlots(int count) {
        Preferences.INSTANCE.put("quick_slot_count", Math.max(1, Math.min(QuickSlot.MAX_SLOTS, count)));
        Toolbar.updateQuickSlots();
    }

    public static int lastReadUpdate() {
        return initialSetting("last_read_update", Game.versionBuild - 1);
    }

    public static void lastReadUpdate(int build) {
        Preferences.INSTANCE.put("last_read_update", build);
    }

    public static int musicVolume() {
        return Math.max(0, Math.min(100, Preferences.INSTANCE.getInt("music_volume", 50)));
    }

    public static void musicVolume(int value) {
        Preferences.INSTANCE.put("music_volume", Math.max(0, Math.min(100, value)));
        applyAudio();
    }

    public static int soundVolume() {
        return Math.max(0, Math.min(100, Preferences.INSTANCE.getInt("sound_volume", 50)));
    }

    public static void soundVolume(int value) {
        Preferences.INSTANCE.put("sound_volume", Math.max(0, Math.min(100, value)));
        applyAudio();
    }

    public static void applyAudio() {
        Music.INSTANCE.masterVolume(musicVolume() / 100f);
        Sample.INSTANCE.volume(soundVolume() / 100f);
        if (!Music.INSTANCE.isEnabled()) Music.INSTANCE.enable(true);
        Sample.INSTANCE.enable(true);
    }

    static int initialSetting(String key, int previous) {
        int value = Preferences.INSTANCE.getInt(key, Integer.MIN_VALUE);
        if (value == Integer.MIN_VALUE) {
            value = previous;
            Preferences.INSTANCE.put(key, value);
        }
        return value;
    }

    public static void switchTo(boolean reborn) {
        reborn(reborn);
        VersionNewsInfo.presentationChanged();
        TitleScene.resetPresentation();
        PixelDungeon.switchNoFade(TitleScene.class);
    }

    public static Class<? extends Scene> menuScene(Class<? extends Scene> scene) {
        boolean reborn = reborn();
        if (scene == TitleScene.class || scene == ClassicTitleScene.class)
            return reborn ? TitleScene.class : ClassicTitleScene.class;
        if (scene == StartScene.class || scene == ClassicStartScene.class)
            return reborn ? StartScene.class : ClassicStartScene.class;
        if (scene == RankingsScene.class || scene == ClassicRankingsScene.class)
            return reborn ? RankingsScene.class : ClassicRankingsScene.class;
        if (scene == BadgesScene.class || scene == ClassicBadgesScene.class)
            return reborn ? BadgesScene.class : ClassicBadgesScene.class;
        if (scene == AboutScene.class || scene == ClassicAboutScene.class)
            return reborn ? AboutScene.class : ClassicAboutScene.class;
        return scene;
    }

    private static final class MusicFade extends Tweener {
        private final float startGain;
        private float gain;
        private boolean switched;

        MusicFade(Scene scene, float gain) {
            super(scene, .6f);
            startGain = gain;
            this.gain = gain;
        }

        @Override
        protected void updateValues(float progress) {
            if (progress < .5f) {
                gain = startGain * (1 - progress * 2);
            } else {
                if (!switched) {
                    Music.INSTANCE.fadeVolume(0);
                    if (target instanceof GameScene) DungeonMusic.enter();
                    else Music.INSTANCE.play(titleMusic(), true);
                    switched = true;
                }
                gain = progress * 2 - 1;
            }
            Music.INSTANCE.fadeVolume(gain);
        }

        @Override
        protected void onComplete() {
            killAndErase();
            destroy();
        }

        @Override
        public void destroy() {
            if (musicFade == this) {
                musicFade = null;
                Music.INSTANCE.fadeVolume(1);
            }
            super.destroy();
        }
    }
}
