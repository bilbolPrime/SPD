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

import java.util.HashSet;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Bleeding;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.actors.buffs.Charm;
import com.bilboldev.pixeldungeonskills.actors.buffs.Vertigo;
import com.bilboldev.pixeldungeonskills.actors.buffs.Cripple;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.Invisibility;
import com.bilboldev.pixeldungeonskills.actors.buffs.Light;
import com.bilboldev.pixeldungeonskills.actors.buffs.Roots;
import com.bilboldev.pixeldungeonskills.actors.buffs.Shadows;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Speed;
import com.bilboldev.pixeldungeonskills.actors.buffs.Levitation;
import com.bilboldev.pixeldungeonskills.actors.buffs.MindVision;
import com.bilboldev.pixeldungeonskills.actors.buffs.Paralysis;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.buffs.Slow;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Rat;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Shopkeeper;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.PoisonParticle;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.DualSwords;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.NecroBlade;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Shuriken;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.features.Door;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundlable;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.GameMath;
import com.bilboldev.utils.Random;

public abstract class Char extends Actor {

	protected static final String TXT_HIT		= "%s hit %s";
	protected static final String TXT_KILL		= "%s killed you...";
	protected static final String TXT_DEFEAT	= "%s defeated %s";
	
	private static final String TXT_YOU_MISSED	= "%s %s your attack";
	private static final String TXT_SMB_MISSED	= "%s %s %s's attack";
	
	private static final String TXT_OUT_OF_PARALYSIS	= "The pain snapped %s out of paralysis";

    public boolean screams = true;

    private String[] MOB_DEATH_SCREAMS =  {
            "...",
            "I will haunt your dreams...",
            "Too strong...",
            "C'est impossible!",
            "Sacre bleu!",
            "Mommy...",
            "I will be avenged..."
    };

    private String[] RAT_DEATH_SCREAMS = {
            "The Rat King will be victorious!",
            "The Rat King will avenge me!",
            "The Rat King shall prevail!",
            //"The Rat King is your father...", too harsh?
            "Forgive me my lord...",
            "You know nothing...",
            "The world is ours... all of its cheese too..."
    };

    private String[] HERO_DEATH_SCREAM = {
            "Ouch!",
            "My face...",
            "I have failed",
            "This game blows...",
            "Nerf it..."
    };

    private String[] PET_FAREWELL = {
            "Farewell...",
            "I have failed you...",
            "I just wanted some love..."
    };

    private String[] MERC_FAREWELL = {
            "I didn't sign up for this...",
            "I want a raise..."
    };

	public int pos = 0;
	
	public CharSprite sprite;
	
	public String name = "mob";
	
	public int HT;
	public int HP;
    public int MP = 0;
    public int MMP = 1;
    public int champ = -1;

	protected float baseSpeed	= 1;
	
	public boolean paralysed	= false;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;
	
	public int viewDistance	= 8;
	
	private HashSet<Buff> buffs = new HashSet<Buff>();

    public String getDeathScream()
    {
        if(this instanceof Hero)
            return HERO_DEATH_SCREAM[Random.IntRange( 0, HERO_DEATH_SCREAM.length - 1 )];
        if(this instanceof Rat)
            return RAT_DEATH_SCREAMS[Random.IntRange( 0, RAT_DEATH_SCREAMS.length - 1 )];
        if(this instanceof SummonedPet)
            return PET_FAREWELL[Random.IntRange( 0, PET_FAREWELL.length - 1 )];
        if(this instanceof HiredMerc)
            return MERC_FAREWELL[Random.IntRange( 0, MERC_FAREWELL.length - 1 )];
        return MOB_DEATH_SCREAMS[Random.IntRange( 0, MOB_DEATH_SCREAMS.length - 1 )];
    }

	@Override
	protected boolean act() {
		Dungeon.level.updateFieldOfView( this );
		return false;
	}
	
