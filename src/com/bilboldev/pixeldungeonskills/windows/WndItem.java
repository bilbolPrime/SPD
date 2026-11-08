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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.MenuModel;
import com.bilboldev.pixeldungeonskills.network.PartyWindows;

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.noosa.ui.Component;
import java.util.ArrayList;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndItem extends WndScrollable {

    private final Hero hero = Dungeon.hero;

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.IRON;
    }

	private static final float BUTTON_WIDTH		= 36;
	private static final float BUTTON_HEIGHT	= 16;

	private static final float GAP	= 2;

	private static final int WIDTH = 120;

	public WndItem( final WndBag owner, final Item item ) {
		this(owner, item, owner != null);
	}

	public static WndItem forParty(Item item, boolean actions) {
		return new WndItem(null, item, actions);
	}

	private WndItem(final WndBag owner, final Item item, boolean actions) {

		super();
        menuModel = new MenuModel(Message.capitalize(Message.item(item)), Message.description(item));
        menuModel.view = MenuModel.View.ITEM;
        menuModel.item = item;
        menuModel.selection = actions;
		int windowWidth = boundedWidth(InspectionTitle.windowWidth(WIDTH));

		int color = titleColor();
		if (item.levelKnown) {
			if (item.level() < 0) {
				color = ItemSlot.DEGRADED;
			} else if (item.level() > 0) {
				color = item.isBroken() ? ItemSlot.WARNING : ItemSlot.UPGRADED;
			}
		}
		float durability = item.isUpgradable() && item.levelKnown
				? (float)item.durability() / item.maxDurability() : Float.NaN;
		Component titlebar = InspectionTitle.create(new ItemSprite(item.image(), item.glowing()),
				Utils.capitalize(item.toString()), color, durability);
		titlebar.setRect(0, 0, windowWidth, 0);
		add(titlebar);

		HighlightedText info = new HighlightedText(  6 );
		info.text(item.displayInfo(), textWidth(windowWidth));
		ArrayList<RedButton> buttons = new ArrayList<>();

		if (hero.isAlive() && actions) {
			for (final String action:item.actions( hero )) {

				RedButton btn = new ActionButton( item.actionName( action, hero ) ) {
					@Override
					protected void onClick() {
						if (PartyWindows.select(this)) return;
						Runnable use = () -> {
							if (!LocalCoop.active() || hero.isAlive()
									&& (hero.belongings.backpack.contains(item) || item.isEquipped(hero))
									&& item.actions(hero).contains(action)) {
								item.execute(hero, action);
							}
						};
						if (!LocalCoop.defer(hero, use)) use.run();
						hide();
						if (owner != null) owner.hide();
					};
				};
				btn.setSize( Math.max( BUTTON_WIDTH, btn.reqWidth() ), BUTTON_HEIGHT );
				buttons.add( btn );
                menuModel.add(btn, Message.action(item, action));

				if (action == item.defaultAction) {
					btn.textColor( titleColor() );
				}

			}
            if (hero.talents() != null && item.defaultAction != null) {
                RedButton bind = new ActionButton(Message.key("skills4.quickslot").toString()) {
                    @Override protected void onClick() {
                        if (PartyWindows.select(this)) return;
                        hide();
                        if (owner != null) owner.hide();
                        LocalCoop.withHero(hero, () -> GameScene.show(new WndQuickSlot(item)));
                    }
                };
                bind.setSize(Math.max(BUTTON_WIDTH, bind.reqWidth()), BUTTON_HEIGHT);
                buttons.add(bind);
                menuModel.add(bind, Message.key("skills4.quickslot"));
            }
		}

		layoutContent(windowWidth, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}
}
