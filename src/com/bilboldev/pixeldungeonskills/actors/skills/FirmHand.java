package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class FirmHand extends PassiveSkillB1{


    {
        name = "Firm Hand";
        image = 10;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public int toHitBonus()
    {
        return  level * 2;
    }

    @Override
    public String info()
    {
        return "Increased chance of hitting target when using melee weapons.\n"
                + costUpgradeInfo();
    }
}
