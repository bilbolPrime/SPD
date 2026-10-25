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
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.quest.DwarfToken;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;

public class WndImp extends WndScrollable {

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.PARCHMENT;
    }

	private static final String TXT_MESSAGE	=
		"Oh yes! You are my hero!\n" +
		"Regarding your reward, I don't have cash with me right now, but I have something better for you. " +
		"This is my family heirloom ring: my granddad took it off a dead paladin's finger.";
	private static final String TXT_REWARD		= "Take the ring";

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;

	public WndImp( final Imp imp, final DwarfToken tokens ) {

		super();
        menuModel = new MenuModel(Message.actor(imp), Message.key("windows.wndimp.message"));
		int windowWidth = Presentation.reborn() ? boundedWidth(InspectionTitle.windowWidth(WIDTH)) : WIDTH;

		Component titlebar = Presentation.reborn()
				? InspectionTitle.create(imp.sprite(), Utils.capitalize(imp.name))
				: new IconTitle(new ItemSprite(tokens.image(), null), Utils.capitalize(tokens.name()));
		titlebar.setRect( 0, 0, windowWidth, 0 );
		add( titlebar );

		BitmapTextMultiline message = PixelScene.createMultiline( TXT_MESSAGE, 6 );
		message.maxWidth = Presentation.reborn() ? textWidth(windowWidth) : windowWidth;
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );

		RedButton btnReward = new RedButton( TXT_REWARD ) {
			@Override
			protected void onClick() {
				takeReward( imp, tokens, Imp.Quest.reward );
			}
		};
		btnReward.setRect( 0, message.y + message.height() + GAP, windowWidth, BTN_HEIGHT );
		add( btnReward );
        menuModel.add(btnReward, Message.key("windows.wndimp.reward"));

		if (Presentation.reborn()) {
			layoutContent(windowWidth, titlebar, textContent(message), GAP, btnReward);
		} else {
			resize(WIDTH, (int)btnReward.bottom());
		}
	}

	private void takeReward( Imp imp, DwarfToken tokens, Item reward ) {

		hide();

		tokens.detachAll( Dungeon.hero.belongings.backpack );

		reward.identify();
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i("custom.generated.entry_you_now_have_s_f58f5ff690", Message.itemName(reward));
		} else {
			Dungeon.level.drop( reward, imp.pos ).sprite.drop();
		}

		imp.flee();

		Imp.Quest.complete();
	}
}
