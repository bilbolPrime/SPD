package com.bilboldev.pixeldungeonskills.actors.skills;


/**
 * Created by Moussa on 20-Jan-17.
 */
public class Mastery extends PassiveSkillB3{


    {
        name = "Mastery";
        tier = 3;
        image = 11;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int  weaponLevelBonus()
    {
        return level;
    }


    @Override
    public String info()
    {
        return "Melee weapons receive bonus to level.\n"
                + costUpgradeInfo();
    }
}
