package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

import java.util.ArrayList;

public class FirmHand extends PassiveSkillB1{

    {
        name = "Firm Hand";
        image = 10;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int toHitBonus()
    {
        return  level * 1;
    }

    @Override
    public String info()
    {
        return "Increases proficiency with melee weapons.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (i) + " attack skill.";
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
            return "\nRequires: Smash";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Smash.class);
        return toReturn;
    }
}
