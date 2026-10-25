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
package com.bilboldev.pixeldungeonskills.items.armor;

import java.util.ArrayList;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;

abstract public class ClassArmor extends Armor {

	private static final String TXT_LOW_HEALTH		= "custom.generated.entry_your_health_is_too_low_03ed8f157d";
	private static final String TXT_NOT_EQUIPPED	= "items.armor.classarmor.not_equipped";

	private int DR;

	{
		levelKnown = true;
		cursedKnown = true;
		defaultAction = special();
	}

	public ClassArmor() {
		super( 6 );
	}

	public static ClassArmor upgrade ( Hero owner, Armor armor ) {

		ClassArmor classArmor = null;

		switch (owner.heroClass) {
		case WARRIOR:
			classArmor = new WarriorArmor();
			break;
		case MERCENARY:
			classArmor = new MercenaryArmor();
			break;
		case ROGUE:
			classArmor = new RogueArmor();
			break;
		case MAGE:
			classArmor = new MageArmor();
			break;
		case NECROMANCER:
			classArmor = new NecromancerArmor();
			break;
		case HUNTRESS:
			classArmor = new HuntressArmor();
			break;
		}

		classArmor.STR = armor.STR;
		classArmor.DR = armor.DR();

		classArmor.inscribe( armor.glyph );
		classArmor.prefix = armor.prefix;

		return classArmor;
	}

	private static final String ARMOR_STR	= "STR";
	private static final String ARMOR_DR	= "DR";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARMOR_STR, STR );
		bundle.put( ARMOR_DR, DR );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		STR = bundle.getInt( ARMOR_STR );
		DR = bundle.getInt( ARMOR_DR );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.HP >= 3 && isEquipped( hero )) {
			actions.add( special() );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action == special()) {

			if (hero.HP < 3) {
				GLog.w(TXT_LOW_HEALTH);
			} else if (!isEquipped( hero )) {
				GLog.w(TXT_NOT_EQUIPPED);
			} else {
				curUser = hero;
				doSpecial();
			}

		} else {
			super.execute( hero, action );
		}
	}

	abstract public String special();
	abstract public void doSpecial();

	@Override
	public int DR() {
		return DR;
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
	public int price() {
		return 0;
	}

	@Override
	public String desc() {
		return "The thing looks awesome!";
	}
}
