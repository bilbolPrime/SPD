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
package com.bilboldev.pixeldungeonskills.items.scrolls;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.IceCaves;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;

import java.util.ArrayList;

public class ScrollOfFrostLevel extends Scroll {

    public static final String AC_TEAR = "TEAR";
    private static final float FREEZE_DURATION = 1000f;

	{
		name = "Scroll of Frost";
	}

    @Override
    public boolean isKnown() {
        return true;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (Dungeon.rules().mode == GameplayMode.REBORN) actions.add(AC_TEAR);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (AC_TEAR.equals(action)) {
            if (Dungeon.rules().mode != GameplayMode.REBORN) return;
            confirmAction(hero, Message.key(ScrollOfFrostLevel.class, "tear_confirm"),
                    Message.key("items.artifacts.chaliceofblood.yes"),
                    Message.key("items.artifacts.chaliceofblood.no"), () -> tear(hero));
            return;
        }
        super.execute(hero, action);
    }

    @Override
    protected boolean canRead(Hero hero) {
        if (Dungeon.rules().mode != GameplayMode.REBORN || IceCaves.active()
                || Dungeon.depth < 6 || Dungeon.depth > 10) {
            GLog.w(ScrollOfFrostLevel.class, "wrong_place");
            return false;
        }
        if (IceCaves.visited()) {
            GLog.w(ScrollOfFrostLevel.class, "spent");
            return false;
        }
        return super.canRead(hero);
    }

    @Override
    protected Message readWarning(Hero hero) {
        return Message.key(ScrollOfFrostLevel.class, "read_confirm");
    }

    private void tear(Hero hero) {
        if (!hero.isAlive() || !hero.belongings.backpack.contains(this)) return;
        detach(hero.belongings.backpack);
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (mob.hostile && mob.isAlive() && !(mob instanceof NPC) && !Bestiary.isBoss(mob)) {
                Buff.prolong(mob, Frost.class, FREEZE_DURATION);
            }
        }
        Sample.INSTANCE.play(Assets.SND_READ);
        Invisibility.dispel();
        Lore.used(this);
        hero.spend(TIME_TO_READ);
        hero.busy();
        ((HeroSprite)hero.sprite).read();
    }

	@Override
	protected void doRead() {

        Sample.INSTANCE.play(Assets.SND_READ);
        Invisibility.dispel();
        setKnown();
        curUser.spend(TIME_TO_READ);
        InterlevelScene.mode = InterlevelScene.Mode.FROST;
        Game.switchScene(InterlevelScene.class);
	}

	@Override
	public String desc() {
		return Messages.get(ScrollOfFrostLevel.class, "desc");
	}

	@Override
	public int price() {
		return isKnown() ? 80 * quantity : super.price();
	}
}
