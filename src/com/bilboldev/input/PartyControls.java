package com.bilboldev.input;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.Window;

public final class PartyControls {
    private PartyControls() { }

    public static boolean pointer() {
        return select(0);
    }

    public static boolean keyboard() {
        return select(1);
    }

    public static boolean keyboardCursor() {
        if (!PartySession.splitControls() || !(Game.scene() instanceof GameScene)) return false;
        Hero[] heroes = LocalCoop.heroes();
        return heroes.length == 2 && Dungeon.hero == heroes[1]
                && (Window.topWindow() != null || !GameScene.canAcceptMovement());
    }

    private static boolean select(int player) {
        if (!(Game.scene() instanceof GameScene) || !LocalCoop.active()) return true;
        Hero hero;
        if (PartySession.network()) {
            hero = PartySession.localHero();
        } else if (PartySession.splitControls()) {
            Hero[] heroes = LocalCoop.heroes();
            hero = heroes.length == 2 ? heroes[player] : null;
        } else {
            return true;
        }
        if (hero == null) return false;
        if (hero == Dungeon.hero) return true;
        if (Window.topWindow() != null || !Touchscreen.pointers.isEmpty()
                || !GameScene.canAcceptMovement() || !LocalCoop.acceptsInput()) return false;
        LocalCoop.select(hero, false);
        return Dungeon.hero == hero;
    }
}
