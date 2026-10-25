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

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.ArrayList;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GamesInProgress;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.SaveSlots;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.pixeldungeonskills.windows.WndPartyLobby;

public class StartScene extends TitleScene {
    private enum Step {
        SLOT, MODE, PARTY, NEARBY, CONTROLS, LOBBY, COMPANION, DEPART, DIFFICULTY, CONFIRM
    }
    private static class StartFlow {
        final HeroClass heroClass;
        Step step;
        int slot = SaveSlots.FIRST;
        GameplayMode mode = GameplayMode.CLASSIC;
        int difficulty = Difficulties.NORMAL.ordinal();
        HeroClass companion;
        boolean splitControls;
        boolean network;
        boolean hosting;
        float departureTime;
        StartFlow(HeroClass heroClass, Step step) {
            this.heroClass = heroClass;
            this.step = step;
        }
    }
    private static StartFlow pendingStart;
    private static Window activeStartWindow;
    public static HeroClass curClass;

    @Override
    protected Page initialPage() {
        return Page.HEROES;
    }

    static void beginStartFlow(PixelScene host, HeroClass heroClass) {
        curClass = heroClass;
        pendingStart = new StartFlow(heroClass, Step.SLOT);
        ensureStartDialog(host);
    }

    static void beginPartyFlow(PixelScene host, HeroClass heroClass) {
        curClass = heroClass;
        pendingStart = new StartFlow(heroClass, Step.PARTY);
        pendingStart.mode = GameplayMode.REBORN;
        ensureStartDialog(host);
    }

    static void cancelStartFlow() {
        pendingStart = null;
        activeStartWindow = null;
    }

    public static void finishNetworkStart() {
        TitleScene.leavingForRun();
    }
    static void releaseWindow() {
        activeStartWindow = null;
    }
    static HeroClass pendingHero() {
        return pendingStart == null ? null : pendingStart.heroClass;
    }

    static HeroClass companionLeader() {
        return pendingStart != null && (pendingStart.step == Step.COMPANION || pendingStart.step == Step.DEPART)
                ? pendingStart.heroClass : null;
    }

    static boolean choosingCompanion() {
        return pendingStart != null && pendingStart.step == Step.COMPANION;
    }

    static boolean chooseCompanion(HeroClass heroClass) {
        if (!choosingCompanion() || heroClass == pendingStart.heroClass) return false;
        pendingStart.companion = heroClass;
        pendingStart.step = Step.DEPART;
        pendingStart.departureTime = PixelDungeon.titleAnimation() ? .4f : 0;
        return true;
    }

    static void updateDeparture(float elapsed) {
        if (pendingStart == null || pendingStart.step != Step.DEPART || Window.topWindow() != null) return;
        pendingStart.departureTime -= elapsed;
        if (pendingStart.departureTime <= 0) startNewGame(pendingStart);
    }

