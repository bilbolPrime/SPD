package com.bilboldev.pixeldungeonskills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.messages.Languages;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;

import java.util.Locale;

public class PixelDungeon extends Game {

    @Override
    protected Class<? extends Scene> resolveScene(Class<? extends Scene> sceneClass) {
        return Presentation.menuScene(sceneClass);
    }

    private static final int DEFAULT_WINDOWED_WIDTH = 1280;
    private static final int DEFAULT_WINDOWED_HEIGHT = 720;

    public PixelDungeon() {
        super(TitleScene.class);

        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade.class,
                "com.watabou.pixeldungeon.items.scrolls.ScrollOfEnhancement");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfHealth.class,
                "com.watabou.pixeldungeon.actors.blobs.Light");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.rings.RingOfMending.class,
                "com.watabou.pixeldungeon.items.rings.RingOfRejuvenation");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.wands.WandOfReach.class,
                "com.watabou.pixeldungeon.items.wands.WandOfTelekenesis");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.blobs.Foliage.class,
                "com.watabou.pixeldungeon.actors.blobs.Blooming");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.buffs.Shadows.class,
                "com.watabou.pixeldungeon.actors.buffs.Rejuvenation");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfPsionicBlast.class,
                "com.watabou.pixeldungeon.items.scrolls.ScrollOfNuclearBlast");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.hero.Hero.class,
                "com.watabou.pixeldungeon.actors.Hero");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Shopkeeper.class,
                "com.watabou.pixeldungeon.actors.mobs.Shopkeeper");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.quest.DriedRose.class,
                "com.watabou.pixeldungeon.items.DriedRose");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MirrorImage.class,
                "com.watabou.pixeldungeon.items.scrolls.ScrollOfMirrorImage$MirrorImage");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.rings.RingOfElements.class,
                "com.watabou.pixeldungeon.items.rings.RingOfCleansing");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.rings.RingOfElements.class,
                "com.watabou.pixeldungeon.items.rings.RingOfResistance");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang.class,
                "com.watabou.pixeldungeon.items.weapon.missiles.RangersBoomerang");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.rings.RingOfPower.class,
                "com.watabou.pixeldungeon.items.rings.RingOfEnergy");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.plants.Dreamweed.class,
                "com.watabou.pixeldungeon.plants.Blindweed");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.plants.Dreamweed.Seed.class,
                "com.watabou.pixeldungeon.plants.Blindweed$Seed");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Shock.class,
                "com.watabou.pixeldungeon.items.weapon.enchantments.Piercing");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Shock.class,
                "com.watabou.pixeldungeon.items.weapon.enchantments.Swing");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfEnchantment.class,
                "com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfEnchantment.class,
                "com.watabou.pixeldungeon.items.Stylus");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.actors.mobs.FetidRat.class,
                "com.watabou.pixeldungeon.actors.mobs.npcs.Ghost$FetidRat");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.plants.Rotberry.class,
                "com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker$Rotberry");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.plants.Rotberry.Seed.class,
                "com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker$Rotberry$Seed");
        com.bilboldev.utils.Bundle.addAlias(
                com.bilboldev.pixeldungeonskills.items.wands.WandOfReach.class,
                "com.watabou.pixeldungeon.items.wands.WandOfTelekinesis");
    }

    @Override
    public void create() {
		Messages.setup(language());
        super.create();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                bringWindowToFront();
            }
        });
        Presentation.applyAudio();
        Sample.INSTANCE.load(
                Assets.SND_CLICK,
                Assets.SND_BADGE,
                Assets.SND_GOLD,
                Assets.SND_DESCEND,
                Assets.SND_STEP,
                Assets.SND_WATER,
                Assets.SND_OPEN,
                Assets.SND_UNLOCK,
                Assets.SND_ITEM,
                Assets.SND_DEWDROP,
                Assets.SND_HIT,
                Assets.SND_MISS,
                Assets.SND_EAT,
                Assets.SND_READ,
                Assets.SND_LULLABY,
                Assets.SND_DRINK,
                Assets.SND_SHATTER,
                Assets.SND_ZAP,
                Assets.SND_LIGHTNING,
                Assets.SND_LEVELUP,
                Assets.SND_DEATH,
                Assets.SND_CHALLENGE,
                Assets.SND_CURSED,
                Assets.SND_EVOKE,
                Assets.SND_TRAP,
                Assets.SND_TOMB,
                Assets.SND_ALERT,
                Assets.SND_MELD,
                Assets.SND_BOSS,
                Assets.SND_BLAST,
                Assets.SND_PLANT,
                Assets.SND_RAY,
                Assets.SND_BEACON,
                Assets.SND_TELEPORT,
                Assets.SND_CHARMS,
                Assets.SND_MASTERY,
                Assets.SND_PUFF,
                Assets.SND_ROCKS,
                Assets.SND_BURNING,
                Assets.SND_FALLING,
                Assets.SND_GHOST,
                Assets.SND_SECRET,
                Assets.SND_BONES,
                Assets.SND_BEE,
                Assets.SND_DEGRADE,
                Assets.SND_MIMIC);
    }

    public static void switchNoFade(Class<? extends PixelScene> sceneClass) {
        PixelScene.noFade = true;
        switchScene(sceneClass);
    }

    public static void landscape(boolean value) {

        Preferences.INSTANCE.put(Preferences.KEY_LANDSCAPE, true);
        Game.instance.reloadCache();
    }

    public static boolean landscape() {
        return true;
    }

    public static void immerse(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_IMMERSIVE, value);
    }

    public static boolean immersed() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_IMMERSIVE, false);
    }

    public static void updateImmersiveMode() {
    }

    public static void uiScale(int value) {
        Preferences.INSTANCE.put(Presentation.reborn() ? "reborn_ui_scale" : "ui_scale", value);
    }

    public static int uiScale() {
        int value = Preferences.INSTANCE.getInt("ui_scale", 0);
        return Presentation.reborn() ? Presentation.initialSetting("reborn_ui_scale", value) : value;
    }

    public static void zoom(int value) {
        Preferences.INSTANCE.put(Presentation.reborn() ? "reborn_zoom" : Preferences.KEY_ZOOM, value);
    }

    public static int zoom() {
        int value = Preferences.INSTANCE.getInt(Preferences.KEY_ZOOM, 0);
        return Presentation.reborn() ? Presentation.initialSetting("reborn_zoom", value) : value;
    }

    public static void music(boolean value) {
        Presentation.musicVolume(value ? 100 : 0);
    }

    public static boolean titleAnimation() {
        return Preferences.INSTANCE.getBoolean("title_animation", true);
    }

    public static void titleAnimation(boolean enabled) {
        Preferences.INSTANCE.put("title_animation", enabled);
    }

    public static boolean music() {
        return Presentation.musicVolume() > 0;
    }

    public static void soundFx(boolean value) {
        Presentation.soundVolume(value ? 100 : 0);
    }

    public static boolean soundFx() {
        return Presentation.soundVolume() > 0;
    }

    public static void language(Languages value) {
        Preferences.INSTANCE.put(Preferences.KEY_LANGUAGE, value.code());
        Messages.setup(value);
    }

    public static Languages language() {
        String code = Preferences.INSTANCE.getString(Preferences.KEY_LANGUAGE, null);
        if (code == null) {
            return Languages.matchLocale(Locale.getDefault());
        }
        return Languages.matchCode(code);
    }

    public static void itemDeg(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_DEGRADATION, value);
    }

    public static boolean itemDeg() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_DEGRADATION, true);
    }

    public static boolean skillFontSize() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_SKILL_FONT_SIZE, true);
    }

    public static void skillFontSize(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_SKILL_FONT_SIZE, value);
    }

    public static void brightness(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_BRIGHTNESS, value);
        if (scene() instanceof GameScene) {
            ((GameScene) scene()).brightness(value);
        }
    }

    public static boolean brightness() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_BRIGHTNESS, false);
    }

    public static void brightnessNew(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_BRIGHTNESS_NEW, value);
        if (scene() instanceof GameScene) {
            ((GameScene) scene()).brightness(value);
        }
    }

    public static int brightnessNew() {
        int value = Preferences.INSTANCE.getInt(Preferences.KEY_BRIGHTNESS_NEW, -10);
        if (value == -10) {
            return brightness() ? 2 : -2;
        }
        return value;
    }

    public static boolean secondQuickSlot() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_SECOND_QUICK_SLOT, false);
    }

    public static void secondQuickSlot(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_SECOND_QUICK_SLOT, value);
    }

    public static boolean supportsWindowedMode() {
        return true;
    }

    public static boolean supportsBorderlessWindowedMode() {
        return true;
    }

    public static void windowedMode(boolean value) {
        setWindowModes(value, value ? false : borderlessWindowedMode());
    }

    public static boolean windowedMode() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_WINDOWED_MODE, false);
    }

    public static void borderlessWindowedMode(boolean value) {
        setWindowModes(value ? false : windowedMode(), value);
    }

    public static boolean borderlessWindowedMode() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_BORDERLESS_WINDOWED_MODE, false);
    }

    public static int windowedWidth() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_WINDOW_WIDTH, DEFAULT_WINDOWED_WIDTH);
    }

    public static int windowedHeight() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_WINDOW_HEIGHT, DEFAULT_WINDOWED_HEIGHT);
    }

    public static void rememberWindowSize(int width, int height) {
        if (!windowedMode() || width <= 0 || height <= 0) {
            return;
        }

        Preferences.INSTANCE.put(Preferences.KEY_WINDOW_WIDTH, width);
        Preferences.INSTANCE.put(Preferences.KEY_WINDOW_HEIGHT, height);
    }

    public static void enabled3d(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_ENABLED_3D, value);
    }

    public static boolean enabled3d() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_ENABLED_3D, true);
    }

    public static void donated(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_DONATED, value);
    }

    public static int donated() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_DONATED, 0);
    }

    public static void maidenUnlocked(Boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_ARCHER_MAIDEN, value);
    }

    public static boolean maidenUnlocked() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_ARCHER_MAIDEN, false);
    }

    public static void disableChampionsUnlocked(Boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_DISABLE_CHAMPIONS, value);
    }

    public static boolean disableChampionsUnlocked() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_DISABLE_CHAMPIONS, false);
    }

    public static void lastClass(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_LAST_CLASS, value);
    }

    public static int lastClass() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_LAST_CLASS, 0);
    }

    public static void challenges(int value) {
        Preferences.INSTANCE.put(Preferences.KEY_CHALLENGES, value);
    }

    public static int challenges() {
        return Preferences.INSTANCE.getInt(Preferences.KEY_CHALLENGES, 0);
    }

    public static void intro(boolean value) {
        Preferences.INSTANCE.put(Preferences.KEY_INTRO, value);
    }

    public static boolean intro() {
        return Preferences.INSTANCE.getBoolean(Preferences.KEY_INTRO, true);
    }

    public static void reportException(Throwable throwable) {
        throwable.printStackTrace();
    }

    private static void setWindowModes(boolean windowed, boolean borderlessWindowed) {
        Preferences.INSTANCE.put(Preferences.KEY_WINDOWED_MODE, windowed);
        Preferences.INSTANCE.put(Preferences.KEY_BORDERLESS_WINDOWED_MODE, borderlessWindowed);
        applyWindowMode(windowed, borderlessWindowed);
    }

    private static void applyWindowMode(boolean windowed, boolean borderlessWindowed) {
        if (Gdx.graphics == null) {
            return;
        }

        Game.resetInputForWindowChange();
        Gdx.graphics.setUndecorated(borderlessWindowed);

        if (borderlessWindowed) {
            com.badlogic.gdx.Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height);
        } else if (windowed) {
            Gdx.graphics.setWindowedMode(windowedWidth(), windowedHeight());
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }

        bringWindowToFront();
    }

    private static void bringWindowToFront() {
        if (Gdx.graphics instanceof Lwjgl3Graphics) {
            ((Lwjgl3Graphics) Gdx.graphics).getWindow().restoreWindow();
            ((Lwjgl3Graphics) Gdx.graphics).getWindow().focusWindow();
        }
    }
}
