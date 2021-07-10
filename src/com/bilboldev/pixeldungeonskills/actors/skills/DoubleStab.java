package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class DoubleStab extends ActiveSkill1{


    {
        name = "Double Strike";
        castText = "Too slow";
        image = 61;
        tier = 1;
        mana = 5;
        useDelay = 3f;
    }

    private int damageBoost = 0;

    @Override
    public float damageModifier()
    {
        if(damageBoost < 1)
            return 1f;
        else
        {
            damageBoost--;
            return 1f + 0.1f * (level - 3);
        }
    }

    @Override
    public boolean doubleStab()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
        {
            damageBoost = 0;
            return false;
        }

            castTextYell();
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();
            damageBoost = 2;
            return true;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Attack twice with equiped melee weapon.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (i - 3) * (10) + "% damage per attack";

            if(i > 2) {
                levelDescription =  "Level " + i  + ": +" + (i - 3) * (10) + "% damage per attack";
            }

            if(i == level){
                sb.append(highlight(levelDescription));
            }
            else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Locksmith and Free Runner";
        }

        return "";
    }


    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Freerunner.class);
        toReturn.add(LockSmith.class);
        return toReturn;
    }
}
