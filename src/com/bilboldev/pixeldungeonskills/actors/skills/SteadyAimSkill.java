package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

import java.util.ArrayList;

public class SteadyAimSkill extends PassiveSkillB1{

    {
        name = "Steady Aim";
        image = 164;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int gunToHitBonus()
    {
        return  level * 1;
    }

    @Override
    public String info()
    {
        return "Practice makes perfect.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (i) + " attack skill with guns.";
            if (i == level){
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
        if (level == 0){
            return "\nRequires: Wanted";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(WantedSkill.class);
        return toReturn;
    }
}
