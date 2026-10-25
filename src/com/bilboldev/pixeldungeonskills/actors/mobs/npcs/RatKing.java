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
package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.sprites.RatKingSprite;

public class RatKing extends NPC {

	{
		name = "rat king";
		spriteClass = RatKingSprite.class;

		state = SLEEPEING;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}

	@Override
	public float speed() {
		return 2f;
	}

	@Override
	protected Char chooseEnemy() {
		return null;
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public void add( Buff buff ) {
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public void interact() {
		sprite.turnTo( pos, Dungeon.hero.pos );
		if (state == SLEEPEING) {
			notice();
			yell(Message.key(RatKing.class, "not_sleeping"));
			state = WANDERING;
		} else {
			yell(Message.key(RatKing.class, "what_is_it"));
		}
	}

	@Override
	public String displayName() {
		return com.bilboldev.pixeldungeonskills.messages.Messages.get( RatKing.class, "name" );
	}

	@Override
	public String description() {
		return com.bilboldev.pixeldungeonskills.messages.Messages.get( RatKing.class, "desc" );
	}
}
