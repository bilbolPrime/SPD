package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class IronTip extends PassiveSkillB3{


    {
        name = "Iron Tip";
        castText = "Don't forget to share...";
        image = 83;
        tier = 3;
    }

    @Override
    public int passThroughTargets(boolean shout)
    {
        if(shout == false)
            return level;

        if(level > 0 && Random.Int(level + 1) > 0)
        {
            //castTextYell();
            multiTargetActive = true;
            return level;
        }
        multiTargetActive = false;

        return 0;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Uses iron arrow tips allowing some arrows to path through their targets and continue their path.\n"
                + costUpgradeInfo();
    }
}
