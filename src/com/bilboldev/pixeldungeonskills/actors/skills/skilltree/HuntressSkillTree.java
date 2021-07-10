package com.bilboldev.pixeldungeonskills.actors.skills.skilltree;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.pixeldungeonskills.actors.skills.Accuracy;
import com.bilboldev.pixeldungeonskills.actors.skills.Aggression;
import com.bilboldev.pixeldungeonskills.actors.skills.AimedShot;
import com.bilboldev.pixeldungeonskills.actors.skills.Awareness;
import com.bilboldev.pixeldungeonskills.actors.skills.Berserker;
import com.bilboldev.pixeldungeonskills.actors.skills.Bombvoyage;
import com.bilboldev.pixeldungeonskills.actors.skills.DoubleShot;
import com.bilboldev.pixeldungeonskills.actors.skills.Endurance;
import com.bilboldev.pixeldungeonskills.actors.skills.FirmHand;
import com.bilboldev.pixeldungeonskills.actors.skills.Fletching;
import com.bilboldev.pixeldungeonskills.actors.skills.Gladiator;
import com.bilboldev.pixeldungeonskills.actors.skills.Hunting;
import com.bilboldev.pixeldungeonskills.actors.skills.KOArrow;
import com.bilboldev.pixeldungeonskills.actors.skills.KneeShot;
import com.bilboldev.pixeldungeonskills.actors.skills.KnockBack;
import com.bilboldev.pixeldungeonskills.actors.skills.Mastery;
import com.bilboldev.pixeldungeonskills.actors.skills.Rampage;
import com.bilboldev.pixeldungeonskills.actors.skills.Regeneration;
import com.bilboldev.pixeldungeonskills.actors.skills.Smash;
import com.bilboldev.pixeldungeonskills.actors.skills.Smite;
import com.bilboldev.pixeldungeonskills.actors.skills.Sniper;
import com.bilboldev.pixeldungeonskills.actors.skills.Toughness;
import com.bilboldev.pixeldungeonskills.actors.skills.Warden;

import java.util.ArrayList;

public class HuntressSkillTree extends SkillTree {

    {
        skillTreeType = SkillTreeType.HUNTRESS;
    }
    public HuntressSkillTree(float x, float y) {
        super(x, y);
    }

    @Override
    public ArrayList<ColorBlock> getBackground() {
        ArrayList<ColorBlock> toReturn = new ArrayList<>();

        // 1st level
        ColorBlock bg = new ColorBlock( SLOT_SIZE * 5, 2, NORMAL );
        bg.x = x + SLOT_SIZE + 2 * SLOT_SIZE;
        bg.y = y  + SLOT_SIZE + SLOT_SIZE / 2 - 1;
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

        addSkill(new Awareness());
        addSkill(new Hunting());

        addSkill(new Accuracy());
        addSkill(new Fletching());
        addSkill(new Warden());
        addSkill(new DoubleShot());

        addSkill(new AimedShot());
        addSkill(new KOArrow());

        addSkill(new KneeShot());
        addSkill(new Sniper());
        addSkill(new Bombvoyage());

        return this;
    }

    @Override
    public ArrayList<SkillContainer> getSkills() {
        ArrayList<SkillContainer> toReturn = new ArrayList<>();


        toReturn.add(new SkillContainer(getSkill(Awareness.class), 2, 1));
        toReturn.add(new SkillContainer(getSkill(Hunting.class), 4, 1));

        toReturn.add(new SkillContainer(getSkill(Accuracy.class), 1, 2));
        toReturn.add(new SkillContainer(getSkill(Fletching.class), 2, 2));
        toReturn.add(new SkillContainer(getSkill(Warden.class), 3, 2));
        toReturn.add(new SkillContainer(getSkill(DoubleShot.class), 4, 2));

        toReturn.add(new SkillContainer(getSkill(AimedShot.class), 2, 3));
        toReturn.add(new SkillContainer(getSkill(KOArrow.class), 4, 3));

        toReturn.add(new SkillContainer(getSkill(KneeShot.class), 2, 4));
        toReturn.add(new SkillContainer(getSkill(Sniper.class), 3, 4));
        toReturn.add(new SkillContainer(getSkill(Bombvoyage.class), 4, 4));

        return toReturn;
    }

    @Override
    public int getRangedAttackSkillBonus(){return getSkill(Accuracy.class).toHitModifier();}

    @Override
    public int getFletching() {return getSkill(Fletching.class).fletching();}

    @Override
    public boolean rangedDodgeChance() { return getSkill(Awareness.class).dodgeChance(); }

    @Override
    public int hunting() {return getSkill(Hunting.class).hunting();}

    @Override
    public boolean doubleShot() {return getSkill(DoubleShot.class).doubleShot();}

    @Override
    public float rangedDamageModifier() {return getSkill(AimedShot.class).rangedDamageModifier() * getSkill(DoubleShot.class).rangedDamageModifier() * getSkill(Sniper.class).rangedDamageModifier();}

    @Override
    public int aimedShot() { return getSkill(AimedShot.class).aimedShot();}

    @Override
    public boolean rangedCripple() { return getSkill(KneeShot.class).cripple();}

    @Override
    public boolean rangedSleep(){return getSkill(KOArrow.class).goToSleep();}

    @Override
    public boolean rangedSlow(){return getSkill(KOArrow.class).slowTarget();}
    @Override
    public boolean arrowToBomb() {return getSkill(Bombvoyage.class).arrowToBomb();}
    @Override
    public int arrowToBombDamage() {return ((Bombvoyage)getSkill(Bombvoyage.class)).damage();}
    @Override
    public float arrowToBombParalysis() {return ((Bombvoyage)getSkill(Bombvoyage.class)).paralysisDuration();}
}
