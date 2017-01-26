package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class KeenEye extends PassiveSkillB3{


    {
        name = "Keen Eye";
        castText = "Gotcha!";
        image = 106;
        tier = 3;
    }


    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Arrows pass through friendly units without harming them.\n"
                + costUpgradeInfo();
    }
}
