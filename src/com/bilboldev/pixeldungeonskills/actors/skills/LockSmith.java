package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class LockSmith extends PassiveSkillA3{

    {
        name = "Lock Smith";

        tier = 3;
        image = 51;
    }

    @Override
    public boolean disableTrap()
    {
        if(Random.Int(100) < 10 * level)
        {
            castText = Message.key("custom.generated.entry_woah_dd29b811cc");
            castTextYell();
            return true;
        }
        castText = Message.key("custom.generated.entry_need_to_train_in_s_more_ce84034f13", Message.skillName(this));
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
        return "Chance to disable traps when triggering them.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (i * 10) + "% chance to disable trap.";
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
            return "\nRequires: Bandit";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Bandit.class);
        return toReturn;
    }
}
