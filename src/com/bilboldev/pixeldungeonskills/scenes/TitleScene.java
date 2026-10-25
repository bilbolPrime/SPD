/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.scenes;

import java.util.ArrayList;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.input.Keys;
import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.noosa.audio.Music;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Rankings;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.VersionNewsInfo;
import com.bilboldev.pixeldungeonskills.actors.Online.OnlineHero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.effects.BannerSprites;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.ExitButton;
import com.bilboldev.pixeldungeonskills.ui.PrefsButton;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndSettings;
import com.bilboldev.pixeldungeonskills.windows.WndUpdateHistory;
import com.bilboldev.utils.Signal;
import com.bilboldev.utils.PointF;

public class TitleScene extends PixelScene {
    public enum Page {
        HOME, HEROES, RANKINGS, BADGES, ABOUT
    }
    static final HeroClass[] HERO_CLASSES = {HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE,
            HeroClass.HUNTRESS, HeroClass.NECROMANCER, HeroClass.MERCENARY};
    private static Page rememberedPage = Page.HOME;
    private static Class<?> rememberedSceneType;
    static HeroClass selectedHero;
    private static final float REVEAL_DELAY = 1f, REVEAL_FADE = 0.3f;
    private static final int[] SHADE = {0x00080812, 0x55080812, 0xE8080812, 0xFF080812};
    private static final int[] TOP_SHADE = {0xCF080812, 0x00080812};
    private static final int[] LEFT_SHADE = {0x00080812, 0xB8080812, 0xFA080812, 0xFF080812};
    private static boolean introShown;

    private final ArrayList<TitleMenuButton> buttons = new ArrayList<>();
    private TitleBackdrop backdrop;
    private TitleLayer home, outgoing;
    private TitleHubPanel panel;
    private Page page;
    private float transition = 1;
    private Image runes;
    private TouchArea introTouch;
    private Signal.Listener<Keys.Key> introKeys;
    private float revealTime, audioTime, time;

    @Override
    public void create() {
        GauntletRun.finish();
        super.create();
        Badges.loadGlobal();
        VersionNewsInfo.prepareEntitlements();
        Badges.saveGlobal();
        Badges.ensureGlobalFile();
        Rankings.INSTANCE.load();
        Difficulties.is3d = PixelDungeon.enabled3d();
        if (Dungeon.hero instanceof OnlineHero) {
            try {
                ((Game)Game.mContext).mService.mHubConnection.stop();
            }
            catch (Exception ignored) { }
        }

        boolean portrait = Game.height > Game.width;
        float zoom = Math.max(1, Math.min(defaultZoom, (float)Math.floor(Math.min(
                Game.width / (portrait ? 180f : 360f), Game.height / (portrait ? 340f : 300f)))));
        Camera.main.zoom(zoom);
        Camera.main.scroll.set(0, 0);
        uiCamera.scroll.set(0, 0);
        int w = Camera.main.width, h = Camera.main.height;
        add(new ColorBlock(w, h, 0xFF080A12));
        add(backdrop = new TitleBackdrop(portrait));
        shade(w, h, portrait);
        add(home = new TitleLayer());

        if (Music.INSTANCE.isCurrent(Presentation.titleMusic())) {
            audioTime = 2f;
        } else {
            Music.INSTANCE.volume(0);
            Music.INSTANCE.play(Presentation.titleMusic(), true);
        }

        addAction(Messages.get(TitleScene.class, "play"), 0, true, Page.HEROES);
        addAction(Messages.get(TitleScene.class, "rankings"), 2, false, Page.RANKINGS);
        addAction(Messages.maybeTranslate("Badges"), 3, false, Page.BADGES);
        addAction(Messages.maybeTranslate("About"), 1, false, Page.ABOUT);
        float menuWidth = Math.min(portrait ? 176 : 164, portrait ? w - 32 : w * 0.35f);
        float buttonHeight = Math.max(34, 48 * Game.density / zoom);
        float gap = 4;
        float menuHeight = buttons.size() * buttonHeight + (buttons.size() - 1) * gap;
        float menuX = portrait ? (w - menuWidth) / 2 : Math.max(16, w * 0.065f);
        boolean shortLandscape = !portrait && h - menuHeight < 150;
        Image title = BannerSprites.get(BannerSprites.Type.PIXEL_DUNGEON);
        float titleScale = shortLandscape ? Math.min(1.35f, (w - menuX - menuWidth - 40) / title.width)
                : Math.min(menuWidth / title.width, (h - menuHeight - 82) / title.height);
        titleScale = Math.max(0.4f, Math.min(1.35f, titleScale));
        title.scale.set(titleScale);
        float groupHeight = menuHeight + title.height() + 14;
        float titleY = portrait ? 32 : Math.max(30, (h - groupHeight) / 2);
        float menuY = portrait ? h - 30 - menuHeight : titleY + title.height() + 14;
        if (shortLandscape) menuY = Math.max(34, (h - 30 - menuHeight) / 2);
        title.x = align(menuX + (menuWidth - title.width()) / 2);
        title.y = align(titleY);
        if (shortLandscape) {
            title.x = align(menuX + menuWidth + (w - menuX - menuWidth - title.width()) / 2);
            title.y = 34;
        }
        home.add(title);
        runes = BannerSprites.get(BannerSprites.Type.PIXEL_DUNGEON_SIGNS);
        runes.scale.set(titleScale);
        runes.x = title.x;
        runes.y = title.y;
        runes.alpha(0.32f);
        home.add(runes);
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setRect(align(menuX), align(menuY + i * (buttonHeight + gap)), menuWidth, buttonHeight);
        }
        utilities(w, h);

