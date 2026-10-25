package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

public class DrainLifeSkill extends ActiveSkill2{

    {
        name = "Drain Life";
        castText = Message.key("custom.generated.necro_merc.drain");
        image = 179;
        tier = 2;
        mana = 10;
        useDelay = 3f;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
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
    public String info()
    {
        return necroMercInfo("drain_life_desc", extendedInfo())

                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_damage_healing", i, (2 * (3 + i)), (2 * (7 + (int)(i * 1.25))));
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

    @Override
    public String requiresInfo(){
        if (level == 0){
            return "\n" + necroMercText("requires_raise_skeleton");
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(RaiseSkeletonSkill.class);
        return toReturn;
    }
}
