package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class KneeShot extends PassiveSkillB2{


    {
        name = "Knee Shot";
        castText = "Easy Target";
        image = 82;
        tier = 2;
    }

    @Override
    public boolean cripple()
    {
        if(Random.Int(100) < 5 * level)
        {
            castTextYell();
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
        return "Aims for weak spots crippling targets.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + 5 * i + "% chance of crippling target.";

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
        return "\nRequires: Aimed Shot";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(AimedShot.class);
        return toReturn;
    }
}
