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
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.Rankings;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Ankh;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.RedButton;

public class WndResurrect extends WndScrollable {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.ARCANE;
    }

	private static final String TXT_MESSAGE	= "You died, but you were given another chance to win this dungeon. Will you take it?";
	private static final String TXT_YES		= "Yes, I will fight!";
	private static final String TXT_NO		= "No, I give up";

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final float GAP		= 2;

	public static WndResurrect instance;
	public static Object causeOfDeath;

	public WndResurrect( final Ankh ankh, Object causeOfDeath ) {

		super();
		int windowWidth = Presentation.reborn() ? boundedWidth(InspectionTitle.windowWidth(WIDTH)) : WIDTH;
		final Hero fallen = Dungeon.hero;
        menuModel = new MenuModel(Message.itemName(ankh), Message.key("custom.generated.entry_you_died_but_you_were_given_another_chance_to_wi_2f7599cafc"));

		instance = this;
		WndResurrect.causeOfDeath = causeOfDeath;

		Component titlebar = InspectionTitle.create(new ItemSprite(ankh.image(), null), ankh.name());
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );

		BitmapTextMultiline message = PixelScene.createMultiline( TXT_MESSAGE, 6 );
		message.maxWidth = Presentation.reborn() ? textWidth(windowWidth) : windowWidth;
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );

		RedButton btnYes = new RedButton( TXT_YES ) {
			@Override
			protected void onClick() {
				hide();
				if (LocalCoop.contains(fallen)) LocalCoop.activate(fallen);

				Statistics.ankhsUsed++;

				InterlevelScene.mode = InterlevelScene.Mode.RESURRECT;
				Game.switchScene( InterlevelScene.class );
			}
		};
		btnYes.setRect( 0, message.y + message.height() + GAP, windowWidth, BTN_HEIGHT );
		add( btnYes );
        menuModel.add(btnYes, Message.key("custom.generated.entry_yes_i_will_fight_0410cf6a13"));

		RedButton btnNo = new RedButton( TXT_NO ) {
			@Override
			protected void onClick() {
				hide();
				if (LocalCoop.contains(fallen)) LocalCoop.activate(fallen);

				Rankings.INSTANCE.submit( false );
				Hero.reallyDie( WndResurrect.causeOfDeath );
			}
		};
		btnNo.setRect( 0, btnYes.bottom() + GAP, windowWidth, BTN_HEIGHT );
		add( btnNo );
        menuModel.add(btnNo, Message.key("custom.generated.entry_no_i_give_up_5b6104911b"));

		if (Presentation.reborn()) {
			layoutContent(windowWidth, titlebar, textContent(message), GAP, btnYes, btnNo);
		} else {
			resize(WIDTH, (int)btnNo.bottom());
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		instance = null;
	}

	@Override
	public void onBackPressed() {
	}
}
