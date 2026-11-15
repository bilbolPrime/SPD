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

import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.utils.Bundle;

public class Barkskin extends Buff {

	private int level = 0;

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("barkskin_level", level);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        level = Math.max(0, bundle.getInt("barkskin_level"));
    }

	@Override
	public boolean act() {
		if (target.isAlive()) {

			spend( TICK );
			if (--level <= 0) {
				detach();
			}

		} else {

			detach();

		}

		return true;
	}

	public int level() {
		return level;
	}

	@Override
	public int remainingTurns() {
		return Math.max(0, level);
	}

	public void level( int value ) {
		if (level < value) {
			level = value;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BARKSKIN;
	}

	@Override
	public String toString() {
		return "Barkskin";
	}
}
