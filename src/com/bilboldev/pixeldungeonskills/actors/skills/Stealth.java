package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Stealth extends PassiveSkillA2{


    {
        name = "Stealth";
        image = 50;
        tier = 2;
    }

    @Override
    public int stealthBonus(){return level;}

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Harder to detect.\n"
                + costUpgradeInfo();
    }
}
