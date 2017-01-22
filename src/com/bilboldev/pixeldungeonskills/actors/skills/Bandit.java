package com.bilboldev.pixeldungeonskills.actors.skills;



/**
 * Created by Moussa on 20-Jan-17.
 */
public class Bandit extends PassiveSkillA1{


    {
        name = "Bandit";
        image = 49;
        tier = 1;
    }

    @Override
    public int lootBonus(int gold)
    {
        return (int) (gold * 0.1f * level);
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "10% more gold per level.\n"
                + "Becomes immune to Mimic gold steal.\n"
                + costUpgradeInfo();
    }
}
