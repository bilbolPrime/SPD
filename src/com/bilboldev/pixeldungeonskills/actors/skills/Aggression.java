package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Aggression extends PassiveSkillB2{


    {
        name = "Aggression";
        image = 9;
        tier = 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float damageModifier()
    {
        return 1f + 0.1f * level;
    }

    @Override
    public String info()
    {
        return "Do more damage while using melee weapons.\n"
                + costUpgradeInfo();
    }
}
