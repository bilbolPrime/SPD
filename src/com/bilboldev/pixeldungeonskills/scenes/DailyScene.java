package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Scene;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.DailyMineLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyHuntLevel;
import com.bilboldev.pixeldungeonskills.levels.DailySealLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyOutpostLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyHoardLevel;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.GameLog;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndDailyResult;
import com.bilboldev.pixeldungeonskills.ui.DailyNotice;

public class DailyScene extends GameScene {
    private static final float NOTICE_TIME = 5;
    private boolean ready;
    private boolean resultShown;
    private DailyNotice notice;
    private String noticedProgress;
    private float noticeTime;
    private int noticedTurn;
    private static String error;

    @Override public boolean canLeave(Class<? extends Scene> next) {
        if (next != DailyScene.class || DailyRun.startRequested() || DailyRun.finished()) return true;
        return Actor.current() == null || Actor.current() == Dungeon.hero && Dungeon.hero.ready;
    }

    @Override public void create() {
        boolean fresh = InterlevelScene.mode == InterlevelScene.Mode.DAILY;
        InterlevelScene.mode = InterlevelScene.Mode.NONE;
        if (fresh) GameLog.wipe();
        super.create();
        ready = true;
        notice = new DailyNotice();
        notice.camera = uiCamera;
        int noticeWidth = Math.min(160, uiCamera.width - 70);
        notice.setRect((uiCamera.width - noticeWidth) / 2f, 26, noticeWidth, 0);
        add(notice);
        noticedProgress = progress();
        noticedTurn = DailyRun.turns();
        if (fresh) {
            notice.text(DailyRun.objective());
            noticeTime = NOTICE_TIME;
            notice.opacity(0);
        }
    }

    @Override public synchronized void update() {
        if (!ready) return;
        super.update();
        DailyRun.checkObjective();
        updateNotice();
        if (DailyRun.finished() && !resultShown && Window.topWindow() == null) {
            resultShown = true;
            String result;
            if (DailyRun.failureMessage() != null) result = DailyRun.failureMessage();
            else if (DailyRun.timedOut()) result = Messages.get("daily.timeout_" + DailyRun.scenario(DailyRun.day()));
            else {
                String key = "daily.lost";
                if (DailyRun.won()) {
                    if (Dungeon.level instanceof DailyHoardLevel) key = "daily.hoard_won";
                    else if (Dungeon.level instanceof DailyOutpostLevel) key = "daily.outpost_won";
                    else if (Dungeon.level instanceof DailySealLevel) key = "daily.seal_won";
                    else if (Dungeon.level instanceof DailyHuntLevel) key = "daily.hunt_won";
                    else if (Dungeon.level instanceof DailyMineLevel) key = "daily.mine_won";
                    else key = "daily.won";
                }
                result = Messages.get(key, DailyRun.turns());
            }
            show(new WndDailyResult(result));
        }
    }

    private String progress() {
        if (Dungeon.level instanceof DailyOutpostLevel) {
            DailyOutpostLevel outpost = (DailyOutpostLevel)Dungeon.level;
            return outpost.waveNumber() + ":" + outpost.waveActive() + ":" + (outpost.nextWaveTurns() <= 10);
        }
        return DailyRun.objective();
    }

    private void updateNotice() {
        if (DailyRun.turns() != noticedTurn) {
            noticedTurn = DailyRun.turns();
            String progress = progress();
            boolean changed = !progress.equals(noticedProgress);
            noticedProgress = progress;
            int remaining = DailyRun.remainingTurns();
            if (changed || remaining == 50 || remaining == 25 || remaining == 10) {
                String text = changed ? DailyRun.objective() : DailyRun.clockText();
                if (changed && Dungeon.level instanceof DailyOutpostLevel) {
                    DailyOutpostLevel outpost = (DailyOutpostLevel)Dungeon.level;
                    if (outpost.waveNumber() < 5 && outpost.nextWaveTurns() <= 10)
                        text = Messages.get("daily.outpost_next", outpost.nextWaveTurns());
                }
                notice.text(text);
                noticeTime = NOTICE_TIME;
            }
        }
        boolean unobstructed = Window.topWindow() == null && !DailyRun.finished();
        noticeTime = Math.max(0, noticeTime - Game.elapsed);
        notice.visible = unobstructed && noticeTime > 0;
        notice.opacity(Math.min(1, Math.min((NOTICE_TIME - noticeTime) / .25f, noticeTime / .5f)));
    }

    public static void startFailed() {
        error = Messages.get("daily.start_failed");
        DailyRun.leave();
        Game.switchScene(TitleScene.class);
    }

    public static void showError() {
        if (error != null) {
            Game.scene().add(new WndMessage(error));
            error = null;
        }
    }
}
