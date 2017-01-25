package com.bilboldev.pixeldungeonskills.actors.mobs.npcs;

import com.bilboldev.noosa.tweeners.AlphaTweener;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.mobs.Brute;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.skills.Endurance;
import com.bilboldev.pixeldungeonskills.actors.skills.MercBruteSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercThiefSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.MercWizardSkillA;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.SummonRat;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MercSprite;
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
        Brute("Brute"), Wizard("Wizard"), Thief("Thief");
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
            }

            return 1;
        }

        public int getDamage(int level)
        {
            return getStrength(level) > 10 ? Random.IntRange( 1, getStrength(level) - 9 ) : 1;
        }

        public int getDefence(int level)
        {
            switch (this)
            {
                case Brute: return 2 *level;
                case Wizard: return  level;
                case Thief: return 3 * level;
            }
            return 1;
        }

        public String getDescription()
        {
            switch (this)
            {
                case Brute: return "Brutes are strong  but slow but can tank a lot of damage.";
                case Wizard: return "Wizards cast spells but cannot take a lot of punishment. They are also weak.";
                case Thief: return "Thieves are very agile and have admirable strength.";
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
            }
            return 0;
        }

        public int getWeaponPlaceHolder()
        {
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
            }

            return new Endurance();
        }

        public int getStrength(int level)
        {
            switch (this)
            {
                case Brute: return 13 + (int) (level / 3);
                case Wizard: return 10 + (int) (level / 5);
                case Thief: return 13 + (int) (level / 4);
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

    public static final int COST_RATE = 15;

    public static final String TXT_LEVEL_UP = "Stronger by the second...";
    public static final String TXT_CANT_EQUIP = "Too heavy for me Sir";

    public MERC_TYPES mercType = MERC_TYPES.Brute;


    public int skillCounter = 90;

    public Skill skill = null;

    public Item weapon = null;
    public Item armor = null;

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

    private int level;

    private static final String LEVEL	= "level";
    private static final String WEAPON		= "weapon";
    private static final String ARMOR		= "armor";

    public HiredMerc()
    {

    }

    public HiredMerc(MERC_TYPES merc)
    {
        this.mercType = merc;
        skill = mercType.getSkill();
        skill.level++;
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
        }
        armor = null;
        ((MercSprite)Dungeon.hero.hiredMerc.sprite).updateArmor();
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
        }
        catch (Exception ex)
        {
            // Being careful
        }

        return damage;
    }

    @Override
    public int attackProc( Char enemy, int damage ) {
        if (enemy instanceof Mob) {
            ((Mob)enemy).aggro( this );
        }
        try {
        if(weapon != null)
            ((Weapon)weapon).proc( this, enemy, damage );
        }
        catch (Exception ex)
        {
            // Being careful
        }

        try
        {
            if(skill.venomousAttack()) // <--- Rogue Venom when present
                Buff.affect(enemy, Poison.class).set(Poison.durationFactor(enemy));
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
        return mercType.getName() + " (LvL " + level + " STR: " + mercType.getStrength(level) + ")";
    }

    @Override
    public CharSprite sprite() {
        CharSprite s = super.sprite();
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

        return ((MeleeWeapon)weapon).damageRoll(this);
    }



    @Override
    protected boolean act() {
        skillCounter++;
      //  if(skillCounter % DEGRADE_RATE == 0)
      //      HP--;


        if(hackFix == false)
        {
            ((MercSprite)super.sprite).updateArmor();
            hackFix = true;
        }

        if(mercType.getSpecialSkillTime() != -1 && skillCounter % mercType.getSpecialSkillTime() == 0)
            skill.mercSummon();


        if (HP <= 0) {
            die( null );
            return true;
        } else {
            return super.act();
        }
    }

    @Override
    public void die(Object src)
    {
        super.die(src);

        if(weapon != null)
        {
            Dungeon.level.drop( weapon, pos ).sprite.drop();
        }

        if(armor != null)
        {
            Dungeon.level.drop( armor, pos ).sprite.drop();
        }

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
            return Utils.format("This %s is wandering", mercType.getName());
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
