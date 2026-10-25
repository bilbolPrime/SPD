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

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.noosa.ui.Component;
import java.util.ArrayList;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Item;
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

		super();
        menuModel = new MenuModel(Message.capitalize(Message.item(item)), Message.description(item));
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

		if (hero.isAlive() && owner != null) {
			for (final String action:item.actions( hero )) {

				RedButton btn = new ActionButton( item.actionName( action, hero ) ) {
					@Override
					protected void onClick() {
						Runnable use = () -> {
							if (!LocalCoop.active() || hero.isAlive()
									&& (hero.belongings.backpack.contains(item) || item.isEquipped(hero))) {
								item.execute(hero, action);
							}
						};
						if (!LocalCoop.defer(hero, use)) use.run();
						hide();
						owner.hide();
					};
				};
				btn.setSize( Math.max( BUTTON_WIDTH, btn.reqWidth() ), BUTTON_HEIGHT );
				buttons.add( btn );
                menuModel.add(btn, Message.action(item, action));

				if (action == item.defaultAction) {
					btn.textColor( titleColor() );
				}

			}
		}

		layoutContent(windowWidth, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}
}
