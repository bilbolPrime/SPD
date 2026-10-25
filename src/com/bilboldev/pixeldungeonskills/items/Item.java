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

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.SnipersMark;
import com.bilboldev.pixeldungeonskills.actors.hero.GauntletHero;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.effects.Degradation;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.WeaponEffect;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.MissileWeapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.scenes.CellSelector;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundlable;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.PointF;
import com.bilboldev.utils.Random;

public class Item implements Bundlable {

	private static final String TXT_PACK_FULL	= "custom.generated.entry_your_pack_is_too_full_for_the_s_0e5c478482";

	private static final String TXT_BROKEN		= "custom.generated.entry_because_of_frequent_use_your_s_has_broken_dca7fdc7de";
	private static final String TXT_GONNA_BREAK	= "custom.generated.entry_because_of_frequent_use_your_s_is_going_to_break_98b49c7f42";

	private static final String TXT_TO_STRING		= "%s";
	private static final String TXT_TO_STRING_X		= "%s x%d";
	private static final String TXT_TO_STRING_LVL	= "%s%+d";
	private static final String TXT_TO_STRING_LVL_X	= "%s%+d x%d";

	private static final float DURABILITY_WARNING_LEVEL	= 1/6f;

	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 0.5f;

	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";
    public static final String AC_STORE		= "STORE";
    public static final String AC_STORE_TAKE		= "STORETAKE";

	public String defaultAction;

	protected String name = "smth";
	protected int image = 0;

	public boolean stackable = false;
	protected int quantity = 1;

    public boolean noDegrade = PixelDungeon.itemDeg();

	public int level = 0;
	private int durability = maxDurability();
	public boolean levelKnown = false;

	public boolean cursed;
	public boolean cursedKnown;

	public boolean unique = false;