    static void ensureStartDialog(final PixelScene host) {
        if (pendingStart == null || (activeStartWindow != null && activeStartWindow.parent == host)) return;
        final StartFlow flow = pendingStart;
        final Step step = flow.step;
        if (step == Step.COMPANION || step == Step.DEPART) return;
        if (step == Step.SLOT) {
            activeStartWindow = new com.bilboldev.pixeldungeonskills.windows.WndSaveSlots(flow.heroClass, host instanceof TitleScene) {
                @Override
                protected void onSelect(int slot, boolean replace) {
                    activeStartWindow = null;
                    if (pendingStart != flow) return;
                    flow.slot = slot;
                    GamesInProgress.Info info = GamesInProgress.check(flow.heroClass, slot);
                    if (info != null && !replace) {
                        pendingStart = null;
                        if (info.unavailable) {
                            host.add(new WndMessage(Message.key(StartScene.class, "unavailable_message")));
                        } else {
                            PartySession.cancelLobby();
                            LocalCoop.clear();
                            curClass = flow.heroClass;
                            InterlevelScene.continueSlot = slot;
                            MissionScene.scenePause = false;
                            InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
                            TitleScene.leavingForRun();
                            Game.switchScene(InterlevelScene.class);
                        }
                    } else {
                        flow.step = Step.MODE;
                        ensureStartDialog(host);
                    }
                }

                @Override
                public void onBackPressed() {
                    pendingStart = null;
                    activeStartWindow = null;
                    super.onBackPressed();
                }
            };
            host.add(activeStartWindow);
            return;
        }
        if (step == Step.LOBBY) {
            activeStartWindow = new WndPartyLobby(flow.hosting, () -> {
                pendingStart = null;
                activeStartWindow = null;
            }, request -> {
                if (pendingStart != flow) return;
                activeStartWindow = null;
                flow.network = true;
                flow.companion = request.second;
                startNewGame(flow);
            });
            host.add(activeStartWindow);
            return;
        }
        ArrayList<String> options = new ArrayList<String>();
        String title;
        String message;
        switch (step) {
            case MODE:
                title = Messages.get(StartScene.class, "gameplay_title");
                message = Messages.get(StartScene.class, "gameplay_message");
                options.add(GameplayMode.CLASSIC.title());
                options.add(GameplayMode.REBORN.title());
                break;
            case DIFFICULTY:
                title = Messages.maybeTranslate("Game Difficulty");
                message = Messages.maybeTranslate("Cannot be changed in game!");
                options.add(Difficulties.EASY.title());
                options.add(Difficulties.NORMAL.title());
                options.add(Difficulties.HELL.title());
                options.add(Difficulties.SUICIDE.title());
                options.add(Difficulties.JUSTKILLME.title());
                break;
            case PARTY:
                title = Messages.get(StartScene.class, "party_mode");
                message = Messages.get(StartScene.class, "party_where");
                options.add(Messages.get(StartScene.class, "same_device"));
                options.add(Messages.get(StartScene.class, "nearby_party"));
                break;
            case NEARBY:
                title = Messages.get(StartScene.class, "nearby_party");
                message = Messages.get(StartScene.class, "nearby_message");
                options.add(Messages.get(StartScene.class, "host_party"));
                options.add(Messages.get(StartScene.class, "join_party"));
                break;
            case CONTROLS:
                title = Messages.get(StartScene.class, "party_controls");
                message = Messages.get(StartScene.class, "controls_message");
                options.add(Messages.get(StartScene.class, "shared_controls"));
                options.add(Messages.get(StartScene.class, "split_controls"));
                break;
            case CONFIRM:
                title = Messages.get(StartScene.class, "start_mode", flow.mode.title());
                message = Difficulties.description(flow.difficulty);

                if (SaveSlots.exists(flow.heroClass, flow.slot))
                    message += "\n\n" + Messages.get(StartScene.class, "replace_warning");
                options.add(Messages.get(StartScene.class, "start_run"));
                options.add(Messages.get(StartScene.class, "cancel_run"));
                break;
            default:
                throw new IllegalStateException("Unknown new-game step");
        }
        activeStartWindow = new WndOptions(title, message, options.toArray(new String[0])) {
            @Override
            protected boolean shouldTranslateOptionLabels() {
                return false;
            }

            @Override
            protected float titleTextScale() {
                return step == Step.CONFIRM && isDesktopRuntime() ? desktopOverlayTextScale() : 1f;
            }

            @Override
            protected float messageTextScale() {
                return step == Step.CONFIRM && isDesktopRuntime() ? desktopOverlayTextScale() * 0.75f : 1f;
            }

            @Override
            protected void onSelect(int index) {
                activeStartWindow = null;
                if (pendingStart != flow) return;
                switch (step) {
                    case MODE:
                        {
                            flow.mode = index == 0 ? GameplayMode.CLASSIC : GameplayMode.REBORN;
                            if (flow.mode == GameplayMode.REBORN) {
                                startNewGame(flow);
                                return;
                            } else {
                                flow.step = Step.DIFFICULTY;
                            }
                        }
                        break;
                    case PARTY:
                        if (index == 1) {
                            flow.step = Step.NEARBY;
                        } else if (isDesktopRuntime()) {
                            flow.step = Step.CONTROLS;
                        } else {
                            choosePartner(host, flow);
                            return;
                        }
                        break;
                    case NEARBY:
                        openLobby(flow, index == 0);
                        break;
                    case CONTROLS:
                        flow.splitControls = index == 1;
                        choosePartner(host, flow);
                        return;
                    case DIFFICULTY:
                        flow.difficulty = Difficulties.getNormalizedDifficulty(index);
                        flow.step = Step.CONFIRM;
                        break;
                    case CONFIRM:
                        pendingStart = null;
                        if (index == 0) startNewGame(flow);
                        return;
                }
                ensureStartDialog(host);
            }

            @Override
            public void onBackPressed() {
                pendingStart = null;
                activeStartWindow = null;
                super.onBackPressed();
            }
        };
        host.add(activeStartWindow);
    }

    private static void choosePartner(PixelScene host, StartFlow flow) {
        flow.step = Step.COMPANION;
        if (host instanceof ClassicStartScene) {
            ((ClassicStartScene)host).refreshHeroes();
        } else {
            TitleScene.selectedHero = null;
            ((TitleScene)host).refreshHeroes();
        }
    }

    private static void openLobby(StartFlow flow, boolean hosting) {
        flow.hosting = hosting;
        flow.step = Step.LOBBY;
        if (hosting) {
            PartySession.host(flow.heroClass);
        } else {
            PartySession.discover(flow.heroClass);
        }
    }

    private static void startNewGame(StartFlow flow) {
        pendingStart = null;
        curClass = flow.heroClass;
        TitleScene.leavingForRun();
        if (!flow.network) PartySession.cancelLobby();
        LocalCoop.clear();
        Dungeon.prepareNewRun(flow.slot, flow.mode, flow.difficulty);
        if (flow.companion == null) {
            Dungeon.deleteGame(flow.heroClass, flow.slot, true);
        } else {
            if (!flow.network) PartySession.sameDevice(flow.splitControls);
            LocalCoop.prepare(flow.heroClass, flow.companion);
        }
        InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
        InterlevelScene.noStory = false;
        MissionScene.scenePause = false;
        if (PixelDungeon.intro()) {
            PixelDungeon.intro(false);
            Game.switchScene(IntroScene.class);
        } else {
            Game.switchScene(InterlevelScene.class);
        }
    }

}
