package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Bombvoyage extends ActiveSkill3{


    {
        name = "Bombvoyage";
        castText = "Bombvoyage";
        image = 91;
        tier = 3;
        mana = 5;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active2.active = false; // Disable Double Arrow
        }
    }

    @Override
    public boolean arrowToBomb()
    {
        if(active == false || Dungeon.hero.MP < getManaCost())
            return false;
        else
        {
            castTextYell();
            Dungeon.hero.MP -= getManaCost();
            return true;
        }
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Attaches a bomb to a standard arrow.\n"
                + costUpgradeInfo();
    }
}
