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

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class ScrollOfSacrifice extends Scroll {

	{
		name = "Scroll of Sacrifice";
	}

	@Override
	protected void doRead() {

        Dungeon.hero.STR++;
        if(Dungeon.hero.HT > 3) {

            Dungeon.hero.HT-= 3;
        }
        else
        {
            Dungeon.hero.HT = 1;
        }

        if(Dungeon.hero.HP > Dungeon.hero.HT)
            Dungeon.hero.HP = Dungeon.hero.HT;

        GLog.w("custom.generated.entry_the_scroll_boosts_your_strength_but_at_a_cost_1162a17fd6");
        GLog.p("custom.generated.entry_1_strength_d542813ae0");
        GLog.n("custom.generated.entry_3_max_hp_bfc464b543");
		setKnown();

		curUser.spendAndNext( TIME_TO_READ );
	}

	@Override
	public String desc() {
		return
			"A scroll that boosts strength at a certain cost.";
	}
}
