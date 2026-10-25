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
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.utils.GLog;

public class WndSadGhost extends WndQuest {

	private static final String TXT_WEAPON	= "Ghost's weapon";
	private static final String TXT_ARMOR	= "Ghost's armor";

	private Ghost ghost;
	private Item questItem;

	public WndSadGhost( final Ghost ghost, final Item item, Message text ) {

		super(ghost, text, Message.key("custom.generated.entry_ghost_s_weapon_2c5ae5f6d3"), Message.key("custom.generated.entry_ghost_s_armor_5e20a9d2c0"));

		this.ghost = ghost;
		questItem = item;
	}

	@Override
	protected void onSelect( int index ) {

		if (questItem != null) {
			questItem.detach( Dungeon.hero.belongings.backpack );
		}

		Item reward = index == 0 ? Ghost.Quest.weapon : Ghost.Quest.armor;
		if (reward.doPickUp( Dungeon.hero )) {
			GLog.i("custom.generated.entry_you_now_have_s_f58f5ff690", Message.itemName(reward));
		} else {
			Dungeon.level.drop( reward, ghost.pos ).sprite.drop();
		}

		ghost.yell(Message.key("windows.wndsadghost.farewell"));
		ghost.die( null );

		Ghost.Quest.complete();
	}
}
