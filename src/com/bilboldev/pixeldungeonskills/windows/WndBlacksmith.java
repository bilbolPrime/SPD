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
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndBlacksmith extends WndScrollable {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.IRON;
    }

	private static final int BTN_SIZE	= 36;
	private static final float GAP		= 2;
	private static final float BTN_GAP	= 10;
	private static final int WIDTH		= 116;

	private ItemButton btnPressed;

	private ItemButton btnItem1;
	private ItemButton btnItem2;
	private RedButton btnReforge;

	private static final String TXT_PROMPT =
		"Ok, a deal is a deal, dat's what I can do for you: I can reforge " +
		"2 items and turn them into one of a better quality.";
	private static final String TXT_SELECT =
		"Select an item to reforge";
	private static final String TXT_REFORGE =
		"Reforge them";

	public WndBlacksmith( Blacksmith troll, Hero hero ) {

		super();
        menuModel = new MenuModel(Message.actor(troll), Message.key("custom.generated.entry_ok_a_deal_is_a_deal_dat_s_what_i_can_do_for_you_097615602d"));
		int windowWidth = Presentation.reborn() ? boundedWidth(InspectionTitle.windowWidth(WIDTH)) : WIDTH;

		Component titlebar = InspectionTitle.create(troll.sprite(), Utils.capitalize(troll.name));
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );

		BitmapTextMultiline message = PixelScene.createMultiline( TXT_PROMPT, 6 );
		message.maxWidth = Presentation.reborn() ? textWidth(windowWidth) : windowWidth;
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );

		btnItem1 = new ItemButton() {
			@Override
			protected void onClick() {
				btnPressed = btnItem1;
				GameScene.selectItem(itemSelector, WndBag.Mode.UPGRADEABLE, Message.key("custom.generated.entry_select_an_item_to_reforge_40739268b1"));
			}
		};
		btnItem1.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.y + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
		add( btnItem1 );
        menuModel.add(btnItem1.slot, Message.key("windows.wndblacksmith.first_item"));

		btnItem2 = new ItemButton() {
			@Override
			protected void onClick() {
				btnPressed = btnItem2;
				GameScene.selectItem(itemSelector, WndBag.Mode.UPGRADEABLE, Message.key("custom.generated.entry_select_an_item_to_reforge_40739268b1"));
			}
		};
		btnItem2.setRect( btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE );
		add( btnItem2 );
        menuModel.add(btnItem2.slot, Message.key("windows.wndblacksmith.second_item"));

		btnReforge = new RedButton( TXT_REFORGE ) {
			@Override
			protected void onClick() {
				Blacksmith.upgrade( btnItem1.item, btnItem2.item );
				hide();
			}
		};
		btnReforge.enable( false );
		btnReforge.setRect( 0, btnItem1.bottom() + BTN_GAP, WIDTH, 20 );
		add( btnReforge );
        menuModel.add(btnReforge, Message.key("windows.wndblacksmith$wndreforge.reforge"));

		if (Presentation.reborn()) {
			Component items = new Component() {
				@Override
				protected void layout() {
					float left = left() + (width() - BTN_SIZE * 2 - BTN_GAP) / 2;
					btnItem1.setRect(left, top() + GAP, BTN_SIZE, BTN_SIZE);
					btnItem2.setRect(btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE);
					height = BTN_SIZE + GAP * 2;
				}
			};
			items.add(btnItem1);
			items.add(btnItem2);
			items.setSize(windowWidth, BTN_SIZE + GAP * 2);
			btnReforge.setRect(0, 0, windowWidth, 20);
			layoutContent(windowWidth, titlebar, textContent(message), items, GAP, btnReforge);
		} else {
			resize(WIDTH, (int)btnReforge.bottom());
		}
	}

	protected WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				btnPressed.item( item );

				if (btnItem1.item != null && btnItem2.item != null) {
					Message result = Blacksmith.verify( btnItem1.item, btnItem2.item );
					if (result != null) {
						GameScene.show( new WndMessage(result) );
						btnReforge.enable( false );
					} else {
						btnReforge.enable( true );
					}
				}
			}
		}
	};

	public static class ItemButton extends Component {

		protected NinePatch bg;
		protected ItemSlot slot;

		public Item item = null;

		@Override
		protected void createChildren() {
			super.createChildren();

			bg = Chrome.get( Chrome.Type.BUTTON );
			add( bg );

			slot = new ItemSlot() {
				@Override
				protected void onTouchDown() {
					bg.brightness( 1.2f );
					Sample.INSTANCE.play( Assets.SND_CLICK );
				};
				@Override
				protected void onTouchUp() {
					bg.resetColor();
				}
				@Override
				protected void onClick() {
					ItemButton.this.onClick();
				}
			};
			add( slot );
		}

		protected void onClick() {};

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size( width, height );

			slot.setRect( x + 2, y + 2, width - 4, height - 4 );
		};

		public void item( Item item ) {
			slot.item( this.item = item );
		}
	}
}
