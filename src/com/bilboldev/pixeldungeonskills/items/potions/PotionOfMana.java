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
package com.bilboldev.pixeldungeonskills.items.potions;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.ManaRecovery;
import com.bilboldev.pixeldungeonskills.actors.buffs.Recovery;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class PotionOfMana extends Potion {

	{
		name = "Potion of Mana";
	}

	@Override
	public void execute(Hero hero, String action) {
		if (AC_DRINK.equals(action) && isKnown() && Dungeon.rules().mode == GameplayMode.REBORN) {
			confirmAction(hero, Message.key(PotionOfMana.class, "recover", Recovery.DURATION),
					Message.action(this, AC_DRINK), Message.key(Item.class, "cancel"), () -> drink(hero));
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	protected void apply( Hero hero ) {
		setKnown();

		if (Dungeon.rules().mode == GameplayMode.REBORN) {
			Buff.affect(hero, ManaRecovery.class).start();
		} else {
			GLog.p("custom.generated.entry_you_feel_your_spiritual_energy_being_replenished_297caad714");
			hero.MP = hero.MMP;
		}
		StatusPane.manaDropping = 0;
	}

	@Override
	public String desc() {
		if (Dungeon.rules().mode == GameplayMode.REBORN) return Messages.get(this, "desc_reborn");
		return
			"An elixir that will instantly replenish your spiritual energy.";
	}

	@Override
	public String displayInfo() {
		return Dungeon.rules().mode == GameplayMode.REBORN ? info() : super.displayInfo();
	}

	@Override
	public int price() {
		return isKnown() ? 30 * quantity : super.price();
	}
}
