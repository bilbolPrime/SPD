package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

public class SoulSpark extends ActiveSkill3{

    {
        name = "Soul Spark";
        castText = Message.key("custom.generated.entry_i_exist_for_others_a6176556a8");
        tier = 3;
        image = 122;
        mana = 3;
    }

    @Override
    public float getAlpha()
    {
        return 1f;
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

            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.SOUL_SPARK);
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
        return "Hatsune's pride...\n"
                + "Sacrifices spiritual energy to heal others.\n"

                + costUpgradeInfo();
    }

}
