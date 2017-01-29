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
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Random;

public class ScrollOfFrostLevel extends Scroll {

	{
		name = "Scroll of Frost";
	}
	
	@Override
	protected void doRead() {

        ColdGirl.cameFrom = Dungeon.depth;
        ColdGirl.cameFromPos = Dungeon.hero.pos;
        InterlevelScene.mode = InterlevelScene.Mode.TELEPORT;
        Game.switchScene(InterlevelScene.class);
        Dungeon.observe();

        setKnown();


        curUser.spendAndNext( TIME_TO_READ );
	}
	
	@Override
	public String desc() {
		return
			"The scroll seems colder than the surroundings. As if it were a portal to a colder dimension.";
	}
	
	@Override
	public int price() {
		return isKnown() ? 80 * quantity : super.price();
	}
}
