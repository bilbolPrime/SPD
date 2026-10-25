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
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import java.util.ArrayList;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndQuest extends WndScrollable {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.PARCHMENT;
    }

	private static final int WIDTH_P	= 120;
	private static final int WIDTH_L	= 144;

	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;
	private static final int DEFAULT_TEXT_SIZE = 6;

	public WndQuest( NPC questgiver, Message text, Message... options ) {

		super();
        menuModel = new MenuModel(Message.capitalize(Message.actor(questgiver)), text);

		int width = boundedWidth(InspectionTitle.windowWidth(landscapeLayout() ? WIDTH_L : WIDTH_P));

		Component titlebar = InspectionTitle.create(questgiver.sprite(), Utils.capitalize(questgiver.name));
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		HighlightedText info = new HighlightedText(DEFAULT_TEXT_SIZE);
		info.text(text.toString(), textWidth(width));
		ArrayList<RedButton> buttons = new ArrayList<>();
		for (int i = 0; i < options.length; i++) {
			final int index = i;
			RedButton button = new ActionButton(options[i].toString()) {
				@Override
				protected void onClick() {
					hide();
					onSelect(index);
				}
			};
			button.setSize(width, BTN_HEIGHT);
			buttons.add(button);
            menuModel.add(button, options[i]);
		}
		layoutContent(width, titlebar, info, GAP, buttons.toArray(new RedButton[0]));
	}

	protected void onSelect( int index ) {};
}
