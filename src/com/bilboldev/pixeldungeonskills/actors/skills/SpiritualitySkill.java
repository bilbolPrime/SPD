package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

public class SpiritualitySkill extends PassiveSkillA1{

    {
        name = "Spirituality";
        image = 25;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        Dungeon.hero.MP += 10;
        Dungeon.hero.MMP += 10;
        return true;
    }

    @Override
    public String info()
    {
        return necroMercInfo("spirituality_desc", extendedInfo()) + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription = necroMercText("level_s_max_mana", i, (10 * i));
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
