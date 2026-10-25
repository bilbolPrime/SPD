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
package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.HashSet;

import com.bilboldev.noosa.Camera;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornCurse;
import com.bilboldev.pixeldungeonskills.effects.particles.SparkParticle;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.traps.LightningTrap;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ShamanSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

public class Shaman extends Mob implements Callback {

	private static final float TIME_TO_ZAP	= 2f;

	private static final String TXT_LIGHTNING_KILLED = "custom.generated.entry_s_s_lightning_bolt_killed_you_7897991f1d";

	{
		name = "gnoll shaman";
		spriteClass = ShamanSprite.class;

		HP = HT = 18;
		defenseSkill = 8;

		EXP = 6;
		maxLvl = 14;

		loot = Generator.Category.SCROLL;
		lootChance = 0.33f;

        name = Dungeon.currentDifficulty.mobPrefix() + name;
        HT *= Dungeon.currentDifficulty.mobHPModifier();
        HP = HT;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 2, 6 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 11;
	}

	@Override
	public int dr() {
		return 4;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return Ballistica.cast( pos, enemy.pos, false, true ) == enemy.pos;
	}

	@Override
	protected boolean doAttack( Char enemy ) {

		if (Level.distance( pos, enemy.pos ) <= 1) {

			return super.doAttack( enemy );

		} else {
			float delay = prepareRebornAttack(enemy, TIME_TO_ZAP);
			RebornChampion champion = buff(RebornChampion.class);
			try {
				boolean visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos];
				if (visible) {
					((ShamanSprite)sprite).zap( enemy.pos );
				}

				spend( delay );

				if (hit( this, enemy, true )) {
					int dmg = Random.Int( 2, 12 );
					if (Level.water[enemy.pos] && !enemy.flying) {
						dmg *= 1.5f;
					}
					dmg = RebornCurse.spellDamage(this, dmg);

					if (enemy == Dungeon.hero || LocalCoop.contains(enemy))
					{
						dmg = adjustHeroDamage((Hero)enemy, dmg);
					}

					enemy.damageFrom( dmg, LightningTrap.LIGHTNING, this );
					if (champion != null) champion.onHit(enemy);

					enemy.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
					enemy.sprite.flash();

					if (enemy == Dungeon.hero || LocalCoop.contains(enemy)) {

						Camera.main.shake( 2, 0.3f );

						if (!enemy.isAlive()) {
							Dungeon.fail( Utils.format( ResultDescriptions.MOB,
								Utils.indefinite( name ), Dungeon.depth ), this );
							GLog.n(TXT_LIGHTNING_KILLED, Message.actor(this));
						}
					}
				} else {
					enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseText() );
				}

				return !visible;
			} finally {
				if (champion != null) champion.finishAttack();
			}
		}
	}

	@Override
	public void call() {
		next();
	}

    @Override
    public int attackProc( Char enemy, int damage ) {
        champEffect(enemy, damage);
        return damage;
    }

	@Override
	public String description() {
		return
			"The most intelligent gnolls can master shamanistic magic. Gnoll shamans prefer " +
			"battle spells to compensate for lack of might, not hesitating to use them " +
			"on those who question their status in a tribe.";
	}

	private static final HashSet<Class<?>> RESISTANCES = new HashSet<Class<?>>();
	static {
		RESISTANCES.add( LightningTrap.Electricity.class );
	}

	@Override
	public HashSet<Class<?>> resistances() {
		return RESISTANCES;
	}
}
