package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Cripple;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.skills.Endurance;
import com.bilboldev.pixeldungeonskills.actors.skills.MercArcherSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercArcherSkillB;
import com.bilboldev.pixeldungeonskills.actors.skills.MercBruteSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercThiefSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercWizardSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.ClothArmor;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.food.ChargrilledMeat;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Dagger;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Knuckles;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Mace;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.FrostBow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

import java.util.HashSet;

/**
 * Created by Moussa on 24-Jan-17.
 */
public class HiredMerc extends NPC {

    public static enum MERC_TYPES
    {
        Brute("Brute"), Wizard("Wizard"), Thief("Thief"), Archer("Archer"), ArcherMaiden("ArcherMaiden");
        public String type = "Brute";
        MERC_TYPES(String type) {this.type = type;}

        public String getName()
        {
            return type;
        }

        public int getHealth(int level)
        {
            switch (this)
            {
                case Brute: return 20 + level * 3;
                case Wizard: return 10 + level;
                case Thief: return 15 + level * 2;
                case Archer: return 15 + level * 2;
                case ArcherMaiden: return 17 + level * 2;
            }

            return 1;
        }

        public int getDamage(int level)
        {
            if(this == ArcherMaiden || this == Archer)
                return getDamageRanged(level);
            return getStrength(level) > 10 ? Random.IntRange( 1, getStrength(level) - 9 ) : 1;
        }

        public int getDamageRanged(int level)
        {
            return getStrength(level) > 3 ? Random.IntRange( 1, getStrength(level) - 3 ) : 1;
        }

        public int getDefence(int level)
        {
            switch (this)
            {
                case Brute: return 2 * level;
                case Wizard: return  level;
                case Thief: return 3 * level;
                case Archer: return 3 * level;
                case ArcherMaiden: return 3 + level  + getStrength(level); // Can't equip armor
            }
            return 1;
        }

        public String getDescription()
        {
            switch (this)
            {
                case Brute: return "Brutes are strong and slow but can tank a lot of damage.";
                case Wizard: return "Wizards cast spells but cannot take a lot of punishment. They are also weak.";
                case Thief: return "Thieves are very agile and have admirable strength.";
                case Archer: return "Archers support from a distance.";
                case ArcherMaiden: return "Only the select few achieve the title of Archer-Maiden.";
            }
            return "";
        }


        public float speedModifier()
        {
            switch (this)
            {
                case Brute: return 0.7f;
                case Wizard: return 1f;
                case Thief: return 1.5f;
                case Archer: return 1.2f;
                case ArcherMaiden: return 1.3f;
            }
            return 1f;
        }

        public int getImage()
        {
            switch (this)
            {
                case Brute: return 0;
                case Wizard: return 24;
                case Thief: return 48;
                case Archer: return 72;
                case ArcherMaiden: return 104;
            }
            return 0;
        }

        public int getWeaponPlaceHolder()
        {
            if(this == ArcherMaiden || this == Archer)
                return ItemSpriteSheet.EMPTY_BOW;
            return ItemSpriteSheet.WEAPON;
        }

        public int getArmorPlaceHolder()
        {
            return ItemSpriteSheet.ARMOR;
        }

        public Skill getSkill()
        {
            switch (this)
            {
                case Brute: return new MercBruteSkillA();
                case Wizard: return new MercWizardSkillA();
                case Thief: return new MercThiefSkillA();
                case Archer: return new MercArcherSkillA();
                case ArcherMaiden: return new MercArcherSkillB();
            }

            return new Endurance();
        }

        public void setSkills(HiredMerc merc)
        {
            merc.skill =  new MercBruteSkillA();
            switch (this)
            {
                case Brute: merc.skill = new MercBruteSkillA(); merc.skill.level = 1;break;
                case Wizard: merc.skill = new MercWizardSkillA();merc.skill.level = 1; break;
                case Thief: merc.skill = new MercThiefSkillA(); merc.skill.level = 1;break;
                case Archer: merc.skill = new MercArcherSkillA(); merc.skill.level = 1;break;
                case ArcherMaiden: merc.skill = new MercArcherSkillA();  merc.skill.level = 1; merc.skillb = new MercArcherSkillB(); merc.skillb.level = 4; break;
            }
        }

