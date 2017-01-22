package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Venom extends PassiveSkillB1{


    {
        name = "Venom";
        castText = "Poison is my specialty";
        image = 57;
        tier = 1;
    }

    public boolean venomousAttack()
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
        return "Chance to poison target.\n"
                + costUpgradeInfo();
    }
}
