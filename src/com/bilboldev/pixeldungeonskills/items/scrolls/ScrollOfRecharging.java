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

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.SpellSprite;
import com.bilboldev.pixeldungeonskills.effects.particles.EnergyParticle;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class ScrollOfRecharging extends Scroll {

	{
		name = "Scroll of Recharging";
	}

	@Override
	protected void doRead() {

		int count = curUser.belongings.charge( true );
		charge( curUser );

		Sample.INSTANCE.play( Assets.SND_READ );
		Invisibility.dispel();

		if (count > 0) {
			if (count > 1) {
				GLog.i("custom.generated.entry_a_surge_of_energy_courses_through_your_pack_rech_af87e7400b");
			} else {
				GLog.i("custom.generated.manual.recharging_your_wand");
			}
			SpellSprite.show( curUser, SpellSprite.CHARGE );
		} else {
			GLog.i("custom.generated.entry_a_surge_of_energy_courses_through_your_pack_but_f5837fa791");
		}
		setKnown();

		readAnimation();
	}

	@Override
	public String desc() {
		return
			"The raw magical power bound up in this parchment will, when released, " +
			"recharge all of the reader's wands to full power.";
	}

	public static void charge( Hero hero ) {
		hero.sprite.centerEmitter().burst( EnergyParticle.FACTORY, 15 );
	}

	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}
