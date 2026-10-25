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

import java.util.ArrayList;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Shopkeeper;
import com.bilboldev.pixeldungeonskills.items.EquipableItem;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfHaggler;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndTradeItem extends WndScrollable {

    @Override
    protected com.bilboldev.pixeldungeonskills.ui.RebornChrome.Material rebornMaterial() {
        return com.bilboldev.pixeldungeonskills.ui.RebornChrome.Material.PARCHMENT;
    }

	private static final float GAP		= 2;
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 16;

	private static final String TXT_SALE		= "FOR SALE: %s - %dg";
	private static final String TXT_BUY			= "Buy for %dg";
	private static final String TXT_SELL		= "Sell for %dg";
	private static final String TXT_SELL_1		= "Sell 1 for %dg";
	private static final String TXT_SELL_ALL	= "Sell all for %dg";
	private static final String TXT_CANCEL		= "Never mind";

	private static final String TXT_SOLD	= "You've sold your %s for %dg";
	private static final String TXT_BOUGHT	= "You've bought %s for %dg";

	private WndBag owner;
	private int windowWidth;
	private Component titlebar;
	private HighlightedText info;

	public WndTradeItem( final Item item, WndBag owner ) {

		super();

		this.owner = owner;

		createDescription(item, false);
		ArrayList<RedButton> buttons = new ArrayList<>();

		if (item.quantity() == 1) {

			RedButton btnSell = new ActionButton( text( "sell", TXT_SELL, item.price() ) ) {
				@Override
				protected void onClick() {
					sell( item );
					hide();
				}
			};
			btnSell.setSize(windowWidth, BTN_HEIGHT);
			buttons.add(btnSell);
            menuModel.add(btnSell, Message.key(WndTradeItem.class, "sell", item.price()));

		} else {

			int priceAll= item.price();
			RedButton btnSell1 = new ActionButton( text( "sell_1", TXT_SELL_1, priceAll / item.quantity() ) ) {
				@Override
				protected void onClick() {
					sellOne( item );
					hide();
				}
			};
			btnSell1.setSize(windowWidth, BTN_HEIGHT);
			buttons.add(btnSell1);
            menuModel.add(btnSell1, Message.key(WndTradeItem.class, "sell_1", priceAll / item.quantity()));
			RedButton btnSellAll = new ActionButton( text( "sell_all", TXT_SELL_ALL, priceAll ) ) {
				@Override
				protected void onClick() {
					sell( item );
					hide();
				}
			};
			btnSellAll.setSize(windowWidth, BTN_HEIGHT);
			buttons.add(btnSellAll);
            menuModel.add(btnSellAll, Message.key(WndTradeItem.class, "sell_all", priceAll));

		}

		RedButton btnCancel = new ActionButton( text( "cancel", TXT_CANCEL ) ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setSize(windowWidth, BTN_HEIGHT);
		buttons.add(btnCancel);
            menuModel.add(btnCancel, Message.key(WndTradeItem.class, "cancel"));
		layoutContent(windowWidth, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}

	public WndTradeItem( final Heap heap, boolean canBuy ) {

		super();

		if(heap.type == Heap.Type.FOR_SALE){
			Dungeon.showGold = true;
		}

		Item item = heap.peek();

		createDescription(item, true);
		ArrayList<RedButton> buttons = new ArrayList<>();

		int price = price( item );

		if (canBuy) {

			RedButton btnBuy = new ActionButton( text( "buy", TXT_BUY, price ) ) {
				@Override
				protected void onClick() {
					hide();
					buy( heap );
				}
			};
			btnBuy.setSize(windowWidth, BTN_HEIGHT);
			btnBuy.enable( price <= Dungeon.gold );
			buttons.add(btnBuy);
            menuModel.add(btnBuy, Message.key(WndTradeItem.class, "buy", price));

			RedButton btnCancel = new ActionButton( text( "cancel", TXT_CANCEL ) ) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnCancel.setSize(windowWidth, BTN_HEIGHT);
			buttons.add(btnCancel);
            menuModel.add(btnCancel, Message.key(WndTradeItem.class, "cancel"));
		}
		layoutContent(windowWidth, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}

	@Override
	public void hide() {

		super.hide();

		Dungeon.showGold = false;

		if (owner != null) {
			owner.hide();
			Shopkeeper.sell();
		}
	}

	private void createDescription( Item item, boolean forSale ) {
        menuModel = new MenuModel(Message.capitalize(Message.item(item)), Message.description(item));

		windowWidth = boundedWidth(InspectionTitle.windowWidth(WIDTH));
		String title = forSale ?
			Utils.capitalize( item.toString() ) + " - " + price( item ) + "g" :
			Utils.capitalize( item.toString() );

		int color = titleColor();
		if (item.levelKnown) {
			if (item.level() < 0) {
				color = ItemSlot.DEGRADED;
			} else if (item.level() > 0) {
				color = item.isBroken() ? ItemSlot.WARNING : ItemSlot.UPGRADED;
			}
		}
		titlebar = InspectionTitle.create(new ItemSprite(item.image(), item.glowing()), title, color, Float.NaN);
		titlebar.setRect(0, 0, windowWidth, 0);

		info = new HighlightedText(6);
		info.text(item.displayInfo(), textWidth(windowWidth));
	}

	private void sell( Item item ) {

		Hero hero = Dungeon.hero;

		if (item.isEquipped( hero ) && !((EquipableItem)item).doUnequip( hero, false )) {
			return;
		}
		item.detachAll( hero.belongings.backpack );

		int price = item.price();

		new Gold( price ).doPickUp( hero );
		GLog.i(WndTradeItem.class, "sold", Message.itemName(item), price);
	}

	private void sellOne( Item item ) {

		if (item.quantity() <= 1) {
			sell( item );
		} else {

			Hero hero = Dungeon.hero;

			item = item.detach( hero.belongings.backpack );
			int price = item.price();

			new Gold( price ).doPickUp( hero );
			GLog.i(WndTradeItem.class, "sold", Message.itemName(item), price);
		}
	}

	private int price( Item item ) {

		int price = item.price() * 5 * (Dungeon.depth / 5 + 1);
		if (Dungeon.hero.buff( RingOfHaggler.Haggling.class ) != null && price >= 2) {
			price /= 2;
		}
		return price;
	}

	private void buy( Heap heap ) {

		Hero hero = Dungeon.hero;
		Item item = heap.pickUp();

		int price = price( item );
		Dungeon.gold -= price;

		GLog.i(WndTradeItem.class, "bought", Message.itemName(item), price);

		if (!item.doPickUp( hero )) {
			Dungeon.level.drop( item, heap.pos ).sprite.drop();
		}
	}

	private static String text( String key, String fallback, Object... args ) {
		String translated = Messages.get( WndTradeItem.class, key, args );
		if (!Messages.NO_TEXT_FOUND.equals( translated )) {
			return translated;
		}

		return Messages.maybeTranslate( fallback, args );
	}
}
