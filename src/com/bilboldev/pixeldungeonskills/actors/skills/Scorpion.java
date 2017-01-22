package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Scorpion extends PassiveSkillB2{


    {
        name = "Scorpion";
        image = 58;
        tier = 2;
    }

    @Override
    public int venomBonus() {return level * 2;}

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Poison does more damage to enemies.\n"
                + costUpgradeInfo();
    }
}
