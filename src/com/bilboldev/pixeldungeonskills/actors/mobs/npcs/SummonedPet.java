package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.CrabSprite;
import com.bilboldev.pixeldungeonskills.sprites.ElementalSprite;
import com.bilboldev.pixeldungeonskills.sprites.EliteJoeSprite;
import com.bilboldev.pixeldungeonskills.sprites.EyeSprite;
import com.bilboldev.pixeldungeonskills.sprites.JoelSprite;
import com.bilboldev.pixeldungeonskills.sprites.MirrorSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.SummonedRatSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;

public class SummonedPet extends NPC {

    boolean dessummonImmunity = true;

    public static enum PET_TYPES
    {
        RAT("Rat"), CRAB("Crab"), SKELETON("Skeleton"), SKELETON_ARCHER("Skeleton Archer"), SPECIAL("Special"), FIRE_ELEMENTAL("Fire Elemental");
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
                case SKELETON_ARCHER:
                case SKELETON: return 15 + 3 * level;
                case FIRE_ELEMENTAL: return 5 + 10 * level;
            }

            return 1;
        }

        public int getDamage(int level)
        {
            switch (this)
            {
                case RAT: return Random.NormalIntRange(1, 5) + level;
                case CRAB: return Random.NormalIntRange(2, 7) + level;
                case SKELETON_ARCHER:
                case SKELETON: return Random.NormalIntRange(3, 10) + level;
                case FIRE_ELEMENTAL: return Random.NormalIntRange(1, Math.min(10, level * 3)) + Math.min(4, level * 1);
            }
            return 1;
        }

        public int getDefence(int level)
        {
            switch (this)
            {
                case RAT: return level;
                case CRAB: return 2 * level;
                case SKELETON_ARCHER:
                case SKELETON: return 3 * level;
                case FIRE_ELEMENTAL: return 5 + Math.min(5, 1 * level);
            }
            return 1;
        }

        public String getDescription()
        {
            switch (this)
            {
                case RAT: return "Summoned rats will protect their master mage.";
                case CRAB: return "Summoned crabs will protect their master mage.";
                case SKELETON_ARCHER: return "Summoned skeleton archers will protect their master mage.";
                case SKELETON: return "Summoned skeletons will protect their master mage.";
                case FIRE_ELEMENTAL: return "Summoned fire elementals will protect their master mage.";
            }
            return "";
        }

        public Class<? extends CharSprite> getSprite()
        {
            switch (this)
            {
                case RAT: return SummonedRatSprite.class;
                case CRAB: return CrabSprite.class;
                case SKELETON_ARCHER:
                case SKELETON: return SkeletonSprite.class;
                case FIRE_ELEMENTAL: return ElementalSprite.class;
            }
            return RatSprite.class;
        }
    }

    private Message customName;

    public void nameText(Message text) {
        customName = text;
        name = text.toString();
    }

    public Message nameText() {
        if (customName != null) return customName;
        switch (petType) {
            case RAT: return Message.key("custom.generated.entry_summoned_rat_728222b62f");
            case CRAB: return Message.key("custom.generated.entry_summoned_crab_b5eaa971cd");
            case SKELETON: return Message.key("custom.generated.entry_summoned_skeleton_ad12d81059");
            case SKELETON_ARCHER: return Message.key("actors.mobs.npcs.summonedpet.skeleton_archer");
            case FIRE_ELEMENTAL: return Message.key("actors.mobs.npcs.summonedpet.fire_elemental");
            default: return Message.key("actors.mobs.npcs.summonedpet.spirit");
        }
    }

    @Override
    public String displayName() {
        return nameText().toString();
    }

    @Override
    public Message nameMessage() {
        return nameText();
    }

    public static final int SUMMONED_PETS_LIMIT = 1;
    public static final int DEGRADE_RATE = 15;

    public static int summonedPets = 0;

    public PET_TYPES petType = PET_TYPES.RAT;

    public int degradeCounter = 1;

    public int range = 1;

    private int customDegradeRate = DEGRADE_RATE;

    public static final String PET_TYPE = "pettype";
    public static final String NAME = "name";
    public static final String SKILL = "skill";
    public static final String SPRITE = "sprite";
    public static final String MAX_HEALTH = "maxhealth";
    public static final String HEALTH = "health";
    public static final String RANGE = "range";

    {
        name = "Summoned Rat";
        spriteClass = RatSprite.class;

        viewDistance = 4;

        WANDERING = new Wandering();

        flying = false;
        state = WANDERING;
    }

    public int level;

    private static final String LEVEL	= "level";

    public SummonedPet()
    {
        super();
    }

    public SummonedPet(PET_TYPES pet)
    {
        this.petType = pet;
        summonedPets++;
        SummonedPet.killExcessPets();
    }

    public SummonedPet(Class<? extends CharSprite> spriteClass)
    {
        this.petType = PET_TYPES.SPECIAL;
        this.spriteClass = spriteClass;
        level = 1;

        if(spriteClass == EyeSprite.class || spriteClass == JoelSprite.class)
        {
            range = 10;
        }

        if(spriteClass == EliteJoeSprite.class)
        {
            range = 5;
        }
    }

    public void hunt(){
        state = HUNTING;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( LEVEL, level );
        bundle.put( PET_TYPE, petType );
        if (nameText().localText() != null) bundle.put(NAME, name);
        else bundle.put("name_text", nameText().store());
        bundle.put( SKILL, defenseSkill );
        bundle.put( HEALTH, HP );
        bundle.put( MAX_HEALTH, HT );
        bundle.put( RANGE, range );
        bundle.put( SPRITE, spriteClass.toString().replace("class ","" ) );
        summonedPets = 0;
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        petType = PET_TYPES.valueOf(bundle.getString( PET_TYPE ));
        level = bundle.getInt( LEVEL );
        if (bundle.contains("name_text")) nameText(Message.restore(bundle.getBundle("name_text")));
        else nameText(Message.local(bundle.getString(NAME)));

        try {
            spriteClass = (Class<? extends CharSprite>)Class.forName(bundle.getString(SPRITE));
        }
        catch (Exception ex)
        {
            spriteClass = RatSprite.class;
        }

        if(petType != PET_TYPES.SPECIAL)
            summonedPets++;

        spawn(level);

        defenseSkill = bundle.getInt(SKILL);
        HP = bundle.getInt( HEALTH );
        HT =  bundle.getInt( MAX_HEALTH );
        range =   bundle.getInt( RANGE);
    }

    public void spawn( int level ) {
        this.level = level;

        customDegradeRate = DEGRADE_RATE;

        HT = petType.getHealth(level);
        HP = HT;
        defenseSkill = petType.getDefence(level);

        if(petType != PET_TYPES.SPECIAL) {
            spriteClass = petType.getSprite();
            name = petType.getName();
        }

        if(petType == PET_TYPES.SKELETON_ARCHER)
            range = 4;
    }

    public void spawn( int level, int maintainHP) {
        this.level = level;

        customDegradeRate = DEGRADE_RATE;

        HT = petType.getHealth(level);
        HP = maintainHP;
        defenseSkill = petType.getDefence(level);

        spriteClass = petType.getSprite();

        name = petType.getName();
    }

    @Override
    public int attackSkill( Char target ) {
        return defenseSkill;
    }

    @Override
    public int damageRoll() {
        if(petType != PET_TYPES.SPECIAL)
            return petType.getDamage(level);
        else
            return Random.Int(defenseSkill / 3, defenseSkill);
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        if (enemy instanceof Mob) {
            if (Level.distance(pos, enemy.pos) < 2) {
                ((Mob)enemy).aggro( this );
            }
        }

        return ((int)(damage * owner().skillTree.getMinionDamageModifier()));
    }

    @Override
    public int defenseProc( Char enemy, int damage ) {
        damage = ((int)(damage * owner().skillTree.getMinionIncomingDamageModifier()));
        return super.defenseProc(enemy, damage);
    }

    @Override
    protected boolean canAttack( Char enemy ) {

        if(Level.distance(pos, enemy.pos) > range)
            return super.canAttack(enemy);

        return Ballistica.cast( pos, enemy.pos, false, true ) == enemy.pos;
    }

    boolean checkedImage;

    @Override
    protected boolean act() {

        if(!checkedImage){
            try {
                if(spriteClass == MirrorSprite.class){
                    ((MirrorSprite)sprite).updateArmor(level);
                }
            }
            catch (Exception ex)
            {

            }

            checkedImage = true;
        }
        if(MissionScene.scenePause == true)
        {
            spend(1f);
            next();
            return false;
        }

        degradeCounter++;

        if(petType != PET_TYPES.SPECIAL) {
            if (degradeCounter % customDegradeRate == 0)
                HP--;

        }

        if (HP <= 0) {
            die( null );
            return true;
        } else {
            try
            {
                sprite. tint( 1.4f, 1.00f, 1.00f, 0.2f );
                sprite.alpha(0.7f);
            }
            catch (Exception e){

            }

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
                if (getCloser( owner().pos )) {
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
            return stateMessage( "wandering" );
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

    public static void killExcessPets(){
        try
        {
            ArrayList<Mob> pets = new ArrayList<>();
            for (Mob mob : Dungeon.level.mobs) {
                if (mob instanceof SummonedPet
                        && (!LocalCoop.active() || ((SummonedPet)mob).owner() == Dungeon.hero)) {
                    pets.add(mob);
                }
            }

            if(pets.size() + 1 > SUMMONED_PETS_LIMIT  + Dungeon.hero.skillTree.summoningLimitBonus()){
                pets.get(0).die(null);
            }
        }
       catch (Exception e){

       }
    }

    public void setLifetimeMultiplier(int multiplier) {
        customDegradeRate = Math.max(1, DEGRADE_RATE * Math.max(1, multiplier));
    }

    public static void killClones(){
        if (LocalCoop.active()) {
            for (Mob mob : new ArrayList<>(Dungeon.level.mobs)) {
                if (mob instanceof SummonedPet && ((SummonedPet)mob).owner() == Dungeon.hero
                        && mob.spriteClass == MirrorSprite.class) {
                    mob.die(null);
                }
            }
            return;
        }
        try
        {
            for (Mob mob : Dungeon.level.mobs) {
                if (mob instanceof SummonedPet) {
                    if(((SummonedPet)mob).spriteClass == MirrorSprite.class){
                        mob.die(null);
                    }

                }
            }
        }
        catch (Exception e){

        }
    }

}
