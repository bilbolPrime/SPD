package com.bilboldev.pixeldungeonskills.actors.skills;

public class SummonerSkill extends PassiveSkillB3{

    {
        name = "Summoner";
        tier = 3;
        image = 35;
    }

    @Override
    public int summoningLimitBonus()
    {
        return level;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public String info()
    {
        return "Summoning limit increases by 1 per level.\n"
                + costUpgradeInfo();
    }
}
