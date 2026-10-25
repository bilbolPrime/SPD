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

import com.bilboldev.pixeldungeonskills.ui.RebornChrome;

import java.util.Locale;

import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndHero extends WndTabbed {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.ARCANE;
    }

	private static final String TXT_EXP		= "Experience";
	private static final String TXT_STR		= "Strength";
    private static final String TXT_MANA	= "Mana";
	private static final String TXT_HEALTH	= "Health";
	private static final String TXT_GOLD	= "Gold Collected";
	private static final String TXT_DEPTH	= "Maximum Depth";

    private static final String TXT_Difficulty		= "Difficulty";

	private static final int WIDTH		= 100;
	private static final int TAB_WIDTH	= 40;
	
	private StatsTab stats;
	private BuffsTab buffs;
	
	private SmartTexture icons;
	private TextureFilm film;
	
	public WndHero() {
		
		super();
		
		icons = TextureCache.get( Assets.BUFFS_LARGE );
		film = new TextureFilm( icons, 16, 16 );
		
		stats = new StatsTab();
		add( stats );
		
		buffs = new BuffsTab();
		add( buffs );
		
		add( new LabeledTab( text( "windows.wndhero.stats", "Stats" ) ) {
			protected void select( boolean value ) {
				super.select( value );
				stats.visible = stats.active = selected;
			};
		} );
		add( new LabeledTab( text( "windows.wndhero.buffs", "Buffs" ) ) {
			protected void select( boolean value ) {
				super.select( value );
				buffs.visible = buffs.active = selected;
			};
		} );
		for (Tab tab : tabs) {
			tab.setSize( TAB_WIDTH, tabHeight() );
		}
		
		resize( WIDTH, (int)Math.max( stats.height(), buffs.height() ) );
		
		select( 0 );
	}
	
	private class StatsTab extends Group {
		
		private static final String TXT_TITLE		= "Level %d %s";
		private static final String TXT_CATALOGUS	= "Catalogus";
		private static final String TXT_JOURNAL		= "Journal";
		
		private static final int GAP = 5;
		
		private float pos;
		
		public StatsTab() {
			
			Hero hero = Dungeon.hero; 

			BitmapText title = PixelScene.createText( 
				text( "windows.wndgameinprogress.title", TXT_TITLE, hero.lvl, hero.className() ).toUpperCase( Locale.ENGLISH ), 9 );
			title.hardlight( titleColor() );
			title.measure();
			add( title );
			
			RedButton btnCatalogus = new RedButton( TXT_CATALOGUS ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndCatalogus() );
				}
			};
			btnCatalogus.setRect( 0, title.y + title.height(), btnCatalogus.reqWidth() + 2, btnCatalogus.reqHeight() + 2 );
			add( btnCatalogus );
			
			RedButton btnJournal = new RedButton( TXT_JOURNAL ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndJournal() );
				}
			};
			btnJournal.setRect( 
				btnCatalogus.right() + 1, btnCatalogus.top(), 
				btnJournal.reqWidth() + 2, btnJournal.reqHeight() + 2 );
			add( btnJournal );
			
			pos = btnCatalogus.bottom() + GAP;
			
			statSlot( text( "windows.wndgameinprogress.str", TXT_STR ), hero.STR() );
			statSlot( text( "windows.wndgameinprogress.health", TXT_HEALTH ), hero.HP + "/" + hero.HT );
            statSlot( TXT_MANA, hero.MP + "/" + hero.MMP );
			statSlot( text( "windows.wndgameinprogress.exp", TXT_EXP ), hero.exp + "/" + hero.maxExp() );

			pos += GAP;

            statSlot( TXT_Difficulty, Dungeon.rules().mode == GameplayMode.REBORN
                    ? GameplayMode.REBORN.title() : Dungeon.currentDifficulty.title() );

			statSlot( text( "windows.wndgameinprogress.gold", TXT_GOLD ), Statistics.goldCollected );
			statSlot( text( "windows.wndgameinprogress.depth", TXT_DEPTH ), Statistics.deepestFloor );
			
			pos += GAP;
		}
		
		private void statSlot( String label, String value ) {
			
			BitmapText txt = PixelScene.createText( label, 8 );
			txt.y = pos;
			add( txt );
			
			txt = PixelScene.createText( value, 8 );
			txt.measure();
			txt.x = PixelScene.align( WIDTH * 0.65f );
			txt.y = pos;
			add( txt );
			
			pos += GAP + txt.baseLine();
		}
		
		private void statSlot( String label, int value ) {
			statSlot( label, Integer.toString( value ) );
		}
		
		public float height() {
			return pos;
		}
	}

	private static String text( String key, String fallback, Object... args ) {
		String value = Messages.get( key, args );
		return Messages.NO_TEXT_FOUND.equals( value ) ? Utils.format( fallback, args ) : value;
	}
	
	private class BuffsTab extends Group {
		
		private static final int GAP = 2;
		
		private float pos;
		
		public BuffsTab() {
			for (Buff buff : Dungeon.hero.buffs()) {
				buffSlot( buff );
			}
		}
		
		private void buffSlot( Buff buff ) {
			
			int index = buff.icon();
			
			if (index != BuffIndicator.NONE) {
				
				Image icon = new Image( icons );
				icon.frame( film.get( index ) );
				icon.y = pos;
				add( icon );
				
				BitmapText txt = PixelScene.createText( buff.displayName(), 8 );
				if (buff instanceof Hunger) {
					txt.hardlight( ((Hunger)buff).isStarving() ? 0xFF4444 : 0xFF8800 );
				}
				txt.x = icon.width + GAP;
				txt.y = pos + (int)(icon.height - txt.baseLine()) / 2;
				add( txt );
				
				pos += GAP + icon.height;
			}
		}
		
		public float height() {
			return pos;
		}
	}
}
