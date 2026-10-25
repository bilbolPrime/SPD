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

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class PotionOfStrength extends Potion {

	{
		name = "Potion of Strength";
	}

	@Override
	protected void apply( Hero hero ) {
		setKnown();

		hero.STR++;
		hero.sprite.showStatus( CharSprite.POSITIVE, Message.key("custom.generated.entry_1_str_8c608e5279") );
		GLog.p("custom.generated.entry_newfound_strength_surges_through_your_body_193442f0c3");

		Badges.validateStrengthAttained();
	}

	@Override
	public String desc() {
		return
			"This powerful liquid will course through your muscles, " +
			"permanently increasing your strength by one point.";
	}

	@Override
	public int price() {
		return isKnown() ? 100 * quantity : super.price();
	}
}
