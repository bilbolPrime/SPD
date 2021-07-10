package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Regeneration extends PassiveSkillA2{


    {
        name = "Regeneration";
        tier = 2;
        image = 2;
        level = 0;
    }


    @Override
    public int healthRegenerationBonus()
    {
        return level;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public String info()
    {
        return "No wound holds a true hero from accomplishing his quest.\n\n"
             //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            float regenerationDelayBase = 10f;
            float regenerationDelayAfter = (float)(10f / Math.pow(1.2, i));

            String levelDescription =  "Level " + i  + ": +" + (int) (100 * (-regenerationDelayAfter + regenerationDelayBase)/ regenerationDelayBase) + "% regeneration.";
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
            return "\nRequires: Endurance";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Endurance.class);
        return toReturn;
    }
}
