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
package com.bilboldev.pixeldungeonskills.items.food;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class OverpricedRation extends Food {

	{
		name = "overpriced food ration";
		image = ItemSpriteSheet.OVERPRICED;
		energy = Hunger.STARVING - Hunger.HUNGRY;
		message = "That food tasted ok.";
	}

	@Override
	public String info() {
		return desc();
	}

	@Override
	public String name() {
		return localized( "name", name );
	}

	@Override
	public String coreName() {
		return localized( "name", name );
	}

	@Override
	public String desc() {
		return localized( "desc", super.desc() );
	}

	@Override
	protected Message eatMessage() {
		return Message.key("items.food.smallration.eat_msg");
	}

	@Override
	public int price() {
		return 20 * quantity;
	}

	private String localized( String key, String fallback ) {
		String value = Messages.get( "items.food.smallration." + key );
		return Messages.NO_TEXT_FOUND.equals( value ) ? fallback : value;
	}
}
