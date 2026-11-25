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

import java.util.ArrayList;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;

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
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.noosa.Group;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.bags.Keyring;
import com.bilboldev.pixeldungeonskills.items.bags.PotionHolder;
import com.bilboldev.pixeldungeonskills.items.bags.ScrollHolder;
import com.bilboldev.pixeldungeonskills.items.bags.SeedPouch;
import com.bilboldev.pixeldungeonskills.items.bags.WandHolster;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
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
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
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
	private ControlRow itemGrid;

	protected int count;
	protected int col;
	protected int row;

	private static Mode lastMode;
	private static Bag lastBag;

    public boolean noDegrade = PixelDungeon.itemDeg();
    private final Hero owner;
    private final boolean readOnly;
    private final Bag bag;

    public WndBag(Bag bag, Listener listener, Mode mode, Message title) {
        this(Dungeon.hero, bag, listener, mode, title, false);
    }

    public WndBag(Hero hero, Bag bag) {
        this(hero, bag, null, Mode.ALL, null, true);
    }

    private WndBag(Hero owner, Bag bag, Listener listener, Mode mode, Message title, boolean readOnly) {

		super();

		this.listener = listener;
        this.owner = owner;
        this.readOnly = readOnly;
        this.bag = bag;
		this.mode = mode;
		this.title = title;
        menuModel = new MenuModel(title != null ? title : Message.capitalize(Message.itemName(bag)), Message.empty());
        menuModel.view = MenuModel.View.BAG;
        menuModel.item = bag;
        menuModel.mode = mode.ordinal();
        menuModel.selection = listener != null;
        menuModel.prompt = title;

		if (!readOnly) {
            lastMode = mode;
            lastBag = bag;
        }

		nCols = Game.width >= Game.height ? COLS_L : COLS_P;
		nRows = (Belongings.BACKPACK_SIZE + 4 + 1) / nCols + ((Belongings.BACKPACK_SIZE + 4 + 1) % nCols > 0 ? 1 : 0);
		int availableWidth = boundedWidth(1000);
		int availableHeight = maxFrameHeight() - frameHeight();
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

		if (nCols == COLS_P) itemGrid = new ControlRow();
		placeItems( bag );

		if (itemGrid != null) {
			int rows = row + (col > 0 ? 1 : 0);
			itemGrid.setSize(slotsWidth, rows * (slotSize + SLOT_MARGIN) - SLOT_MARGIN);
			ScrollPane pane = itemGrid.scrollPane(itemGrid);
			add(pane);
			resize(slotsWidth, slotsHeight + TITLE_HEIGHT);
			pane.setRect(0, TITLE_HEIGHT, slotsWidth, height - TITLE_HEIGHT);
		} else {
			resize(slotsWidth, slotsHeight + TITLE_HEIGHT);
		}

		ArrayList<Bag> bags = carriedBags(owner);
		int tabWidth = Math.min(TAB_WIDTH, (slotsWidth + chrome.marginHor() - 2) / bags.size());
		int firstTab = menuModel.choices.size();

		for (Bag b : bags) {
			if (b != null) {
				BagTab tab = new BagTab( b );
				tab.setSize( tabWidth, tabHeight() );
				add( tab );
                menuModel.add(tab, Message.itemName(b));

				tab.select( b == bag );
			}
		}

		int potion = bags.indexOf(owner.belongings.getItem(PotionHolder.class));
		int after = bags.indexOf(owner.belongings.getItem(ScrollHolder.class));
		if (after < 0) after = bags.indexOf(owner.belongings.getItem(SeedPouch.class));
		if (potion >= 0 && after > potion) {
			MenuModel.Choice choice = menuModel.choices.remove(firstTab + potion);
			menuModel.choices.add(firstTab + after, choice);
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

		Belongings stuff = owner.belongings;
		boolean backpack = (container == stuff.backpack);

		boolean equipment = backpack || itemGrid == null || PartySession.remoteContext();
		placeItem( stuff.weapon != null ? stuff.weapon : new Placeholder( ItemSpriteSheet.WEAPON ), equipment );
		placeItem( stuff.armor != null ? stuff.armor : new Placeholder( ItemSpriteSheet.ARMOR ), equipment );
		placeItem( stuff.ring1 != null ? stuff.ring1 : new Placeholder( ItemSpriteSheet.RING ), equipment );
		placeItem( stuff.ring2 != null ? stuff.ring2 : new Placeholder( ItemSpriteSheet.RING ), equipment );
        placeItem( stuff.bow != null ? stuff.bow : new Placeholder( ItemSpriteSheet.EMPTY_BOW ), equipment );

		int equipmentSlots = equipment ? ((5 + nCols - 1) / nCols) * nCols : 0;
		if (!backpack) {
			count = equipmentSlots;
			col = 0;
			row = equipmentSlots / nCols;
		}

		for (Item item : container.items) {
			if (backpack && Bag.isHolder(item)) {
                placeItem(item, false);
                continue;
            }
			placeItem( item );
		}

		while (count - (backpack ? 5 : equipmentSlots) < container.size) {
			placeItem( null );
		}

		if (container == owner.belongings.backpack) {
			row = itemGrid == null ? nRows - 1 : Math.max(nRows - 1, row);
			col = nCols - 1;
			placeItem( new Gold(readOnly ? 0 : Dungeon.gold) );
		}
	}

	protected void placeItem( final Item item ) {
		placeItem(item, true);
	}

	private void placeItem(Item item, boolean visible) {
		ItemButton button = new ItemButton(item);
		if (item != null && !(item instanceof Placeholder)) menuModel.add(button, Message.item(item));
		if (!visible) {

			button.visible = button.active = false;
			add(button);
			return;
		}

		int x = col * (slotSize + SLOT_MARGIN);
		int y = (itemGrid == null ? TITLE_HEIGHT : 0) + row * (slotSize + SLOT_MARGIN);
		if (itemGrid == null) add(button.setPos(x, y));
		else itemGrid.add(button.setPos(x, y));

		if (++col >= nCols) {
			col = 0;
			row++;
		}

		count++;
	}

	@Override
	public void onMenuPressed() {
		if (!readOnly && PartyWindows.close(this)) return;
		if (listener == null) {
			hide();
		}
	}

	@Override
	public void onBackPressed() {
		if (!readOnly && PartyWindows.close(this)) return;
		if (listener != null) {
			listener.onSelect( null );
		}
		super.onBackPressed();
	}

	@Override
	protected void onClick( Tab tab ) {
        if (!readOnly && PartyWindows.select(tab)) return;
        Group host = parent;
        hide();
        WndBag next = new WndBag(owner, ((BagTab)tab).bag, listener, mode, title, readOnly);
        if (readOnly) host.add(next);
        else GameScene.show(next);
	}

	@Override
	protected int tabHeight() {
		return 20;
	}

    public static ArrayList<Bag> carriedBags(Hero hero) {
        ArrayList<Bag> bags = new ArrayList<>();
        Belongings stuff = hero.belongings;
        for (Bag bag : new Bag[]{stuff.backpack, stuff.getItem(PotionHolder.class),
                stuff.getItem(SeedPouch.class), stuff.getItem(ScrollHolder.class),
                stuff.getItem(WandHolster.class), stuff.getItem(Keyring.class)})
            if (bag != null) bags.add(bag);
        for (Item item : stuff.backpack.items)
            if (Bag.isHolder(item) && !bags.contains(item)) bags.add((Bag)item);
        return bags;
    }

    public static Image bagIcon(Bag bag) {
        if (bag instanceof SeedPouch) return Icons.SEED_POUCH.get();
        if (bag instanceof ScrollHolder) return Icons.SCROLL_HOLDER.get();
        if (bag instanceof PotionHolder) return Icons.POTION_HOLDER.get();
        if (bag instanceof WandHolster) return Icons.WAND_HOLSTER.get();
        if (bag instanceof Keyring) return Icons.KEYRING.get();
        return Icons.BACKPACK.get();
    }

	private class BagTab extends Tab {

		private Image icon;

		private Bag bag;

		public BagTab( Bag bag ) {
			super();

			this.bag = bag;

			icon = bagIcon(bag);
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

			icon.copy( bagIcon(bag) );
			icon.x = x + (width - icon.width) / 2;
			icon.y = y + (height - icon.height) / 2 - 2 - (selected ? 0 : 1);
			if (!selected && icon.y < y + CUT) {
				RectF frame = icon.frame();
				frame.top += (y + CUT - icon.y) / icon.texture.height;
				icon.frame( frame );
				icon.y = y + CUT;
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
			if (itemGrid != null) itemGrid.addControl(hotArea);
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

        @Override protected Hero viewer() { return owner; }

		@Override
		public void item( Item item ) {
			if (durability != null) {
				for (ColorBlock bar : durability) {
					remove(bar);
					bar.destroy();
				}
				durability = null;
			}
            super.item(item);
            if (readOnly && item != null) {
                icon.view(ItemSpriteSheet.identifiedImage(item.getClass(), item.image()), item.glowing());
                bg.texture(TextureCache.createSolid(item.isEquipped(owner) ? EQUIPPED : NORMAL));
                if (item instanceof Wand) {
                    Wand wand = (Wand)item;
                    topLeft.text(wand.curCharges + "/" + wand.maxCharges);
                }
                enable(!(item instanceof Placeholder));
                return;
            }
			if (item != null) {

				bg.texture( TextureCache.createSolid( item.isEquipped(owner) ? EQUIPPED : NORMAL ) );
				if (item.cursed && item.cursedKnown) {
					bg.ra = +0.2f;
					bg.ga = -0.1f;
                } else if (!item.isIdentified()) {
					bg.ra = 0.1f;
					bg.ba = 0.1f;
				}

                if (!noDegrade && bag.owner != null && bag.owner.isAlive() && item.isUpgradable() && item.levelKnown
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
						mode == Mode.FOR_SALE && (item.price() > 0) && (!item.isEquipped(owner) || !item.cursed) ||
						mode == Mode.UPGRADEABLE && item.isUpgradable() ||
						mode == Mode.UNIDENTIFED && !item.isIdentified() ||
                        mode == Mode.WEAPON && (item instanceof MeleeWeapon || item instanceof Boomerang) ||
						(mode == Mode.ARMOR || mode == Mode.MERC_ARMOR) && (item instanceof Armor) ||
						mode == Mode.ENCHANTABLE && (item instanceof MeleeWeapon || item instanceof Boomerang || item instanceof Bow || item instanceof Armor || item instanceof IceWeapon) ||
						mode == Mode.WAND && (item instanceof Wand) ||
						mode == Mode.SEED && (item instanceof Seed) ||
						mode == Mode.ALL
					);

                    if(mode == Mode.BOW)
                        enable(item instanceof Bow && Dungeon.hero.belongings.bow != item);
                    if(mode == Mode.HEALING_POTION)
                        enable((item instanceof PotionOfHealing || item instanceof PotionOfRejuvenation) && ((Potion)item).isKnown());
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
            if (readOnly) {
                if (item instanceof Bag) {
                    Group host = WndBag.this.parent;
                    hide();
                    host.add(new WndBag(owner, (Bag)item));
                } else if (!(item instanceof Placeholder)) {
                    LibraryCatalog.Entry entry = LibraryCatalog.find(item);
                    String name = entry == null ? item.name() : entry.name();
                    if (item.level() != 0) name += String.format(java.util.Locale.ROOT, "%+d", item.level());
                    if (item.quantity() > 1) name += " x " + item.quantity();
                    WndBag.this.parent.add(new WndTitledMessage(new ItemSprite(
                            ItemSpriteSheet.identifiedImage(item.getClass(), item.image()), item.glowing()), name,
                            entry == null ? item.info(null) : entry.customizable() ? entry.info(item) : entry.description()));
                }
                return;
            }
			if (PartyWindows.select(this)) return;
			if (listener != null) {
				if (!listener.onBeforeSelect(item)) return;
				hide();
				listener.onSelect( item );

			} else {

				WndBag.this.add( new WndItem( WndBag.this, item ) );

			}
		}

		@Override
		protected boolean onLongClick() {
            if (readOnly) { onClick(); return true; }
			if (PartyWindows.select(this, true)) return true;
			if (listener == null && item.defaultAction != null) {
				hide();
				if (Dungeon.hero.talents() != null) {
					GameScene.show(new WndQuickSlot(item));
					return true;
				}
				QuickSlot.values[0] = item.stackable ? item.getClass() : item;
				QuickSlot.refresh();
				return true;
			} else {
				return false;
			}
		}
	}

	public interface Listener {
		default boolean onBeforeSelect(Item item) { return true; }
		void onSelect( Item item );
	}
}
