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

import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfElements.Resistance;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;

public class Poison extends Buff implements Hero.Doom {

	protected float left;
	private boolean championSource;
	private transient Hero source;

	private static final String LEFT	= "left";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
		bundle.put( "champion_source", championSource );

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getFloat( LEFT );
		championSource = bundle.getBoolean( "champion_source" );
	}

	public void set( float duration ) {
		this.left = duration;
		source = LocalCoop.active() ? Dungeon.hero : null;
		championSource = false;
	};

	public void setChampionDuration(float duration) {
		set(duration);
		championSource = true;
	}

	public void extendItemDuration(float duration) {
		if (left <= 0) championSource = true;
		left = Math.max(left, duration);
	}

	@Override
	public int icon() {
		return BuffIndicator.POISON;
	}

	@Override
	public String toString() {
		return "Poisoned";
	}

	@Override
	public boolean act() {
		if (LocalCoop.active() && source != null && source != Dungeon.hero) {
			return LocalCoop.withHero(source, () -> tick());
		}
		return tick();
	}

	private boolean tick() {
		if (target.isAlive()) {

            if (target instanceof Mob && !championSource)
                target.damage( ((int)(Dungeon.hero.skillTree.venomDamageModifier() * ((int)(left / 3) + 1 ))), this );
            else
			    target.damage( (int)(left / 3) + 1, this );
			spend( TICK );

			if ((left -= TICK) <= 0) {
				detach();
			}

		} else {

			detach();

		}

		return true;
	}

	public static float durationFactor( Char ch ) {
		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() : 1;
	}

	@Override
	public void onDeath() {
		Badges.validateDeathFromPoison();

		Dungeon.fail( Utils.format( ResultDescriptions.POISON, Dungeon.depth ), Poison.class );
		GLog.n(Poison.class, "ondeath");
	}
}
