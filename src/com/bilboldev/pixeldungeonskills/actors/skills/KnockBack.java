package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class KnockBack extends ActiveSkill2{


    {
        name = "KnockBack";
        castText = "KnockBack!";
        tier = 2;
        image = 18;
        mana = 5;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active1.active = false; // Disable Smash
            hero.heroSkills.active3.active = false; // Disable Rampage
        }
    }

    @Override
    public float damageModifier()
    {
        if(active == false || Dungeon.hero.MP < getManaCost())
            return 1f;
        else
        {
            return 1f + 0.1f * level;
        }
    }

    @Override
    public boolean knocksBack()
    {
        if(active == false || Dungeon.hero.MP < getManaCost())
            return false;
        else
        {
            castTextYell();
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();
            return true;
        }
    }


    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public int getManaCost()
    {
        return (int)Math.ceil(mana * (1 + 0.55 * level));
    }

    @Override
    public String info()
    {
        return "Hits harder and knocks back target.\n"
                + costUpgradeInfo();
    }

}
