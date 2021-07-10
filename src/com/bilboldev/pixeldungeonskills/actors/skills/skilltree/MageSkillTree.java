package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.skills.Aggression;
import com.bilboldev.pixeldungeonskills.actors.skills.AggressiveMinions;
import com.bilboldev.pixeldungeonskills.actors.skills.BattleMage;
import com.bilboldev.pixeldungeonskills.actors.skills.Berserker;
import com.bilboldev.pixeldungeonskills.actors.skills.DarkBolt;
import com.bilboldev.pixeldungeonskills.actors.skills.Endurance;
import com.bilboldev.pixeldungeonskills.actors.skills.Enrage;
import com.bilboldev.pixeldungeonskills.actors.skills.FirmHand;
import com.bilboldev.pixeldungeonskills.actors.skills.Gladiator;
import com.bilboldev.pixeldungeonskills.actors.skills.KnockBack;
import com.bilboldev.pixeldungeonskills.actors.skills.MageActive;
import com.bilboldev.pixeldungeonskills.actors.skills.MagePassiveB;
import com.bilboldev.pixeldungeonskills.actors.skills.Mastery;
import com.bilboldev.pixeldungeonskills.actors.skills.Meditation;
import com.bilboldev.pixeldungeonskills.actors.skills.Rampage;
import com.bilboldev.pixeldungeonskills.actors.skills.Regeneration;
import com.bilboldev.pixeldungeonskills.actors.skills.Slow;
import com.bilboldev.pixeldungeonskills.actors.skills.Smash;
import com.bilboldev.pixeldungeonskills.actors.skills.Smite;
import com.bilboldev.pixeldungeonskills.actors.skills.Sorcerer;
import com.bilboldev.pixeldungeonskills.actors.skills.Spark;
import com.bilboldev.pixeldungeonskills.actors.skills.SpiritArmor;
import com.bilboldev.pixeldungeonskills.actors.skills.Spirituality;
import com.bilboldev.pixeldungeonskills.actors.skills.SummonRat;
import com.bilboldev.pixeldungeonskills.actors.skills.SummonSkeleton;
import com.bilboldev.pixeldungeonskills.actors.skills.Summoner;
import com.bilboldev.pixeldungeonskills.actors.skills.Toughness;
import com.bilboldev.pixeldungeonskills.actors.skills.Warlock;
import com.bilboldev.pixeldungeonskills.actors.skills.Wizard;

import java.util.ArrayList;

public class MageSkillTree extends SkillTree {
    {
        skillTreeType = SkillTreeType.MAGE;
    }
    public MageSkillTree(float x, float y) {
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



        // Mana
        bg = buildBlock(1, 4);
       // toReturn.add( bg );

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
        addSkill(new Spark());
        addSkill(new Wizard());
        addSkill(new Sorcerer());

        addSkill(new Spirituality());
        addSkill(new Meditation());
        addSkill(new BattleMage());
        addSkill(new SpiritArmor());

        addSkill(new SummonRat());
        addSkill(new AggressiveMinions());

        addSkill(new Slow());
        addSkill(new Warlock());
        addSkill(new Enrage());

        return this;
    }

    @Override
    public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> toReturn = new ArrayList<>();

        toReturn.add(new SkillContainer(getSkill(Spark.class), 1, 1));
        toReturn.add(new SkillContainer(getSkill(Wizard.class), 2, 1));
        toReturn.add(new SkillContainer(getSkill(Sorcerer.class), 4, 1));

        toReturn.add(new SkillContainer(getSkill(Spirituality.class), 1, 2));
        toReturn.add(new SkillContainer(getSkill(Meditation.class), 2, 2));
        toReturn.add(new SkillContainer(getSkill(BattleMage.class), 3, 2));
        toReturn.add(new SkillContainer(getSkill(SpiritArmor.class), 4, 2));

        toReturn.add(new SkillContainer(getSkill(SummonRat.class), 2, 3));
        toReturn.add(new SkillContainer(getSkill(AggressiveMinions.class), 4, 3));

        //toReturn.add(new SkillContainer(getSkill(Slow.class), 1, 4));
        toReturn.add(new SkillContainer(getSkill(Warlock.class), 3, 4));
        toReturn.add(new SkillContainer(getSkill(Enrage.class), 4, 4));

        return toReturn;
    }


    @Override
    public float getWandRechargeSpeedModifier(){
        return getSkill(Wizard.class).wandRechargeSpeedReduction();
    }

    @Override
    public float getWandDamageModifier(){
        return getSkill(Sorcerer.class).wandDamageBonus();
    }

    @Override
    public int SpiritArmorAbsorb(int damage) {
        return ((SpiritArmor)getSkill(SpiritArmor.class)).incomingDamageReduction(damage);
    }

    @Override
    public int getManaRegenerationBonus(){
        return ((Meditation)getSkill(Meditation.class)).manaRegenerationBonus();
    };

    @Override
    public float getMinionDamageModifier() {
        return ((AggressiveMinions)getSkill(AggressiveMinions.class)).damageModifier()
                + (((Enrage)getSkill(Enrage.class)).damageModifier() - 1f);
    }

    @Override
    public float getMinionIncomingDamageModifier(){
        return ((Enrage)getSkill(Enrage.class)).incomingDamageModifier();
    }

    @Override
    public int summoningLimitBonus() { return getSkill(Warlock.class).level > 0 ? 1 : 0; }

    @Override
    public void castSpark(){
        Dungeon.hero.MP -= getSkill(Spark.class).getManaCost();
        getSkill(Spark.class).castTextYell();
    }
}
