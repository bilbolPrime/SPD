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
package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public abstract class NPC extends Mob {

	private transient Hero owner = LocalCoop.active() ? Dungeon.hero : null;

	public Hero owner() {
		return LocalCoop.active() && owner != null ? owner : Dungeon.hero;
	}

	public void partyOwner(Hero hero) {
		if (LocalCoop.active()) owner = hero;
	}

	{
		HP = HT = 1;
		EXP = 0;

		hostile = false;
		state = PASSIVE;
	}

	@Override
	protected Char chooseNonHeroEnemy() {

		if (state != PASSIVE && state != FLEEING
				&& buff(Amok.class) == null && buff(Terror.class) == null) {
			ArrayList<Mob> enemies = new ArrayList<Mob>();
			for (Mob mob : Dungeon.level.mobs) {
				if (mob != this && mob.hostile && mob.isAlive()
						&& Level.fieldOfView[mob.pos] && mob.invisible <= 0
						&& !isCharmedBy(mob) && canAttack(mob)) {
					enemies.add(mob);
				}
			}
			if (!enemies.isEmpty()) return Random.element(enemies);
		}
		return chooseEnemy();
	}

	protected void throwItem() {
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null) {
			int n;
			do {
				n = pos + Level.NEIGHBOURS8[Random.Int( 8 )];
			} while (!Level.passable[n] && !Level.avoid[n]);
			Dungeon.level.drop( heap.pickUp(), n ).sprite.drop( pos );
		}
	}

	@Override
	public void beckon( int cell ) {
	}

	abstract public void interact();
}