        public void setEquipment(HiredMerc merc)
        {
            switch (this)
            {
                case Brute:
                    merc.weapon = new Mace().identify();
                    merc.armor = new LeatherArmor().identify();
                    merc.carrying = new ChargrilledMeat();
                    break;
                case Wizard:
                merc.weapon = new Knuckles().identify();
                merc.armor = new ClothArmor().identify();
                merc.carrying = new PotionOfHealing();
                    break;
                case Thief:    merc.weapon = new Dagger().identify();
                    merc.armor = new ClothArmor().identify();
                    merc.carrying = new PotionOfHealing();
                    break;
                case Archer:   merc.weapon = new Bow(1);
                    merc.armor = new ClothArmor().identify();
                    merc.carrying = new PotionOfHealing();break;
                case ArcherMaiden: merc.weapon = new FrostBow(1);  merc.carrying = new PotionOfHealing().identify(); break;
            }
        }

        public int getStrength(int level)
        {
            switch (this)
            {
                case Brute: return 13 + (int) (level / 3);
                case Wizard: return 10 + (int) (level / 5);
                case Thief: return 13 + (int) (level / 4);
                case Archer: return 11 + (int) (level / 4);
                case ArcherMaiden: return 12 + (int) (level / 4);
            }
            return 0;
        }

        public int getSpecialSkillTime()
        {
            switch (this)
            {
                case Wizard: return 100;
            }
            return -1;
        }
    }

    public static boolean archerMaidenUnlocked = false;
    public static final String MAIDEN_UNLOCK_BY= "Please consider donating to unlock this feature.";
    public static final int COST_RATE = 15;

    public static final String TXT_LEVEL_UP = "Stronger by the second...";
    public static final String TXT_CANT_EQUIP = "Too heavy for me Sir";

    public MERC_TYPES mercType = MERC_TYPES.Brute;


    public int rangedAttackCooldown = 0;
    public static final int RANGED_COOLDOWN = 5;

    public int skillCounter = 90;

    public Skill skill = null;
    public Skill skillb = null;
    public Skill skillc = null;

    public Item weapon = null;
    public Item armor = null;

    public Item carrying = null;

    public boolean hackFix = false;


    public static final String MERC_TYPE = "merctype";

    {
        name = "Brute";
        spriteClass = MercSprite.class;

        viewDistance = 4;

        WANDERING = new Wandering();

        flying = false;
        state = WANDERING;
    }

    public int level;

    private static final String LEVEL	= "level";
    private static final String WEAPON		= "weapon";
    private static final String ARMOR		= "armor";

    public HiredMerc()
    {

    }

    public HiredMerc(MERC_TYPES merc)
    {
        this.mercType = merc;
        //skill = mercType.getSkill();
        mercType.setSkills(this);
       // mercType.setEquipment(this); // done in hiring phase now
        //skill.level++;
    }

    public void equipWeapon(Item item)
    {
        unEquipWeapon();
        weapon = item;

        if(canEquip(weapon) == false)
        {
            unEquipWeapon();
            sprite.showStatus( CharSprite.NEGATIVE, TXT_CANT_EQUIP );
        }
    }

    public void unEquipWeapon()
    {
        if(weapon != null)
        {
            Dungeon.level.drop( weapon, pos ).sprite.drop();
        }
        weapon = null;
    }

    public void equipArmor(Item item)
    {
        unEquipArmor();
        armor = item;
        if(canEquip(armor) == false)
        {
            unEquipArmor();
            sprite.showStatus( CharSprite.NEGATIVE, TXT_CANT_EQUIP );
        }
        else
            ((MercSprite)Dungeon.hero.hiredMerc.sprite).updateArmor();
    }

