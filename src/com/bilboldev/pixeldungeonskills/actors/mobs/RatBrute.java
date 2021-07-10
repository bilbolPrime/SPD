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
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.sprites.RatArcherSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatBruteSprite;

public class RatBrute extends Rat {

	{
		name = "marsupial brute rat";
		HP = HT = 20 + GauntletScene.level / 5;
		defenseSkill = 7;
        name = Dungeon.currentDifficulty.mobPrefix() + name;
		HT *= Dungeon.currentDifficulty.mobHPModifier();
		HP = HT;
		spriteClass = RatBruteSprite.class;
	}


	@Override
	public String description() {
		return
			super.description() + "\n" +
					"This one seems larger than the average rat... tougher as well.";
	}

	@Override
	public boolean act(){
		if (HP <= 0) {
			return true;
		} else {
			try
			{
				sprite. tint( 1.7f, 0.80f, 0.80f, 0.2f );
				//sprite.alpha(1f);
			}
			catch (Exception e){

			}


			return super.act();
		}
	}
}
