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

import com.bilboldev.pixeldungeonskills.utils.GLog;

public class ScrollOfReadiness extends Scroll {

	{
		name = "Scroll of Readiness";
	}

	@Override
	protected void doRead() {

        GLog.w("custom.generated.entry_inspiring_words_that_boost_moral_and_remove_fati_114cb7f9b0");
        GLog.p("custom.generated.entry_your_active_skills_are_ready_6c0b4530f6");
		setKnown();

		curUser.spendAndNext( TIME_TO_READ );
	}

	@Override
	public String desc() {
		return
			"When read, words of inspiration will make your ready for combat.";
	}
}
