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
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.PixelDungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.hero.Belongings;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.bags.Keyring;
import com.bilboldev.pixeldungeonskills.items.bags.PotionHolder;
import com.bilboldev.pixeldungeonskills.items.bags.ScrollHolder;
import com.bilboldev.pixeldungeonskills.items.bags.SeedPouch;
import com.bilboldev.pixeldungeonskills.items.bags.WandHolster;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.plants.Plant.Seed;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.utils.GameMath;
import com.bilboldev.utils.RectF;

public class WndBag extends WndTabbed {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.IRON;
    }

	public static enum Mode {
		ALL,
		UNIDENTIFED,
		UPGRADEABLE,
		QUICKSLOT,
		FOR_SALE,
		WEAPON,
		ARMOR,
        MERC_ARMOR,
		ENCHANTABLE,
		WAND,
		SEED,
        BOW,
        HEALING_POTION,
        BRUTE_HOLD
	}

	protected static final int COLS_P	= 4;
	protected static final int COLS_L	= 6;

	protected static final int SLOT_SIZE	= 28;
	protected static final int SLOT_MARGIN	= 1;

	protected static final int TAB_WIDTH	= 25;

	protected static final int TITLE_HEIGHT	= 12;

	private Listener listener;
	private WndBag.Mode mode;
	private Message title;

	private int nCols;
	private int nRows;
	private int slotSize;

	protected int count;
	protected int col;
	protected int row;

	private static Mode lastMode;
	private static Bag lastBag;

    public boolean noDegrade = PixelDungeon.itemDeg();

	public WndBag( Bag bag, Listener listener, Mode mode, Message title ) {

		super();

		this.listener = listener;
		this.mode = mode;
		this.title = title;
        menuModel = new MenuModel(title != null ? title : Message.capitalize(Message.itemName(bag)), Message.empty());

		lastMode = mode;
		lastBag = bag;

		nCols = landscapeLayout() ? COLS_L : COLS_P;
		nRows = (Belongings.BACKPACK_SIZE + 4 + 1) / nCols + ((Belongings.BACKPACK_SIZE + 4 + 1) % nCols > 0 ? 1 : 0);
		int availableWidth = boundedWidth(1000);
		int availableHeight = (int)(Game.height / camera.zoom) - frameHeight() - 8;
		int columnWidth = (availableWidth - SLOT_MARGIN * (nCols - 1)) / nCols;
		int rowHeight = (availableHeight - TITLE_HEIGHT - SLOT_MARGIN * (nRows - 1)) / nRows;
		slotSize = Math.max(16, Math.min(Presentation.reborn() ? 32 : SLOT_SIZE,
				Math.min(columnWidth, rowHeight)));

		int slotsWidth = slotSize * nCols + SLOT_MARGIN * (nCols - 1);
		int slotsHeight = slotSize * nRows + SLOT_MARGIN * (nRows - 1);
		String titleText = (title != null ? title : Message.capitalize(Message.itemName(bag))).toString();

		BitmapText txtTitle = PixelScene.createText( titleText, 9 );
		txtTitle.hardlight( titleColor() );
		txtTitle.measure();
		txtTitle.x = (int)(slotsWidth - txtTitle.width()) / 2;
		txtTitle.y = (int)(TITLE_HEIGHT - txtTitle.height()) / 2;
		add( txtTitle );

		placeItems( bag );

		resize( slotsWidth, slotsHeight + TITLE_HEIGHT );

		Belongings stuff = Dungeon.hero.belongings;
		Bag[] bags = {
			stuff.backpack,
			stuff.getItem( SeedPouch.class ),
			stuff.getItem( ScrollHolder.class ),
			stuff.getItem(PotionHolder.class),
			stuff.getItem( WandHolster.class ),
			stuff.getItem( Keyring.class )};
		int tabWidth = TAB_WIDTH;
		if (Presentation.reborn()) {
			int bagCount = 0;
			for (Bag b : bags) if (b != null) bagCount++;
			tabWidth = Math.min(TAB_WIDTH, (slotsWidth + chrome.marginHor() - 2) / bagCount);
		}

		for (Bag b : bags) {
			if (b != null) {
				BagTab tab = new BagTab( b );
				tab.setSize( tabWidth, tabHeight() );
				add( tab );
                menuModel.add(tab, Message.itemName(b));

				tab.select( b == bag );
			}
		}
	}

	public static WndBag lastBag( Listener listener, Mode mode, Message title ) {

		if (mode == lastMode && lastBag != null &&
			Dungeon.hero.belongings.backpack.contains( lastBag )) {

			return new WndBag( lastBag, listener, mode, title );

		} else {

			return new WndBag( Dungeon.hero.belongings.backpack, listener, mode, title );

		}
	}

	public static WndBag seedPouch( Listener listener, Mode mode, Message title ) {
		SeedPouch pouch = Dungeon.hero.belongings.getItem( SeedPouch.class );
		return pouch != null ?
			new WndBag( pouch, listener, mode, title ) :
			new WndBag( Dungeon.hero.belongings.backpack, listener, mode, title );
	}

	protected void placeItems( Bag container ) {

		Belongings stuff = Dungeon.hero.belongings;
		placeItem( stuff.weapon != null ? stuff.weapon : new Placeholder( ItemSpriteSheet.WEAPON ) );
		placeItem( stuff.armor != null ? stuff.armor : new Placeholder( ItemSpriteSheet.ARMOR ) );
		placeItem( stuff.ring1 != null ? stuff.ring1 : new Placeholder( ItemSpriteSheet.RING ) );
		placeItem( stuff.ring2 != null ? stuff.ring2 : new Placeholder( ItemSpriteSheet.RING ) );
        placeItem( stuff.bow != null ? stuff.bow : new Placeholder( ItemSpriteSheet.EMPTY_BOW ) );

		boolean backpack = (container == Dungeon.hero.belongings.backpack);
		if (!backpack) {
			count = nCols;
			col = 0;
			row = 1;
		}

		for (Item item : container.items) {
			placeItem( item );
		}

		while (count-(backpack ? 5 : nCols) < container.size) {
			placeItem( null );
		}

		if (container == Dungeon.hero.belongings.backpack) {
			row = nRows - 1;
			col = nCols - 1;
			placeItem( new Gold( Dungeon.gold ) );
		}
	}

	protected void placeItem( final Item item ) {

		int x = col * (slotSize + SLOT_MARGIN);
		int y = TITLE_HEIGHT + row * (slotSize + SLOT_MARGIN);

		ItemButton button = new ItemButton(item);
        add(button.setPos(x, y));
        if (item != null && !(item instanceof Placeholder)) menuModel.add(button, Message.item(item));

		if (++col >= nCols) {
			col = 0;
			row++;
		}

		count++;
	}

	@Override
	public void onMenuPressed() {
		if (listener == null) {
			hide();
		}
	}

	@Override
	public void onBackPressed() {
		if (listener != null) {
			listener.onSelect( null );
		}
		super.onBackPressed();
	}

	@Override
	protected void onClick( Tab tab ) {
		hide();
		GameScene.show( new WndBag( ((BagTab)tab).bag, listener, mode, title ) );
	}

	@Override
	protected int tabHeight() {
		return 20;
	}

	private class BagTab extends Tab {

		private Image icon;

		private Bag bag;

		public BagTab( Bag bag ) {
			super();

			this.bag = bag;

			icon = icon();
			add( icon );
		}

		@Override
		protected void select( boolean value ) {
			super.select( value );
			icon.am = selected ? 1.0f : 0.6f;
		}

		@Override
		protected void layout() {
			super.layout();

			icon.copy( icon() );
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2 - 2 - (selected ? 0 : 1);
			if (!selected && icon.y < y + CUT) {
				RectF frame = icon.frame();
				frame.top += (y + CUT - icon.y) / icon.texture.height;
				icon.frame( frame );
				icon.y = y + CUT;
			}
		}

		private Image icon() {
			if (bag instanceof SeedPouch) {
				return Icons.get( Icons.SEED_POUCH );
			} else if (bag instanceof ScrollHolder) {
				return Icons.get( Icons.SCROLL_HOLDER );
			} else if (bag instanceof PotionHolder) {
				return Icons.get( Icons.POTION_HOLDER );
			}else if (bag instanceof WandHolster) {
				return Icons.get( Icons.WAND_HOLSTER );
			} else if (bag instanceof Keyring) {
				return Icons.get( Icons.KEYRING );
			} else {
				return Icons.get( Icons.BACKPACK );
			}
		}
	}

	private static class Placeholder extends Item {
		{
			name = null;
		}

		public Placeholder( int image ) {
			this.image = image;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

		@Override
		public boolean isEquipped( Hero hero ) {
			return true;
		}
	}

	private class ItemButton extends ItemSlot {

		private static final int NORMAL		= 0xFF4A4D44;
		private static final int EQUIPPED	= 0xFF63665B;

		private static final int NBARS	= 3;

		private Item item;
		private ColorBlock bg;

		private ColorBlock durability[];

		public ItemButton( Item item ) {

			super( item );

			this.item = item;
			if (item instanceof Gold) {
				bg.visible = false;
			}

			width = height = slotSize;
		}

		@Override
		protected void createChildren() {
			bg = com.bilboldev.pixeldungeonskills.ui.RebornSlot.create(slotSize, slotSize, NORMAL);
			add( bg );

			super.createChildren();
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			if (durability != null) {
				for (int i=0; i < NBARS; i++) {
					durability[i].x = x + 1 + i * 3;
					durability[i].y = y + height - 3;
				}
			}

			super.layout();
		}

		@Override
		public void item( Item item ) {
			if (durability != null) {
				for (ColorBlock bar : durability) {
					remove(bar);
					bar.destroy();
				}
				durability = null;
			}
			super.item( item );
			if (item != null) {

				bg.texture( TextureCache.createSolid( item.isEquipped( Dungeon.hero ) ? EQUIPPED : NORMAL ) );
				if (item.cursed && item.cursedKnown) {
					bg.ra = +0.2f;
					bg.ga = -0.1f;
				} else if (!item.isIdentified()) {
					bg.ra = 0.1f;
					bg.ba = 0.1f;
				}

				if (!noDegrade && lastBag.owner.isAlive() && item.isUpgradable() && item.levelKnown
						&& item.maxDurability() > 0) {
					durability = new ColorBlock[NBARS];
					int nBars = (int)GameMath.gate( 0, Math.round( (float)NBARS * item.durability() / item.maxDurability() ), NBARS );
					for (int i=0; i < nBars; i++) {
						durability[i] = new ColorBlock( 2, 2, 0xFF00EE00 );
						add( durability[i] );
					}
					for (int i=nBars; i < NBARS; i++) {
						durability[i] = new ColorBlock( 2, 2, 0xFFCC0000 );
						add( durability[i] );
					}
				}

				if (item.name() == null) {
					enable( false );
				} else {
					enable(
						mode == Mode.QUICKSLOT && (item.defaultAction != null) ||
						mode == Mode.FOR_SALE && (item.price() > 0) && (!item.isEquipped( Dungeon.hero ) || !item.cursed) ||
						mode == Mode.UPGRADEABLE && item.isUpgradable() ||
						mode == Mode.UNIDENTIFED && !item.isIdentified() ||
                        mode == Mode.WEAPON && (item instanceof MeleeWeapon || item instanceof Boomerang) ||
						(mode == Mode.ARMOR || mode == Mode.MERC_ARMOR) && (item instanceof Armor) ||
						mode == Mode.ENCHANTABLE && (item instanceof MeleeWeapon || item instanceof Boomerang || item instanceof Bow || item instanceof Armor) ||
						mode == Mode.WAND && (item instanceof Wand) ||
						mode == Mode.SEED && (item instanceof Seed) ||
						mode == Mode.ALL
					);

                    if(mode == Mode.BOW)
                        enable(item instanceof Bow && Dungeon.hero.belongings.bow != item);
                    if(mode == Mode.HEALING_POTION)
                        enable(item instanceof PotionOfHealing && ((Potion)item).isKnown());
                    if(mode == Mode.BRUTE_HOLD)
                        enable(!(item instanceof Gold)  && !(item instanceof Bag) && item != Dungeon.hero.belongings.weapon && item != Dungeon.hero.belongings.armor && item != Dungeon.hero.belongings.ring1 && item != Dungeon.hero.belongings.ring2 && item != Dungeon.hero.belongings.bow);
                    if(mode == Mode.MERC_ARMOR && item instanceof Armor && ((Armor)item).tier > 5)
                        enable(false);
				}
			} else {
				bg.color( NORMAL );
			}
			layout();
		}

		@Override
		protected void onTouchDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
		};

		protected void onTouchUp() {
			bg.brightness( 1.0f );
		};

		@Override
		protected void onClick() {
			if (listener != null) {

				hide();
				listener.onSelect( item );

			} else {

				WndBag.this.add( new WndItem( WndBag.this, item ) );

			}
		}

		@Override
		protected boolean onLongClick() {
			if (listener == null && item.defaultAction != null) {
				hide();
				QuickSlot.values[0] = item.stackable ? item.getClass() : item;
				QuickSlot.refresh();
				return true;
			} else {
				return false;
			}
		}
	}

	public interface Listener {
		void onSelect( Item item );
	}
}
