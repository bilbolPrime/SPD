package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 25-Jan-17.
 */
public class MercThiefSkillA extends Venom {
    {
        tag = "mercA";
    }

    @Override
    protected boolean upgrade()
    {
        return false;
    }

    @Override
    public boolean venomousAttack()
    {
        if(Random.Int(100) < 5 * level + 15)
        {
            return true;
        }
        return false;
    }


    @Override
    public String info()
    {
        return "Chance to poison target.\n";
    }
}
