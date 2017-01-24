package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.CrabSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.HashSet;

/**
 * Created by Moussa on 24-Jan-17.
 */
public class SummonedPet extends NPC {

    public static enum PET_TYPES
    {
        RAT("Rat"), CRAB("Crab"), SKELETON("Skeleton");
        public String type = "Rat";
        PET_TYPES(String type) {this.type = type;}

        public String getName()
        {
            return "Summoned " + type;
        }

        public int getHealth(int level)
        {
            switch (this)
            {
                case RAT: return 7 + level;
                case CRAB: return 10 + 2 * level;
                case SKELETON: return 15 + 3 * level;
            }

            return 1;
        }

        public int getDamage(int level)
        {
            switch (this)
            {
                case RAT: return Random.NormalIntRange(1, 5) + level;
                case CRAB: return Random.NormalIntRange(2, 7) + level;
                case SKELETON: return Random.NormalIntRange(3, 10) + level;
            }
            return 1;
        }

        public int getDefence(int level)
        {
            switch (this)
            {
                case RAT: return level;
                case CRAB: return 2 * level;
                case SKELETON: return 3 * level;
            }
            return 1;
        }

        public String getDescription()
        {
            switch (this)
            {
                case RAT: return "Summoned rats will protect their master mage.";
                case CRAB: return "Summoned crabs will protect their master mage.";
                case SKELETON: return "Summoned skeletons will protect their master mage.";
            }
            return "";
        }

        public Class<? extends CharSprite> getSprite()
        {
            switch (this)
            {
                case RAT: return RatSprite.class;
                case CRAB: return CrabSprite.class;
                case SKELETON: return SkeletonSprite.class;
            }
            return RatSprite.class;
        }
    }

    public static final int SUMMONED_PETS_LIMIT = 3;
    public static final int DEGRADE_RATE = 15;

    public static int summonedPets = 0;

    public PET_TYPES petType = PET_TYPES.RAT;


    public int degradeCounter = 1;

    public static final String PET_TYPE = "pettype";

    {
        name = "Summoned Rat";
        spriteClass = RatSprite.class;

        viewDistance = 4;

        WANDERING = new Wandering();

        flying = false;
        state = WANDERING;
    }

    private int level;

    private static final String LEVEL	= "level";


    public SummonedPet(PET_TYPES pet)
    {
        this.petType = pet;
        summonedPets++;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( LEVEL, level );
        bundle.put( PET_TYPE, petType );
        summonedPets = 0; // Game is saving, set summoned pets to 0
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        petType = PET_TYPES.valueOf(bundle.getString( PET_TYPE ));
        spawn( bundle.getInt( LEVEL) , HP);
    }

    public void spawn( int level ) {
        this.level = level;

        HT = petType.getHealth(level);
        HP = HT;
        defenseSkill = petType.getDefence(level);

        spriteClass = petType.getSprite();
    }

    public void spawn( int level, int maintainHP) {
        this.level = level;

        HT = petType.getHealth(level);
        HP = maintainHP;
        defenseSkill = petType.getDefence(level);

        spriteClass = petType.getSprite();
    }

    @Override
    public int attackSkill( Char target ) {
        return defenseSkill;
    }

    @Override
    public int damageRoll() {
        return petType.getDamage(level);
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        if (enemy instanceof Mob) {
            ((Mob)enemy).aggro( this );
        }
        return damage;
    }

    @Override
    protected boolean act() {
        degradeCounter++;
        if(degradeCounter % DEGRADE_RATE == 0)
            HP--;

        if(summonedPets > SUMMONED_PETS_LIMIT + Dungeon.hero.heroSkills.passiveB3.summoningLimitBonus())
            HP = 0;

        if (HP <= 0) {
            die( null );
            return true;
        } else {
            return super.act();
        }
    }


    protected Char chooseEnemy() {

        if (enemy == null || !enemy.isAlive()) {
            HashSet<Mob> enemies = new HashSet<Mob>();
            for (Mob mob:Dungeon.level.mobs) {
                if (mob.hostile && Level.fieldOfView[mob.pos]) {
                    enemies.add( mob );
                }
            }

            return enemies.size() > 0 ? Random.element( enemies ) : null;

        } else {

            return enemy;

        }
    }

    @Override
    public String description() {
        return
                petType.getDescription();
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();
    static {
        IMMUNITIES.add( Poison.class );
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }


    private class Wandering implements AiState {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {
            if (enemyInFOV) {

                enemySeen = true;

                notice();
                state = HUNTING;
                target = enemy.pos;

            } else {

                enemySeen = false;

                int oldPos = pos;
                if (getCloser( Dungeon.hero.pos )) {
                    spend( 1 / speed() );
                    return moveSprite( oldPos, pos );
                } else {
                    spend( TICK );
                }

            }
            return true;
        }

        @Override
        public String status() {
            return Utils.format("This %s is wandering", petType.getName());
        }
    }

    @Override
    public void interact() {

        int curPos = pos;

        moveSprite( pos, Dungeon.hero.pos );
        move( Dungeon.hero.pos );

        Dungeon.hero.sprite.move( Dungeon.hero.pos, curPos );
        Dungeon.hero.move( curPos );

        Dungeon.hero.spend( 1 / Dungeon.hero.speed() );
        Dungeon.hero.busy();
    }
}
