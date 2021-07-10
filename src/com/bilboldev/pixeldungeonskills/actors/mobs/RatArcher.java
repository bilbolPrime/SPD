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
package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.sprites.RatArcherSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.utils.Random;

public class RatArcher extends Rat {

	{
		name = "marsupial ranged rat";
		defenseSkill = 2;
        name = Dungeon.currentDifficulty.mobPrefix() + name;
        range = 3;
		spriteClass = RatArcherSprite.class;
	}


	@Override
	public String description() {
		return
			super.description() + "\n" +
					"This one seems capable of attacking from distance...";
	}
}
