package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Meditation extends PassiveSkillA2{


    {
        name = "Meditation";
        image = 26;
        tier = 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public int manaRegenerationBonus() {return level;}

    @Override
    public String info()
    {
        return "Increased mana regeneration.\n"
                + costUpgradeInfo();
    }
}