	private static final String POS			= "pos";
	private static final String TAG_HP		= "HP";
	private static final String TAG_HT		= "HT";
    private static final String TAG_MP		= "MP";
    private static final String TAG_MT		= "MT";
	private static final String BUFFS		= "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, pos );
		bundle.put( TAG_HP, HP );
		bundle.put( TAG_HT, HT );
        bundle.put( TAG_MP, MP );
        bundle.put( TAG_MT, MMP );
		bundle.put( BUFFS, buffs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		pos = bundle.getInt( POS );
		HP = bundle.getInt( TAG_HP );
		HT = bundle.getInt( TAG_HT );
        MP = bundle.getInt( TAG_MP );
        MMP = bundle.getInt( TAG_MT );

		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null) {
				((Buff)b).attachTo( this );
			}
		}
	}


	public boolean attack( Char enemy ) {
		
		boolean visibleFight = Dungeon.visible[pos] || Dungeon.visible[enemy.pos];
		
		if (hit( this, enemy, false )) {
			
			if (visibleFight) {
				GLog.i( TXT_HIT, name, enemy.name );
			}
			
			// FIXME
			int dr = this instanceof Hero && ((Hero)this).rangedWeapon != null && ((Hero)this).subClass == HeroSubClass.SNIPER ? 0 :
				Random.IntRange( 0, enemy.dr() );
			
			int dmg = damageRoll();

            if(enemy == Dungeon.hero)
            {
                dmg *= Dungeon.currentDifficulty.damageModifier();
                dmg *= Dungeon.hero.heroSkills.passiveA3.incomingDamageModifier(); //  <--- Warrior Toughness if present
                dmg -= Dungeon.hero.heroSkills.passiveA3.incomingDamageReduction(dmg); // <--- Mage SpiritArmor if present
            }
            else
            {
                if(this == Dungeon.hero && Dungeon.hero.rangedWeapon == null)
                {
                    dmg *= Dungeon.hero.heroSkills.passiveB2.damageModifier(); //  <--- Warrior Aggression if present
                    dmg *= Dungeon.hero.heroSkills.active1.damageModifier(); //  <--- Warrior Smash if present and active
                    dmg *= Dungeon.hero.heroSkills.active2.damageModifier(); //  <--- Warrior Knockback if present and active
                    dmg *= Dungeon.hero.heroSkills.active3.damageModifier(); //  <--- Warrior Rampage if present and active



                    if(!(Bestiary.isBoss( enemy )) && Dungeon.hero.heroSkills.active3.AoEDamage()) //  <--- Warrior Rampage if present and active
                    {
                        Dungeon.hero.heroSkills.active3.active = false; // Prevent infinite callstack
                        for(int possibleTarget : Level.NEIGHBOURS8)
                        {
                            Char tmpTarget = Actor.findChar( pos + possibleTarget );
                            if(tmpTarget != null && tmpTarget != enemy && tmpTarget instanceof Mob && !(tmpTarget instanceof NPC))
                                attack(tmpTarget);
                        }
                        Dungeon.hero.heroSkills.active3.active = true; // Should be safe now
                    }

                    if(!Bestiary.isBoss( enemy ) && enemy instanceof Mob && ((Mob)enemy).state instanceof Mob.Sleeping && Dungeon.hero.heroSkills.passiveB3.instantKill())
                        dmg = ((Mob) enemy).HP + dr;
                }
                if(this == Dungeon.hero)
                {
                    if(Dungeon.hero.heroSkills.passiveB1.venomousAttack()) // <--- Rogue Venom when present
                        Buff.affect(enemy, Poison.class).set(Poison.durationFactor(enemy));
                }
            }

			int effectiveDamage = Math.max( dmg - dr, 0 );
			
			effectiveDamage = attackProc( enemy, effectiveDamage );
			effectiveDamage = enemy.defenseProc( this, effectiveDamage );
            if(effectiveDamage < 0)
			    return true;
            enemy.damage( effectiveDamage, this );


            if(!Bestiary.isBoss( enemy ) && this == Dungeon.hero && Dungeon.hero.heroSkills.active2.damageBonus(enemy.HP) > 0 && Dungeon.hero.rangedWeapon instanceof Shuriken) // <-- Rogue Deadeye when present
                enemy.damage( Dungeon.hero.heroSkills.active2.damageBonus(enemy.HP, true), this );

            if(!Bestiary.isBoss( enemy ) && this == Dungeon.hero && Dungeon.hero.heroSkills.passiveB2.cripple() && Dungeon.hero.rangedWeapon != null) // <-- Huntress knee shot when present
                Buff.prolong( enemy, Cripple.class, Cripple.DURATION );

			if (visibleFight) {
				Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
			}

			if (enemy == Dungeon.hero) {
				Dungeon.hero.interrupt();
				if (effectiveDamage > enemy.HT / 4) {
					Camera.main.shake( GameMath.gate( 1, effectiveDamage / (enemy.HT / 4), 5), 0.3f );
				}
			}
			
			enemy.sprite.bloodBurstA( sprite.center(), effectiveDamage );
			enemy.sprite.flash();

            if(this instanceof Hero && ((Hero)this).rangedWeapon == null && ((Hero)this).belongings.weapon instanceof DualSwords)
            {
                if (enemy.isAlive())
                {
                    if(((DualSwords) ((Hero)this).belongings.weapon).secondHit == false)
                    {
                        ((DualSwords) ((Hero)this).belongings.weapon).secondHit = true;
                        attack(enemy);
                    }
                    else
                        ((DualSwords) ((Hero)this).belongings.weapon).secondHit = false;

                }
            }

            if(this instanceof Hero && ((Hero)this).rangedWeapon == null && ((Hero)this).belongings.weapon instanceof NecroBlade)
            {
                if (enemy.isAlive() == false)
                {
                    ((NecroBlade)Dungeon.hero.belongings.weapon).updateCharge(enemy.HT > 22 ? (int)Math.floor(enemy.HT / 22) : 1);
                    GLog.p("NecroBlade absored a portion of " + enemy.name + "'s life energy.");

                }
            }

			if (!enemy.isAlive() && visibleFight) {
				if (enemy == Dungeon.hero) {
					
					if (Dungeon.hero.killerGlyph != null) {
						
					// FIXME
					//	Dungeon.fail( Utils.format( ResultDescriptions.GLYPH, Dungeon.hero.killerGlyph.name(), Dungeon.depth ) );
					//	GLog.n( TXT_KILL, Dungeon.hero.killerGlyph.name() );
						
					} else {
						if (Bestiary.isBoss( this )) {
							Dungeon.fail( Utils.format( ResultDescriptions.BOSS, name, Dungeon.depth ) );
						} else {
							Dungeon.fail( Utils.format( ResultDescriptions.MOB, 
								Utils.indefinite( name ), Dungeon.depth ) );
						}
						
						GLog.n( TXT_KILL, name );
					}
					
				} else {
					GLog.i( TXT_DEFEAT, name, enemy.name );
				}
			}
			
			return true;
			
		} else {
			
			if (visibleFight) {
				String defense = enemy.defenseVerb();
				enemy.sprite.showStatus( CharSprite.NEUTRAL, defense );
				if (this == Dungeon.hero) {
					GLog.i( TXT_YOU_MISSED, enemy.name, defense );
				} else {
					GLog.i( TXT_SMB_MISSED, enemy.name, defense, name );
				}
				
				Sample.INSTANCE.play(Assets.SND_MISS);
			}
			
			return false;
			
		}
	}
	
	public static boolean hit( Char attacker, Char defender, boolean magic ) {
		float acuRoll = Random.Float( attacker.attackSkill( defender ) );
		float defRoll = Random.Float( defender.defenseSkill( attacker ) );

        if(defender instanceof Hero && Level.distance( attacker.pos, defender.pos ) > 1 && ((Hero)defender).heroSkills.passiveA3.dodgeChance()) // <--- Huntress Awareness if present
            return false;

		return (magic ? acuRoll * 2 : acuRoll) >= defRoll;
	}

	public int attackSkill( Char target ) {
		return 0;
	}
	
	public int defenseSkill( Char enemy ) {
		return 0;
	}
	
	public String defenseVerb() {
		return "dodged";
	}
	
	public int dr() {
		return 0;
	}
	
	public int damageRoll() {
		return 1;
	}
	
	public int attackProc( Char enemy, int damage ) {
		return damage;
	}
	
	public int defenseProc( Char enemy, int damage ) {
		return damage;
	}
	
	public float speed() {
		return buff( Cripple.class ) == null ? baseSpeed : baseSpeed * 0.5f;
	}
	
	public void damage( int dmg, Object src ) {
		
		if (HP <= 0) {
			return;
		}

        /*
        if(src instanceof Hero) {
            Hero heroSrc = (Hero) src;
            if (heroSrc.rangedWeapon instanceof Arrow && heroSrc.belongings.bow instanceof Bow) {

            }
            else
                Buff.detach( this, Frost.class );
        }
        else
		    Buff.detach( this, Frost.class );

        */

		Class<?> srcClass = src.getClass();
		if (immunities().contains( srcClass )) {
			dmg = 0;
		} else if (resistances().contains( srcClass )) {
			dmg = Random.IntRange( 0, dmg );
		}
		
		if (buff( Paralysis.class ) != null) {
			if (Random.Int( dmg ) >= Random.Int( HP )) {
				Buff.detach( this, Paralysis.class );
				if (Dungeon.visible[pos]) {
					GLog.i( TXT_OUT_OF_PARALYSIS, name );
				}
			}
		}

        if(src instanceof Hero) {
            Hero heroSrc = (Hero) src;
            if (heroSrc.rangedWeapon instanceof Arrow && heroSrc.belongings.bow instanceof Bow) {
                heroSrc.belongings.bow.bowSpecial(this);
                ((Arrow) heroSrc.rangedWeapon).arrowEffect(heroSrc, this);
            }
        }

		HP -= dmg;
		if (dmg > 0 || src instanceof Char) {
			sprite.showStatus( HP > HT / 2 ? 
				CharSprite.WARNING : 
				CharSprite.NEGATIVE,
				Integer.toString( dmg ) );
		}
		if (HP <= 0) {
			die( src );
		}
	}
	
	public void destroy() {
		HP = 0;
		Actor.remove( this );
		Actor.freeCell( pos );
	}
	
	public void die( Object src ) {
        if(!(this instanceof Shopkeeper || this instanceof Imp || this instanceof Blacksmith) && screams)
            this.sprite.showStatus(CharSprite.NEUTRAL, getDeathScream());

		destroy();
		sprite.die();
	}
	
	public boolean isAlive() {
		return HP > 0;
	}
	
	@Override
	protected void spend( float time ) {
		
		float timeScale = 1f;
		if (buff( Slow.class ) != null) {
			timeScale *= 0.5f;
		}
		if (buff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		
		super.spend( time / timeScale );
	}
	
	public HashSet<Buff> buffs() {
		return buffs;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Buff> HashSet<T> buffs( Class<T> c ) {
		HashSet<T> filtered = new HashSet<T>();
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Buff> T buff( Class<T> c ) {
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				return (T)b;
			}
		}
		return null;
	}
	
	public boolean isCharmedBy( Char ch ) {
		int chID = ch.id();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}
	
	public void add( Buff buff ) {
		
		buffs.add( buff );
		Actor.add( buff );
		
		if (sprite != null) {
			if (buff instanceof Poison) {
				
				CellEmitter.center( pos ).burst( PoisonParticle.SPLASH, 5 );
				sprite.showStatus( CharSprite.NEGATIVE, "poisoned" );
				
			} else if (buff instanceof Amok) {
				
				sprite.showStatus( CharSprite.NEGATIVE, "amok" );

			} else if (buff instanceof Slow) {

				sprite.showStatus( CharSprite.NEGATIVE, "slowed" );
				
			} else if (buff instanceof MindVision) {
				
				sprite.showStatus( CharSprite.POSITIVE, "mind" );
				sprite.showStatus( CharSprite.POSITIVE, "vision" );
				
			} else if (buff instanceof Paralysis) {

				sprite.add( CharSprite.State.PARALYSED );
				sprite.showStatus( CharSprite.NEGATIVE, "paralysed" );
				
			} else if (buff instanceof Terror) {
				
				sprite.showStatus( CharSprite.NEGATIVE, "frightened" );
				
			} else if (buff instanceof Roots) {
				
				sprite.showStatus( CharSprite.NEGATIVE, "rooted" );
				
			} else if (buff instanceof Cripple) {

				sprite.showStatus( CharSprite.NEGATIVE, "crippled" );
				
			} else if (buff instanceof Bleeding) {

				sprite.showStatus( CharSprite.NEGATIVE, "bleeding" );
				
			} else if (buff instanceof Vertigo) {

				sprite.showStatus( CharSprite.NEGATIVE, "dizzy" );
				
			} else if (buff instanceof Sleep) {
				sprite.idle();
			}
			
			  else if (buff instanceof Burning) {
				sprite.add( CharSprite.State.BURNING );
			} else if (buff instanceof Levitation) {
				sprite.add( CharSprite.State.LEVITATING );
			} else if (buff instanceof Frost) {
				sprite.add( CharSprite.State.FROZEN );
			} else if (buff instanceof Champ) {
                switch(((Champ) buff).type) {
                    case Champ.CHAMP_VAMPERIC: sprite.add(CharSprite.State.CHAMPRED);
                        break;
                    case Champ.CHAMP_CHIEF: sprite.add(CharSprite.State.CHAMPWHITE);
                        break;
                    case Champ.CHAMP_CURSED: sprite.add(CharSprite.State.CHAMPBLACK);
                        break;
                    case Champ.CHAMP_FOUL: sprite.add(CharSprite.State.CHAMPYELLOW);
                        break;
                }
            } else if (buff instanceof Invisibility) {
				if (!(buff instanceof Shadows)) {
					sprite.showStatus( CharSprite.POSITIVE, "invisible" );
				}
				sprite.add( CharSprite.State.INVISIBLE );
			}
		}
	}
	
	public void remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );
		
		if (buff instanceof Burning) {
			sprite.remove( CharSprite.State.BURNING );
		} else if (buff instanceof Levitation) {
			sprite.remove( CharSprite.State.LEVITATING );
		} else if (buff instanceof Invisibility && invisible <= 0) {
			sprite.remove( CharSprite.State.INVISIBLE );
		} else if (buff instanceof Paralysis) {
			sprite.remove( CharSprite.State.PARALYSED );
		} else if (buff instanceof Frost) {
			sprite.remove( CharSprite.State.FROZEN );
		} 
	}
	
	public void remove( Class<? extends Buff> buffClass ) {
		for (Buff buff : buffs( buffClass )) {
			remove( buff );
		}
	}
	
	
	
	@Override
	protected void onRemove() {
		for (Buff buff : buffs.toArray( new Buff[0] )) {
			buff.detach();
		}
	}
	
	public void updateSpriteState() {
		for (Buff buff:buffs) {
			if (buff instanceof Burning) {
				sprite.add( CharSprite.State.BURNING );
			} else if (buff instanceof Levitation) {
				sprite.add( CharSprite.State.LEVITATING );
			} else if (buff instanceof Invisibility) {
				sprite.add( CharSprite.State.INVISIBLE );
			} else if (buff instanceof Paralysis) {
				sprite.add( CharSprite.State.PARALYSED );
			} else if (buff instanceof Frost) {
				sprite.add( CharSprite.State.FROZEN );
			} else if (buff instanceof Light) {
				sprite.add( CharSprite.State.ILLUMINATED );
			}
		}
	}
	
	public int stealth() {
		return 0;
	}
	
	public void move( int step ) {
		
		if (Level.adjacent( step, pos ) && buff( Vertigo.class ) != null) {
			step = pos + Level.NEIGHBOURS8[Random.Int( 8 )];
			if (!(Level.passable[step] || Level.avoid[step]) || Actor.findChar( step ) != null) {
				return;
			}
		}
		
		if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
			Door.leave( pos );
		}
		
		pos = step;
		
		if (flying && Dungeon.level.map[pos] == Terrain.DOOR) {
			Door.enter( pos );
		}
		
		if (this != Dungeon.hero) {
			sprite.visible = Dungeon.visible[pos];
		}
	}
	
	public int distance( Char other ) {
		return Level.distance( pos, other.pos );
	}
	
	public void onMotionComplete() {
		next();
	}
	
	public void onAttackComplete() {
		next();
	}
	
	public void onOperateComplete() {
		next();
	}
	
	private static final HashSet<Class<?>> EMPTY = new HashSet<Class<?>>();
	
	public HashSet<Class<?>> resistances() {
		return EMPTY;
	}
	
	public HashSet<Class<?>> immunities() {
		return EMPTY;
	}
}
