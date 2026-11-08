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
package com.bilboldev.pixeldungeonskills.items.weapon.melee;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Random;

public class MeleeWeapon extends Weapon {

	public int tier;

	public MeleeWeapon( int tier, float acu, float dly ) {
		super();

		this.tier = tier;

		ACU = acu;
		DLY = dly;

		STR = typicalSTR();
	}

	protected int min0() {
		return tier;
	}

	protected int max0() {
		return (int)((tier * tier - tier + 10) / ACU * DLY);
	}

	@Override
	public int min() {
		return isBroken() ? min0() : min0() + level();
	}

	@Override
	public int max() {
		return isBroken() ? max0() : max0() + level() * tier;
	}

	@Override
	final public Item upgrade() {
		return upgrade( false );
	}

	public Item upgrade( boolean enchant ) {
		STR--;
		return super.upgrade( enchant );
	}

	public Item safeUpgrade() {
		return upgrade( enchantment != null );
	}

	@Override
	public Item degrade() {
		STR++;
		return super.degrade();
	}

	public int typicalSTR() {
		return 8 + tier * 2;
	}

	@Override
	public String info() {
		return info(Dungeon.hero);
	}

	@Override
	public String info(Hero viewer) {

		final String p = "\n\n";

		StringBuilder info = new StringBuilder( desc() );

		info.append(prefixInfo());

		if (levelKnown) {
			info.append( p );
			info.append( Messages.get( MeleeWeapon.class, "stats_known", tier, min(), max(), STR() ) );
		} else {
			info.append( p );
			info.append( Messages.get( MeleeWeapon.class, "stats_unknown", tier, min0(), max0(), typicalSTR() ) );
			if (viewer != null && typicalSTR() > viewer.STR()) {
				info.append( " " );
				info.append( Messages.get( MeleeWeapon.class, "probably_too_heavy" ) );
			}
		}

		String handling = null;
		if (DLY < 1f) {
			handling = ACU > 1f ? "weapon_fast_and_accurate"
					: ACU < 1f ? "weapon_fast_but_inaccurate" : "weapon_fast";
		} else if (DLY > 1f) {
			handling = ACU > 1f ? "weapon_slow_but_accurate"
					: ACU < 1f ? "weapon_slow_and_inaccurate" : "weapon_slow";
		} else if (ACU != 1f) {
			handling = ACU > 1f ? "weapon_accurate" : "weapon_inaccurate";
		}
		if (handling != null) {
			info.append( " " ).append( Messages.get( "network.partytext." + handling ) );
		}
		switch (imbue) {
		case SPEED:
			info.append( " " ).append( Messages.get( "custom.generated.entry_it_was_balanced_to_make_it_faster_b36ba83810" ) );
			break;
		case ACCURACY:
			info.append( " " ).append( Messages.get( "custom.generated.entry_it_was_balanced_to_make_it_more_accurate_b328f44d91" ) );
			break;
		case NONE:
		}

		if (enchantment != null && !isIdentified()) {
			info.append( " " ).append( Messages.get( "custom.generated.entry_it_is_enchanted_6868574903" ) );
		}

		if (levelKnown && viewer != null && viewer.belongings.backpack.items.contains( this )) {
			if (STR() > viewer.STR()) {
				info.append( p );
				info.append( Messages.get( Weapon.class, "too_heavy" ) );
			}
			if (STR() < viewer.STR()) {
				info.append( p );
				info.append( Messages.get( Weapon.class, "excess_str", viewer.STR() - STR() ) );
			}
		}

		if (viewer != null && isEquipped( viewer )) {
			if (viewer.skillTree != null && viewer.skillTree.getWeaponLevelBonus() > 0) {
				info.append( p );
                info.append( "Your mastery of melee weapons makes it easier to use this weapon (+ " + viewer.skillTree.getWeaponLevelBonus() + " levels)\n" );
			}
			if (cursed) {
				info.append( p );
				info.append( Messages.get( Weapon.class, "cursed_worn" ) );
			}
		} else if (cursedKnown && cursed) {
			info.append( p );
			info.append( Messages.get( Weapon.class, "cursed" ) );
		}

		return info.toString();
	}

	@Override
	public int price() {
		int price = 20 * (1 << (tier - 1));
		if (enchantment != null) {
			price *= 1.5;
		}
		return considerState( price );
	}

	@Override
	public Item random() {
		super.random();

		if (Random.Int( 10 + level() ) == 0) {
			enchant();
		}

		return this;
	}

	public String highlight(String input){
		StringBuilder sb = new StringBuilder();
		String[] data = input.split(" ");
		for(int i = 0; i < data.length; i++){
			sb.append("_" + data[i] + "_ ");
		}

		return sb.toString();
	}
}
