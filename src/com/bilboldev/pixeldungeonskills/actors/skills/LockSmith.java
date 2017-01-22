package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class LockSmith extends PassiveSkillA3{


    {
        name = "Lock Smith";

        tier = 3;
        image = 51;
    }

    @Override
    public boolean disableTrap()
    {
        if(Random.Int(100) < 33 * level)
        {
            castText = "Woah!!";
            castTextYell();
            return true;
        }
        castText = "Need to train in " + name + " more...";
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
        return "33% per level chance to disable traps.\n"
                + costUpgradeInfo();
    }
}
