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
package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.input.GameAction;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Belongings;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.utils.Bundle;
import java.util.Arrays;

public class QuickSlot extends Button implements WndBag.Listener {

	private static final String TXT_SELECT_ITEM = "Select an item for the quickslot";

	public static final int MAX_SLOTS = 10;
	public static Object[] values = new Object[MAX_SLOTS];
	private static final QuickSlot[] slots = new QuickSlot[MAX_SLOTS];
	private int index;

	private Item itemInSlot;
	private ItemSlot slot;

	private Image crossB;
	private Image crossM;

	private boolean targeting = false;

	private static Char lastTarget= null;

	public static void resetSlots() {
		Arrays.fill(values, null);
		lastTarget = null;
	}

	public static Char lastTarget() {
		return lastTarget;
	}

	public static void lastTarget(Char target) {
		lastTarget = target;
	}

	public void primary() {
		index(0);
	}

	public void secondary() {
		index(1);
	}

	public void index(int value) {
		index = value;
		slots[index] = this;
		item( select() );
		bindAction(GameAction.valueOf("QUICKSLOT_" + (index + 1)));
	}

	@Override
	public boolean triggerAction(boolean alternate) {
		if (!acceptsAction()) return false;

		if (!alternate && slot.acceptsAction()) return slot.triggerAction(false);
		return super.triggerAction(alternate);
	}

	@Override
	public void destroy() {
		super.destroy();

		if (slots[index] == this) slots[index] = null;
		crossM.remove();

		lastTarget = null;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		slot = new ItemSlot() {
			@Override
			protected void onClick() {
				if (PartySession.input(Dungeon.hero, "quick", index)) return;
				if (targeting) {
					GameScene.handleCell( lastTarget.pos );
				} else {
					if (LocalCoop.active()) {
						final Item item = select();
						final Hero user = Dungeon.hero;
						final int slotIndex = index;
						if (LocalCoop.defer(user, () -> {
							if (user.isAlive() && (user.belongings.backpack.contains(item) || item.isEquipped(user))) {
								QuickSlot current = slots[slotIndex];
								if (current != null) current.useTargeting();
								item.execute(user);
							}
						})) return;
					}
					useTargeting();
					select().execute(Dungeon.hero);
				}
			}
			@Override
			protected boolean onLongClick() {
				return QuickSlot.this.onLongClick();
			}
			@Override
			protected void onTouchDown() {
				icon.lightness( 0.7f );
			}
			@Override
			protected void onTouchUp() {
				icon.resetColor();
			}
		};
		add( slot );

		crossB = Icons.TARGET.get();
		crossB.visible = false;
		add( crossB );

		crossM = new Image();
		crossM.copy( crossB );
	}

	@Override
	protected void layout() {
		super.layout();

		slot.fill( this );

		crossB.x = PixelScene.align( x + (width - crossB.width) / 2 );
		crossB.y = PixelScene.align( y + (height - crossB.height) / 2 );
	}

