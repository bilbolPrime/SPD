package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Spirituality extends PassiveSkillA1{


    {
        name = "Spirituality";
        image = 25;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        Dungeon.hero.MP += 5;
        Dungeon.hero.MMP += 5;
        return true;
    }


    @Override
    public String info()
    {
        return "+5 to mana per level.\n"
                + costUpgradeInfo();
    }
}
