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

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.RatKingSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatScoutSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.utils.Random;

import java.util.HashSet;

public class RatScout extends NPC {

	{
		name = "rat scout";
		spriteClass = RatScoutSprite.class;
		
		state = WANDERING;
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1;
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
	public void add( Buff buff ) {
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public void interact() {
		int curPos = pos;

		moveSprite( pos, Dungeon.hero.pos );
		move( Dungeon.hero.pos );

		Dungeon.hero.sprite.move( Dungeon.hero.pos, curPos );
		Dungeon.hero.move( curPos );

		Dungeon.hero.spend( 1 / Dungeon.hero.speed() );
		Dungeon.hero.busy();
	}

	@Override
	public boolean act(){
		boolean result = super.act();

		try
		{
			Dungeon.level.heroFOV[pos] = true;
			Dungeon.level.heroFOV[pos + 1] = true;
			Dungeon.level.heroFOV[pos - 1] = true;
			Dungeon.level.heroFOV[pos + Dungeon.level.WIDTH + 1] = true;
			Dungeon.level.heroFOV[pos + Dungeon.level.WIDTH - 1] = true;
			Dungeon.level.heroFOV[pos - Dungeon.level.WIDTH + 1] = true;
			Dungeon.level.heroFOV[pos - Dungeon.level.WIDTH - 1] = true;
			Dungeon.level.heroFOV[pos + Dungeon.level.WIDTH] = true;
			Dungeon.level.heroFOV[pos - Dungeon.level.WIDTH] = true;

			Char tauntedEnemy = tauntEnemy();
			if(tauntedEnemy != null ){
				if (tauntedEnemy instanceof Mob) {
					((Mob)tauntedEnemy).aggro( this );
				}
			}

			Dungeon.observe();
		}
		catch (Exception e){

		}

		return result;
	}

	@Override
	public String description() {
		return 
			"A poorly fed rodent. It's smaller than a usual rat but still quite loud...";
	}

	protected Char tauntEnemy() {

		HashSet<Mob> enemies = new HashSet<Mob>();
		for (Mob mob:Dungeon.level.mobs) {
			if (mob.hostile && Level.fieldOfView[mob.pos] && distance(mob) < 3) {
				enemies.add( mob );
			}
		}

		return enemies.size() > 0 ? Random.element( enemies ) : null;
	}
}
