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
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Wandmaker;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class WndWandmaker extends WndQuest {

	private static final String TXT_MESSAGE	=
		"Oh, I see you have succeeded! I do hope it hasn't troubled you too much. " +
		"As I promised, you can choose one of my high quality wands.";
	private static final String TXT_BATTLE		= "Battle wand";
	private static final String TXT_NON_BATTLE	= "Non-battle wand";

	private static final String TXT_FARAWELL	= "Good luck in your quest, %s!";

	private Wandmaker wandmaker;
	private Item questItem;

	public WndWandmaker( final Wandmaker wandmaker, final Item item ) {

		super(wandmaker,
		        Message.key("custom.generated.entry_oh_i_see_you_have_succeeded_i_do_hope_it_hasn_t_391b217b15"),
		        Message.key("custom.generated.entry_battle_wand_86e26294c3"),
		        Message.key("custom.generated.entry_non_battle_wand_62723a978c"));

		this.wandmaker = wandmaker;
		questItem = item;
	}

	@Override
	protected void onSelect( int index ) {

		questItem.detach( Dungeon.hero.belongings.backpack );

		Item reward = index == 0 ? Wandmaker.Quest.wand1 : Wandmaker.Quest.wand2;
		reward.identify();
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i("custom.generated.entry_you_now_have_s_f58f5ff690", Message.itemName(reward));
		} else {
			Dungeon.level.drop( reward, wandmaker.pos ).sprite.drop();
		}

		wandmaker.yell(Message.key("windows.wndwandmaker.farewell", Message.hero(Dungeon.hero)));
		wandmaker.destroy();

		wandmaker.sprite.die();

		Wandmaker.Quest.complete();
	}
}
