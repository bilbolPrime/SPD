package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class KneeShot extends PassiveSkillB2{


    {
        name = "Knee Shot";
        castText = "Easy Target";
        image = 82;
        tier = 2;
    }

    @Override
    public boolean cripple()
    {
        if(Random.Int(100) < 10 * level)
        {
            castTextYell();
            return true;
        }
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
        return "Aims for weak spots crippling targets.\n"
                + costUpgradeInfo();
    }
}
