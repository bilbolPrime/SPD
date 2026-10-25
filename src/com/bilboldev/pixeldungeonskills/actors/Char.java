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

import com.bilboldev.pixeldungeonskills.messages.Message;
import java.util.HashSet;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.messages.Messages;
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
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.buffs.Paralysis;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
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
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.WeaponEffect;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.DualSwords;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.NecroBlade;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Shuriken;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.levels.features.Door;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.JoeSprite;
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

    private static final Message[] MOB_DEATH_SCREAMS = {
            Message.key("actors.hero.hero.wait"),
            Message.key("custom.generated.entry_i_will_haunt_your_dreams_2e4c06b9f6"),
            Message.key("custom.generated.entry_too_strong_95f39375a3"),
            Message.key("custom.generated.entry_c_est_impossible_ebdf7038f6"),
            Message.key("custom.generated.entry_sacre_bleu_c31dcb65a4"),
            Message.key("custom.generated.entry_mommy_492d8372ae"),
            Message.key("custom.generated.entry_i_will_be_avenged_fe2f761dca")
    };

    private static final Message[] RAT_DEATH_SCREAMS = {
            Message.key("custom.generated.entry_the_rat_king_will_be_victorious_cb154559fc"),
            Message.key("custom.generated.entry_the_rat_king_will_avenge_me_31a5b58579"),
            Message.key("custom.generated.entry_the_rat_king_shall_prevail_df3efc0c9b"),
            Message.key("custom.generated.entry_forgive_me_my_lord_ac20cfea4f"),
            Message.key("custom.generated.entry_you_know_nothing_cdef29bf7a"),
            Message.key("custom.generated.entry_the_world_is_ours_all_of_its_cheese_too_3e04d68714")
    };

    private static final Message[] HERO_DEATH_SCREAM = {
            Message.key("custom.generated.entry_ouch_2f282e1a70"),
            Message.key("custom.generated.entry_my_face_b70e059f5d"),
            Message.key("custom.generated.entry_i_have_failed_b200a406af"),
            Message.key("custom.generated.entry_this_game_blows_fcfaf2141e"),
            Message.key("custom.generated.entry_nerf_it_91dfa33434")
    };

    private static final Message[] PET_FAREWELL = {
            Message.key("custom.generated.entry_farewell_ef42e0a5e0"),
            Message.key("custom.generated.entry_i_have_failed_you_297628b5e1"),
            Message.key("custom.generated.entry_i_just_wanted_some_love_50face9910")
    };

    private static final Message[] MERC_FAREWELL = {
            Message.key("custom.generated.entry_i_didn_t_sign_up_for_this_9c2d7e6e2b"),
            Message.key("custom.generated.entry_i_want_a_raise_692b7cdb82")
    };

	private static final Message[] JOE_FAREWELL = {
			Message.key("custom.generated.entry_i_was_joe_2ad99f9c8d")
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

    public Message deathScream()
    {
		if (this instanceof Hero) {
			return HERO_DEATH_SCREAM[Random.IntRange( 0, HERO_DEATH_SCREAM.length - 1 )];
		}
		if (this instanceof Rat) {
			return RAT_DEATH_SCREAMS[Random.IntRange( 0, RAT_DEATH_SCREAMS.length - 1 )];
		}
		if (this instanceof SummonedPet) {
			if (sprite instanceof JoeSprite) {
				return JOE_FAREWELL[Random.IntRange( 0, JOE_FAREWELL.length - 1 )];
			}
			return PET_FAREWELL[Random.IntRange( 0, PET_FAREWELL.length - 1 )];
		}
		if (this instanceof HiredMerc) {
			return MERC_FAREWELL[Random.IntRange( 0, MERC_FAREWELL.length - 1 )];
		}
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

	public void storePartyRuntime(Bundle bundle) {
		bundle.put("hp", HP);
		bundle.put("ht", HT);
		bundle.put("mp", MP);
		bundle.put("mmp", MMP);
		bundle.put("time", time);
		bundle.put("champ", champ);
		bundle.put("speed", baseSpeed);
		bundle.put("paralysed", paralysed);
		bundle.put("rooted", rooted);
		bundle.put("flying", flying);
		bundle.put("invisible", invisible);
		bundle.put("view_distance", viewDistance);
		int index = 0;
		for (Buff buff : buffs) {
			Bundle clock = new Bundle();
			clock.put("id", buff.id());
			clock.put("type", buff.getClass().getName());
			clock.put("time", buff.time);
			bundle.put("buff_" + index++, clock);
		}
		bundle.put("buff_count", index);
	}

	public void restorePartyRuntime(Bundle bundle) {
		HP = bundle.getInt("hp");
		HT = bundle.getInt("ht");
		MP = bundle.getInt("mp");
		MMP = bundle.getInt("mmp");
		time = bundle.getFloat("time");
		champ = bundle.getInt("champ");
		baseSpeed = bundle.getFloat("speed");
		paralysed = bundle.getBoolean("paralysed");
		rooted = bundle.getBoolean("rooted");
		flying = bundle.getBoolean("flying");
		invisible = bundle.getInt("invisible");
		viewDistance = bundle.getInt("view_distance");
		HashSet<Buff> unmatched = new HashSet<Buff>(buffs);
		for (int index = 0; index < bundle.getInt("buff_count"); index++) {
			Bundle clock = bundle.getBundle("buff_" + index);
			Buff matched = null;
			for (Buff buff : unmatched) {
				if (!buff.getClass().getName().equals(clock.getString("type"))) continue;
				matched = buff;
				if (buff.id() == clock.getInt("id")) break;
			}
			if (matched != null) {
				matched.time = clock.getFloat("time");
				unmatched.remove(matched);
			}
		}
	}

	public boolean attack( Char enemy ) {
		if (LocalCoop.active()) {
			if (enemy == null || !enemy.isAlive() || !isAlive()) return false;
			if (LocalCoop.contains(this) && LocalCoop.contains(enemy)) return false;
			Hero owner = null;
			if (LocalCoop.contains(this)) {
				owner = (Hero)this;
			} else if (this instanceof NPC && !((NPC)this).hostile) {
				owner = ((NPC)this).owner();
			} else if (LocalCoop.contains(enemy)) {
				owner = (Hero)enemy;
			}
			if (owner != null && owner != Dungeon.hero) {
				return LocalCoop.withHero(owner, () -> resolveAttack(enemy));
			}
		}
		return resolveAttack(enemy);
	}

	private boolean resolveAttack(Char enemy) {
		Hero attacker = this == Dungeon.hero || LocalCoop.contains(this) ? (Hero)this : null;
		Hero defender = enemy == Dungeon.hero || LocalCoop.contains(enemy) ? (Hero)enemy : null;

		boolean visibleFight = Dungeon.visible[pos] || Dungeon.visible[enemy.pos];

		if (hit( this, enemy, false )) {

			if (visibleFight) {
				if (attacker != null) {
					GLog.i(Char.class, "you_hit", Message.actor(enemy));
				} else if (defender != null) {
					GLog.i(Char.class, "enemy_hit", Message.actor(this));
				} else {
					GLog.i(Char.class, "hit", Message.actor(this), Message.actor(enemy));
				}
			}

			int dr = this instanceof Hero && ((Hero)this).rangedWeapon != null && ((Hero)this).subClass == HeroSubClass.SNIPER ? 0 :
				Random.IntRange( 0, enemy.dr() );

			int dmg = damageRoll();
			RebornChampion champion = buff(RebornChampion.class);
			if (champion != null) dmg = champion.damage(dmg);
			NecroWeakened weakened = buff(NecroWeakened.class);
			if (weakened != null) {
				dmg = Math.max(0, (int)(dmg * weakened.outgoingDamageModifier()));
			}

            if(defender != null)
            {
                dmg = adjustHeroDamage(defender, dmg);
            }
            else
            {
                if(attacker != null && attacker.rangedWeapon == null)
                {
                    dmg *= attacker.heroSkills.passiveB2.damageModifier();
                    dmg *= attacker.heroSkills.active1.damageModifier();
                    dmg *= attacker.heroSkills.active2.damageModifier();
                    dmg *= attacker.heroSkills.active3.damageModifier();

                    if(!(Bestiary.isBoss( enemy )) && attacker.heroSkills.active3.AoEDamage())
                    {
                        attacker.heroSkills.active3.active = false;
                        for(int possibleTarget : Level.NEIGHBOURS8)
                        {
                            Char tmpTarget = Actor.findChar( pos + possibleTarget );
                            if(tmpTarget != null && tmpTarget != enemy && tmpTarget instanceof Mob && !(tmpTarget instanceof NPC))
                                attack(tmpTarget);
                        }
                        attacker.heroSkills.active3.active = true;
                    }

					if(!(Bestiary.isBoss( enemy )) && attacker.skillTree.MeleeAoEDamage())
					{
						for(int possibleTarget : Level.NEIGHBOURS8)
						{
							Char tmpTarget = Actor.findChar( pos + possibleTarget );
							if(tmpTarget != null && tmpTarget != enemy && tmpTarget instanceof Mob && !(tmpTarget instanceof NPC))
								attack(tmpTarget);
						}
					}

                    if(!Bestiary.isBoss( enemy ) && enemy instanceof Mob && ((Mob)enemy).state instanceof Mob.Sleeping && attacker.skillTree.instantKill())
                        dmg = ((Mob) enemy).HP + dr;
                }
				if(attacker != null
						&& attacker.rangedWeapon != null
						&& attacker.rangedWeapon instanceof Arrow)
				{
					dmg *= attacker.skillTree.rangedDamageModifier();
					if(attacker.belongings.bow != null){
                        dmg *= attacker.belongings.bow.damageBonus();
                    }
				}

                if(attacker != null)
                {
                    if(attacker.skillTree.venomousAttack())
                        Buff.affect(enemy, Poison.class).set(Poison.durationFactor(enemy));
                }
            }

			WeaponEffect prefix = null;
			if (attacker != null) {
				Hero hero = attacker;
				if (hero.rangedWeapon instanceof Arrow && hero.belongings.bow != null) {
					prefix = hero.belongings.bow.weaponEffect;
				} else if (hero.rangedWeapon == null && hero.belongings.weapon instanceof Weapon) {
					prefix = hero.belongings.weapon.weaponEffect;
				}
			}
			if (prefix != null && !prefix.roll(this, enemy)) prefix = null;
			if (prefix != null) dmg *= prefix.damageMultiplier();
			int effectiveDamage = Math.max( dmg - dr, 0 );

			effectiveDamage = attackProc( enemy, effectiveDamage );
			effectiveDamage = enemy.defenseProc( this, effectiveDamage );
            if(effectiveDamage < 0)
			    return true;
			int hpBefore = enemy.HP;
            enemy.damageFrom( effectiveDamage, this, this );
			if (prefix != null && isAlive()) {
				prefix.onHit(this, enemy, Math.max(0, hpBefore - Math.max(0, enemy.HP)));
			}
			if (champion != null) champion.onHit(enemy);

            if(!Bestiary.isBoss( enemy ) && attacker != null && attacker.heroSkills.active2.damageBonus(enemy.HP) > 0 && attacker.rangedWeapon instanceof Shuriken)
                enemy.damage( attacker.heroSkills.active2.damageBonus(enemy.HP, true), this );

            if(!Bestiary.isBoss( enemy ) && attacker != null && attacker.skillTree.rangedCripple() && attacker.rangedWeapon != null)
                Buff.prolong( enemy, Cripple.class, Cripple.DURATION );

			if (visibleFight) {
				Sample.INSTANCE.play( Assets.SND_HIT, 1, 1, Random.Float( 0.8f, 1.25f ) );
			}

			if (defender != null) {
				defender.interrupt();
				if (effectiveDamage > enemy.HT / 4) {
					Camera.main.shake( GameMath.gate( 1, effectiveDamage / (enemy.HT / 4), 5), 0.3f );
				}
			}
			try
			{
				enemy.sprite.bloodBurstA( sprite.center(), effectiveDamage );
				enemy.sprite.flash();
			}
			catch (Exception e){}

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
                    Hero wielder = LocalCoop.active() ? (Hero)this : Dungeon.hero;
                    ((NecroBlade)wielder.belongings.weapon).updateCharge(enemy.HT > 22 ? (int)Math.floor(enemy.HT / 22) : 1);
					GLog.p("custom.generated.manual.necroblade_absored", Message.actor(enemy));

                }
            }

			if (!enemy.isAlive() && visibleFight) {
				if (defender != null) {

					if (defender.killerGlyph != null) {

					} else {
						if (Bestiary.isBoss( this )) {
							Dungeon.fail( Utils.format( ResultDescriptions.BOSS, name, Dungeon.depth ), this );
						} else {
							Dungeon.fail( Utils.format( ResultDescriptions.MOB,
								Utils.indefinite( name ), Dungeon.depth ), this );
						}

							GLog.n(Char.class, "kill", Message.actor(this));
					}

				} else {
						GLog.i(Char.class, "defeat", Message.actor(enemy));
				}
			}

			return true;

		} else {

			try {
				if (visibleFight) {
					Message defense = enemy.defenseText();
					enemy.sprite.showStatus(CharSprite.NEUTRAL, defense);
					if (attacker != null) {
						GLog.i(Char.class, "you_missed", Message.actor(enemy), enemy.defenseText());
					} else if (defender != null) {
						GLog.i(Char.class, "enemy_missed", Message.actor(this), enemy.defenseText());
					} else {
						GLog.i(Char.class, "mob_missed", Message.actor(enemy), enemy.defenseText(), Message.actor(this));
					}

					Sample.INSTANCE.play(Assets.SND_MISS);
				}
			} catch (Exception e){}
			return false;

		}
	}

	protected int adjustHeroDamage(Hero hero, int damage) {
		if (LocalCoop.contains(hero) && hero != Dungeon.hero) {
			final int incoming = damage;
			return LocalCoop.withHero(hero, () -> adjustHeroDamage(hero, incoming));
		}
		damage *= Dungeon.currentDifficulty.damageModifier();
		damage *= hero.heroSkills.passiveA3.incomingDamageModifier();
		damage -= hero.heroSkills.passiveA3.incomingDamageReduction(damage);
		return damage;
	}

	public static boolean hit( Char attacker, Char defender, boolean magic ) {
		try {
			int bonusAttackSkill = 0;
			if(attacker instanceof Hero
					&& ((Hero) attacker).rangedWeapon instanceof Arrow){
				bonusAttackSkill = (LocalCoop.active() ? (Hero)attacker : Dungeon.hero).skillTree.aimedShot();
			}

			float acuRoll = Random.Float(attacker.attackSkill(defender) + bonusAttackSkill);
			float defRoll = Random.Float(defender.defenseSkill(attacker));

			if (defender instanceof Hero && Level.distance(attacker.pos, defender.pos) > 1 && ((Hero) defender).skillTree.rangedDodgeChance())
				return false;

			if ((magic ? acuRoll * 2 : acuRoll) < defRoll) return false;
			if (defender instanceof Hero && (magic || Level.distance(attacker.pos, defender.pos) > 1)
					&& ((Hero)defender).dodgeWithArmor(attacker)) return false;
			return true;
		}
		catch(Exception e){return false;}
	}

	public int attackSkill( Char target ) {
		return 0;
	}

	public int defenseSkill( Char enemy ) {
		return 0;
	}

	public String defenseVerb() {
		return defenseText().toString();
	}

    public Message defenseText() {
        return Message.key(Char.class, "def_verb");
    }

	public String displayName() {
		return Messages.maybeTranslate( name );
	}

    public Message nameMessage() {
        return Message.key(getClass(), "name");
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

	private int damageColor = -1;
	private Char damageAttacker;

	public void damageFrom(int dmg, Object src, Char attacker) {
		Char previous = damageAttacker;
		damageAttacker = attacker;
		try {
			damage(dmg, src);
		} finally {
			damageAttacker = previous;
		}
	}

	protected Char takeDamageAttacker() {
		Char attacker = damageAttacker;
		damageAttacker = null;
		return attacker;
	}

	public void damage(int dmg, Object src, int color) {
		int previous = damageColor;
		damageColor = color;
		try {
			damage(dmg, src);
		} finally {
			damageColor = previous;
		}
	}

	public void damage( int dmg, Object src ) {

		if (HP <= 0) {
			return;
		}

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
					GLog.i(Char.class, "out_of_paralysis", Message.actor(this));
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
			sprite.showStatus( damageColor != -1 ? damageColor :
				(HP > HT / 2 ? CharSprite.WARNING : CharSprite.NEGATIVE),
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
            this.sprite.showStatus(CharSprite.NEUTRAL, deathScream());

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
				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.poison.name") );

			} else if (buff instanceof Amok) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.amok.name") );

			} else if (buff instanceof Slow) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.slow.name") );

			} else if (buff instanceof MindVision) {

				sprite.showStatus( CharSprite.POSITIVE, Message.key("custom.generated.entry_mind_d42c011296") );
				sprite.showStatus( CharSprite.POSITIVE, Message.key("custom.generated.entry_vision_625f139d6c") );

			} else if (buff instanceof Paralysis) {

				sprite.add( CharSprite.State.PARALYSED );
				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.paralysis.name") );

			} else if (buff instanceof Terror) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("custom.generated.entry_frightened_817fe62389") );

			} else if (buff instanceof Roots) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.roots.name") );

			} else if (buff instanceof Cripple) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.cripple.name") );

			} else if (buff instanceof Bleeding) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.buffs.bleeding.name") );

			} else if (buff instanceof Vertigo) {

				sprite.showStatus( CharSprite.NEGATIVE, Message.key("custom.generated.entry_dizzy_16199d8652") );

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
					sprite.showStatus( CharSprite.POSITIVE, Message.key("actors.buffs.invisibility.name") );
				}
				sprite.add( CharSprite.State.INVISIBLE );
			}
		}
	}

	public void remove( Buff buff ) {

		try
		{
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
	catch (Exception e){

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
			} else if (buff instanceof RebornChampion) {
				((RebornChampion)buff).updateSpriteState();
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

		if (LocalCoop.active() && Actor.findChar(pos) == this) Actor.freeCell(pos);
		pos = step;
		if (LocalCoop.active()) Actor.occupyCell(this);

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
