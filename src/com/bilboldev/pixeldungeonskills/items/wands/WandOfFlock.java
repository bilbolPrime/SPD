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
package com.bilboldev.pixeldungeonskills.items.wands;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.SheepSprite;
import com.bilboldev.pixeldungeonskills.utils.BArray;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.PathFinder;
import com.bilboldev.utils.Random;

public class WandOfFlock extends Wand {

	{
		name = "Wand of Flock";
	}
	
	@Override
	protected void onZap( int cell ) {
		
		int level = power();
		
		int n = level + 2;
		
		if (Actor.findChar( cell ) != null && Ballistica.distance > 2) {
			cell = Ballistica.trace[Ballistica.distance - 2];
		}
		
		boolean[] passable = BArray.or( Level.passable, Level.avoid, null );
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				passable[((Char)actor).pos] = false;
			}
		}
		
		PathFinder.buildDistanceMap( cell, passable, n );
		int dist = 0;
		
		if (Actor.findChar( cell ) != null) {
			PathFinder.distance[cell] = Integer.MAX_VALUE;
			dist = 1;
		}
		
		float lifespan = level + 3;
		
	sheepLabel:
		for (int i=0; i < n; i++) {
			do {
				for (int j=0; j < Level.LENGTH; j++) {
					if (PathFinder.distance[j] == dist) {
						
						Sheep sheep = new Sheep();
						sheep.lifespan = lifespan;
						sheep.pos = j;
						GameScene.add( sheep );
						Dungeon.level.mobPress( sheep );
						
						CellEmitter.get( j ).burst( Speck.factory( Speck.WOOL ), 4 );
						
						PathFinder.distance[j] = Integer.MAX_VALUE;
						
						continue sheepLabel;
					}
				}
				dist++;
			} while (dist < n);
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.wool( curUser.sprite.parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
		return 
			"A flick of this wand summons a flock of magic sheep, creating temporary impenetrable obstacle.";
	}
	
	public static class Sheep extends NPC {
		
		private static final String[] QUOTES = {"Baa!", "Baa?", "Baa.", "Baa..."};
		
		{
			name = "sheep";
			spriteClass = SheepSprite.class;
		}
		
		public float lifespan;
		
		private boolean initialized = false;
		
		@Override
		protected boolean act() {
			if (initialized) {
				HP = 0;

				destroy();
				sprite.die();
				
			} else {
				initialized = true;
				spend( lifespan + Random.Float( 2 ) );
			}
			return true;
		}
		
		@Override
		public void damage( int dmg, Object src ) {
		}
		
		@Override
		public String description() {
			return 
				"This is a magic sheep. What's so magical about it? You can't kill it. " +
				"It will stand there until it magcially fades away, all the while chewing cud with a blank stare.";
		}

		@Override
		public void interact() {
			yell( Random.element( QUOTES ) );
		}
	}
}
