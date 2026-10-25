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
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.ArmorEvasion;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.EquipableItem;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.glyphs.*;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundlable;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class Armor extends EquipableItem {

	private static final int HITS_TO_KNOW	= 10;

	private static final String TXT_EQUIP_CURSED	= "your %s constricts around you painfully";

	private static final String TXT_IDENTIFY	= "you are now familiar enough with your %s to identify it. It is %s.";

	private static final String TXT_TO_STRING	= "%s :%d";
	private static final String TXT_BROKEN		= "broken %s :%d";

	private static final String TXT_INCOMPATIBLE =
		"Interaction of different types of magic has erased the glyph on this armor!";

	public int tier;

    public static int previewTier(Bundle armor) {
        if (armor == null || armor.isNull()) return 0;
        String name = armor.getString("__className");
        name = name.substring(name.lastIndexOf('.') + 1);
        switch (name) {
            case "ClothArmor": return 1;
            case "LeatherArmor": return 2;
            case "MailArmor": return 3;
            case "ScaleArmor": return 4;
            case "PlateArmor": return 5;
            case "WarriorArmor": case "MageArmor": case "RogueArmor":
            case "HuntressArmor": case "MercenaryArmor": case "NecromancerArmor": return 6;
            default: return 0;
        }
    }
	public int STR;

	private int hitsToKnow = HITS_TO_KNOW;

	public Glyph glyph;
	public ArmorPrefix prefix;

	public Armor( int tier ) {

		this.tier = tier;

		STR = typicalSTR();
	}

	private static final String UNFAMILIRIARITY	= "unfamiliarity";
	private static final String GLYPH			= "glyph";
	private static final String PREFIX = "armor_prefix";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( UNFAMILIRIARITY, hitsToKnow );
		bundle.put( GLYPH, glyph );
		bundle.put( PREFIX, prefix == null ? "" : prefix.id );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if ((hitsToKnow = bundle.getInt( UNFAMILIRIARITY )) == 0) {
			hitsToKnow = HITS_TO_KNOW;
		}
		inscribe( (Glyph)bundle.get( GLYPH ) );
		prefix = ArmorPrefix.restore(bundle.getString(PREFIX));
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( isEquipped( hero ) ? AC_UNEQUIP : AC_EQUIP );
		return actions;
	}

	@Override
	public boolean doEquip( Hero hero ) {

		detach( hero.belongings.backpack );

		if (hero.belongings.armor == null || hero.belongings.armor.doUnequip( hero, true, false )) {

			hero.belongings.armor = this;

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n(Armor.class, "equip_cursed");
			}

			((HeroSprite)hero.sprite).updateArmor();

			hero.spendAndNext( time2equip( hero ) );
			return true;

		} else {

			collect( hero.belongings.backpack );
			return false;

		}
	}

	@Override
	protected float time2equip( Hero hero ) {
		return 2 / hero.speed();
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			hero.belongings.armor = null;
			Buff.detach(hero, ArmorEvasion.class);
			((HeroSprite)hero.sprite).updateArmor();

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.armor == this;
	}

	public int DR() {
		return tier * (2 + effectiveLevel() + (glyph == null ? 0 : 1));
	}

	@Override
	public Item upgrade() {
		return upgrade( false );
	}

	public Item upgrade( boolean inscribe ) {

		if (glyph != null) {
			if (!inscribe && Random.Int( level() ) > 0) {
				GLog.w(Armor.class, "incompatible");
				inscribe( null );
			}
		} else {
			if (inscribe) {
				inscribe();
			}
		};

		STR--;

		return super.upgrade();
	}

	public Item safeUpgrade() {
		return upgrade( glyph != null );
	}

	@Override
	public Item degrade() {
		STR++;
		return super.degrade();
	}

	@Override
	public int maxDurability( int lvl ) {
		return 6 * (lvl < 16 ? 16 - lvl : 1);
	}

	public int proc( Char attacker, Char defender, int damage ) {

		if (glyph != null) {
			damage = glyph.proc( this, attacker, defender, damage );
		}

		if (!levelKnown) {
			if (--hitsToKnow <= 0) {
				levelKnown = true;
				GLog.w(Armor.class, "identify");
				Badges.validateItemLevelAquired( this );
			}
		}

		use();

		return damage;
	}

	@Override
	public String toString() {
		return levelKnown ? Utils.format( isBroken() ? TXT_BROKEN : TXT_TO_STRING, super.toString(), STR ) : super.toString();
	}

	@Override
	public String name() {
		return glyph == null ? super.name() : glyph.name( super.name() );
	}

	@Override
	public String prefixName(String baseName) {
		return prefix == null ? super.prefixName(baseName) : prefix.name(baseName);
	}

	@Override
	public String prefixInfo() {
		return prefix == null ? super.prefixInfo() : "\n\n" + prefix.description();
	}

	@Override
	public String info() {
		StringBuilder info = new StringBuilder( desc() );
		info.append(prefixInfo());

		if (levelKnown) {
			info.append( "\n\n" );
			info.append( Messages.get( Armor.class, "curr_absorb", tier, 0, Math.max( DR(), 0 ), STR ) );

			if (STR > Dungeon.hero.STR()) {
				info.append( "\n\n" );
				info.append( Messages.get( Armor.class, "too_heavy" ) );
			}
		} else {
			info.append( "\n\n" );
			info.append( Messages.get( Armor.class, "avg_absorb", tier, 0, typicalDR(), typicalSTR() ) );
			if (typicalSTR() > Dungeon.hero.STR()) {
				info.append( " " );
				info.append( Messages.get( Armor.class, "probably_too_heavy" ) );
			}
		}

		if (isEquipped( Dungeon.hero )) {
			if (cursed) {
				info.append( "\n\n" );
				info.append( Messages.get( Armor.class, "cursed_worn" ) );
			}
		} else if (cursedKnown && cursed) {
			info.append( "\n\n" );
			info.append( Messages.get( Armor.class, "cursed" ) );
		}

		return info.toString();
	}

	@Override
	public Item random() {
		randomPrefix();
		if (Random.Float() < 0.4) {
			int n = 1;
			if (Random.Int( 3 ) == 0) {
				n++;
				if (Random.Int( 3 ) == 0) {
					n++;
				}
			}
			if (Random.Int( 2 ) == 0) {
				upgrade( n );
			} else {
				degrade( n );
				cursed = true;
			}
		}

		if (Random.Int( 10 ) == 0) {
			inscribe();
		}

		return this;
	}

	public int typicalSTR() {
		return 7 + tier * 2;
	}

	@Override
	public void randomPrefix() {
		if (prefix == null && Random.Int(100) < 50) {
			ArmorPrefix[] prefixes = ArmorPrefix.values();
			prefix = prefixes[Random.Int(prefixes.length)];
		}
	}

	public int typicalDR() {
		return tier * 2;
	}

	@Override
	public int price() {
		int price = 10 * (1 << (tier - 1));
		if (glyph != null) {
			price *= 1.5;
		}
		return considerState( price );
	}

	public Armor inscribe( Glyph glyph ) {
		this.glyph = glyph;
		return this;
	}

	public Armor inscribe() {

		Class<? extends Glyph> oldGlyphClass = glyph != null ? glyph.getClass() : null;
		Glyph gl = Glyph.random();
		while (gl.getClass() == oldGlyphClass) {
			gl = Armor.Glyph.random();
		}

		return inscribe( gl );
	}

	public boolean multiplicity(){
		return glyph != null && glyph.getClass() == Multiplicity.class;
	}

	public boolean isInscribed() {
		return glyph != null;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return glyph != null ? glyph.glowing() : null;
	}

	public static abstract class Glyph implements Bundlable {

		private static final Class<?>[] glyphs = new Class<?>[]{
			Bounce.class, Affection.class, AntiEntropy.class, Multiplicity.class,
			Potential.class, Metabolism.class, Stench.class, Viscosity.class,
			Displacement.class, Entanglement.class, AutoRepair.class };

		private static final float[] chances= new float[]{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		public abstract int proc( Armor armor, Char attacker, Char defender, int damage );

		public String name( String armorName ) {
			return armorName;
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}

		public ItemSprite.Glowing glowing() {
			return ItemSprite.Glowing.WHITE;
		}

		public boolean checkOwner( Char owner ) {
			if (!owner.isAlive() && owner instanceof Hero) {

				((Hero)owner).killerGlyph = this;
				Badges.validateDeathFromGlyph();
				return true;

			} else {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph random() {
			try {
				return ((Class<Glyph>)glyphs[ Random.chances( chances ) ]).newInstance();
			} catch (Exception e) {
				return null;
			}
		}

	}
}
