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

import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.items.TomeOfMastery;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndChooseWay extends WndScrollable {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.ARCANE;
    }

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	private static final float GAP		= 2;
	private int windowWidth;
	private Component titlebar;
	private HighlightedText info;

	public WndChooseWay( final TomeOfMastery tome, final HeroSubClass way1, final HeroSubClass way2 ) {

		super();

		final String TXT_MASTERY	= "Which way will you follow?";
		final String TXT_CANCEL		= "I'll decide later";

		Message description = Message.paragraphs(Message.subclass(way1, true), Message.subclass(way2, true), Message.key("custom.generated.entry_which_way_will_you_follow_1a7ef0cad1"));
        menuModel = new MenuModel(Message.itemName(tome), description);
        float bottom = createDescription(tome, description.toString());

		RedButton btnWay1 = new RedButton( Utils.capitalize( way1.title() ) ) {
			@Override
			protected void onClick() {
				hide();
				tome.choose( way1 );
			}
		};
		btnWay1.setRect( 0, bottom + GAP, (windowWidth - GAP) / 2, BTN_HEIGHT );
		add( btnWay1 );
        menuModel.add(btnWay1, Message.capitalize(Message.subclass(way1, false)));

		RedButton btnWay2 = new RedButton( Utils.capitalize( way2.title() ) ) {
			@Override
			protected void onClick() {
				hide();
				tome.choose( way2 );
			}
		};
		btnWay2.setRect( btnWay1.right() + GAP, btnWay1.top(), btnWay1.width(), BTN_HEIGHT );
		add( btnWay2 );
        menuModel.add(btnWay2, Message.capitalize(Message.subclass(way2, false)));

		RedButton btnCancel = new RedButton( TXT_CANCEL ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect( 0, btnWay2.bottom() + GAP, windowWidth, BTN_HEIGHT );
		add( btnCancel );
        menuModel.add(btnCancel, Message.key("windows.wndchooseability.cancel"));

		if (Presentation.reborn()) {
			layoutContent(windowWidth, titlebar, info, GAP, btnWay1, btnWay2, btnCancel);
		} else {
			resize(WIDTH, (int)btnCancel.bottom());
		}
	}

	public WndChooseWay( final TomeOfMastery tome, final HeroSubClass way ) {

		super();

		final String TXT_REMASTERY	= "Do you want to respec into %s?";

		final String TXT_OK		= "Yes, I want to respec";
		final String TXT_CANCEL	= "Maybe later";

		Message description = Message.paragraphs(Message.subclass(way, true),
		        Message.key("custom.generated.entry_do_you_want_to_respec_into_s_f6a910bf74",
		        Message.indefinite(Message.subclass(way, false))));
        menuModel = new MenuModel(Message.itemName(tome), description);
        float bottom = createDescription(tome, description.toString());

		RedButton btnWay = new RedButton( TXT_OK ) {
			@Override
			protected void onClick() {
				hide();
				tome.choose( way );
			}
		};
		btnWay.setRect( 0, bottom + GAP, windowWidth, BTN_HEIGHT );
		add( btnWay );
        menuModel.add(btnWay, Message.key("custom.generated.entry_yes_i_want_to_respec_717c71e80b"));

		RedButton btnCancel = new RedButton( TXT_CANCEL ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect( 0, btnWay.bottom() + GAP, windowWidth, BTN_HEIGHT );
		add( btnCancel );
        menuModel.add(btnCancel, Message.key("custom.generated.entry_maybe_later_27ad1d8334"));

		if (Presentation.reborn()) {
			layoutContent(windowWidth, titlebar, info, GAP, btnWay, btnCancel);
		} else {
			resize(WIDTH, (int)btnCancel.bottom());
		}
	}

	private float createDescription( TomeOfMastery tome, String text ) {
		windowWidth = Presentation.reborn() ? boundedWidth(InspectionTitle.windowWidth(WIDTH)) : WIDTH;
		titlebar = InspectionTitle.create(new ItemSprite(tome.image(), null), tome.name());
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );

		info = new HighlightedText(6);
		info.text(text, Presentation.reborn() ? textWidth(windowWidth) : windowWidth);
		info.setPos(titlebar.left(), titlebar.bottom() + GAP);
		add(info);

		return info.bottom();
	}
}
