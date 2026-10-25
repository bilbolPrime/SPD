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

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfMending;

public class Regeneration extends Buff {

	private static final float REGENERATION_DELAY = 10;

	@Override
	public boolean act() {
		if (target.isAlive()) {

			Hero hero = (Hero)target;
			if (target.HP < target.HT && !hero.isStarving()) {
				target.HP += 1;
			}

			int bonus = 0;
			for (Buff buff : target.buffs( RingOfMending.Rejuvenation.class )) {
				bonus += ((RingOfMending.Rejuvenation)buff).level;
			}

			int skillBonus = hero.heroSkills.passiveA2.healthRegenerationBonus()
					+ hero.skillTree.getRegenerationBonus();
			float delay = REGENERATION_DELAY
					* com.bilboldev.pixeldungeonskills.actors.skills.Regeneration.delayMultiplier(skillBonus);
			spend( (float)(delay / Math.pow( 1.2, bonus )) );

		} else {

			diactivate();

		}

		return true;
	}
}
