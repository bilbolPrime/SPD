package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SilentDeath extends PassiveSkillB3{


    {
        name = "Silent Death";
        castText = "Eternal Slumber";
        tier = 3;
        image = 59;
    }

    public boolean instantKill()
    {

        if(Random.Int(100) < 10 * level)
        {
            castText = "Eternal Slumber";
            castTextYell();
            return true;
        }
        castText = "Almost had him...";
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
        return "Chance to instantly kill sleeping enemies with melee attacks.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":  +" + (int)(i * 10) + "% chance of insta-kill.";

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
            return "\nRequires: Assassin";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Assassin.class);
        return toReturn;
    }
}