        page = Music.INSTANCE.isCurrent(Presentation.titleMusic()) && rememberedSceneType == getClass()
                ? rememberedPage : initialPage();
        rememberedSceneType = getClass();
        rememberedPage = page;
        backdrop.visit(page, true);
        backdrop.selectHeroes(selectedHero, StartScene.companionLeader());
        if (page != Page.HOME) {
            home.visible = home.active = false;
            add(panel = TitleHubPanel.create(this, page, backdrop));
            bringToFront(panel);
        }

        revealTime = introShown || page != Page.HOME || !PixelDungeon.titleAnimation() ? REVEAL_DELAY + REVEAL_FADE : 0;
        introShown = true;
        introTouch = new TouchArea(0, 0, w, h) {
            @Override
            protected void onTouchDown(Touch touch) {
                reveal();
            }
        };
        add(introTouch);
        introKeys = new Signal.Listener<Keys.Key>() {
            @Override
            public void onSignal(Keys.Key key) {
                if (key.pressed && revealTime < REVEAL_DELAY + REVEAL_FADE
                        && key.code != Keys.VOLUME_UP && key.code != Keys.VOLUME_DOWN) {
                    reveal();
                    Keys.event.cancel();
                }
            }
        };
        Keys.event.add(introKeys);
        updateReveal();
        noFade = false;
        if (page == Page.HOME && VersionNewsInfo.haveMessage()) {
            reveal();
            add(VersionNewsInfo.getWelcomeWindow());
        }
    }

    protected Page initialPage() {
        return rememberedPage;
    }

    public Page page() {
        return page;
    }

    public boolean transitioning() {
        return transition < 1;
    }

    public static void resetPresentation() {
        rememberedPage = Page.HOME;
        rememberedSceneType = null;
        StartScene.cancelStartFlow();
    }

    static void leavingForRun() {
        rememberedPage = Page.HOME;
        StartScene.cancelStartFlow();
    }

    void refreshHeroes() {
        if (page != Page.HEROES) return;
        if (panel != null) {
            remove(panel);
            panel.destroy();
        }
        backdrop.selectHeroes(selectedHero, StartScene.companionLeader());
        add(panel = TitleHubPanel.create(this, Page.HEROES, backdrop));
        bringToFront(panel);
    }

    void back() {
        onBackPressed();
    }

    public void navigate(Page destination) {
        if (destination == page || transitioning() || Window.topWindow() != null) return;
        StartScene.cancelStartFlow();
        backdrop.selectHero(selectedHero);
        outgoing = page == Page.HOME ? home : panel;
        outgoing.active = false;
        page = rememberedPage = destination;
        transition = PixelDungeon.titleAnimation() ? 0 : 1;
        if (destination == Page.HOME) {
            panel = null;
            home.visible = true;
            home.opacity = 0;
            home.active = false;
        } else {
            add(panel = TitleHubPanel.create(this, page, backdrop));
            bringToFront(panel);
            panel.opacity = 0;
            panel.active = false;
        }
        backdrop.visit(page, transition == 1);
        updateTransition(0);
    }

    private void updateTransition(float dt) {
        transition = !PixelDungeon.titleAnimation() ? 1 : Math.min(1, transition + dt / .85f);
        if (outgoing != null) {
            outgoing.opacity = Math.max(0, 1 - transition * 3.5f);
            if (outgoing.opacity == 0) {
                outgoing.visible = false;
                if (outgoing != home) {
                    remove(outgoing);
                    outgoing.destroy();
                }
                outgoing = null;
            }
        }
        TitleLayer incoming = page == Page.HOME ? home : panel;
        incoming.opacity = Math.max(0, Math.min(1, (transition - .45f) / .55f));
        incoming.active = transition == 1;
    }

    private void addAction(String label, int icon, boolean primary, final Page destination) {
        TitleMenuButton button = new TitleMenuButton(label, icon, primary) {
            @Override
            protected void onClick() {
                navigate(destination);
            }
        };
        home.add(button);
        buttons.add(button);
    }

    private void shade(int w, int h, boolean portrait) {
        Image bottom = new Image(TextureCache.createGradient(1, 64, SHADE));
        bottom.scale.set(w, h * (portrait ? 0.76f : 0.34f) / 64);
        bottom.y = h - bottom.height();
        add(bottom);
        Image top = new Image(TextureCache.createGradient(1, 64, TOP_SHADE));
        top.scale.set(w, h * 0.25f / 64);
        add(top);
        if (!portrait) {
            Image left = new Image(TextureCache.createGradient(1, 64, LEFT_SHADE));
            left.scale.set(h, w * 0.58f / 64);
            left.angle = 90;
            left.x = left.height();
            add(left);
        }
    }

    private void utilities(int w, int h) {
        PrefsButton prefs = new PrefsButton();
        prefs.setRect(8, 8, 24, 24);
        add(prefs);
        ExitButton exit = new ExitButton();
        exit.setRect(w - 30, 8, 24, 24);
        add(exit);

        Button history = new Button() {
            private Image scroll;
            private Emitter sparks;

            @Override
            protected void createChildren() {
                super.createChildren();
                scroll = new Image(Assets.CHROME, 32, 32, 32, 32);
                scroll.scale.set(.5f);
                add(scroll);
                sparks = new Emitter();
                add(sparks);
                if (Presentation.lastReadUpdate() < Game.versionBuild) {
                    sparks.pour(Speck.factory(Speck.LIGHT), .3f);
                }
            }

            @Override
            protected void layout() {
                super.layout();
                scroll.x = align(x + (width - scroll.width()) / 2);
                scroll.y = y;
                sparks.pos(scroll.x - 2, scroll.y - 2, scroll.width() + 4, scroll.height() + 4);
            }

            @Override
            protected void onTouchDown() {
                scroll.brightness(1.3f);
                Sample.INSTANCE.play(Assets.SND_CLICK);
            }

            @Override
            protected void onTouchUp() {
                scroll.resetColor();
            }

            @Override
            protected void onClick() {
                TitleScene.this.add(new WndUpdateHistory());
                sparks.on = false;
                sparks.kill();
            }
        };
        history.setRect(exit.left() - 28, 8, 24, 24);
        add(history);

        BitmapText version = new BitmapText("v " + Game.version + " (" + Game.versionBuild + ")", font1x, false);
        version.measure();
        version.hardlight(0x8D899C);
        version.x = w - version.width() - 10;
        version.y = h - 18;
        home.add(version);
        BitmapText vanilla = new BitmapText(Game.vanillaVersion, font1x, false);
        vanilla.measure();
        vanilla.hardlight(0x575362);
        vanilla.x = w - vanilla.width() - 10;
        vanilla.y = h - 9;
        home.add(vanilla);
    }

    private void reveal() {
        revealTime = REVEAL_DELAY + REVEAL_FADE;
        updateReveal();
    }

    public void pointerMoved(float x, float y) {
        boolean available = Window.topWindow() == null && x >= 0 && y >= 0;
        PointF point = Camera.main.screenToCamera((int)x, (int)y);
        for (TitleMenuButton button : buttons) {
            button.hover(available && button.isActive() && button.isVisible() && button.inside(point.x, point.y));
        }
        if (panel != null) panel.pointerMoved(available ? point : null);
    }

    private void updateReveal() {
        float amount = Math.max(0, Math.min(1, (revealTime - REVEAL_DELAY) / REVEAL_FADE));
        amount = amount * amount * (3 - 2 * amount);
        for (TitleMenuButton button : buttons) button.opacity(amount);
        if (introTouch != null) introTouch.active = page == Page.HOME && amount < 1;
    }

    @Override
    public void update() {
        float dt = Math.min(Game.elapsed, 0.1f);
        if (!PixelDungeon.titleAnimation()) revealTime = REVEAL_DELAY + REVEAL_FADE;
        revealTime += dt;
        audioTime += dt;
        if (PixelDungeon.titleAnimation()) time += dt;
        runes.alpha(0.28f + (float)Math.sin(time * 0.65f) * 0.10f);
        if (Music.INSTANCE.isEnabled()) Music.INSTANCE.volume(Math.min(1, audioTime / 2f));
        updateReveal();
        updateTransition(dt);
        super.update();
        if (page == Page.HEROES && !transitioning()) {
            StartScene.ensureStartDialog(this);
            StartScene.updateDeparture(dt);
        }
    }

    @Override
    protected void onMenuPressed() {
        reveal();
        add(new WndSettings(false));
    }

    @Override
    public void destroy() {
        StartScene.releaseWindow();
        Badges.saveGlobal();
        Keys.event.remove(introKeys);
        super.destroy();
    }

    @Override
    protected void onBackPressed() {
        HeroClass leader = StartScene.companionLeader();
        if (leader != null) {
            StartScene.cancelStartFlow();
            selectedHero = leader;
            refreshHeroes();
            return;
        }
        if (page != Page.HOME) navigate(Page.HOME);
        else super.onBackPressed();
    }
}
