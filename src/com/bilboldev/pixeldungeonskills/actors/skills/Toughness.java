package com.bilboldev.pixeldungeonskills.actors.skills;


import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Toughness extends PassiveSkillA3{


    {
        name = "Toughness";
        tier = 3;
        image = 3;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float incomingDamageModifier()
    {
        return 1f - level * 0.04f;
    }

    @Override
    public String info()
    {
        return "Toughness reduces the amount of incoming damage.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": -" + (int) (Math.round(100 * (i * 0.04f))) + "% damage taken.";
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
