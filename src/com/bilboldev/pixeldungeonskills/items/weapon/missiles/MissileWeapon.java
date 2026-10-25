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

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.ArrayList;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;

abstract public class MissileWeapon extends Weapon {

	private static final String TXT_MISSILES	= "Missile weapon";
	private static final String TXT_YES			= "Yes, I know what I'm doing";
	private static final String TXT_NO			= "No, I changed my mind";
	private static final String TXT_R_U_SURE	=
		"Do you really want to equip it as a melee weapon?";

	{
		stackable = true;
		levelKnown = true;
		defaultAction = AC_THROW;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.heroClass != HeroClass.HUNTRESS && hero.heroClass != HeroClass.ROGUE) {
			actions.remove( AC_EQUIP );
			actions.remove( AC_UNEQUIP );
		}
		return actions;
	}

	@Override
	protected void onThrow( int cell ) {
		Char enemy = Actor.findChar( cell );
		if (enemy == null || enemy == curUser) {
			super.onThrow( cell );
		} else {
			if (!curUser.shoot( enemy, this )) {
				miss( cell );
			}
		}
	}

	protected void miss( int cell ) {
		super.onThrow( cell );
	}

	@Override
	public void proc( Char attacker, Char defender, int damage ) {

		super.proc( attacker, defender, damage );

		Hero hero = (Hero)attacker;
		if (hero.rangedWeapon == null && stackable) {
			if (quantity == 1) {
				doUnequip( hero, false, false );
			} else {
				detach( null );
			}
		}
	}

	@Override
	public boolean doEquip( final Hero hero ) {
		GameScene.show(
			new WndOptions(Message.key("custom.generated.entry_missile_weapon_1dc9f454b2"),
			        Message.key("custom.generated.entry_do_you_really_want_to_equip_it_as_a_melee_weapon_08f7984b51"),
			        Message.key("items.artifacts.chaliceofblood.yes"),
			        Message.key("items.artifacts.chaliceofblood.no")) {
				@Override
				protected void onSelect(int index) {
					if (index == 0) {
						MissileWeapon.super.doEquip( hero );
					}
				};
			}
		);

		return false;
	}

	@Override
	public Item random() {
		return this;
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
	public String info() {

		StringBuilder info = new StringBuilder( desc() );

		int min = min();
		int max = max();
		info.append( "\n\n" );
		info.append( Messages.get( MissileWeapon.class, "damage_desc", min, max ) );
		info.append( "\n\n" );
		info.append( Messages.get( MissileWeapon.class, "distance" ) );

		if (Dungeon.hero.belongings.backpack.items.contains( this )) {
			if (STR > Dungeon.hero.STR()) {
				info.append( "\n\n" );
				info.append( Messages.get( Weapon.class, "too_heavy" ) );
			}
			if (STR < Dungeon.hero.STR()) {
				info.append( "\n\n" );
				info.append( Messages.get( Weapon.class, "excess_str", Dungeon.hero.STR() - STR ) );
			}
		}

		if (isEquipped( Dungeon.hero )) {
			if (cursed) {
				info.append( "\n\n" );
				info.append( Messages.get( Weapon.class, "cursed_worn" ) );
			}
		} else if (cursedKnown && cursed) {
			info.append( "\n\n" );
			info.append( Messages.get( Weapon.class, "cursed" ) );
		}

		return info.toString();
	}
}
