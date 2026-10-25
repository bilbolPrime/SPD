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
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Badges;
import com.bilboldev.pixeldungeonskills.Challenges;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.GauntletRun;
import com.bilboldev.pixeldungeonskills.Statistics;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Champ;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.buffs.Weakness;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.Flare;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.Wound;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.Corpse;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public abstract class Mob extends Char {
    public int rebornPack;

	private static final String	TXT_DIED	= "custom.generated.entry_you_hear_something_died_in_the_distance_c8daadf6e5";

	protected static final String	TXT_ECHO	= "echo of ";

	protected static final String TXT_NOTICE1	= "?!";

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
        if (rebornPack != 0) bundle.put("reborn_pack", rebornPack);
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
        rebornPack = bundle.getInt("reborn_pack");
	}

	public CharSprite sprite() {

		CharSprite sprite = null;
		try {
			sprite = spriteClass.newInstance();
		} catch (Exception e) {
		}
		return sprite;
	}

	public void storePartyRuntime(Bundle bundle) {
		super.storePartyRuntime(bundle);
		bundle.put("enemy", enemy == null ? 0 : enemy.id());
		bundle.put("seen", enemySeen);
		bundle.put("alerted", alerted);
		bundle.put("defense", defenseSkill);
		bundle.put("experience", EXP);
		bundle.put("max_level", maxLvl);
		bundle.put("range", range);
		bundle.put("hostile", hostile);
	}

	public void restorePartyRuntime(Bundle bundle) {
		super.restorePartyRuntime(bundle);
		Actor target = Actor.findById(bundle.getInt("enemy"));
		enemy = target instanceof Char ? (Char)target : null;
		enemySeen = bundle.getBoolean("seen");
		alerted = bundle.getBoolean("alerted");
		defenseSkill = bundle.getInt("defense");
		EXP = bundle.getInt("experience");
		maxLvl = bundle.getInt("max_level");
		range = bundle.getInt("range");
		hostile = bundle.getBoolean("hostile");
	}

	public void restorePartyEnemy(Char target) {
		enemy = target;
	}

	@Override
	protected boolean act() {

		super.act();

		boolean justAlerted = alerted;
		alerted = false;
		if(sprite == null){
			return true;
		}
		sprite.hideAlert();

		if (paralysed) {
			enemySeen = false;
			spend( TICK );
			return true;
		}

		enemy = chooseEnemy();

		if(enemy != null && !canAttack(enemy)){
			enemy = chooseNonHeroEnemy();
		}

		boolean enemyInFOV =
			enemy != null && enemy.isAlive() &&
			Level.fieldOfView[enemy.pos] && enemy.invisible <= 0;

		return state.act( enemyInFOV, justAlerted );
	}

	protected Char chooseEnemy() {

		if (buff( Amok.class ) != null) {
			if (enemy == Dungeon.hero || LocalCoop.contains(enemy) || enemy == null) {

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

		if (LocalCoop.active()) {
			if (enemy != null && enemy.isAlive() && Level.fieldOfView[enemy.pos] && enemy.invisible <= 0) {
				return enemy;
			}
			Hero nearest = null;
			for (Hero hero : LocalCoop.heroes()) {
				if (hero.isAlive() && hero.invisible <= 0 && Level.fieldOfView[hero.pos]
						&& (nearest == null || distance(hero) < distance(nearest))) {
					nearest = hero;
				}
			}
			if (nearest != null) return nearest;
			return enemy != null && enemy.isAlive() ? enemy : null;
		}
		return enemy != null && enemy.isAlive() ? enemy : Dungeon.hero;
	}

	protected Char chooseNonHeroEnemy() {

		if (buff( Amok.class ) != null) {
			return chooseEnemy();
		}

		Terror terror = (Terror)buff( Terror.class );
		if (terror != null) {
			return chooseEnemy();
		}
		if (LocalCoop.active()) {
			for (Hero hero : LocalCoop.heroes()) {
				if (hero.isAlive() && hero.invisible <= 0 && Level.fieldOfView[hero.pos] && canAttack(hero)) {
					return hero;
				}
			}
		}

		HashSet<Mob> enemies = new HashSet<Mob>();
		for (Mob mob:Dungeon.level.mobs) {
			if (mob != this && Level.fieldOfView[mob.pos]
					&& ((mob instanceof HiredMerc) || (mob instanceof SummonedPet))
			&& canAttack(mob)) {
				enemies.add( mob );
			}
		}
		if (enemies.size() > 0) {
			return Random.element( enemies );
		}

		if (LocalCoop.active()) return chooseEnemy();
		return enemy != null && enemy.isAlive() ? enemy : Dungeon.hero;
	}

	protected Hero targetHero() {
		if (!LocalCoop.active()) return Dungeon.hero;
		Dungeon.level.updateFieldOfView(this);
		Char chosen = chooseEnemy();
		if (LocalCoop.contains(chosen)) return (Hero)chosen;
		Hero nearest = null;
		for (Hero hero : LocalCoop.heroes()) {
			if (hero.isAlive() && (nearest == null || distance(hero) < distance(nearest))) nearest = hero;
		}
		return nearest != null ? nearest : LocalCoop.primary();
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
				sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.mobs.mob.rage"));
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
			sprite.showStatus( CharSprite.NEGATIVE, Message.key("actors.mobs.mob.rage"));
			state = HUNTING;
		}
	}

	protected boolean canAttack( Char enemy ) {
		return (Level.adjacent( pos, enemy.pos ) || (Level.distance(pos, enemy.pos) <= range  &&  Ballistica.cast(pos, enemy.pos, false, true) == enemy.pos)) && !isCharmedBy( enemy );
	}

    public boolean canBeKnockedBackInto(int newPos)
    {

        return !rooted && newPos >= 0 && newPos < Level.LENGTH && Level.adjacent(pos, newPos)
                && (Level.passable[newPos] || Level.avoid[newPos]) && !Level.pit[newPos]
                && Actor.findChar(newPos) == null;
    }

	protected boolean getCloser( int target ) {

		if (rooted) {
			return false;
		}

		int step = Dungeon.findPath( this, pos, target,
			Dungeon.rules().hasRebornBosses() ? RebornBoss.movementFloor() : Level.passable,
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
			Dungeon.rules().hasRebornBosses() ? RebornBoss.movementFloor() : Level.passable,
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
		float delay = prepareRebornAttack(enemy, attackDelay());

		boolean visible = Dungeon.visible[pos];

		if (visible) {
			sprite.attack( enemy.pos );
		} else {
			attack( enemy );
		}

		spend( delay );

		return !visible;
	}

	protected float prepareRebornAttack(Char enemy, float delay) {
		RebornChampion champion = buff(RebornChampion.class);
		if (champion != null) {
			champion.beginAttack(enemy);
			delay *= champion.delayMultiplier();
		}
		return delay;
	}

	@Override
	public boolean attack(Char enemy) {
		RebornChampion champion = buff(RebornChampion.class);
		if (champion == null) return super.attack(enemy);
		champion.ensureAttack(enemy);
		try {
			if (!isAlive() || enemy == null || !enemy.isAlive()) return false;
			boolean hit = super.attack(enemy);
			if (champion.doubleAttack() && isAlive() && enemy.isAlive() && canAttack(enemy)) {

				hit = super.attack(enemy) || hit;
			}
			return hit;
		} finally {
			champion.finishAttack();
		}
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
		if (!enemySeen && (enemy == Dungeon.hero || LocalCoop.contains(enemy))
				&& ((Hero)enemy).subClass == HeroSubClass.ASSASSIN) {
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

		boolean survivor = Dungeon.hero.isAlive();
		if (LocalCoop.active()) {
			for (Hero hero : LocalCoop.heroes()) survivor |= hero.isAlive();
		}
		if (survivor) {

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

			if (LocalCoop.active()) {
				for (Hero hero : LocalCoop.heroes()) {
					if (hero.isAlive()) LocalCoop.withHero(hero, () -> giveExperience());
				}
			} else {
				giveExperience();
			}
		}
	}

	private void giveExperience() {
		int exp = exp();
		if (exp <= 0) return;
		if (champ > 0) {
			exp = (int)Math.ceil(1.5 * exp);
			Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, Message.key("custom.generated.entry_dexp_champion_killed_d2c1bc2f19", exp));
		} else {
			Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, Message.key("custom.generated.entry_dexp_1797e813c2", exp));
		}
		Dungeon.hero.earnExp(exp);
	}

	public int exp() {
		return Dungeon.hero.lvl <= maxLvl ? EXP : 0;
	}

	@Override
	public void die( Object cause ) {

		super.die( cause );
		PartySession.mobDied(this);

		if (Dungeon.hero != null && hostile && !(this instanceof NPC) && !(this instanceof SummonedPet)) {
			int wantedGold = Dungeon.hero.skillTree == null ? 0 : Dungeon.hero.skillTree.getWantedGoldBonus();
			if (wantedGold > 0) {
				Dungeon.gold += wantedGold;
				Statistics.goldCollected += wantedGold;
				Badges.validateGoldCollected();
				Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, Message.key("actors.mobs.mob.gold", wantedGold));
			}
		}

        if(this instanceof SummonedPet)
            SummonedPet.summonedPets--;

        if(champ > 0)
            dropLootGuaranteed();

		boolean eligible = Dungeon.hero.lvl <= maxLvl + 2;
		Hero necromancer = Dungeon.hero.heroClass == HeroClass.NECROMANCER ? Dungeon.hero : null;
		if (LocalCoop.active()) {
			for (Hero hero : LocalCoop.heroes()) {
				if (!hero.isAlive()) continue;
				eligible |= hero.lvl <= maxLvl + 2;
				if (hero.heroClass == HeroClass.NECROMANCER) necromancer = hero;
			}
		}
		if (eligible && !(GauntletRun.active() && noLoot)) {
			dropLoot();
		}

		if (necromancer != null
				&& !Bestiary.isBoss(this)
				&& hostile
				&& !(this instanceof NPC)
				&& !(this instanceof SummonedPet)) {
			Dungeon.level.drop( new Corpse(), pos ).sprite.drop();
			Sample.INSTANCE.play( Assets.SND_BONES );
			GLog.i(TXT_DIED);
		}
	}

	protected Object loot = null;
	protected float lootChance = 0;

	@SuppressWarnings("unchecked")

    protected void dropLootGuaranteed() {

		if(Random.Int(10) > Dungeon.currentDifficulty.championDropChanceNatural() || noLoot){
			return;
		}

		if(Random.Float() < 0.2f){
			Item item = Generator.random(  );
			Dungeon.level.drop( item, pos ).sprite.drop();
		}
        else {
			Item item = Generator.randomConsumable(  );
			Dungeon.level.drop( item, pos ).sprite.drop();
		}

    }

    boolean noLoot = false;
    public void noLoot(){
		lootChance = 0;

		noLoot = true;
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
				item.randomPrefix();

			}

			Dungeon.level.drop( item, pos ).sprite.drop();
		}
	}

	public boolean reset() {
		return false;
	}

    public void champEffect(Char enemy, int damage )
    {
        if(enemy == null)
            return;

        if(enemy instanceof NPC)
            return;

        if(!(enemy instanceof Hero))
            return;

        try {
            if (champ != -1) {
                if (champ == Champ.CHAMP_VAMPERIC) {
                    int reg = Math.min(Dungeon.rules().hasRebornEncounters() ? damage/2 : damage, HT - HP);

                    if (reg > 0) {
                        HP += reg;
                        sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
                    }
                } else if (champ == Champ.CHAMP_CURSED) {
                    Buff.affect(enemy, Weakness.class, Dungeon.rules().hasRebornEncounters() ? 3 : 5);
                } else if (champ == Champ.CHAMP_FOUL) {
                    Buff.affect(enemy, Poison.class).set(Dungeon.rules().hasRebornEncounters() ? 2 : 5);
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
		String localized = message( "desc" );
		return localized != null ? localized : Messages.maybeTranslate( "Real description is coming soon!" );
	}

	public String displayName() {
		String localized = message( "name" );
		RebornChampion reborn = buff(RebornChampion.class);
		if (reborn != null) return reborn.displayName(localized != null ? localized : name);
		return localized != null ? localized : name;
	}

    @Override public Message nameMessage() {
        Message base = super.nameMessage();
        return buff(RebornChampion.class) == null ? base : Message.key(RebornChampion.class, "name", base);
    }

	public String displayDescription() {
		String localized = message( "desc" );
		return localized != null ? localized : Messages.maybeTranslate( description() );
	}

	protected String stateMessage( String key ) {
		return Messages.get( Mob.class, key, displayName() );
	}

	protected String message( String key, Object... args ) {
		String value = Messages.get( this, key, args );
		return Messages.NO_TEXT_FOUND.equals( value ) ? null : value;
	}

	public void notice() {
		sprite.showAlert();
        if (rebornPack != 0 && enemy != null) {
            for (Mob mob : Dungeon.level.mobs) {
                if (mob != this && mob.rebornPack == rebornPack && mob.state == mob.SLEEPEING
                        && Level.distance(pos, mob.pos) <= 6) {

                    mob.state = mob.WANDERING;
                    mob.target = enemy.pos;
                }
            }
        }
	}

	public void yell(Message str) {
		GLog.n("custom.generated.entry_s_s_3ac152a1c1", Message.actor(this), str);
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
			return stateMessage( "sleeping" );
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
			return stateMessage( "wandering" );
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
			return stateMessage( "hunting" );
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
			return stateMessage( "fleeing" );
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
			return stateMessage( "passive" );
		}
	}

}
