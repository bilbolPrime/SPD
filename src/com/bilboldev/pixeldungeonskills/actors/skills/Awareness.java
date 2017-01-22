package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Awareness extends PassiveSkillA3{


    {
        name = "Awareness";
        image = 75;
        tier = 3;
    }

    @Override
    public boolean dodgeChance()
    {
        if(Random.Int(100) < 10 * level)
        {
            castText = "Too easy..";
            castTextYell();
            return true;
        }
        castText = "I am losing my touch...";
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
        return "10% chance per level to dodge a hostile ranged attack.\n"
                + costUpgradeInfo();
    }
}
