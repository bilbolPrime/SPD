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
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.Regrowth;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Callback;

import java.util.ArrayList;

public class WandOfRegrowth extends Wand {

	{
		name = "Wand of Regrowth";
	}

	static final String AC_SUMMON = "Nature's Blessing";
	static final int AC_SUMMON_COST = 40;

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if(Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.MAGE){
			actions.add(AC_SUMMON);
		}
		return actions;
	}

	@Override
	protected void onZap( int cell ) {
		
		for (int i=1; i < Ballistica.distance-1; i++) {
			int p = Ballistica.trace[i];
			int c = Dungeon.level.map[p];
			if (c == Terrain.EMPTY || 
				c == Terrain.EMBERS || 
				c == Terrain.EMPTY_DECO) {
				
				Level.set( p, Terrain.GRASS );
				GameScene.updateMap( p );
				if (Dungeon.visible[p]) {
					GameScene.discoverTile( p, c );
				}
				
			}
		}
		
		int c = Dungeon.level.map[cell];
		if (c == Terrain.EMPTY || 
			c == Terrain.EMBERS || 
			c == Terrain.EMPTY_DECO || 
			c == Terrain.GRASS ||
			c == Terrain.HIGH_GRASS) {
			
			GameScene.add( Blob.seed( cell, (power() + 2) * 20, Regrowth.class ) );
			
		} else {
			
			GLog.i( "nothing happened" );
			
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_SUMMON )) {

			if(hero.MP >= AC_SUMMON_COST){
				curUser = hero;
				curItem = this;
				hero.MP -= AC_SUMMON_COST;

				Plant.Seed seed = (Plant.Seed) Generator.random( Generator.Category.SEED );
				GLog.p( "Nature has blessed you with a "  + seed.name());
				Dungeon.level.drop( seed, hero.pos ).sprite.drop();
			}
			else {
				GLog.n( "You need at least " + AC_SUMMON_COST + " mana to use " + AC_SUMMON );
			}

		} else {
			super.execute( hero, action );
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.foliage( curUser.sprite.parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
		return
			"\"When life ceases new life always begins to grow... The eternal cycle always remains!\"\n\n" +
					highlight("Mages adept with magic can infuse their spiritual energy with the wand to forge a random seed!") + "\n\n" +
					highlight("Nature's Blessing: Infuses  " + AC_SUMMON_COST + " mana into the wand. The wand will create a random seed for the mage.");
	}
}
