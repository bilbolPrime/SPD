package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.levels.TutorialLevel;
import com.bilboldev.pixeldungeonskills.ui.GameLog;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndTutorial;

public class TutorialScene extends GameScene {
    private TutorialLevel level;
    private Hero hero;
    private int shownLesson = -1;
    private boolean progressShown;
    private boolean closing;

    @Override public boolean canLeave(Class<? extends Scene> next) {
        boolean allowed = !TutorialRun.acceptsInput() || super.canLeave(next);
        if (allowed && next != TutorialScene.class
                && !(next == InterlevelScene.class && InterlevelScene.mode == InterlevelScene.Mode.TUTORIAL)) closing = true;
        return allowed;
    }

    @Override public void destroy() {
        super.destroy();
        if (closing) TutorialRun.finish();
    }

    @Override public void create() {
        boolean fresh = InterlevelScene.mode == InterlevelScene.Mode.TUTORIAL;
        InterlevelScene.mode = InterlevelScene.Mode.NONE;
        level = TutorialRun.level();
        hero = Dungeon.hero;
        if (fresh) GameLog.wipe();
        super.create();
    }

    @Override public synchronized void update() {
        if (level == null || TutorialRun.level() != level || Dungeon.hero != hero) return;
        super.update();
        TutorialRun.update();
        if (Window.topWindow() == null && hero.ready && Actor.current() == hero) {
            if (shownLesson != TutorialRun.lesson() || !progressShown && (TutorialRun.canAdvance() || TutorialRun.completed())) {
                shownLesson = TutorialRun.lesson();
                progressShown = TutorialRun.canAdvance() || TutorialRun.completed();
                show(new WndTutorial());
            }
        }
        cellSelector.enabled = TutorialRun.acceptsInput() && hero.ready && Window.topWindow() == null;
    }

    @Override protected void onBackPressed() {
        if (!cancel()) show(new WndTutorial());
    }

    @Override public void draw() {
        if (level != null && TutorialRun.level() == level && Dungeon.hero == hero) super.draw();
    }
}
