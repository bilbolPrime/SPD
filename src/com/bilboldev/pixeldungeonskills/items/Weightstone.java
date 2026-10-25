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
import com.bilboldev.pixeldungeonskills.ui.MenuModel;

import java.util.ArrayList;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.pixeldungeonskills.windows.InspectionTitle;
import com.bilboldev.pixeldungeonskills.windows.WndBag;
import com.bilboldev.pixeldungeonskills.windows.WndScrollable;

public class Weightstone extends Item {

	private static final String TXT_SELECT_WEAPON	= "Select a weapon to balance";
	private static final String TXT_FAST			= "custom.generated.entry_you_balanced_your_s_to_make_it_faster_30e5eb4b18";
	private static final String TXT_ACCURATE		= "custom.generated.entry_you_balanced_your_s_to_make_it_more_accurate_579d879ffd";

	private static final float TIME_TO_APPLY = 2;

	private static final String AC_APPLY = "APPLY";

	{
		name = "weightstone";
		image = ItemSpriteSheet.WEIGHT;

		stackable = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_APPLY );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action == AC_APPLY) {

			curUser = hero;
			GameScene.selectItem(itemSelector, WndBag.Mode.WEAPON, Message.key("custom.generated.entry_select_a_weapon_to_balance_3526c55a4e"));

		} else {

			super.execute( hero, action );

		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	private void apply( Weapon weapon, boolean forSpeed ) {

		detach( curUser.belongings.backpack );

		weapon.fix();
		if (forSpeed) {
			weapon.imbue = Weapon.Imbue.SPEED;
			GLog.p(TXT_FAST, Message.itemName(weapon));
		} else {
			weapon.imbue = Weapon.Imbue.ACCURACY;
			GLog.p(TXT_ACCURATE, Message.itemName(weapon));
		}

		curUser.sprite.operate( curUser.pos );
		Sample.INSTANCE.play( Assets.SND_MISS );

		curUser.spend( TIME_TO_APPLY );
		curUser.busy();
	}

	@Override
	public int price() {
		return 40 * quantity;
	}

	@Override
	public String info() {
		return
			"Using a weightstone, you can balance your melee weapon to increase its speed or accuracy.";
	}

	private final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				GameScene.show( new WndBalance( (Weapon)item ) );
			}
		}
	};

	public class WndBalance extends WndScrollable {

		private static final String TXT_CHOICE = "How would you like to balance your %s?";

		private static final String TXT_SPEED		= "For speed";
		private static final String TXT_ACCURACY	= "For accuracy";
		private static final String TXT_CANCEL		= "Never mind";

		private static final int WIDTH			= 120;
		private static final int MARGIN 		= 2;
		private static final int BUTTON_WIDTH	= WIDTH - MARGIN * 2;
		private static final int BUTTON_HEIGHT	= 20;

		public WndBalance( final Weapon weapon ) {
			super();
			int windowWidth = Presentation.reborn() ? boundedWidth(InspectionTitle.windowWidth(WIDTH)) : WIDTH;
			ArrayList<RedButton> buttons = new ArrayList<>();
            menuModel = new MenuModel(Message.item(weapon), Message.key("custom.generated.entry_how_would_you_like_to_balance_your_s_66ae17fbcd", Message.itemName(weapon)));

			Component titlebar = InspectionTitle.create(new ItemSprite(weapon.image(), weapon.glowing()),
					Utils.capitalize(weapon.toString()));
			titlebar.setRect( 0, 0, windowWidth, 0 );
			add( titlebar );

			BitmapTextMultiline tfMesage = PixelScene.createMultiline( Utils.format( TXT_CHOICE, weapon.name() ), 8 );
			tfMesage.maxWidth = windowWidth - MARGIN * 2;
			tfMesage.measure();
			tfMesage.x = MARGIN;
			tfMesage.y = titlebar.bottom() + MARGIN;
			add( tfMesage );

			float pos = tfMesage.y + tfMesage.height();

			if (weapon.imbue != Weapon.Imbue.SPEED) {
				RedButton btnSpeed = new RedButton( TXT_SPEED ) {
					@Override
					protected void onClick() {
						hide();
						Weightstone.this.apply( weapon, true );
					}
				};
				btnSpeed.setRect( MARGIN, pos + MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT );
				add( btnSpeed );
				buttons.add(btnSpeed);
            menuModel.add(btnSpeed, Message.key("custom.generated.entry_for_speed_0005c2408c"));

				pos = btnSpeed.bottom();
			}

			if (weapon.imbue != Weapon.Imbue.ACCURACY) {
				RedButton btnAccuracy = new RedButton( TXT_ACCURACY ) {
					@Override
					protected void onClick() {
						hide();
						Weightstone.this.apply( weapon, false );
					}
				};
				btnAccuracy.setRect( MARGIN, pos + MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT );
				add( btnAccuracy );
				buttons.add(btnAccuracy);
            menuModel.add(btnAccuracy, Message.key("custom.generated.entry_for_accuracy_fc787a16d1"));

				pos = btnAccuracy.bottom();
			}

			RedButton btnCancel = new RedButton( TXT_CANCEL ) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnCancel.setRect( MARGIN, pos + MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT );
			add( btnCancel );
			buttons.add(btnCancel);
            menuModel.add(btnCancel, Message.key("items.stones.stoneofaugmentation$wndaugment.cancel"));

			if (Presentation.reborn()) {
				for (RedButton button : buttons) {
					button.setSize(windowWidth, BUTTON_HEIGHT);
				}
				layoutContent(windowWidth, titlebar, textContent(tfMesage), MARGIN, buttons.toArray(new RedButton[0]));
			} else {
				resize(WIDTH, (int)btnCancel.bottom() + MARGIN);
			}
		}

		protected void onSelect( int index ) {};
	}
}
