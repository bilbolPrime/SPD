package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Dominance extends ActiveSkill3{


    {
        name = "Dominance";
        castText = "You forced me!";
        tier = 3;
        image = 115;
        mana = 3;
    }


    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(level > 0 && hero.MP >= getManaCost())
            actions.add(AC_CAST);
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_CAST && hero.MP >= getManaCost())
        {
            //hero.MP -= getManaCost();
           // castTextYell();
            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.DOMINANCE);
            Dungeon.hero.heroSkills.lastUsed = this;
        }
    }

    @Override
    public int getManaCost()
    {
        return (int)Math.ceil(mana * (1 + 0.6 * level));
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public float getAlpha()
    {
        return 1f;
    }

    @Override
    public String info()
    {
        return "The target is no more than a slave to your will.\n"
                + costUpgradeInfo();
    }

}
