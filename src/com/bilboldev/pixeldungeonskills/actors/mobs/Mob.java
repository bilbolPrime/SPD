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

import java.util.HashSet;

import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Challenges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.buffs.Weakness;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.Wound;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.PathFinder;
import com.bilboldev.utils.Random;

public abstract class Mob extends Char {
	
	private static final String	TXT_DIED	= "You hear something died in the distance";
	
	protected static final String	TXT_ECHO	= "echo of ";
	
	protected static final String TXT_NOTICE1	= "?!";
	protected static final String TXT_RAGE		= "#$%^";
	protected static final String TXT_EXP		= "%+dEXP";
    protected static final String TXT_EXP_CHAMP		= "%+dEXP (Champion killed!)";

	public AiState SLEEPEING	= new Sleeping();
	public AiState HUNTING		= new Hunting();
	public AiState WANDERING	= new Wandering();
	public AiState FLEEING		= new Fleeing();
	public AiState PASSIVE		= new Passive();
	public AiState state = SLEEPEING;
	
	public Class<? extends CharSprite> spriteClass;
	
	protected int target = -1;
	
	public int defenseSkill = 0;
	
	protected int EXP = 1;
	protected int maxLvl = 30;
	
	protected Char enemy;
	protected boolean enemySeen;
	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;
	
	public boolean hostile = true;
	
	private static final String STATE	= "state";
	private static final String TARGET	= "target";

    public int range = 0;

	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		if (state == SLEEPEING) {
			bundle.put( STATE, Sleeping.TAG );
		} else if (state == WANDERING) {
			bundle.put( STATE, Wandering.TAG );
		} else if (state == HUNTING) {
			bundle.put( STATE, Hunting.TAG );
		} else if (state == FLEEING) {
			bundle.put( STATE, Fleeing.TAG );
		} else if (state == PASSIVE) {
			bundle.put( STATE, Passive.TAG );
		}
		bundle.put( TARGET, target );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		String state = bundle.getString( STATE );
		if (state.equals( Sleeping.TAG )) {
			this.state = SLEEPEING;
		} else if (state.equals( Wandering.TAG )) {
			this.state = WANDERING;
		} else if (state.equals( Hunting.TAG )) {
			this.state = HUNTING;
		} else if (state.equals( Fleeing.TAG )) {
			this.state = FLEEING;
		} else if (state.equals( Passive.TAG )) {
			this.state = PASSIVE;
		}

