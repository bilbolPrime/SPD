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
package com.bilboldev.pixeldungeonskills.actors;

import java.util.Arrays;
import java.util.HashSet;

import android.util.SparseArray;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Bundlable;
import com.bilboldev.utils.Bundle;

public abstract class Actor implements Bundlable {
	
	public static final float TICK	= 1f;

	public float time;
	
	private int id = 0;
	
	protected abstract boolean act();
	
	protected void spend( float time ) {
		this.time += time;
	}
	
	protected void postpone( float time ) {
		if (this.time < now + time) {
			this.time = now + time;
		}
	}
	
	protected float cooldown() {
		return time - now;
	}
	
	protected void diactivate() {
		time = Float.MAX_VALUE;
	}
	
	protected void onAdd() {}
	
	protected void onRemove() {}
	
	private static final String TIME	= "time";
	private static final String ID		= "id";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( TIME, time );
		bundle.put( ID, id );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		time = bundle.getFloat( TIME );
		id = bundle.getInt( ID );
	}
	
	public int id() {
		if (id > 0) {
			return id;
		} else {
			int max = 0;
			for (Actor a : all) {
				if (a.id > max) {
					max = a.id;
				}
			}
			return (id = max + 1);
		}
	}
	
	// **********************
	// *** Static members ***
	
	private static HashSet<Actor> all = new HashSet<Actor>();
	private static Actor current;
	
	private static SparseArray<Actor> ids = new SparseArray<Actor>();
	
	private static float now = 0;
	
	private static Char[] chars = new Char[Level.LENGTH];
	
	public static void clear() {
		
		now = 0;
		
		Arrays.fill( chars, null );
		all.clear();
		
		ids.clear();
	}
	
	public static void fixTime() {
		
		if (Dungeon.hero != null && all.contains( Dungeon.hero )) {
			Statistics.duration += now;
		}
		
		float min = Float.MAX_VALUE;
		for (Actor a : all) {
			if (a.time < min) {
				min = a.time;
			}
		}
		for (Actor a : all) {
			a.time -= min;
		}
		now = 0;
	}
	
	public static void init() {

		add( Dungeon.hero );
		
		for (Mob mob : Dungeon.level.mobs) {
			add( mob );
		}
		
		for (Blob blob : Dungeon.level.blobs.values()) {
			add( blob );
		}
		
		current = null;
	}
	
	public static void occupyCell( Char ch ) {
		chars[ch.pos] = ch;
	}
	
	public static void freeCell( int pos ) {
		chars[pos] = null;
	}
	
	/*protected*/public void next() {
		if (current == this) {
			current = null;
		}
	}
	
	public static void process() {
		
		if (current != null) {
			return;
		}
	
		boolean doNext;

		do {
			now = Float.MAX_VALUE;
			current = null;
			
			Arrays.fill( chars, null );
			
			for (Actor actor : all) {
				if (actor.time < now) {
					now = actor.time;
					current = actor;
				}
				
				if (actor instanceof Char) {
					Char ch = (Char)actor;
					chars[ch.pos] = ch;
				}
			}

			if (current != null) {
				
				if (current instanceof Char && ((Char)current).sprite != null && ((Char)current).sprite.isMoving) {
					// If it's character's turn to act, but its sprite 
					// is moving, wait till the movement is over
					current = null;
					break;
				}

				//if(current instanceof  Char && ((Char)current).sprite != null)
					doNext = current.act();
				//else doNext = true;
				if (doNext && !Dungeon.hero.isAlive()) {
					doNext = false;
					current = null;
				}
			} else {
				doNext = false;
			}
			
		} while (doNext);
	}
	
	public static void add( Actor actor ) {
		add( actor, now );
	}
	
	public static void addDelayed( Actor actor, float delay ) {
		add( actor, now + delay );
	}
	
	private static void add( Actor actor, float time ) {
		
		if (all.contains( actor )) {
			return;
		}
		
		if (actor.id > 0) {
			ids.put( actor.id,  actor );
		}
		
		all.add( actor );
		actor.time += time;
		actor.onAdd();
		
		if (actor instanceof Char) {
			Char ch = (Char)actor;
			chars[ch.pos] = ch;
			for (Buff buff : ch.buffs()) {
				all.add( buff );
				buff.onAdd();
			}
		}
	}
	
	public static void remove( Actor actor ) {
		
		if (actor != null) {
			all.remove( actor );
			actor.onRemove();
			
			if (actor.id > 0) {
				ids.remove( actor.id );
			}
		}
	}
	
	public static Char findChar( int pos ) {
		return chars[pos];
	}
	
	public static Actor findById( int id ) {
		return ids.get( id );
	}
	
	public static HashSet<Actor> all() {
		return all;
	}
}
