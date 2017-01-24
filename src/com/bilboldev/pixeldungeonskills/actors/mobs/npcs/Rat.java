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
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.HashSet;

/* Retired class, use SummonedPet.java*/
public class Rat extends NPC {
	
	{
		name = "Summoned Rat";
		spriteClass = RatSprite.class;
		
		viewDistance = 4;
		
		WANDERING = new Wandering();
		
		flying = false;
		state = WANDERING;
	}

	private int level;
	
	private static final String LEVEL	= "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		spawn( bundle.getInt( LEVEL ) );
	}
	
	public void spawn( int level ) {
		this.level = level;
		
		HT = (3 + level) * 2;
		defenseSkill = 1 + level;
	}
	
	@Override
	public int attackSkill( Char target ) {
		return defenseSkill;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( HT / 10, HT / 4 );
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		if (enemy instanceof Mob) {
			((Mob)enemy).aggro( this );
		}
		return damage;
	}
	
	@Override
	protected boolean act() {
		HP--;
		if (HP <= 0) {
			die( null );
			return true;
		} else {
			return super.act();
		}
	}
	
	protected Char chooseEnemy() {
		
		if (enemy == null || !enemy.isAlive()) {
			HashSet<Mob> enemies = new HashSet<Mob>();
			for (Mob mob:Dungeon.level.mobs) {
				if (mob.hostile && Level.fieldOfView[mob.pos]) {
					enemies.add( mob );
				}
			}
			
			return enemies.size() > 0 ? Random.element( enemies ) : null;
			
		} else {
			
			return enemy;
			
		}
	}
	
	@Override
	public String description() {
		return
			"Summoned rats will protect their master mage.";
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
	
	private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
	static {
		IMMUNITIES.add( Poison.class );
	}
	
	@Override
	public HashSet<Class<?>> immunities() {
		return IMMUNITIES;
	}
	
	private class Wandering implements AiState {

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV) {
				
				enemySeen = true;
				
				notice();
				state = HUNTING;
				target = enemy.pos;
				
			} else {
				
				enemySeen = false;
				
				int oldPos = pos;
				if (getCloser( Dungeon.hero.pos )) {
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					spend( TICK );
				}
				
			}
			return true;
		}
		
		@Override
		public String status() {
			return Utils.format( "This %s is wandering", name );
		}
	}
}