package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

public class QuickDrawSkill extends PassiveSkillB3{

    {
        name = "Quick Draw";
        tier = 3;
        image = 165;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float gunAttackSpeedModifier() {
        return 1f + 0.05f * level;
    }

    @Override
    public String info()
    {
        return "Quickness is key.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (5 * i) + "% speed with guns.";
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
            return "\nRequires: Steady Aim";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(SteadyAimSkill.class);
        return toReturn;
    }
}
