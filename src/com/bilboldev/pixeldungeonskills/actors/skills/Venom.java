package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Venom extends ActiveSkill{


    {
        name = "Venom";
        castText = "Poison is my specialty";
        image = 57;
        mana = 5;
        tier = 1;
    }

    private boolean checkPoison = false;

    @Override
    public float damageModifier()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return 1f;
        else
        {
            checkPoison = true;
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();

            castTextYell();
            return 1f + 0.02f * level;
        }
    }

    public boolean venomousAttack()
    {
        if(!checkPoison){
            return false;
        }

        checkPoison = false;

        if(Random.Int(100) < 10 * level)
        {
            //castTextYell();
            return true;
        }
        return false;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public String info()
    {
        return "Stronger attack with chance to poison target.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":  +" + (int)(i * 2) + "% damage, " + (int)(i * 10) + "% to poison.";

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
            return "\nRequires: Stealth";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Stealth.class);
        return toReturn;
    }
}
