package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class AggressiveMinions extends PassiveSkillB2{


    {
        name = "Rabid Minions";
        image = 134;
        tier = 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float damageModifier()
    {
        return 1f + 0.05f * level;
    }

    @Override
    public String info()
    {
        return "The warlock uses dark magic to make his minions more aggressive.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int)(100 * (0.05f * i)) + "% damage.";
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
            return "\nRequires: Warlock and Summon Minion";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(SummonRat.class);
        toReturn.add(Warlock.class);
        return toReturn;
    }
}
