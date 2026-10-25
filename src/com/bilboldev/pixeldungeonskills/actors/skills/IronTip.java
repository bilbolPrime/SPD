package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.utils.Random;

public class IronTip extends PassiveSkillB3{

    {
        name = "Iron Tip";
        castText = Message.key("custom.generated.entry_don_t_forget_to_share_884e9f6a94");
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
