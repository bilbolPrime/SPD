package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

public class LichSkill extends PassiveSkillB3{

    {
        name = "Lich";
        tier = 3;
        image = 177;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int  weaponLevelBonus()
    {
        return level;
    }

    @Override
    public String info()
    {
        return necroMercText("lich_desc") + "\n\n"

                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if (level == 0)
        {
            sb.append(necroMercText("lich_unlock"));
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlightNecroMercText("lich_double_power"));
        sb.append("\n");
        sb.append(highlightNecroMercText("lich_extra_skeleton"));
        sb.append("\n");
        sb.append("\n");
        sb.append(highlightNecroMercText("lich_feed"));
        sb.append("\n");
        sb.append("\n");

        return  sb.toString();
    }

    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }

    @Override
    public String requiresInfo(){
        if (level == 0){
            return "\n" + necroMercText("requires_lich_subclass");
        }

        return "";
    }

    @Override
    public int sparkBonusDamage(){
        return 0;
    }
}
