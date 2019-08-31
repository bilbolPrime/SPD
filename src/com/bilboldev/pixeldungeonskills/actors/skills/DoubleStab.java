package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class DoubleStab extends ActiveSkill1{


    {
        name = "Double Strike";
        castText = "Too slow";
        image = 61;
        tier = 1;
        mana = 5;
    }



    @Override
    public boolean doubleStab()
    {
        if(active == false || Dungeon.hero.MP < getManaCost())
            return false;

            //castTextYell();
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();
            return true;

    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Attacks twice with equipped melee weapon.\n"
                + costUpgradeInfo();
    }
}