    public void unEquipArmor()
    {
        if(armor != null)
        {
            Dungeon.level.drop( armor, pos ).sprite.drop();
            armor = null;
            if(HP > 0)
                ((MercSprite)Dungeon.hero.hiredMerc.sprite).updateArmor();
        }
    }

    public void equipItem(Item item)
    {
        unEquipItem();
        carrying = item;
    }


    public void unEquipItem()
    {
        if(carrying != null)
        {
            Dungeon.level.drop( carrying, pos ).sprite.drop();
        }
        carrying = null;
    }


    public int getArmorTier()
    {
        if(armor == null)
            return 0;
        return ((Armor) armor).tier;
    }

    public boolean canEquip(Item item)
    {
        if(item instanceof Weapon)
            return mercType.getStrength(level) >= ((Weapon)item).STR;
        if(item instanceof Armor)
            return mercType.getStrength(level) >= ((Armor)item).STR;
        return false;
    }


    @Override
    public int defenseProc( Char enemy, int damage ) {
        try {
            if (armor != null) {
                damage = ((Armor) armor).proc(enemy, this, damage);
            }
            damage *= skill.incomingDamageModifier();
        }
        catch (Exception ex)
        {
            // Being careful
        }



        return damage;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {

        rangedAttackCooldown = 0;

        if (enemy instanceof Mob) {
            if(mercType != MERC_TYPES.Archer && mercType != MERC_TYPES.ArcherMaiden  || Level.adjacent( pos, enemy.pos ))
                ((Mob)enemy).aggro( this );
        }
        try {
        if(weapon != null && !(weapon instanceof Bow))
            ((Weapon)weapon).proc( this, enemy, damage );

            if(weapon instanceof Bow)
            {
                ((Bow) weapon).bowSpecial(enemy);
            }
        }
        catch (Exception ex)
        {
            // Being careful
        }

        try
        {
            if(skill.venomousAttack()) // <--- Venom when present
                Buff.affect(enemy, Poison.class).set(Poison.durationFactor(enemy));

            if(skill.cripple()) // <-- KneeShot
                Buff.affect(enemy, Cripple.class);
        }
        catch (Exception ex)
        {
            // never too careful
        }

        return damage;
    }


    @Override
    public void storeInBundle( Bundle bundle ) {
        /*
        super.storeInBundle( bundle );
        bundle.put( LEVEL, level );
        bundle.put( MERC_TYPE, mercType);
        bundle.put( WEAPON, weapon );
        bundle.put( ARMOR, armor );
        */
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
         /*
        super.restoreFromBundle( bundle );
        mercType = MERC_TYPES.valueOf(bundle.getString(MERC_TYPE));
        skill = mercType.getSkill();
        weapon = (KindOfWeapon)bundle.get( WEAPON );

        armor = (Armor)bundle.get( ARMOR );

        spawn( bundle.getInt( LEVEL) , HP);
        */
    }


    public void spawn( int level ) {
        this.level = level;

        HT = mercType.getHealth(level);
        HP = HT;
        defenseSkill = mercType.getDefence(level);

        name = mercType.getName();
    }

    public void spawn( int level, int maintainHP) {
        this.level = level;

        HT = mercType.getHealth(level);
        HP = maintainHP;
        name =  mercType.getName();
        defenseSkill = mercType.getDefence(level);

    }

    public void skillLevel(int level)
    {
        if(skill != null)
            skill.level = level;
    }



    public void level()
    {
        level++;
        HT = mercType.getHealth(level);
        HP = HT;
        sprite.showStatus( CharSprite.POSITIVE, TXT_LEVEL_UP );

        if(skill.level < Skill.MAX_LEVEL)
            skill.level++;
    }

    public String getNameAndLevel()
    {
        return mercType.getName() + " (LvL " + level + (mercType != MERC_TYPES.ArcherMaiden ? " STR: " + mercType.getStrength(level) + ")" : ")");
    }

    @Override
    public CharSprite sprite() {
        CharSprite s = super.sprite();
        if(s instanceof MercSprite)
            ((MercSprite)s).updateArmor( mercType );
        return s;
    }

    @Override
    public int attackSkill( Char target ) {
        return defenseSkill;
    }

    @Override
    public int damageRoll()
    {
        if(weapon == null)
            return mercType.getDamage(level);

        if(weapon instanceof Bow)
            return (int)(mercType.getDamage(level) * 1.2f);

        if(weapon instanceof Boomerang)
            return ((Boomerang)weapon).damageRoll(this);

        if(weapon instanceof MeleeWeapon)
            return ((MeleeWeapon)weapon).damageRoll(this);

        return ((Weapon) weapon).damageRoll(this);
    }




    @Override
    protected boolean canAttack( Char enemy ) {

        if(mercType != MERC_TYPES.Archer && mercType != MERC_TYPES.ArcherMaiden)
            return super.canAttack(enemy);

        if(mercType == MERC_TYPES.ArcherMaiden)
            return Ballistica.castMaiden(pos, enemy.pos) == enemy.pos;


        return Ballistica.cast( pos, enemy.pos, false, true ) == enemy.pos;
    }


    @Override
    public boolean doAttack(Char enemy)
    {
        if(Level.adjacent(pos, enemy.pos) || mercType != MERC_TYPES.Archer && mercType != MERC_TYPES.ArcherMaiden)
            return super.doAttack(enemy);

        if(rangedAttackCooldown < RANGED_COOLDOWN)
        {
            sprite.showStatus(CharSprite.NEUTRAL, "Reloading Sir");
            spend( attackDelay() );
            next();
            return false;
        }

        return super.doAttack(enemy);
    }



    @Override
    protected boolean act() {

        spend( 0.01f ); // Fail safe to prevent complete game freeze.

        skillCounter++;
      //  if(skillCounter % DEGRADE_RATE == 0)
      //      HP--;

        rangedAttackCooldown++;

        if(hackFix == false)
        {
            ((MercSprite)super.sprite).updateArmor();
            hackFix = true;
        }

        if(mercType.getSpecialSkillTime() != -1 && skillCounter % mercType.getSpecialSkillTime() == 0)
            skill.mercSummon();




        if (HP <= 0) {

           // if(((MercSprite)super.sprite()).halo != null)
             //   ((MercSprite)super.sprite()).halo.putOut();
            die( null );
            return true;
        } else {

            return super.act();
        }
    }

    @Override
    public void die(Object src)
    {

        if(carrying instanceof PotionOfHealing)
        {
            GLog.p(" " + name + " consumed a Potion Of Healing ");
            super.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 4);
            HP = HT;
            carrying = null;
            return;
        }

        super.die(src);

        unEquipWeapon();
        unEquipArmor();
        unEquipItem();

        Dungeon.hero.hiredMerc = null;

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
                mercType.getDescription();
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

            if(pos == Dungeon.hero.pos)
                return true;

            if (enemyInFOV) {

                enemySeen = true;

                notice();
                state = HUNTING;
                target = enemy.pos;

            } else {

                enemySeen = false;

                int oldPos = pos;
                if (getCloser( Dungeon.hero.pos )) {
                    spend( 1 / (speed() * mercType.speedModifier()) );
                    return moveSprite( oldPos, pos );
                } else {
                    spend( TICK );
                }

            }
            return true;
        }

        @Override
        public String status() {
            return Utils.format("This %s is wandering", name);
        }
    }

    @Override
    public void interact() {
        if(pos == Dungeon.hero.pos)
            return;

        int curPos = pos;

        moveSprite( pos, Dungeon.hero.pos );
        move( Dungeon.hero.pos );

        Dungeon.hero.sprite.move( Dungeon.hero.pos, curPos );
        Dungeon.hero.move( curPos );

        Dungeon.hero.spend( 1 / Dungeon.hero.speed() );
        Dungeon.hero.busy();
    }
}
