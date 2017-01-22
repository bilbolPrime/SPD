package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Accuracy extends PassiveSkillB1{


    {
        name = "Accuracy";
        image = 81;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float toHitModifier(){return 1f + 0.1f * level;}

    @Override
    public String info()
    {
        return "Increases chance to hit when using a ranged weapon.\n"
                + costUpgradeInfo();
    }
}
