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
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class Bow extends MissileWeapon {

	{
		name = "bow";
		image = ItemSpriteSheet.Bow;


        stackable = false;
	}

    @Override
    public boolean doEquip( final Hero hero ) {

        if (hero.belongings.bow == null ) {

            hero.belongings.bow = this;
            hero.spendAndNext(TIME_TO_EQUIP);
            detach( hero.belongings.backpack );
            updateQuickslot();
            return true;
        }
        else
        {

            if (hero.belongings.bow.doUnequip( hero, true, false )) {
                doEquip( hero );
            } else {
                collect( hero.belongings.backpack );
            }

            updateQuickslot();
            return true;

        }

    }

    @Override
    public boolean doUnequip( Hero hero, boolean collect, boolean single ) {


        if ( collect( hero.belongings.backpack )) {
            hero.belongings.bow = null;
            updateQuickslot();
            return true;
        } else {
            updateQuickslot();
            return false;
        }
    }

	public Bow() {
		this( 1 );
	}

	public Bow(int number) {
		super();
		quantity = number;
	}


	@Override
	public String desc() {
		return 
			"A basic bow used to fire arrows and bomb arrows.";
	}
	
	@Override
	public Item random() {
		quantity = 1;
		return this;
	}
	
	@Override
	public int price() {
		return quantity * 15;
	}

    @Override
    public void doDrop( Hero hero ) {
        if(hero.belongings.bow == this)
            hero.belongings.bow = null;
        super.doDrop(hero);
    }

    @Override
    public boolean isEquipped( Hero hero ) {
        return hero.belongings.bow == this;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.remove( AC_THROW );
        actions.remove( AC_EQUIP );
        if(Dungeon.hero.belongings.bow != this) {
            if(actions.contains(AC_EQUIP) == false)
                actions.add(AC_EQUIP);
        }
        else
        {
            if(actions.contains(AC_UNEQUIP) == false)
                actions.add(AC_UNEQUIP);
        }

        return actions;
    }

    @Override
    public Bow enchant()
    {
        return Random.Int(10) < 8 ? new FrostBow() : new FlameBow();
    }

    @Override
    public int min(){return 0;}

    @Override
    public int max(){return 0;}

    @Override
    public String info() {

        StringBuilder info = new StringBuilder( desc() );


        return info.toString();
    }

    public void bowSpecial(Char target)
    {

    }
}
