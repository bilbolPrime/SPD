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
import java.util.Locale;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.hero.Belongings;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.ui.BadgesList;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.ui.ItemSlot;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndRanking extends WndTabbed {

    @Override
    protected com.bilboldev.pixeldungeonskills.ui.RebornChrome.Material rebornMaterial() {
        return com.bilboldev.pixeldungeonskills.ui.RebornChrome.Material.STONE;
    }
	
	private static final String TXT_ERROR		= "Unable to load additional information";
	
	private static final String TXT_STATS	= "Stats";
	private static final String TXT_ITEMS	= "Items";
	private static final String TXT_BADGES	= "Badges";
	
	private final int windowWidth;
	private final int windowHeight;
	
	private Thread thread;
	private String error = null;
	
	private Image busy;
	
	public WndRanking( final String gameFile ) {
		
		super();
		windowWidth = boundedWidth(190);
		windowHeight = Math.max(48, Math.min(200, (int)(Game.height / camera.zoom) - frameHeight() - 8));
		resize( windowWidth, windowHeight );
		
		thread = new Thread() {
			@Override
			public void run() {
				try {
					Badges.loadGlobal();
					Dungeon.loadGame( gameFile );
				} catch (Exception e ) {
					error = TXT_ERROR;
				}
			}
		};
		thread.start();

		busy = Icons.BUSY.get();	
		busy.origin.set( busy.width / 2, busy.height / 2 );
		busy.angularSpeed = 720;
		busy.x = (windowWidth - busy.width) / 2;
		busy.y = (windowHeight - busy.height) / 2;
		add( busy );
	}
	
	@Override
	public void update() {
		super.update();
		
		if (thread != null && !thread.isAlive()) {
			thread = null;
            if(Dungeon.hero == null)
                error = TXT_ERROR;
			if (error == null) {
				remove( busy );
				createControls();
			} else {
				hide();
				Game.scene().add( new WndError( TXT_ERROR ) );
			}
		}
	}
	
	private void createControls() {
		
		String[] labels = 
			{TXT_STATS, TXT_ITEMS, TXT_BADGES};
		Group[] pages = 
			{new StatsTab(), new ItemsTab(), new BadgesTab()};
		
		for (int i=0; i < pages.length; i++) {
			
			add( pages[i] );
			
			Tab tab = new RankingTab( labels[i], pages[i] );
			tab.setSize( (windowWidth + chrome.marginHor() - 2) / 3f, tabHeight() );
			add( tab );
		}
		
		select( 0 );
	}

	private class RankingTab extends LabeledTab {
		
		private Group page;
		
		public RankingTab( String label, Group page ) {
			super( label );
			this.page = page;
		}
		
		@Override
		protected void select( boolean value ) {
			super.select( value );
			if (page != null) {
				page.visible = page.active = selected;
			}
		}
	}
	
	private class StatsTab extends Group {
		
		private static final int GAP	= 4;

		public StatsTab() {
			super();
			ControlRow content = new ControlRow();

            if(Dungeon.hero == null)
            {
                IconTitle title = new IconTitle();
                title.label( "ERROR loading data" );
				title.setRect( 0, 0, windowWidth, 0 );
                add( title );
                return;
            }

			String heroClass = Dungeon.hero.className();
			
			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar( Dungeon.hero.heroClass, Dungeon.hero.tier() ) );
			title.label( Utils.format( Messages.get( StatsTab.class, "title" ), Dungeon.hero.lvl, heroClass ).toUpperCase( Locale.ENGLISH ) );
			title.setRect( 0, 0, textWidth(windowWidth), 0 );
			content.add( title );
			
			float pos = title.bottom();
			
			if (Dungeon.challenges > 0) {
				RedButton btnChallenges = new ActionButton( Messages.get( StatsTab.class, "challenges" ) ) {
					{ content.addControl(hotArea); }
					@Override
					protected void onClick() {
						Game.scene().add( new WndChallenges( Dungeon.challenges, false ) );
					}
				};
				btnChallenges.setRect( 0, pos + GAP,
						Math.min(textWidth(windowWidth), btnChallenges.reqWidth() + 2), 18 );
				btnChallenges.setSize(btnChallenges.width(), Math.max(18, btnChallenges.reqHeight() + 2));
				content.add( btnChallenges );
				
				pos = btnChallenges.bottom();
			}
			
			pos += GAP + GAP;
			
			pos = statSlot( content, Messages.get( StatsTab.class, "str" ), Integer.toString( Dungeon.hero.STR ), pos );
			pos = statSlot( content, "Health", Integer.toString( Dungeon.hero.HT ), pos );
			
			pos += GAP;
			
			pos = statSlot( content, Messages.get( StatsTab.class, "duration" ), Integer.toString( (int)Statistics.duration ), pos );
			
			pos += GAP;
			
			pos = statSlot( content, Messages.get( StatsTab.class, "depth" ), Integer.toString( Statistics.deepestFloor ), pos );
			pos = statSlot( content, Messages.get( StatsTab.class, "enemies" ), Integer.toString( Statistics.enemiesSlain ), pos );
			pos = statSlot( content, Messages.get( StatsTab.class, "gold" ), Integer.toString( Statistics.goldCollected ), pos );
			
			pos += GAP;
			
			pos = statSlot( content, Messages.get( StatsTab.class, "food" ), Integer.toString( Statistics.foodEaten ), pos );
			pos = statSlot( content, Messages.get( StatsTab.class, "alchemy" ), Integer.toString( Statistics.potionsCooked ), pos );
			pos = statSlot( content, "Ankhs Used", Integer.toString( Statistics.ankhsUsed ), pos );

			pos = statSlot( content, "Difficulty", Dungeon.rules().mode == GameplayMode.REBORN
					? GameplayMode.REBORN.title() : Difficulties.title(Dungeon.hero.difficulty), pos );
			content.setSize(textWidth(windowWidth), pos);
			ScrollPane list = content.scrollPane(content);
			camera = WndRanking.this.camera;
			add(list);
			list.setSize(windowWidth, windowHeight);
		}
		
		private float statSlot( Group parent, String label, String value, float pos ) {
			
			BitmapTextMultiline name = PixelScene.createMultiline(Messages.maybeTranslate(label), 8);
			name.maxWidth = (int)(windowWidth * .6f) - GAP;
			name.measure();
			name.y = pos;
			parent.add(name);
			BitmapTextMultiline number = PixelScene.createMultiline(value, 8);
			number.maxWidth = textWidth(windowWidth) - (int)(windowWidth * .6f);
			number.measure();
			number.x = PixelScene.align(windowWidth * .6f);
			number.y = pos;
			parent.add(number);
			return pos + GAP + Math.max(name.height(), number.height());
		}
	}
	
	private class ItemsTab extends Group {
		
		private final Component content = new Component();
		private final ArrayList<LabelledItemButton> items = new ArrayList<>();
		private float pos;
		
		public ItemsTab() {
			super();

            if(Dungeon.hero == null)
            {
                IconTitle title = new IconTitle();
                title.label( "ERROR loading data" );
				title.setRect( 0, 0, windowWidth, 0 );
                add( title );
                return;
            }

			Belongings stuff = Dungeon.hero.belongings;
			addItem( stuff.weapon );
			addItem( stuff.armor );
			addItem( stuff.ring1 );
			addItem( stuff.ring2 );
			addItem( stuff.bow );

			if (!stuff.backpack.items.isEmpty() || Dungeon.gold > 0) {
				addHeading( stuff.backpack.name() );
				for (Item item : stuff.backpack) {
					addItem( item );
				}
				if (Dungeon.gold > 0) {
					addItem( new Gold( Dungeon.gold ) );
				}
			}

			if (!Dungeon.hero.storage.backpack.items.isEmpty()) {
				addHeading( "Storage" );
				for (Item item : Dungeon.hero.storage.backpack) {
					addItem( item );
				}
			}

			content.setSize( windowWidth, pos );
			ScrollPane list = new ScrollPane( content ) {
				@Override
				public void onClick( float x, float y ) {
					for (LabelledItemButton slot : items) {
						if (slot.inside( x, y )) {
							Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
							slot.onClick();
							break;
						}
					}
				}
			};
			camera = WndRanking.this.camera;
			add( list );
			list.setSize( windowWidth, windowHeight );
		}

		private void addItem( Item item ) {
			if (item == null) {
				return;
			}
			LabelledItemButton slot = new LabelledItemButton( item );
			slot.setRect( 0, pos, windowWidth - 4, LabelledItemButton.SIZE );
			content.add( slot );
			items.add( slot );
			pos += slot.height() + 1;
		}

		private void addHeading( String title ) {
			if (pos > 0) {
				pos += 4;
			}
			BitmapText heading = PixelScene.createText( Utils.capitalize( Messages.maybeTranslate( title ) ), 7 );
			heading.hardlight( titleColor() );
			heading.y = pos;
			content.add( heading );
			pos += heading.baseLine() + 3;
		}
	}
	
	private class BadgesTab extends Group {
		
		public BadgesTab() {
			super();

            if(Dungeon.hero == null)
            {
                IconTitle title = new IconTitle();
                title.label( "ERROR loading data" );
				title.setRect( 0, 0, windowWidth, 0 );
                add( title );
                return;
            }

			camera = WndRanking.this.camera;
			
			ScrollPane list = new BadgesList( false );
			add( list );
			
			list.setSize( windowWidth, windowHeight );
		}
	}
	
	private class ItemButton extends Button {
		
		public static final int SIZE	= 26;
		
		protected Item item;
		
		protected ItemSlot slot;
		private ColorBlock bg;
		
		public ItemButton( Item item ) {
			
			super();

			this.item = item;
			
			slot.item( item );
			if (item.cursed && item.cursedKnown) {
				bg.ra = +0.2f;
				bg.ga = -0.1f;
			} else if (!item.isIdentified()) {
				bg.ra = 0.1f;
				bg.ba = 0.1f;
			}
		}
		
		@Override
		protected void createChildren() {	
			
			bg = com.bilboldev.pixeldungeonskills.ui.RebornSlot.create(SIZE, SIZE, 0xFF4A4D44);
			add( bg );
			
			slot = new ItemSlot();
			add( slot );
			
			super.createChildren();
		}
		
		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;
			
			slot.setRect( x, y, SIZE, SIZE );
			
			super.layout();
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
			Game.scene().add( new WndItem( null, item ) );
		}
	}

	private class LabelledItemButton extends ItemButton {
		private BitmapText name;
		
		public LabelledItemButton( Item item ) {
			super( item );
		}
		
		@Override
		protected void createChildren() {	
			super.createChildren();
			
			name = PixelScene.createText( "?", 7 );
			add( name );
		}
		
		@Override
		protected void layout() {
			
			super.layout();
			
			name.x = slot.right() + 2;
			name.y = y + (height - name.baseLine()) / 2;
			
			String str = Utils.capitalize( item.name() );
			name.text( str );
			name.measure();
			if (name.width() > width - name.x) {
				do {
					str = str.substring( 0, str.length() - 1 );
					name.text( str + "..." );
					name.measure();
				} while (name.width() > width - name.x);
			}
		}
	}
}
