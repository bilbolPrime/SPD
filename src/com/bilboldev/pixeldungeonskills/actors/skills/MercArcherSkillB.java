package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 25-Jan-17.
 */
public class MercArcherSkillB extends KeenEye {
    {
        tag = "mercB";
    }

    @Override
    protected boolean upgrade()
    {
        return false;
    }


    @Override
    public String info()
    {
        return "Arrows pass through friendly units without harming them.\n";
    }
}
