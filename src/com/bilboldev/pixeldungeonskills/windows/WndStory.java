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

import com.bilboldev.input.Touchscreen.Touch;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.TouchArea;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.messages.Languages;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class WndStory extends Window {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.PARCHMENT;
    }

	private static final int WIDTH = 120;
	private static final int MARGIN = 6;

	private static final float bgR	= 0.77f;
	private static final float bgG	= 0.73f;
	private static final float bgB	= 0.62f;

	public static final int ID_SEWERS		= 0;
	public static final int ID_PRISON		= 1;
	public static final int ID_CAVES		= 2;
	public static final int ID_METROPOLIS	= 3;
	public static final int ID_HALLS		= 4;

	private BitmapTextMultiline tf;

	private float delay;

	public WndStory( String text ) {
		super( 0, 0, Chrome.get( Chrome.Type.SCROLL ) );

		tf = PixelScene.createMultiline( text, 7 );
		tf.maxWidth = WIDTH - MARGIN * 2;
		tf.measure();
		tf.ra = bgR;
		tf.ga = bgG;
		tf.ba = bgB;
		tf.rm = -bgR;
		tf.gm = -bgG;
		tf.bm = -bgB;
		if (com.bilboldev.pixeldungeonskills.Presentation.reborn()) {
			tf.resetColor();
			tf.hardlight(0xE8D8BE);
		}
		tf.x = MARGIN;
		add( tf );

		add( new TouchArea( chrome ) {
			@Override
			protected void onClick( Touch touch ) {
				hide();
			}
		} );

		resize( (int)(tf.width() + MARGIN * 2), (int)Math.min( tf.height(), 180 ) );
	}

	@Override
	public void update() {
		super.update();

		if (delay > 0 && (delay -= Game.elapsed) <= 0) {
			shadow.visible = chrome.visible = tf.visible = true;
		}
	}

	private static String chapterBodyKey( int id ) {
		switch (id) {
		case ID_SEWERS:
			return "journal.document.intros.sewers.body";
		case ID_PRISON:
			return "journal.document.intros.prison.body";
		case ID_CAVES:
			return "journal.document.intros.caves.body";
		case ID_METROPOLIS:
			return "journal.document.intros.city.body";
		case ID_HALLS:
			return "journal.document.intros.halls.body";
		default:
			return null;
		}
	}

	public static void showChapter( int id ) {

		if (Dungeon.chapters.contains( id )) {
			return;
		}

		String bodyKey = chapterBodyKey( id );
		String text = bodyKey == null ? null : Messages.get( bodyKey );
		if (text != null && !Messages.NO_TEXT_FOUND.equals( text )) {
			WndStory wnd = new WndStory( text );
			if ((wnd.delay = 0.6f) > 0) {
				wnd.shadow.visible = wnd.chrome.visible = wnd.tf.visible = false;
			}

			Game.scene().add( wnd );

			Dungeon.chapters.add( id );
		}
	}

    public static void showStory( String text ) {
		String localized = text == null ? null : Messages.maybeTranslate( text );
		if (localized != null && (Messages.lang() == Languages.ENGLISH || !localized.equals( text ))) {
			WndStory wnd = new WndStory( localized );
            if ((wnd.delay = 0.6f) > 0) {
                wnd.shadow.visible = wnd.chrome.visible = wnd.tf.visible = false;
            }

            Game.scene().add( wnd );
        }
    }
}
