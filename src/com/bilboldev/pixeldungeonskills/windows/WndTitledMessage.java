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

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;

public class WndTitledMessage extends WndScrollable {

	private static final int WIDTH_P	= 120;
	private static final int WIDTH_L	= 144;

	private static final int GAP	= 2;
	private static final int DEFAULT_TEXT_SIZE = 6;

	public WndTitledMessage(Image icon, Message title, Message message) {
        this(icon, title.toString(), message.toString());
        menuModel = new MenuModel(title, message);
    }

    public WndTitledMessage( Image icon, String title, String message ) {

		this( new IconTitle( icon, title ), message );

	}

	public WndTitledMessage( Component titlebar, String message ) {
		this(titlebar, message, 0);
	}

	protected WndTitledMessage( Component titlebar, String message, int preferredWidth ) {

		super();

		if (preferredWidth <= 0) {
			preferredWidth = landscapeLayout() ? WIDTH_L : WIDTH_P;
		}
		int width = boundedWidth(preferredWidth);

		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		HighlightedText text = new HighlightedText(DEFAULT_TEXT_SIZE);
		text.text(message, textWidth(width));
		layoutContent(width, titlebar, text, GAP);
	}
}
