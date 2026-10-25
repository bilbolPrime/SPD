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

import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;

public class WndMessage extends WndScrollable {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;

	private static final int MARGIN = 4;
	private static final int DEFAULT_TEXT_SIZE = 6;

	public WndMessage(Message text) {
        this(text.toString());
        menuModel = new MenuModel(Message.empty(), text);
    }

    public WndMessage( String text ) {

		super();
		String localizedText = Messages.maybeTranslate( text );
		int width = boundedWidth(landscapeLayout() ? WIDTH_L : WIDTH_P);
		BitmapTextMultiline info = PixelScene.createMultiline(localizedText, DEFAULT_TEXT_SIZE);
		info.maxWidth = textWidth(width);
		info.measure();
		layoutContent(Math.min(width, (int)Math.ceil(info.width()) + SCROLL_GUTTER),
				null, textContent(info), MARGIN);
	}
}
