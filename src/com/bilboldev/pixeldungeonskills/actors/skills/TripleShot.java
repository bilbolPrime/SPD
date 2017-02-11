package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class TripleShot extends ActiveSkill2{


    {
        name = "Triple Shot";
        castText = "Catch Sweetie";
        image = 92;
        tier = 2;
        mana = 5;
    }

    private int count = 0; // prevent infinite loop

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
        else if(count < 2)
        {
            count++;
            if(count == 1) {
                castTextYell();
                Dungeon.hero.MP -= getManaCost();
                StatusPane.manaDropping += getManaCost();
            }
            return true;
        }
        count = 0;
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
        return "Shoots three arrows at the same time.\n"
                + costUpgradeInfo();
    }
}
