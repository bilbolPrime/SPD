package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

public class TripleShot extends ActiveSkill2{

    {
        name = "Triple Shot";
        castText = Message.key("custom.generated.entry_catch_sweetie_4ca656a4e7");
        image = 92;
        tier = 2;
        mana = 5;
    }

    private int count = 0;

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active3.active = false;
        }
    }

    @Override
    public boolean doubleShot()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return false;
        else if(count < 2)
        {
            count++;
            if(count == 1) {
                Dungeon.hero.MP -= getManaCost();
                StatusPane.manaDropping += getManaCost();

                castTextYell();
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