		target = bundle.getInt( TARGET );
	}
	
	public CharSprite sprite() {
		CharSprite sprite = null;
		try {
			sprite = spriteClass.newInstance();
		} catch (Exception e) {
		}
		return sprite;
	}
	
	@Override
	protected boolean act() {
		
		super.act();
		
		boolean justAlerted = alerted;
		alerted = false;
		
		sprite.hideAlert();
		
		if (paralysed) {
			enemySeen = false;
			spend( TICK );
			return true;
		}
		
		enemy = chooseEnemy();
		
		boolean enemyInFOV = 
			enemy != null && enemy.isAlive() && 
			Level.fieldOfView[enemy.pos] && enemy.invisible <= 0;
		
		return state.act( enemyInFOV, justAlerted );
	}
	
	protected Char chooseEnemy() {
		
		if (buff( Amok.class ) != null) {
			if (enemy == Dungeon.hero || enemy == null) {
				
				HashSet<Mob> enemies = new HashSet<Mob>();
				for (Mob mob:Dungeon.level.mobs) {
					if (mob != this && Level.fieldOfView[mob.pos]) {
						enemies.add( mob );
					}
				}
				if (enemies.size() > 0) {
					return Random.element( enemies );
				}
				
			}
		}
		
		Terror terror = (Terror)buff( Terror.class );
		if (terror != null) {
			Char source = (Char)Actor.findById( terror.object );
			if (source != null) {
				return source;
			}
		}

		return enemy != null && enemy.isAlive() ? enemy : Dungeon.hero;
	}
	
	protected boolean moveSprite( int from, int to ) {

		if (sprite.isVisible() && (Dungeon.visible[from] || Dungeon.visible[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.place( to );
			return true;
		}
	}
	
	@Override
	public void add( Buff buff ) {
		super.add( buff );
		if (buff instanceof Amok) {
			if (sprite != null) {
				sprite.showStatus( CharSprite.NEGATIVE, TXT_RAGE );
			}
			state = HUNTING;
		} else if (buff instanceof Terror) {
			state = FLEEING;
		} else if (buff instanceof Sleep) {
			if (sprite != null) {
				new Flare( 4, 32 ).color( 0x44ffff, true ).show( sprite, 2f ) ;
			}
			state = SLEEPEING;
			postpone( Sleep.SWS );
		}
	}
	
	@Override
	public void remove( Buff buff ) {
		super.remove( buff );
		if (buff instanceof Terror) {
			sprite.showStatus( CharSprite.NEGATIVE, TXT_RAGE );
			state = HUNTING;
		}
	}
	
	protected boolean canAttack( Char enemy ) {
		return (Level.adjacent( pos, enemy.pos ) || (Level.distance(pos, enemy.pos) <= range  &&  Ballistica.cast(pos, enemy.pos, false, true) == enemy.pos)) && !isCharmedBy( enemy );
	}

    public boolean canBeKnockedBackInto(int newPos)
    {
        if (rooted)
            return false;

        try {
            PathFinder.Path tmp = PathFinder.find(pos, newPos, Dungeon.passable);
            return tmp != null && tmp.size() < 3;
        }
        catch (Exception ex)
        {
            return false; // Prevents crash if out of bounds
        }
    }

	protected boolean getCloser( int target ) {
		
		if (rooted) {
			return false;
		}
		
		int step = Dungeon.findPath( this, pos, target, 
			Level.passable, 
			Level.fieldOfView );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean getFurther( int target ) {
		int step = Dungeon.flee( this, pos, target, 
			Level.passable, 
			Level.fieldOfView );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void move( int step ) {
		super.move( step );
		
		if (!flying) {
			Dungeon.level.mobPress( this );
		}
	}
	
	protected float attackDelay() {
		return 1f;
	}
	
	protected boolean doAttack( Char enemy ) {
		
		boolean visible = Dungeon.visible[pos];
		
		if (visible) {
			sprite.attack( enemy.pos );
		} else {
			attack( enemy );
		}
				
		spend( attackDelay() );
		
		return !visible;
	}
	
	@Override
	public void onAttackComplete() {
		attack( enemy );
		super.onAttackComplete();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return enemySeen && !paralysed ? defenseSkill : 0;
	}
	
	@Override
	public int defenseProc( Char enemy, int damage ) {
		if (!enemySeen && enemy == Dungeon.hero && ((Hero)enemy).subClass == HeroSubClass.ASSASSIN) {
			damage += Random.Int( 1, damage );
			Wound.hit( this );
		}
		return damage;
	}
	
	public void aggro( Char ch ) {
		enemy = ch;
	}
	
	@Override
	public void damage( int dmg, Object src ) {

		Terror.recover( this );
		
		if (state == SLEEPEING) {
			state = WANDERING;
		}
		alerted = true;
		
		super.damage( dmg, src );
	}
	
	
	@Override
	public void destroy() {
		
		super.destroy();
		
		Dungeon.level.mobs.remove( this );
		
		if (Dungeon.hero.isAlive()) {

			if (hostile) {
				Statistics.enemiesSlain++;
				Badges.validateMonstersSlain();
				Statistics.qualifiedForNoKilling = false;
				
				if (Dungeon.nightMode) {
					Statistics.nightHunt++;
				} else {
					Statistics.nightHunt = 0;
				}
				Badges.validateNightHunter();
			}

			int exp = exp();

			if (exp > 0) {
                if(this.champ > 0)
                {
                    Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, TXT_EXP_CHAMP, (int)Math.ceil(1.5 * exp));
                    Dungeon.hero.earnExp((int)Math.ceil(1.5 * exp));
                }
                else {
                    Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, TXT_EXP, exp );
                    Dungeon.hero.earnExp( exp );
                }
			}
		}
	}
	
	public int exp() {
		return Dungeon.hero.lvl <= maxLvl ? EXP : 0;
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );

        if(this instanceof SummonedPet)
            SummonedPet.summonedPets--;

        if(champ > 0)
            dropLootGuaranteed();

		if (Dungeon.hero.lvl <= maxLvl + 2) {
			dropLoot();
		}

        if(sprite != null)
            sprite.NotAChamp();

		if (Dungeon.hero.isAlive() && !Dungeon.visible[pos]) {	
			GLog.i( TXT_DIED );
		}
	}
	
	protected Object loot = null;
	protected float lootChance = 0;
	
	@SuppressWarnings("unchecked")

    protected void dropLootGuaranteed() {

        Item item = Generator.random(  );
        Dungeon.level.drop( item, pos ).sprite.drop();

    }

	protected void dropLoot() {
		if (loot != null && Random.Float() < lootChance) {
			Item item = null;
			if (loot instanceof Generator.Category) {
				
				item = Generator.random( (Generator.Category)loot );
				
			} else if (loot instanceof Class<?>) {
				
				item = Generator.random( (Class<? extends Item>)loot );
				
			} else {
				
				item = (Item)loot;
				
			}
			Dungeon.level.drop( item, pos ).sprite.drop();
		}
	}
	
	public boolean reset() {
		return false;
	}

    public void champEffect(Char enemy, int damage )
    {
        if(enemy == null) // Happens sometimes with summoned stuff and NPCs
            return;

        if(enemy instanceof NPC)
            return;

        if(!(enemy instanceof Hero))
            return;

        try {
            if (champ != -1) {
                if (champ == Champ.CHAMP_VAMPERIC) {
                    int reg = Math.min(damage, HT - HP);

                    if (reg > 0) {
                        HP += reg;
                        sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
                    }
                } else if (champ == Champ.CHAMP_CURSED) {
                    Buff.affect(enemy, Weakness.class, 5);
                } else if (champ == Champ.CHAMP_FOUL) {
                    Buff.affect(enemy, Poison.class).set(5);
                }
            }
        }
        catch (Exception ex)
        {

        }
    }

	public void beckon( int cell ) {
		
		notice();
		
		if (state != HUNTING) {
			state = WANDERING;
		}
		target = cell;
	}
	
	public String description() {
		return "Real description is coming soon!";
	}
	
	public void notice() {
		sprite.showAlert();
	}
	
	public void yell( String str ) {
		GLog.n( "%s: \"%s\" ", name, str );
	}
	
	public interface AiState {
		public boolean act( boolean enemyInFOV, boolean justAlerted );
		public String status();
	}
	
	public class Sleeping implements AiState {
		
		public static final String TAG	= "SLEEPING";
		
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && Random.Int( distance( enemy ) + enemy.stealth() + (enemy.flying ? 2 : 0) ) == 0) {
					
				enemySeen = true;

				notice();
				state = HUNTING;
				target = enemy.pos;
				
				if (Dungeon.isChallenged( Challenges.SWARM_INTELLIGENCE )) {
					for (Mob mob : Dungeon.level.mobs) {
						if (mob != Mob.this) {
							mob.beckon( target );
						}
					}
				}
				
				spend( TIME_TO_WAKE_UP );
				
			} else {
				
				enemySeen = false;
				
				spend( TICK );
				
			}
			return true;
		}
		
		@Override
		public String status() {
			return Utils.format( "This %s is sleeping", name );
		}
	}
	
	private class Wandering implements AiState {
		
		public static final String TAG	= "WANDERING";
		
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && (justAlerted || Random.Int( distance( enemy ) / 2 + enemy.stealth() ) == 0)) {
				
				enemySeen = true;
				
				notice();
				state = HUNTING;
				target = enemy.pos;
				
			} else {
				
				enemySeen = false;
				
				int oldPos = pos;
				if (target != -1 && getCloser( target )) {
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					target = Dungeon.level.randomDestination();
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
	
	private class Hunting implements AiState {
		
		public static final String TAG	= "HUNTING";
		
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			if (enemyInFOV && canAttack( enemy )) {
				
				return doAttack( enemy );
				
			} else {

				if (enemyInFOV) {
					target = enemy.pos;
				}

				int oldPos = pos;
				if (target != -1 && getCloser( target )) {
					
					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );
					
				} else {
					
					spend( TICK );
					state = WANDERING;
					target = Dungeon.level.randomDestination();
					return true;
				}
			}
		}
		
		@Override
		public String status() {
			return Utils.format( "This %s is hunting", name );
		}
	}
	
	protected class Fleeing implements AiState {
		
		public static final String TAG	= "FLEEING";
		
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			if (enemyInFOV) {
				target = enemy.pos;
			}
			
			int oldPos = pos;
			if (target != -1 && getFurther( target )) {
				
				spend( 1 / speed() );
				return moveSprite( oldPos, pos );
				
			} else {
				
				spend( TICK );
				nowhereToRun();
				
				return true;
			}
		}
		
		protected void nowhereToRun() {
		}
		
		@Override
		public String status() {
			return Utils.format( "This %s is fleeing", name );
		}
	}
	
	private class Passive implements AiState {
		
		public static final String TAG	= "PASSIVE";
		
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = false;
			spend( TICK );
			return true;
		}
		
		@Override
		public String status() {
			return Utils.format( "This %s is passive", name );
		}
	}

}
