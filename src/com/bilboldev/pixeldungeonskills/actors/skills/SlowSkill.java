package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

public class SlowSkill extends ActiveSkill2{

    {
        name = "Slow";
        castText = Message.key("custom.generated.entry_basic_training_6ca0ee1d5f");
        tier = 2;
        image = 135;
        mana = 3;
        useDelay = 2f;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (level > 0 && hero.MP >= getManaCost())
            actions.add(AC_CAST);
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action == Skill.AC_CAST && hero.MP >= getManaCost())
        {

            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.SPARK);
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
        return necroMercInfo("slow_desc", extendedInfo())
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_speed_defence", i, (int)(0.05f * i * 100), i);
            if (i == level){
                sb.append(highlight(levelDescription));
            }
            else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return  sb.toString();
    }
}
