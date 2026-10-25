package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class SilentDeath extends PassiveSkillB3{

    {
        name = "Silent Death";
        castText = Message.key("custom.generated.entry_eternal_slumber_332843bd03");
        tier = 3;
        image = 59;
    }

    public boolean instantKill()
    {

        if(Random.Int(100) < 10 * level)
        {
            castText = Message.key("custom.generated.entry_eternal_slumber_332843bd03");
            castTextYell();
            return true;
        }
        castText = Message.key("custom.generated.entry_almost_had_him_a5fd432b44");
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
