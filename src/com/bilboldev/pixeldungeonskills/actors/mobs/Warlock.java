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

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornCurse;
import com.bilboldev.pixeldungeonskills.actors.buffs.Weakness;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Death;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.WarlockSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;

public class Warlock extends Mob implements Callback {

	private static final float TIME_TO_ZAP	= 1f;

	private static final String TXT_SHADOWBOLT_KILLED = "custom.generated.entry_s_s_shadow_bolt_killed_you_fccbd3d9bd";

	{
		name = "dwarf warlock";
		spriteClass = WarlockSprite.class;

		HP = HT = 70;
		defenseSkill = 18;

		EXP = 11;
		maxLvl = 21;

		loot = Generator.Category.POTION;
		lootChance = 0.83f;

        name = Dungeon.currentDifficulty.mobPrefix() + name;
        HT *= Dungeon.currentDifficulty.mobHPModifier();
        HP = HT;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 12, 20 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 25;
	}

	@Override
	public int dr() {
		return 8;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return Ballistica.cast( pos, enemy.pos, false, true ) == enemy.pos;
	}

	protected boolean doAttack( Char enemy ) {

		if (Level.adjacent( pos, enemy.pos )) {

			return super.doAttack( enemy );

		} else {
			RebornChampion champion = buff(RebornChampion.class);
			if (champion != null) champion.beginAttack(enemy);
			boolean visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos];
			if (visible) {
				((WarlockSprite)sprite).zap( enemy.pos );
			} else {
				zap();
			}

			return !visible;
		}
	}

	private void zap() {
		RebornChampion champion = buff(RebornChampion.class);
		if (champion != null) champion.ensureAttack(enemy);
		try {
			spend( TIME_TO_ZAP );
			if (champion != null && (!isAlive() || enemy == null || !enemy.isAlive())) return;

			if (hit( this, enemy, true )) {
				if (champion == null && (enemy == Dungeon.hero || LocalCoop.contains(enemy)) && Random.Int( 2 ) == 0) {
					Buff.prolong( enemy, Weakness.class, Weakness.duration( enemy ) );
				}

				int dmg = Random.NormalIntRange( 12, 18 );
				dmg = RebornCurse.spellDamage(this, dmg);
				if (enemy == Dungeon.hero || LocalCoop.contains(enemy))
				{
					dmg = adjustHeroDamage((Hero)enemy, dmg);
				}

				enemy.damageFrom( dmg, this, this );
				if (champion != null) champion.onHit(enemy);

				if (!enemy.isAlive() && (enemy == Dungeon.hero || LocalCoop.contains(enemy))) {
					Dungeon.fail( Utils.format( ResultDescriptions.MOB,
						Utils.indefinite( name ), Dungeon.depth ), this );
					GLog.n(TXT_SHADOWBOLT_KILLED, Message.actor(this));
				}
			} else {
				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseText() );
			}
		} finally {
			if (champion != null) champion.finishAttack();
		}
	}

	public void onZapComplete() {
		zap();
		next();
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
			"When dwarves' interests have shifted from engineering to arcane arts, " +
			"warlocks have come to power in the city. They started with elemental magic, " +
			"but soon switched to demonology and necromancy.";
	}

	private static final HashSet<Class<?>> RESISTANCES = new HashSet<Class<?>>();
	static {
		RESISTANCES.add( Death.class );
	}

	@Override
	public HashSet<Class<?>> resistances() {
		return RESISTANCES;
	}
}