    public WeaponEffect weaponEffect = null;

	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};

	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<String>();
		actions.add( AC_DROP );
		actions.add( AC_THROW );
        if(hero.pos == Dungeon.level.storage && this != hero.belongings.weapon && this != hero.belongings.armor && this != hero.belongings.ring1 && this != hero.belongings.ring2  && this != hero.belongings.bow)
            actions.add( AC_STORE );
		return actions;
	}

	public boolean doPickUp( Hero hero ) {
		if(hero instanceof GauntletHero){
			return guantletPickup(hero);
		}

		if (collect( hero.belongings.backpack )) {

			GameScene.pickUp( this );
			Sample.INSTANCE.play( Assets.SND_ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			return true;

		} else {
			return false;
		}
	}

	private boolean guantletPickup(Hero hero){
		if (collect( hero.belongings.backpack )) {

			GauntletScene.pickUp( this );
			Sample.INSTANCE.play( Assets.SND_ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			return true;

		} else {
			return false;
		}
	}

	public void doDrop( Hero hero ) {
		hero.spendAndNext( TIME_TO_DROP );
		Dungeon.level.drop( detachAll( hero.belongings.backpack ), hero.pos ).sprite.drop( hero.pos );
	}

	public void doThrow( Hero hero ) {
		GameScene.selectCell( thrower );
	}

	public void execute( Hero hero, String action ) {

		curUser = hero;
		curItem = this;

		if (action.equals( AC_DROP )) {

			doDrop( hero );

		} else if (action.equals( AC_THROW )) {

			doThrow( hero );

		}
        else if (action.equals( AC_STORE )) {

            doAddStorage( hero );

        }
        else if (action.equals( AC_STORE_TAKE )) {

            doTakeStorage( hero );

        }
	}

    public void doTakeStorage( Hero hero ) {
        hero.spendAndNext( TIME_TO_DROP );
        Dungeon.level.drop( detachAll( hero.storage.backpack ), hero.pos ).sprite.drop( hero.pos );
    }

    public void doAddStorage( Hero hero ) {
        if(collect( hero.storage.backpack))
        {
            hero.spendAndNext( TIME_TO_DROP );
            detachAll( hero.belongings.backpack );
        }
    }

	public void execute( Hero hero ) {
		execute( hero, defaultAction );
	}

	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
	}

    public void onThrowColdGirl( int cell ) {
        Heap heap = Dungeon.level.drop( this, cell );
        if (!heap.isEmpty()) {
            heap.sprite.drop( cell );
        }
        Dungeon.hero.belongings.weapon = null;
    }

	public boolean collect( Bag container ) {

		ArrayList<Item> items = container.items;

		if (items.contains( this )) {
			return true;
		}

		for (Item item:items) {
			if (item instanceof Bag && ((Bag)item).grab( this )) {
				return collect( (Bag)item );
			}
		}

		if (stackable) {

			Class<?>c = getClass();
			for (Item item:items) {
				if (item.getClass() == c) {
					item.quantity += quantity;
					item.updateQuickslot();
					return true;
				}
			}
		}

		if (items.size() < container.size) {

			if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
				Badges.validateItemLevelAquired( this );
			}

			items.add( this );
			QuickSlot.refresh();
			Collections.sort( items, itemComparator );
			return true;

		} else {

			GLog.n(TXT_PACK_FULL, Message.itemName(this));
			return false;

		}
	}

	public boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}

	public final Item detach( Bag container ) {

		if (quantity <= 0) {

			return null;

		} else
		if (quantity == 1) {

			return detachAll( container );

		} else {

			quantity--;
			updateQuickslot();

			try {
				Item detached = getClass().newInstance();
				detached.onDetach( );
				return detached;
			} catch (Exception e) {
				return null;
			}
		}
	}

	public final Item detachAll( Bag container ) {

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove( this );
				item.onDetach( );
				QuickSlot.refresh();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}

		return this;
	}

	protected void onDetach( ) {
	}

	public int level()
    {

		if(Dungeon.hero != null && Dungeon.hero.skillTree != null && this instanceof MeleeWeapon && Dungeon.hero.belongings.weapon == this)
			return level +  Dungeon.hero.skillTree.getWeaponLevelBonus();

		return level;
	}

	public void level( int value ) {
		level = value;
	}

	public int effectiveLevel() {
		return isBroken() ? 0 : level();
	}

	public Item upgrade() {

		cursed = false;
		cursedKnown = true;

		level++;
		fix();

		return this;
	}

	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}

		return this;
	}

	public Item degrade() {

		this.level--;
		fix();

		return this;
	}

	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}

		return this;
	}

	public void use() {

        if(noDegrade == true)
            return;

		if (level > 0 && !isBroken()) {
			int threshold = (int)(maxDurability() * DURABILITY_WARNING_LEVEL);
			if (durability-- >= threshold && threshold > durability && levelKnown) {
				GLog.w(TXT_GONNA_BREAK, Message.itemName(this));
			}
			if (isBroken()) {
				getBroken();
				if (levelKnown) {
					GLog.n(TXT_BROKEN, Message.itemName(this));
					Dungeon.hero.interrupt();

					CharSprite sprite = Dungeon.hero.sprite;
					PointF point = sprite.center().offset( 0, -16 );
					if (this instanceof Weapon) {
						sprite.parent.add( Degradation.weapon( point ) );
					} else if (this instanceof Armor) {
						sprite.parent.add( Degradation.armor( point ) );
					} else if (this instanceof Ring) {
						sprite.parent.add( Degradation.ring( point ) );
					} else if (this instanceof Wand) {
						sprite.parent.add( Degradation.wand( point ) );
					}
					Sample.INSTANCE.play( Assets.SND_DEGRADE );
				}
			}
		}
	}

	public boolean isBroken() {
		return durability <= 0;
	}

	public void getBroken() {
	}

	public void fix() {
		durability = maxDurability();
	}

	public void polish() {
		if (durability < maxDurability()) {
			durability++;
		}
	}

	public int durability() {
		return durability;
	}

	public int maxDurability( int lvl ) {
		return 1;
	}

	final public int maxDurability() {
		return maxDurability( level );
	}

	public int visiblyUpgraded() {
		return levelKnown ? level() : 0;
	}

	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}

	public boolean visiblyBroken() {
		return levelKnown && isBroken();
	}

	public boolean isUpgradable() {
		return true;
	}

	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}

	public boolean isEquipped( Hero hero ) {
		return false;
	}

	public Item identify() {

		levelKnown = true;
		cursedKnown = true;

		return this;
	}

	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}

	@Override
	public String toString() {

		if (levelKnown && level != 0) {
			if (quantity > 1) {
				return Utils.format( TXT_TO_STRING_LVL_X, name(), level, quantity );
			} else {
				return Utils.format( TXT_TO_STRING_LVL, name(), level() );
			}
		} else {
			if (quantity > 1) {
				return Utils.format( TXT_TO_STRING_X, name(), quantity );
			} else {
				return Utils.format( TXT_TO_STRING, name() );
			}
		}
	}

	public String actionName( String action, Hero hero ) {
		if (action == null) {
			return "";
		}

		String localized = message( "ac_" + action.toLowerCase( Locale.ENGLISH ) );
		return localized != null ? localized : action;
	}

	public String name() {
		return prefixName(trueName());
	}

	public String prefixName(String baseName) {
		return weaponEffect == null ? baseName : weaponEffect.name(baseName);
	}

	public String prefixInfo() {
		return weaponEffect == null ? "" : "\n\n" + weaponEffect.description();
	}

	public String coreName() {
		return trueName();
	}

	public final String trueName() {
		String localized = message( "name" );
		return localized != null ? localized : name;
	}

	public int image() {
		return image;
	}

	public ItemSprite.Glowing glowing() {
		return null;
	}

	public String info() {
		return desc();
	}

	public String displayInfo() {
		String info = info();
		String translatedDesc = message( "desc" );
		if (translatedDesc == null) {
			return info;
		}

		String currentDesc = desc();
		if (currentDesc != null && info != null && info.startsWith( currentDesc )) {
			return translatedDesc + info.substring( currentDesc.length() );
		}

		return info == null || info.length() == 0 ? translatedDesc : info;
	}

	public String desc() {
		String localized = message( "desc" );
		return localized != null ? localized : "";
	}

	public int quantity() {
		return quantity;
	}

	public void quantity( int value ) {
		quantity = value;
	}

	public int price() {
		return 0;
	}

	protected String message( String key, Object... args ) {
		String value = Messages.get( this, key, args );
		return Messages.NO_TEXT_FOUND.equals( value ) ? null : value;
	}

	public int considerState( int price ) {
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level > 0) {
				price *= (level + 1);
				if (isBroken()) {
					price /= 2;
				}
			} else if (level < 0) {
				price /= (1 - level);
			}
		}
		if (price < 1) {
			price = 1;
		}

		return price;
	}

	public static Item virtual( Class<? extends Item> cl ) {
		try {

			Item item = (Item)cl.newInstance();
			item.quantity = 0;
			return item;

		} catch (Exception e) {
			return null;
		}
	}

	public Item random() {
		return this;
	}

	public void randomPrefix() {
		if (weaponEffect == null && WeaponEffect.supports(this) && Random.Int(100) < 50) {
			weaponEffect = WeaponEffect.random(this);
		}
	}

	public String status() {
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}

	public void updateQuickslot() {
		Object content = stackable ? getClass() : this;
		for (Object value : QuickSlot.values) {
			if (value == content) {
				QuickSlot.refresh();
				break;
			}
		}
	}

	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String DURABILITY		= "durability";
    private static final String EFFECT		= "effect";

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( QUANTITY, quantity );
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		bundle.put( EFFECT, weaponEffect == null ? "" : weaponEffect.Name );
		if (isUpgradable()) {
			bundle.put( DURABILITY, durability );
		}
		QuickSlot.save( bundle, this );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		quantity	= bundle.getInt( QUANTITY );
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );

		if (bundle.contains(EFFECT)) {
			weaponEffect = WeaponEffect.restore(bundle.getString(EFFECT));
		}

		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}

		cursed	= bundle.getBoolean( CURSED );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );

		if (isUpgradable()) {
			durability = bundle.getInt( DURABILITY );
		}

		QuickSlot.restore( bundle, this );
	}

	public void cast( final Hero user, int dst ) {

		final int cell = Ballistica.cast( user.pos, dst, false, true );
		user.sprite.zap( cell );
		user.busy();

		Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );

		Char enemy = Actor.findChar( cell );
		QuickSlot.target( this, enemy );

		float delay = TIME_TO_THROW;
		if (this instanceof MissileWeapon) {
			delay *= ((MissileWeapon)this).speedFactor( user );
			if (enemy != null) {
				SnipersMark mark = user.buff( SnipersMark.class );
				if (mark != null) {
					if (mark.object == enemy.id()) {
						delay *= 0.5f;
					}
					user.remove( mark );
				}
			}
		}
		final float finalDelay = delay;
		final int dstFinal = dst;
		((MissileSprite)user.sprite.parent.recycle( MissileSprite.class )).
			reset( user.pos, cell, this, new Callback() {
				@Override
				public void call() {
					Item.this.detach( user.belongings.backpack ).onThrow( cell );
                    if(curUser instanceof Hero && curItem instanceof Arrow && Dungeon.hero.skillTree.doubleShot())
                    {
                        if(Dungeon.hero.heroSkills.passiveB3.passThroughTargets(false) > 0)
                        {
                            curItem.castSPD(curUser, dstFinal, Dungeon.hero.heroSkills.passiveB3.passThroughTargets(true));
                        }
                        else
                            curItem.cast( curUser, dstFinal );
                    }
                    else
					    user.spendAndNext( finalDelay );
				}
			} );
	}

    public void cast( final HiredMerc user, int dst ) {

        final int cell = Ballistica.cast( user.pos, dst, false, true );
        user.sprite.zap( cell );

        Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );

        Char enemy = Actor.findChar( cell );
        QuickSlot.target( this, enemy );

        float delay = TIME_TO_THROW;
        if (this instanceof MissileWeapon) {

            if (enemy != null) {
                SnipersMark mark = user.buff( SnipersMark.class );
                if (mark != null) {
                    if (mark.object == enemy.id()) {
                        delay *= 0.5f;
                    }
                    user.remove( mark );
                }
            }
        }
        final float finalDelay = delay;

        ((MissileSprite)user.sprite.parent.recycle( MissileSprite.class )).
                reset( user.pos, cell, this, new Callback() {
                    @Override
                    public void call() {

                    }
                } );
    }

    public void castSPD( final Hero user, int dst, int skip ) {

        final int cell = Ballistica.cast( user.pos, dst, skip);
        user.sprite.zap( cell );
        user.busy();

        Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );

        Char enemy = Actor.findChar( cell );
        QuickSlot.target( this, enemy );

        float delay = TIME_TO_THROW;
        if (this instanceof MissileWeapon) {
            delay *= ((MissileWeapon)this).speedFactor( user );
            if (enemy != null) {
                SnipersMark mark = user.buff( SnipersMark.class );
                if (mark != null) {
                    if (mark.object == enemy.id()) {
                        delay *= 0.5f;
                    }
                    user.remove( mark );
                }
            }
        }
        final float finalDelay = delay;
        final int dstFinal = dst;
        ((MissileSprite)user.sprite.parent.recycle( MissileSprite.class )).
                reset( user.pos, cell, this, new Callback() {
                    @Override
                    public void call() {
                        Item.this.detach( user.belongings.backpack ).onThrow( cell );
                        if(curUser instanceof Hero && curItem instanceof Arrow && Dungeon.hero.skillTree.doubleShot())
                        {
                            if(Dungeon.hero.heroSkills.passiveB3.passThroughTargets(false) > 0)
                            {
                                curItem.castSPD(curUser, dstFinal, Dungeon.hero.heroSkills.passiveB3.passThroughTargets(true));
                            }
                            else
                                curItem.cast( curUser, dstFinal );
                        }
                        else
                            user.spendAndNext( finalDelay );
                    }
                } );
    }

	protected static Hero curUser = null;
	protected static Item curItem = null;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				if (curItem instanceof MissileWeapon && LocalCoop.contains(Actor.findChar(target))) return;
				curItem.cast( curUser, target );
			}
		}
		@Override
		public Message prompt() {
			return Message.key(Item.class, "prompt");
		}
	};
}
