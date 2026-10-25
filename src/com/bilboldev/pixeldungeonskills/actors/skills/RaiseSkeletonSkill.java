package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

public class RaiseSkeletonSkill extends ActiveSkill2{

    {
        name = "Raise Skeleton";
        castText = Message.key("custom.generated.necro_merc.rise");
        tier = 2;
        image = 178;
        mana = 10;
        useDelay = 3f;
        quickCast = true;
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if (level > 0 && hero.MP >= getManaCost())
            actions.add(AC_CAST);

        if (hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action == Skill.AC_CAST && hero.MP >= getManaCost())
        {

            Legend.haxWand.castSpell(WandOfMagicCasting.CAST_TYPES.SPARK);
            Dungeon.hero.heroSkills.lastUsed = this;
        }

        if (action == Skill.AC_ADVANCE )
        {
            super.execute(hero, action);
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
        return necroMercInfo("raise_skeleton_desc", extendedInfo())
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_skeleton", i, i);
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

    public float getDamage(){
        return Random.IntRange( (3 + level), (7 + (int)(level * 1.25)));
    }

    public boolean getBlind(){
        return false;
    }
}
