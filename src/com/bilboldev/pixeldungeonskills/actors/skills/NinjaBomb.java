package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class NinjaBomb extends ActiveSkill2{


    {
        name = "Ninja Bomb";
        castText = "Go to sleep";
        tier = 2;
        image = 65;
        mana = 8;
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
            //castTextYell();
            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.NINJA_BOMB);
            Dungeon.hero.heroSkills.lastUsed = this;
        }
    }

    @Override
    public int getManaCost()
    {
        return (int)Math.ceil(mana * (1 + 0.5 * level));
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Throws a bomb that emits sleeping gas on impact.\n"

                + costUpgradeInfo();
    }
}
