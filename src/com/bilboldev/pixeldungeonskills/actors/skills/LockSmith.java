package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class LockSmith extends PassiveSkillA3{


    {
        name = "Lock Smith";

        tier = 3;
        image = 51;
    }

    @Override
    public boolean disableTrap()
    {
        if(Random.Int(100) < 10 * level)
        {
            castText = "Woah!!";
            castTextYell();
            return true;
        }
        castText = "Need to train in " + name + " more...";
        castTextYell();
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
        return "Chance to disable traps when triggering them.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (i * 10) + "% chance to disable trap.";
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
            return "\nRequires: Bandit";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Bandit.class);
        return toReturn;
    }
}
