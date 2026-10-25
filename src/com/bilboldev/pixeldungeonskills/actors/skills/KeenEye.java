package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.utils.Random;

public class KeenEye extends PassiveSkillB3{

    {
        name = "Keen Eye";
        castText = Message.key("badges$badge.boss_challenge_2.title");
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
