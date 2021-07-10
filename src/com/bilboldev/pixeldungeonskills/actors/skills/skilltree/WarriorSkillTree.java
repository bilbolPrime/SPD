package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.actors.skills.Aggression;
import com.bilboldev.pixeldungeonskills.actors.skills.Berserker;
import com.bilboldev.pixeldungeonskills.actors.skills.Endurance;
import com.bilboldev.pixeldungeonskills.actors.skills.FirmHand;
import com.bilboldev.pixeldungeonskills.actors.skills.Gladiator;
import com.bilboldev.pixeldungeonskills.actors.skills.KnockBack;
import com.bilboldev.pixeldungeonskills.actors.skills.Mastery;
import com.bilboldev.pixeldungeonskills.actors.skills.Rampage;
import com.bilboldev.pixeldungeonskills.actors.skills.Regeneration;
import com.bilboldev.pixeldungeonskills.actors.skills.Smash;
import com.bilboldev.pixeldungeonskills.actors.skills.Smite;
import com.bilboldev.pixeldungeonskills.actors.skills.Toughness;

import java.util.ArrayList;

public class WarriorSkillTree extends SkillTree {
    {
        skillTreeType = SkillTreeType.WARRIOR;
    }

    public WarriorSkillTree(float x, float y) {
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
        addSkill(new Smash());
        addSkill(new FirmHand());
        addSkill(new Mastery());

        addSkill(new Endurance());
        addSkill(new Regeneration());
        addSkill(new Gladiator());
        addSkill(new Smite());

        addSkill(new Toughness());
        addSkill(new Aggression());

        addSkill(new KnockBack());
        addSkill(new Berserker());
        addSkill(new Rampage());

        return this;
    }

    @Override
    public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> toReturn = new ArrayList<>();

        toReturn.add(new SkillContainer(getSkill(Smash.class), 1, 1));
        toReturn.add(new SkillContainer(getSkill(FirmHand.class), 2, 1));
        toReturn.add(new SkillContainer(getSkill(Mastery.class), 4, 1));

        toReturn.add(new SkillContainer(getSkill(Endurance.class), 1, 2));
        toReturn.add(new SkillContainer(getSkill(Regeneration.class), 2, 2));
        toReturn.add(new SkillContainer(getSkill(Gladiator.class), 3, 2));
        toReturn.add(new SkillContainer(getSkill(Smite.class), 4, 2));

        toReturn.add(new SkillContainer(getSkill(Toughness.class), 2, 3));
        toReturn.add(new SkillContainer(getSkill(Aggression.class), 4, 3));

        toReturn.add(new SkillContainer(getSkill(KnockBack.class), 2, 4));
        toReturn.add(new SkillContainer(getSkill(Berserker.class), 3, 4));
        toReturn.add(new SkillContainer(getSkill(Rampage.class), 4, 4));

        return toReturn;
    }

    @Override
    public float getMeleeModifier(){
        try
        {
            float damageModifier = getSkill(Smash.class).damageModifier()
            + getSkill(Smite.class).damageModifier() + getSkill(KnockBack.class).damageModifier()
            + getSkill(Rampage.class).damageModifier() + getSkill(Aggression.class).damageModifier()
            + getSkill(Berserker.class).damageModifier() - 5f;
            return damageModifier;
        }
        catch (Exception e){
            return 1f;
        }
    }

    @Override
    public float getDamageReductionModifier()
    {
        try
        {
            float damageReductionModifier = getSkill(Toughness.class).incomingDamageModifier()
                    + getSkill(Berserker.class).incomingDamageModifier() - 1;

            return damageReductionModifier;
        }
        catch (Exception e){
            return 1f;
        }
    }

    @Override
    public int getMeleeAttackSkillBonus()
    {
        try
        {
            return getSkill(FirmHand.class).toHitBonus() + getSkill(Gladiator.class).toHitBonus();
        }
        catch (Exception e){
            return 0;
        }
    };

    @Override
    public int getRegenerationBonus(){
        try
        {
            return getSkill(Regeneration.class).healthRegenerationBonus();
        }
        catch (Exception e){
            return 0;
        }
    };

    @Override
    public boolean canKnockBack() {
        try
        {
            return getSkill(KnockBack.class).knocksBack();
        }
        catch (Exception e){
            return false;
        }
    }
    @Override
    public int getWeaponLevelBonus() {
        try
        {
            return getSkill(Mastery.class).weaponLevelBonus();
        }
        catch (Exception e){
            return 0;
        }
    }

    @Override
    public float getMeleeSpeedBonus()
    {
        try
        {
            return getSkill(Mastery.class).meleeSpeedModifier() + getSkill(Gladiator.class).meleeSpeedModifier() + getSkill(Smite.class).meleeSpeedModifier() - 2f;
        }
        catch (Exception e){
            return 1f;
        }
    };

    @Override
    public boolean MeleeAoEDamage()
    {
        try
        {
            return getSkill(Rampage.class).AoEDamage();
        }
        catch (Exception e){
            return false;
        }
    }
}
