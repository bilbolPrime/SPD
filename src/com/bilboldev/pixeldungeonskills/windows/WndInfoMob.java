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

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.pixeldungeonskills.ui.HealthBar;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndInfoMob extends WndTitledMessage {

	public WndInfoMob( Mob mob ) {
		
		super(Presentation.reborn() ? new RebornMobTitle(mob) : new MobTitle(mob), desc(mob),
				InspectionTitle.windowWidth(0));
		
	}
	
	private static String desc( Mob mob ) {
		if (Presentation.reborn()) return mob.displayDescription();
		
		StringBuilder builder = new StringBuilder( mob.displayDescription() );
		
		builder.append( "\n\n" + mob.state.status() + "." );
		
		return builder.toString();
	}

	private static class RebornMobTitle extends Component {
		private static final int PORTRAIT_WIDTH = 46;
		private static final int PORTRAIT_HEIGHT = 50;
		private final RebornChrome frame;
		private final Image plinth;
		private final CharSprite image;
		private final BitmapTextMultiline name, status;
		private final ColorBlock healthRim, healthEmpty, healthFill, healthShine, rule;
		private final MobBuffs buffs;
		private final float health;

		RebornMobTitle(Mob mob) {
			frame = new RebornChrome(Chrome.Type.BUTTON).material(RebornChrome.Material.STONE);
			add(frame);
			plinth = new Image(Assets.TILES_CITY_3D, 80, 16, 16, 16);
			plinth.scale.set(2, 1);
			plinth.hardlight(0x887B95);
			add(plinth);
			image = mob.sprite();
			image.origin.set(0);
			float scale = Math.min(2, Math.min((PORTRAIT_WIDTH - 8) / image.width,
					(PORTRAIT_HEIGHT - 12) / image.height));
			image.scale.set(scale >= 1 ? (float)Math.floor(scale) : scale);
			add(image);

			name = PixelScene.createMultiline(Utils.capitalize(mob.displayName()), 9);
			name.hardlight(titleColor());
			add(name);
			status = PixelScene.createMultiline(mob.state.status() + ".", 6);
			status.hardlight(0xAEA5B5);
			add(status);

			health = mob.HT > 0 ? Math.max(0, Math.min(1, (float)mob.HP / mob.HT)) : 0;
			add(healthRim = new ColorBlock(1, 1, 0xFF71647C));
			add(healthEmpty = new ColorBlock(1, 1, 0xFF37252C));
			add(healthFill = new ColorBlock(1, 1, 0xFF527F65));
			add(healthShine = new ColorBlock(1, 1, 0xFF91B88A));
			add(buffs = new MobBuffs(mob));
			add(rule = new ColorBlock(1, 1, 0xFF514458));
		}

		@Override
		protected void layout() {
			frame.x = x;
			frame.y = y;
			frame.size(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
			plinth.x = x + (PORTRAIT_WIDTH - plinth.width()) / 2;
			plinth.y = y + PORTRAIT_HEIGHT - 18;
			image.x = PixelScene.align(x + (PORTRAIT_WIDTH - image.width()) / 2);
			image.y = PixelScene.align(y + PORTRAIT_HEIGHT - 9 - image.height());

			float left = x + PORTRAIT_WIDTH + 8;
			float available = Math.max(1, right() - left);
			name.x = left;
			name.y = y + 2;
			name.maxWidth = (int)available;
			name.measure();
			healthRim.x = left;
			healthRim.y = (float)Math.ceil(name.y + name.height()) + 5;
			healthRim.size(available, 6);
			healthEmpty.x = healthFill.x = healthShine.x = left + 1;
			healthEmpty.y = healthFill.y = healthShine.y = healthRim.y + 1;
			healthEmpty.size(available - 2, 4);
			healthFill.size((available - 2) * health, 4);
			healthShine.size((available - 2) * health, 1);
			status.x = left;
			status.y = healthRim.y + 11;
			status.maxWidth = (int)available;
			status.measure();
			buffs.setRect(left, (float)Math.ceil(status.y + status.height()) + 4, available, 0);
			height = Math.max(PORTRAIT_HEIGHT, buffs.bottom() - y);
			rule.x = x;
			rule.y = y + height + 5;
			rule.size(width, 1);
			height += 10;
		}
	}

	private static class MobBuffs extends BuffIndicator {
		MobBuffs(Mob mob) {
			super(mob);
		}

		@Override
		protected void layout() {
			super.layout();
			int columns = Math.max(1, (int)(width + 2) / (SIZE + 2));
			int count = 0;
			for (int i = 0; i < length; i++) {
				Gizmo child = memberAt(i);
				if (child instanceof Image) {
					Image icon = (Image)child;
					icon.x = x + count % columns * (SIZE + 2);
					icon.y = y + count / columns * (SIZE + 2);
					count++;
				}
			}
			height = count == 0 ? 0 : ((count - 1) / columns + 1) * (SIZE + 2) - 2;
		}
	}
	
	private static class MobTitle extends Component {
		
		private static final int GAP	= 2;
		
		private CharSprite image;
		private BitmapText name;
		private HealthBar health;
		private BuffIndicator buffs;
		
		public MobTitle( Mob mob ) {
			
			name = PixelScene.createText( Utils.capitalize( mob.displayName() ), 9 );
			name.hardlight( titleColor() );
			name.measure();	
			add( name );
			
			image = mob.sprite();
			add( image );
			
			health = new HealthBar();
			health.level( (float)mob.HP / mob.HT );
			add( health );
			
			buffs = new BuffIndicator( mob );
			add( buffs );
		}
		
		@Override
		protected void layout() {
			
			image.x = 0;
			image.y = Math.max( 0, name.height() + GAP + health.height() - image.height );
			
			name.x = image.width + GAP;
			name.y = image.height - health.height() - GAP - name.baseLine();
			
			float w = width - image.width - GAP;
			
			health.setRect( image.width + GAP, image.height - health.height(), w, health.height() ); 
			
			buffs.setPos( 
				name.x + name.width() + GAP, 
				name.y + name.baseLine() - BuffIndicator.SIZE );

			height = health.bottom();
		}
	}
}