	@Override
	protected void onClick() {
		if (PartySession.input(Dungeon.hero, "quick_assign", index)) return;
		GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, Message.key("custom.generated.entry_select_an_item_for_the_quickslot_7c094feb25"));
	}

	@Override
	protected boolean onLongClick() {
		if (PartySession.input(Dungeon.hero, "quick_assign", index)) return true;
		GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, Message.key("custom.generated.entry_select_an_item_for_the_quickslot_7c094feb25"));
		return true;
	}

	@SuppressWarnings("unchecked")
	private Item select() {

		Object content = values[index];
		if (content instanceof Item) {

			return (Item)content;

		} else if (content != null) {

			Item item = Dungeon.hero.belongings.getItem( (Class<? extends Item>)content );
			return item != null ? item : Item.virtual( (Class<? extends Item>)content );

		} else {

			return null;

		}
	}

	public static void partyUse(int index, boolean assign) {
		if (index < 0 || index >= MAX_SLOTS) return;
		QuickSlot slot = slots[index];
		if (slot == null) return;
		if (assign) {
			GameScene.selectItem(slot, WndBag.Mode.QUICKSLOT, Message.key("custom.generated.entry_select_an_item_for_the_quickslot_7c094feb25"));
			return;
		}
		Hero hero = Dungeon.hero;
		Item item = slot.select();
		if (item == null || item.quantity() <= 0
				|| !(hero.belongings.backpack.contains(item) || item.isEquipped(hero))) return;
		if (LocalCoop.defer(hero, () -> partyUse(index, false))) return;
		item.execute(hero);
	}

	@Override
	public void onSelect( Item item ) {
		if (item != null) {
			values[index] = item.stackable ? item.getClass() : item;
			refresh();
		}
	}

	public void item( Item item ) {
		slot.item( item );
		itemInSlot = item;
		enableSlot();
	}

	public void enable( boolean value ) {
		active = value;
		if (value) {
			enableSlot();
		} else {
			slot.enable( false );
		}
	}

	private void enableSlot() {
		slot.enable( !(itemInSlot instanceof Bow) && (!(itemInSlot instanceof Arrow)  || Dungeon.hero.belongings.bow != null) &&
			itemInSlot != null &&
			itemInSlot.quantity() > 0 &&
			(Dungeon.hero.belongings.backpack.contains( itemInSlot ) || itemInSlot.isEquipped( Dungeon.hero )));
	}

	private void useTargeting() {

		targeting = lastTarget != null && lastTarget.isAlive() && Dungeon.visible[lastTarget.pos];

		if (targeting) {
			int pos = Ballistica.cast( Dungeon.hero.pos, lastTarget.pos, false, true );
			if (pos != lastTarget.pos) {
				lastTarget = null;
				targeting = false;
			}
		}

		if (!targeting) {
			int n = Dungeon.hero.visibleEnemies();
			for (int i=0; i < n; i++) {
				Mob enemy = Dungeon.hero.visibleEnemy( i );
				int pos = Ballistica.cast( Dungeon.hero.pos, enemy.pos, false, true );
				if (pos == enemy.pos) {
					lastTarget = enemy;
					targeting = true;
					break;
				}
			}
		}

		if (targeting) {
			if (Actor.all().contains( lastTarget )) {
				lastTarget.sprite.parent.add( crossM );
				crossM.point(Difficulties.is3d ? DungeonTerrainTilemap.raisedTileToWorld( lastTarget.pos ) :  DungeonTilemapOld.tileToWorld( lastTarget.pos ));
				crossB.visible = true;
			} else {
				lastTarget = null;
			}
		}
	}

	public static void refresh() {
		if (LocalCoop.isRestoring()) return;
		for (QuickSlot slot : slots) {
			if (slot != null) slot.item(slot.select());
		}
	}

	public static void target( Item item, Char target ) {
		if (target != Dungeon.hero) {
			lastTarget = target;
			HealthIndicator.instance.target( target );
		}
	}

	public static void cancel() {
		for (QuickSlot slot : slots) {
			if (slot == null || !slot.targeting) continue;
			slot.crossB.visible = false;
			slot.crossM.remove();
			slot.targeting = false;
		}
	}

	private static final String QUICKSLOTS = "quick_slots";

	@SuppressWarnings("unchecked")
	public static void save( Bundle bundle ) {
		Belongings stuff = Dungeon.hero.belongings;
		String[] types = new String[MAX_SLOTS];
		Arrays.fill(types, "");
		for (int i = 0; i < MAX_SLOTS; i++) {
			if (values[i] instanceof Class && stuff.getItem((Class<? extends Item>)values[i]) != null) {
				types[i] = ((Class<?>)values[i]).getName();
			}
		}
		bundle.put(QUICKSLOTS, types);
	}

	public static void save( Bundle bundle, Item item ) {
		int[] assigned = new int[MAX_SLOTS];
		int count = 0;
		for (int i = 0; i < MAX_SLOTS; i++) {
			if (values[i] == item) assigned[count++] = i;
		}
		if (count > 0) bundle.put(QUICKSLOTS, Arrays.copyOf(assigned, count));
	}

	public static void restore( Bundle bundle ) {
		resetSlots();
		String[] types = bundle.getStringArray(QUICKSLOTS);
		if (types == null) return;
		for (int i = 0; i < Math.min(types.length, MAX_SLOTS); i++) {
			if (types[i].isEmpty()) continue;
			try {
				values[i] = Class.forName(types[i], false, Item.class.getClassLoader()).asSubclass(Item.class);
			} catch (ClassNotFoundException | ClassCastException ignored) {
			}
		}
	}

	public static void restore( Bundle bundle, Item item ) {
		int[] assigned = bundle.getIntArray(QUICKSLOTS);
		if (assigned == null) return;
		for (int index : assigned) {
			if (index >= 0 && index < MAX_SLOTS) values[index] = item.stackable ? item.getClass() : item;
		}
	}
}
