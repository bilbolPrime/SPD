package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

public class NinjaBomb extends ActiveSkill2{

    {
        name = "Ninja Bomb";
        castText = Message.key("custom.generated.entry_go_to_sleep_35eda7eae5");
        tier = 2;
        image = 65;
        mana = 8;
        useDelay = 10f;
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

            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.NINJA_BOMB);
            Dungeon.hero.heroSkills.lastUsed = this;
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
        return "Throws a bomb that emits sleeping gas on impact.\n"

                + costUpgradeInfo();
    }
}
