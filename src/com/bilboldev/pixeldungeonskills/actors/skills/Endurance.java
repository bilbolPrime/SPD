package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Endurance extends PassiveSkillA1{


    {
        name = "Endurance";
        image = 1;
    }

    @Override
    protected boolean upgrade()
    {
        Dungeon.hero.HT += 5;
        Dungeon.hero.HP += 5;
        return true;
    }


    @Override
    public String info()
    {
        return "+5 to health per level.\n"
                + costUpgradeInfo();
    }
}
