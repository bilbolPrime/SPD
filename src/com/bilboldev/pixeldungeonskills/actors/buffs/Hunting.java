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
package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.food.MysteryMeat;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class Hunting extends Buff {



	@Override
	public boolean act() {
		if (target.isAlive()) {


			Hero hero = (Hero)target;



            if(hero.heroSkills.passiveA2.hunting() < 1)// Huntress Hunting if present
            {
                spend( 100 );
                return true;
            }


                  GLog.p("Hunted... something...");
                  MysteryMeat steak = new MysteryMeat();
                  Dungeon.level.drop( steak, hero.pos ).sprite.drop();

                spend( 100 - 10 * hero.heroSkills.passiveA2.hunting() );

		} else {

			diactivate();

		}

		return true;
	}
	

}
