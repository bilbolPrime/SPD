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
package com.bilboldev.pixeldungeonskills.items.bags;

import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class PotionHolder extends Bag {

	{
		name = "potion holder";
		image = ItemSpriteSheet.POTION_HOLDER;
		
		size = 18;
	}
	
	@Override
	public boolean grab( Item item ) {
		return item instanceof Potion;
	}
	
	@Override
	public int price() {
		return 50;
	}
	
	@Override
	public String info() {
		return
			"You can place any number of potions into this container.";
	}
}
