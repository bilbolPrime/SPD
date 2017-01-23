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
package com.bilboldev.pixeldungeonskills.items;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.utils.Random;

public class RemoteBomb extends Item {
	
	{
		name = "remote bomb";
		image = ItemSpriteSheet.RemoteBomb;
		defaultAction = AC_THROW;
		stackable = true;
	}

	@Override
	protected void onThrow( int cell ) {
		if (Level.pit[cell]) {
			super.onThrow( cell );
		}
        else
        {
            RemoteBombGround tmp = new RemoteBombGround();
            tmp.pos = cell;
            Dungeon.level.drop( tmp, cell ).sprite.drop();
        }


    }
			




	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public Item random() {
        quantity = Random.Int(1, 1);
		return this;
	}	
	
	@Override
	public int price() {
		return 10 * quantity;
	}
	
	@Override
	public String info() {
		return
                "After being thrown, this bomb will explode once it receives a signal from a trigger beacon.";
	}
}
