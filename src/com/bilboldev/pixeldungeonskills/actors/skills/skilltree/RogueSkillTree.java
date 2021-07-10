package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.actors.skills.Assassin;
import com.bilboldev.pixeldungeonskills.actors.skills.Bandit;
import com.bilboldev.pixeldungeonskills.actors.skills.DoubleStab;
import com.bilboldev.pixeldungeonskills.actors.skills.Evasion;
import com.bilboldev.pixeldungeonskills.actors.skills.Freerunner;
import com.bilboldev.pixeldungeonskills.actors.skills.LockSmith;
import com.bilboldev.pixeldungeonskills.actors.skills.Scorpion;
import com.bilboldev.pixeldungeonskills.actors.skills.ShadowCloneNew;
import com.bilboldev.pixeldungeonskills.actors.skills.SilentDeath;
import com.bilboldev.pixeldungeonskills.actors.skills.SmokeBomb;
import com.bilboldev.pixeldungeonskills.actors.skills.Stealth;
import com.bilboldev.pixeldungeonskills.actors.skills.Venom;

import java.util.ArrayList;

public class RogueSkillTree extends SkillTree {
    {
        skillTreeType = SkillTreeType.ROGUE;
    }
    public RogueSkillTree(float x, float y) {
        super(x, y);
    }

    @Override
    public ArrayList<ColorBlock> getBackground() {
        ArrayList<ColorBlock> toReturn = new ArrayList<>();

        // 1st level
        ColorBlock bg = new ColorBlock( SLOT_SIZE * 7, 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE + SLOT_SIZE / 2 - 1;
        toReturn.add( bg );

        // Mana
        bg = buildBlock(1, 1);
        toReturn.add( bg );

        // Mana Reg
        bg = buildBlock(2, 1);
        toReturn.add( bg );

        // Mana Armor
        bg = buildBlock(4, 1);
        toReturn.add( bg );

        // 2nd level
        bg = new ColorBlock( SLOT_SIZE * 2, 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE * 5 - 1;
        bg.y =   y  + SLOT_SIZE + SLOT_SIZE / 2 - 1;
        toReturn.add( bg );

        bg = new ColorBlock( SLOT_SIZE * 2, 2, NORMAL );
        bg.x = x + SLOT_SIZE  * 5;
        bg.y =  y  + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add( bg );

        // Mana
        bg = buildBlock(1, 2);
        toReturn.add( bg );

        // Mana Reg
        bg = buildBlock(2, 2);
        toReturn.add( bg );

        // Mana Armor
        bg = buildBlock(3, 2);
        toReturn.add( bg );

        // Mana Armor
        bg = buildBlock(4, 2);
        toReturn.add( bg );

        // 3rd level
        bg = new ColorBlock( SLOT_SIZE * 7, 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE * 3 + SLOT_SIZE  + 6;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE ;
        bg.y = y  + SLOT_SIZE * 2 + SLOT_SIZE / 2 + 6;
        toReturn.add( bg );

        // Mana
        bg = buildBlock(2, 3);
        toReturn.add( bg );

        // Mana Reg
        bg = buildBlock(4, 3);
        toReturn.add( bg );


        // 4th level
        bg = new ColorBlock( SLOT_SIZE * 2, 2, NORMAL );
        bg.x = x + SLOT_SIZE  * 5;
        bg.y = y  + SLOT_SIZE * 3 + SLOT_SIZE  + 6 + SLOT_SIZE *3/2;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE * 5 - 1;
        bg.y =  y  + SLOT_SIZE * 4 + SLOT_SIZE / 2;
        toReturn.add( bg );

        bg = new ColorBlock( 2, SLOT_SIZE + SLOT_SIZE / 2, NORMAL );
        bg.x = x + SLOT_SIZE * 3 - 1;
        bg.y =  y  + SLOT_SIZE * 4 + SLOT_SIZE / 2;
        toReturn.add( bg );


        // Mana
        bg = buildBlock(2, 4);
        toReturn.add( bg );

        // Mana Reg
        bg = buildBlock(3, 4);
        toReturn.add( bg );

        // Mana Armor
        bg = buildBlock(4, 4);
        toReturn.add( bg );

        return toReturn;
    }

    @Override
    public SkillTree build(){
        skillDictionary.clear();
        addSkill(new Bandit());
        addSkill(new LockSmith());
        addSkill(new DoubleStab());

        addSkill(new Stealth());
        addSkill(new SmokeBomb());
        addSkill(new Freerunner());
        addSkill(new ShadowCloneNew());

        addSkill(new Venom());
        addSkill(new Evasion());

        addSkill(new Scorpion());
        addSkill(new Assassin());
        addSkill(new SilentDeath());

        return this;
    }

    @Override
    public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> toReturn = new ArrayList<>();

        toReturn.add(new SkillContainer(getSkill(Bandit.class), 1, 1));
        toReturn.add(new SkillContainer(getSkill(LockSmith.class), 2, 1));
        toReturn.add(new SkillContainer(getSkill(DoubleStab.class), 4, 1));

        toReturn.add(new SkillContainer(getSkill(Stealth.class), 1, 2));
        toReturn.add(new SkillContainer(getSkill(SmokeBomb.class), 2, 2));
        toReturn.add(new SkillContainer(getSkill(Freerunner.class), 3, 2));
        toReturn.add(new SkillContainer(getSkill(ShadowCloneNew.class), 4, 2));

        toReturn.add(new SkillContainer(getSkill(Venom.class), 2, 3));
        toReturn.add(new SkillContainer(getSkill(Evasion.class), 4, 3));

        toReturn.add(new SkillContainer(getSkill(Scorpion.class), 2, 4));
        toReturn.add(new SkillContainer(getSkill(Assassin.class), 3, 4));
        toReturn.add(new SkillContainer(getSkill(SilentDeath.class), 4, 4));

        return toReturn;
    }

    @Override
    public int getLootBonus(int gold){
        return getSkill(Bandit.class).lootBonus(gold);
    };

    @Override
    public boolean disableTrap() {return getSkill(LockSmith.class).disableTrap();}

    @Override
    public boolean doubleStab()
    {
        return getSkill(DoubleStab.class).doubleStab();
    }

    @Override
    public boolean venomousAttack(){return getSkill(Venom.class).venomousAttack();}


    @Override
    public float getMeleeModifier(){
        try
        {
            float damageModifier = 1f;
            damageModifier *= getSkill(DoubleStab.class).damageModifier();
            damageModifier *= getSkill(Venom.class).damageModifier();
            return damageModifier;
        }
        catch (Exception e){
            return 1f;
        }
    }

    @Override
    public float venomDamageModifier(){
        return getSkill(Scorpion.class).venomDamageModifier();
    }

    @Override
    public int getStealthBonus(){
        return getSkill(Stealth.class).stealthBonus() + getSkill(Assassin.class).stealthBonus();
    }

    @Override
    public int evasionDefenceBonus() {return getSkill(Evasion.class).evasionDefenceBonus(); }

    @Override
    public boolean instantKill() { return getSkill(SilentDeath.class).instantKill(); }
}
