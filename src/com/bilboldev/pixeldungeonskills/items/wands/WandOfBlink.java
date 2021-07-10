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
import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MirrorImage;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfTeleportation;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class WandOfBlink extends Wand {

	{
		name = "Wand of Blink";
	}

	static final String AC_AFTER_IMAGE = "CREATE AFTER IMAGE";
	static final int AC_AFTER_IMAGE_COST = 80;

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if(Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.MAGE){
			actions.add(AC_AFTER_IMAGE);
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_AFTER_IMAGE )) {

			if(hero.MP >= AC_AFTER_IMAGE_COST){
				curUser = hero;
				curItem = this;
				hero.MP -= AC_AFTER_IMAGE_COST;

				ArrayList<Integer> respawnPoints = new ArrayList<Integer>();

				for (int i = 0; i < Level.NEIGHBOURS8.length; i++) {
					int p = curUser.pos + Level.NEIGHBOURS8[i];
					if (Actor.findChar( p ) == null && (Level.passable[p] || Level.avoid[p])) {
						respawnPoints.add( p );
					}
				}

				int nImages = 1;
				while (nImages > 0 && respawnPoints.size() > 0) {
					int index = Random.index( respawnPoints );

					MirrorImage mob = new MirrorImage();
					mob.duplicate( curUser );
					GameScene.add( mob );
					WandOfBlink.appear( mob, respawnPoints.get( index ) );

					respawnPoints.remove( index );
					nImages--;
				}
			}
			else {
				GLog.n( "You need at least " + AC_AFTER_IMAGE_COST + " mana to use " + AC_AFTER_IMAGE );
			}

		} else {
			super.execute( hero, action );
		}
	}

	@Override
	protected void onZap( int cell ) {

		int level = power();
		
		if (Ballistica.distance > level + 4) {
			cell = Ballistica.trace[level + 3];
		} else if (Actor.findChar( cell ) != null && Ballistica.distance > 1) {
			cell = Ballistica.trace[Ballistica.distance - 2];
		}
		
		curUser.sprite.visible = true;
		appear( Dungeon.hero, cell );
		Dungeon.observe();
	}
	
	@Override
	protected void fx( int cell, Callback callback ) {
		MagicMissile.whiteLight( curUser.sprite.parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
		curUser.sprite.visible = false;
	}
	
	public static void appear( Char ch, int pos ) {
		
		ch.sprite.interruptMotion();
		
		ch.move( pos );
		ch.sprite.place( pos );
		
		if (ch.invisible == 0) {
			ch.sprite.alpha( 0 );
			ch.sprite.parent.add( new AlphaTweener( ch.sprite, 1, 0.4f ) );
		}
		
		ch.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
		Sample.INSTANCE.play( Assets.SND_TELEPORT );
	}
	
	@Override
	public String desc() {
		return
			"This wand will allow you to teleport in the chosen direction. " +
			"Creatures and inanimate obstructions will block the teleportation.\n\n" +
					highlight("Mages adept with magic can infuse their spiritual energy with the wand to create an image of themselves!") + "\n\n" +
					highlight("Create After Image: Infuses  " + AC_AFTER_IMAGE_COST + " mana into the wand. The wand will create an image of the mage.");
	}
}
