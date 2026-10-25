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
package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.pixeldungeonskills.RebornChallenges;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.RunSeed;
import com.bilboldev.pixeldungeonskills.ui.ChallengeIcons;
import com.bilboldev.pixeldungeonskills.ui.SeedDisplay;

import com.bilboldev.input.Touchscreen;
import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Button;
import com.bilboldev.pixeldungeonskills.Rankings;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.pixeldungeonskills.windows.WndError;
import com.bilboldev.pixeldungeonskills.windows.WndRanking;

public class RankingsScene extends TitleScene {
    @Override
    protected Page initialPage() {
        return Page.RANKINGS;
    }

	public static class Record extends Button {

		private static final float GAP	= 4;

		private static final int TEXT_WIN	= 0xFFFF88;
		private static final int TEXT_LOSE	= 0xCCCCCC;

		private Rankings.Record rec;

		private ItemSprite shield;
		private ColorBlock latestMark;
		private BitmapText position;
		private BitmapTextMultiline desc;
		private Image classIcon;
        private ChallengeIcons challenges;
        private SeedDisplay seed;

		public Record( int pos, boolean latest, Rankings.Record rec ) {
			super();

			this.rec = rec;
            if (rec.rules.mode == GameplayMode.REBORN) {
                challenges = new ChallengeIcons(rec.challenges & RebornChallenges.ALL, 12,
                        5, true);
                challenges.useScrollPaneInput();
                add(challenges);
                seed = new SeedDisplay(rec.seed(), RunSeed.SYMBOLS, false);
                seed.useScrollPaneInput();
                add(seed);
            }

			if (latest) {
				latestMark = new ColorBlock(2, 1, rec.win ? 0xFFE1CB8E : 0xFF9D87B9);
				addToBack(latestMark);
			}

			position.text( Integer.toString( pos+1 ) );
			position.measure();

			desc.text(rec.rules.mode.title() + ": " + rec.desc(), false);
			desc.measure();

			if (rec.win) {
				shield.view( ItemSpriteSheet.AMULET, null );
				position.hardlight( TEXT_WIN );
				desc.hardlight( TEXT_WIN );
			} else {
				position.hardlight( TEXT_LOSE );
				desc.hardlight( TEXT_LOSE );
			}

			classIcon.copy( Icons.get( rec.heroClass ) );
		}

		@Override
		protected void createChildren() {

			super.createChildren();

			shield = new ItemSprite( ItemSpriteSheet.TOMB, null );
			add( shield );

			position = new BitmapText( PixelScene.font1x );
			add( position );

			desc = createMultiline( 9 );
			add( desc );

			classIcon = new Image();
			add( classIcon );
		}

		@Override
		protected void layout() {
			float iconWidth = Math.max(shield.width(), classIcon.width());
			float iconHeight = shield.height() + GAP + classIcon.height();
			desc.x = align(x + iconWidth + GAP * 3);
			desc.maxWidth = (int)(right() - desc.x - GAP);
			desc.measure();
            if (challenges != null) challenges.fitWidth(right() - desc.x - GAP);
            height = challenges == null ? Math.max(39, desc.height() + 12)
                    : desc.height() + challenges.height() + 39;
			height = Math.max(height, iconHeight + 12);
			super.layout();

			shield.x = align(x + (iconWidth - shield.width()) / 2);
			shield.y = align(y + (height - iconHeight) / 2);

			position.x = align( shield.x + (shield.width - position.width()) / 2 );
			position.y = align( shield.y + (shield.height - position.height()) / 2 + 1 );

			if (latestMark != null) {
				latestMark.x = x - 3;
                latestMark.y = y + 5;
                latestMark.size(2, height - 10);
			}

			classIcon.x = align(x + (iconWidth - classIcon.width()) / 2);
			classIcon.y = align(shield.y + shield.height() + GAP);

			desc.y = align(y + (height - desc.height()) / 2);
            if (challenges != null) {
                desc.y = y + 6;
                challenges.setPos(desc.x, desc.y + desc.height() + 4);
                seed.setRect(desc.x, challenges.bottom() + 3, challenges.width(), 20);
            }
		}

		@Override
		protected void onClick() {
			openDetails();
		}

		void useScrollPaneInput() {
			Touchscreen.event.remove(hotArea);
		}

		void openDetails() {
			if (rec.gameFile.length() > 0) {
				com.bilboldev.noosa.Game.scene().add( new WndRanking( rec.gameFile ) );
			} else {
				com.bilboldev.noosa.Game.scene().add( new WndError( Messages.get( RankingsScene.class, "no_info" ) ) );
			}
		}

        void open(float x, float y) {
            if (seed != null && seed.inside(x, y)) seed.open();
            else if (challenges != null && challenges.inside(x, y)) challenges.open();
            else openDetails();
        }
	}
}
