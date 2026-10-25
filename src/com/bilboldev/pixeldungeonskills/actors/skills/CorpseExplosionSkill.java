package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

public class CorpseExplosionSkill extends ActiveSkill3{

    {
        name = "Corpse Explosion";
        castText = Message.key("custom.generated.necro_merc.explode");
        tier = 2;
        image = 184;
        mana = 10;
        useDelay = 2f;
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
        return necroMercInfo("corpse_explosion_desc", extendedInfo())
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_explosion_damage", i, (12 + i * 6));
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
            return "\n" + necroMercText("requires_lich");
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(LichSkill.class);
        return toReturn;
    }
}
