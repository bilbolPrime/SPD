package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.skills.ActiveSkill;
import com.bilboldev.pixeldungeonskills.actors.skills.Assassin;
import com.bilboldev.pixeldungeonskills.actors.skills.BattleMage;
import com.bilboldev.pixeldungeonskills.actors.skills.Berserker;
import com.bilboldev.pixeldungeonskills.actors.skills.Freerunner;
import com.bilboldev.pixeldungeonskills.actors.skills.Gladiator;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.Sniper;
import com.bilboldev.pixeldungeonskills.actors.skills.Spark;
import com.bilboldev.pixeldungeonskills.actors.skills.Warden;
import com.bilboldev.pixeldungeonskills.actors.skills.Warlock;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public abstract class SkillTree {
    protected SkillTreeType skillTreeType = SkillTreeType.WARRIOR;

    protected static final int NORMAL		= 0xFF4A4D44;
    protected static final int EQUIPPED	= 0xFF63665B;

    protected static final int COLS_P	= 4;
    protected static final int COLS_L	= 6;

    protected static final int SLOT_SIZE	= 14;
    protected static final int SLOT_MARGIN	= 1;

    protected static final int TAB_WIDTH	= 25;

    protected static final int TITLE_HEIGHT	= 12;

    protected float x, y;

    protected ArrayList<Skill> skills;

    protected Hashtable<Class<? extends Skill>, Skill> skillDictionary = new Hashtable<>();

    protected SkillTree(float x, float y){
        this.x = x;
        this.y = y;
        skills = new ArrayList<>();
    }

    public abstract ArrayList<ColorBlock> getBackground();

    public abstract ArrayList<SkillContainer> getSkills();

    public SkillTree setCoords(float x, float y){
        this.x = x;
        this.y = y;
        return this;
    }

    protected ColorBlock buildBlock(int x, int y){
        ColorBlock toReturn = new ColorBlock(SLOT_SIZE, SLOT_SIZE, NORMAL );
        toReturn.x = this.x + (x != 4 ? SLOT_SIZE / 2 + SLOT_SIZE * 2 * (x - 1) : SLOT_SIZE * 7);
        toReturn.y = this.y  + SLOT_SIZE + (y - 1) * 3 / 2 * SLOT_SIZE + (y - 1) * SLOT_SIZE / 2;

        // F this BS
        if(y == 3){
            toReturn.y = 4 * SLOT_SIZE;
        }

        if(y == 4){
            toReturn.y =SLOT_SIZE * 3 + SLOT_SIZE+ SLOT_SIZE *3/2;
        }
        return toReturn;
    }

    protected SkillTree build(){
        return this;
    }

    protected void addSkill(Skill skill){
        skillDictionary.put(skill.getClass(), skill);
    }

    protected Skill getSkill(Class<? extends Skill> targetSkill){

        if(skillDictionary.containsKey(targetSkill)){
            return skillDictionary.get(targetSkill);
        }

        return null;
    }

    public float getMeleeModifier(){
        return 1f;
    }

    public float getDamageReductionModifier() { return 1f; }

    public int getMeleeAttackSkillBonus(){ return 0; };

    public float getMeleeSpeedBonus(){ return 1f; };

    public  float venomDamageModifier(){
        return 1f;
    }

    public float getSparkDamage(){
        try
        {
            return ((Spark)getSkill(Spark.class)).getDamage()
                    + getSkill(Warlock.class).sparkBonusDamage();
        }
        catch (Exception e){
            return 0f;
        }
    }

    public boolean getSparkBlind(){
        try
        {
            return ((Spark)getSkill(Spark.class)).getBlind();
        }
        catch (Exception e){
            return false;
        }
    }

    public float getWandRechargeSpeedModifier(){
        return 1f;
    }

    public float getWandDamageModifier(){
        return 1f;
    }

    public int SpiritArmorAbsorb(int damage) { return 0 ;}

    public int getRegenerationBonus(){ return 0; };

    public boolean canKnockBack() { return false; }

    public int getWeaponLevelBonus() { return  0; }

    public boolean MeleeAoEDamage() { return false; }

    public int getManaRegenerationBonus(){ return 0; };

    public int summoningLimitBonus() { return 0; }

    public float getMinionDamageModifier() {return 1f; }

    public float getMinionIncomingDamageModifier() { return 1f; }

    public int getLootBonus(int gold){ return 0; };

    public int getStealthBonus(){ return 0; };

    public int evasionDefenceBonus() { return 0; }

    public boolean instantKill() { return false; }

    public boolean disableTrap() {return false;}

    public boolean doubleStab()
    {
        return false;
    }

    public boolean venomousAttack(){return false;}

    public int getRangedAttackSkillBonus(){ return 0; };

    public float rangedDamageModifier() {return 1f;}

    public boolean rangedCripple() {return false;}

    public int getFletching() {return 0;}

    public int hunting() {return 0;}

    public boolean rangedDodgeChance() { return false; }

    public boolean rangedSleep(){return false;}
    public boolean rangedSlow(){return false;}
    public boolean arrowToBomb() {return false;}
    public int arrowToBombDamage() {return 0;}
    public float arrowToBombParalysis() {return 0f;}


    public boolean doubleShot() {return false;}
    public int aimedShot() { return 0;}

    public boolean canLevel(Skill skill){
        return Skill.availableSkill > 0 && skill.level < Skill.MAX_LEVEL && haveRequirements(skill);
    }

    public void advance(Skill skill){
        if(canLevel(skill))
        {
            if(skill.requestUpgrade()){
                GLog.p("You have gained a level in " + skill.name + "!");
            } else {
                GLog.n("Could not gain a level in " + skill.name + "!!!");
            }
        }
    }

    protected boolean haveRequirements(Skill skill){
        for(Class<? extends Skill> requirement : skill.getRequirements()){
            if(getSkill(requirement) == null || getSkill(requirement).level == 0){
                return false;
            }
        }
        return true;
    }

    public void subclassChosen(HeroSubClass heroSubClass){
        switch (heroSubClass){
            case GLADIATOR: getSkill(Gladiator.class).level = 1;
            break;
            case BERSERKER: getSkill(Berserker.class).level = 1;
            break;
            case BATTLEMAGE: getSkill(BattleMage.class).level = 1;
                break;
            case WARLOCK: getSkill(Warlock.class).level = 1;
                break;
            case FREERUNNER: getSkill(Freerunner.class).level = 1;
                break;
            case ASSASSIN: getSkill(Assassin.class).level = 1;
                break;
            case WARDEN: getSkill(Warden.class).level = 1;
                break;
            case SNIPER: getSkill(Sniper.class).level = 1;
                break;
        }
    }

    public void activate(Skill skill){
        for(Skill skill1 : skillDictionary.values()){
            if(skill1 instanceof ActiveSkill && skill1.getClass() != skill.getClass()){
                skill1.active = false;
            }
        }
    }

    public void restoreSkillsFromBundle( Bundle bundle ) {
        for(Skill skill1 : skillDictionary.values()){
            try
            {
                skill1.level = bundle.getInt( "SKILL_3.0_" + " " + skill1.getClass().getName());
                try
                {
                    skill1.availableAfter = (float)  bundle.getFloat("SKILL_3.0_COOLDOWN_" + " " + skill1.getClass().getName());
                }
                catch (Exception e){

                }
            }
            catch (Exception e){

            }
        }
    }

    public void storeInBundle( Bundle bundle )
    {
        for(Skill skill1 : skillDictionary.values()){
            try
            {
                bundle.put( "SKILL_3.0_" + " " + skill1.getClass().getName(),  skill1.level);
                bundle.put( "SKILL_3.0_COOLDOWN_" + " " + skill1.getClass().getName(),  skill1.availableAfter - Skill.skillTrack);
            }
            catch (Exception e){

            }
        }
    }

    public void castSpark(){

    }
}
