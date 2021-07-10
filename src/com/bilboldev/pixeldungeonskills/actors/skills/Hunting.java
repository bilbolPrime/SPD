package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Hunting extends PassiveSkillA3{


    {
        name = "Hunting";
        image = 74;
        tier = 3;
    }

    @Override
    public int hunting() {return level == 0 ? 0 : level * 5;}

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Even in the dungeon, the huntress can still find prey.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": Create food with time.";

            if(i != 1){
                levelDescription =  "Level " + i  + ": " + (i * 5) + "% faster hunting.";
            }

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
        return "\nRequires: Awareness and Warden";
    }


    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Warden.class);
        toReturn.add(Awareness.class);
        return toReturn;
    }
}
