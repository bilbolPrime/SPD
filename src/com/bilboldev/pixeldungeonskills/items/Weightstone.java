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

import java.util.ArrayList;

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.pixeldungeonskills.windows.IconTitle;
import com.bilboldev.pixeldungeonskills.windows.WndBag;

public class Weightstone extends Item {
	
	private static final String TXT_SELECT_WEAPON	= "Select a weapon to balance";
	private static final String TXT_FAST			= "you balanced your %s to make it faster";
	private static final String TXT_ACCURATE		= "you balanced your %s to make it more accurate";
	
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
			GameScene.selectItem( itemSelector, WndBag.Mode.WEAPON, TXT_SELECT_WEAPON );
			
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
			GLog.p( TXT_FAST, weapon.name() );
		} else {
			weapon.imbue = Weapon.Imbue.ACCURACY;
			GLog.p( TXT_ACCURATE, weapon.name() );
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
	
	public class WndBalance extends Window {

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
			
			IconTitle titlebar = new IconTitle( weapon );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );
			
			BitmapTextMultiline tfMesage = PixelScene.createMultiline( Utils.format( TXT_CHOICE, weapon.name() ), 8 );
			tfMesage.maxWidth = WIDTH - MARGIN * 2;
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
			
			resize( WIDTH, (int)btnCancel.bottom() + MARGIN );
		}
		
		protected void onSelect( int index ) {};
	}
}
