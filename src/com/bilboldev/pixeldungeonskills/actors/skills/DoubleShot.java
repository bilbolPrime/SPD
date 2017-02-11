package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class DoubleShot extends ActiveSkill2{


    {
        name = "Double Shot";
        castText = "Two for one";
        image = 90;
        tier = 2;
        mana = 5;
    }

    private boolean onDouble = false; // prevent infinite loop

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active3.active = false; // Disable Bombvoyage
        }
    }

    @Override
    public boolean doubleShot()
    {
        if(active == false || Dungeon.hero.MP < getManaCost())
            return false;
        else if(onDouble == false)
        {
            onDouble = true;
            castTextYell();
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();
            return true;
        }
        onDouble = false;
        return false;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Shoots two arrows at the same time.\n"
                + costUpgradeInfo();
    }
}
