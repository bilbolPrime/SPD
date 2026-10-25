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

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class ScrollOfLullaby extends Scroll {

	{
		name = "Scroll of Lullaby";
	}

	@Override
	protected void doRead() {

		curUser.sprite.centerEmitter().start( Speck.factory( Speck.NOTE ), 0.3f, 5 );
		Sample.INSTANCE.play( Assets.SND_LULLABY );
		Invisibility.dispel();

		int count = 0;
		Mob affected = null;
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Level.fieldOfView[mob.pos]) {
				Buff.affect( mob, Sleep.class );
				if (mob.buff( Sleep.class ) != null) {
					affected = mob;
					count++;
				}
			}
		}

		switch (count) {
		case 0:
			GLog.i("custom.generated.entry_the_scroll_utters_a_soothing_melody_8f4bfe2775");
			break;
		case 1:
			GLog.i("custom.generated.entry_the_scroll_utters_a_soothing_melody_and_the_s_fa_f5e3877aba", Message.actor(affected));
			break;
		default:
			GLog.i("custom.generated.entry_the_scroll_utters_a_soothing_melody_and_the_mons_2ca746ffba");
		}
		setKnown();

		readAnimation();
	}

	@Override
	public String desc() {
		return
			"A soothing melody will put all creatures in your field of view into a deep sleep, " +
			"giving you a chance to flee or make a surprise attack on them.";
	}

	@Override
	public int price() {
		return isKnown() ? 50 * quantity : super.price();
	}
}
