package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

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

    public static float delayMultiplier(int level) {
        return 0.5f + (float)(0.5 / Math.pow(1.2, level));
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

                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int)(100 * (1 - delayMultiplier(i))) + "% regeneration.";
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
