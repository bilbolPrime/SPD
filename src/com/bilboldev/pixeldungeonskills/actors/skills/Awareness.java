package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.utils.Random;

public class Awareness extends PassiveSkillA2{

    {
        name = "Awareness";
        image = 144;
        tier = 2;
    }

    @Override
    public boolean dodgeChance()
    {
        if(Random.Int(100) < 5 * level)
        {
            castText = Message.key("custom.generated.entry_too_easy_6d68e4cd2d");
            castTextYell();
            return true;
        }
        castText = Message.key("custom.generated.entry_i_am_losing_my_touch_6f0710a71a");
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
        return "The huntress is always aware of her surroundings making it harder for enemies to hit her with ranged attacks.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (int) (50 * (i * 0.1f)) + "% chance to dodge.";
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

}
