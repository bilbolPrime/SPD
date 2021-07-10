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
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.particles.FlameParticle;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class WandOfFirebolt extends Wand {

	{
		name = "Wand of Firebolt";
	}

	static final String AC_SUMMON = "SUMMON THE FLAMES";
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

		int level = power();
		
		for (int i=1; i < Ballistica.distance - 1; i++) {
			int c = Ballistica.trace[i];
			if (Level.flamable[c]) {
				GameScene.add( Blob.seed( c, 1, Fire.class ) );
			}
		}
		
		GameScene.add( Blob.seed( cell, 1, Fire.class ) );
					
		Char ch = Actor.findChar( cell );
		if (ch != null) {	
			
			ch.damage( ((int)(wandBonusDamageModifier() * Random.Int( 1, (int)(8 + level * level))) ), this );
			Buff.affect( ch, Burning.class ).reignite( ch );

			ch.sprite.emitter().burst( FlameParticle.FACTORY, 5 );
			
			if (ch == curUser && !ch.isAlive()) {
				Dungeon.fail( Utils.format( ResultDescriptions.WAND, name, Dungeon.depth ) );
				GLog.n( "You killed yourself with your own Wand of Firebolt..." );
			}
		}
	}
	
	protected void fx( int cell, Callback callback ) {
		MagicMissile.fire( curUser.sprite.parent, curUser.pos, cell, callback );
		Sample.INSTANCE.play( Assets.SND_ZAP );
	}
	
	@Override
	public String desc() {
		return
			"This wand unleashes bursts of magical fire. It will ignite " +
			"flammable terrain, and will damage and burn a creature it hits.\n\n" +
					highlight("Mages adept with magic can infuse their spiritual energy with the wand to summon mighty Fire Elementals!") + "\n\n"
					+ highlight("Summon Fire Elemental: Drains all mana (at least " + AC_SUMMON_COST + "); summons a Fire Elemental. Fire Elemental's strength depends on mana used and are subject to summon limit. ");
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_SUMMON )) {

			if(hero.MP >= AC_SUMMON_COST){
				curUser = hero;
				curItem = this;

				boolean spawned = false;
				for (int nu = 0; nu < 1 ; nu++) {
					int newPos = hero.pos;
					if (Actor.findChar(newPos) != null) {
						ArrayList<Integer> candidates = new ArrayList<Integer>();
						boolean[] passable = Level.passable;

						for (int n : Level.NEIGHBOURS4) {
							int c = hero.pos + n;
							if(c < 0 || c >= Level.passable.length)
								continue;
							if (passable[c] && Actor.findChar(c) == null) {
								candidates.add(c);
							}
						}
						newPos = candidates.size() > 0 ? Random.element(candidates) : -1;
						if (newPos != -1) {
							spawned = true;
							SummonedPet crab = new SummonedPet(SummonedPet.PET_TYPES.FIRE_ELEMENTAL);
							crab.spawn(1 + (hero.MP - AC_SUMMON_COST) / 30);
							crab.pos = newPos;
							GameScene.add(crab);
							Actor.addDelayed(new Pushing(crab, hero.pos, newPos), -1);
							crab.sprite.alpha(0);
							crab.sprite.parent.add(new AlphaTweener(crab.sprite, 1, 0.15f));
						}
					}
				}

				int managUsed = hero.MP;
				if(spawned == true) {
					hero.MP = 0;
					StatusPane.manaDropping += managUsed;
					Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "The flames obey");
					hero.spend( 1f );
					hero.busy();
					hero.sprite.operate( hero.pos );
				}
			}
			else {
				GLog.n( "You need at least " + AC_SUMMON_COST + " mana to use " + AC_SUMMON );
			}

		} else {
			super.execute( hero, action );
		}
	}
}
