package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

public class SpiritBinderSkill extends PassiveSkillB3{

    {
        name = "Spirit Binder";
        tier = 3;
        image = 176;
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
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }

    @Override
    public String info()
    {
        return necroMercText("spirit_binder_desc") + "\n"

                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if (level == 0)
        {
            sb.append(necroMercText("spirit_binder_unlock"));
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlightNecroMercText("raise_skeleton_root"));
        sb.append("\n");
        sb.append(highlightNecroMercText("wand_charge_hit"));

        sb.append("\n");
        sb.append("\n");
        sb.append(necroMercText("wand_charge_damage"));
        sb.append("\n");
        for (int i = 1; i <= 5; i++)
        {

            String levelDescription = necroMercText("charge_s_damage", i, i);
            sb.append(levelDescription);
            sb.append("\n");
        }

        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if (level == 0){
            return "\n" + necroMercText("requires_spirit_binder_subclass");
        }

        return "";
    }
}
