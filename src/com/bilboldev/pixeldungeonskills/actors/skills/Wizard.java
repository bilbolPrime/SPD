package com.bilboldev.pixeldungeonskills.actors.skills;


/**
 * Created by Moussa on 20-Jan-17.
 */
public class Wizard extends PassiveSkillB1{


    {
        name = "Wizard";
        image = 33;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public float wandRechargeSpeedReduction()
    {
        return 1f - 0.1f * level;
    }

    @Override
    public String info()
    {
        return "Wands recharge faster.\n"
                + costUpgradeInfo();
    }
}
