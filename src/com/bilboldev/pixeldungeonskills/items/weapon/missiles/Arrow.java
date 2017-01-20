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
package com.bilboldev.pixeldungeonskills.items.weapon.missiles;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class Arrow extends MissileWeapon {

	{
		name = "arrow";
		image = ItemSpriteSheet.Arrow;

        stackable = true;
	}

	public Arrow() {
		this( 1 );
	}

	public Arrow(int number) {
		super();
		quantity = number;
	}


	@Override
	public String desc() {
		return 
			"Arrows are more powerful and accurate than darts, but they require an equipped bow.";
	}
	
	@Override
	public Item random() {
        quantity = Random.Int(5, 20);
		return this;
	}
	
	@Override
	public int price() {
		return quantity * 5;
	}

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if(Dungeon.hero.belongings.bow != null) {
            if(actions.contains(AC_THROW) == false)
            actions.add(AC_THROW);
        }
        else
            actions.remove( AC_THROW );
        actions.remove(AC_EQUIP);

        return actions;
    }

    @Override
    public int min(){return 3;}


    @Override
    public int max(){return 5;}

    @Override
    public String info() {

        StringBuilder info = new StringBuilder( desc() );


        return info.toString();
    }

}
