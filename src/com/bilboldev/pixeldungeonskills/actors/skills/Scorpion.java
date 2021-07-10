package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Scorpion extends PassiveSkillB2{


    {
        name = "Scorpion";
        image = 58;
        tier = 2;
    }

    @Override
    public float venomDamageModifier() {return 1f + level * 0.1f;}

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Poison does more damage to enemies.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":  +" + (int)(i * 10) + "% damage from poison.";

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
            return "\nRequires: Venom";
        }

        return "";
    }


    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Venom.class);
        return toReturn;
    }
}
