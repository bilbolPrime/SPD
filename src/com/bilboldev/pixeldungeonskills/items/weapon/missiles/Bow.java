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

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class Bow extends MissileWeapon {

    public static final String AC_SHOOT = "SHOOT";

	{
        name = "Bow";
		image = ItemSpriteSheet.Bow;

        stackable = false;
        defaultAction = AC_SHOOT;
	}

    public static Arrow ammunition(Hero hero) {
        for (Item item : hero.belongings.backpack) {
            if (item.getClass() == Arrow.class && item.quantity() > 0) return (Arrow)item;
        }
        return null;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (!AC_SHOOT.equals(action)) {
            super.execute(hero, action);
            return;
        }
        if (!hero.isAlive()) return;
        if (!isEquipped(hero)) {
            QuickSlot.cancel();
            GLog.w(Bow.class, "equip");
            return;
        }
        Arrow arrows = ammunition(hero);
        if (arrows == null) {
            QuickSlot.cancel();
            GLog.w(Bow.class, "no_arrows");
            return;
        }
        arrows.execute(hero, AC_THROW);
    }

    @Override
    public boolean doEquip( final Hero hero ) {

        if (isEquipped(hero)) return true;

        if (hero.belongings.bow == null ) {

            hero.belongings.bow = this;
            hero.refreshBonuses();
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
            hero.refreshBonuses();
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
        String localized = message( "desc" );
        return localized != null ? localized :
            "A basic bow used to fire arrows and bomb arrows.";
	}

	@Override
	public Item random() {
		EquipmentAttribute.roll(this);
		randomPrefix();
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
        QuickSlot.cancel();
        QuickSlot.refresh();
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
        if(hero.belongings.bow != this) {
            if(actions.contains(AC_EQUIP) == false)
                actions.add(AC_EQUIP);
        }
        else
        {
            actions.add(AC_SHOOT);
            if(actions.contains(AC_UNEQUIP) == false)
                actions.add(AC_UNEQUIP);
        }

        return actions;
    }

    @Override
    public Bow enchant()
    {
        Bow bow = Random.Int(10) < 8 ? new FrostBow() : new FlameBow();
        bow.weaponEffect = weaponEffect;
        return bow;
    }

    @Override
    public int min(){return 0;}

    @Override
    public int max(){return 0;}

    @Override
    public String info(Hero viewer) {
        return info();
    }

    @Override
    public String info() {

        StringBuilder info = new StringBuilder( desc() );
        info.append(prefixInfo());
        return info.toString();
    }

    public void bowSpecial(Char target)
    {

    }

    public void bowSpecial(Hero archer, Char target, int damage) {
        bowSpecial(target);
    }

    public float damageBonus(){
	    if(level < 3)
	        return  1 + 0.2f * level;
	    if(level < 5)
	        return  1 + 0.6f + (level - 3) * 0.1f;

	    return  1 + 0.8f + (level - 5) * 0.05f;
    }

    @Override
    public boolean isUpgradable(){
	    return  true;
    }
}
