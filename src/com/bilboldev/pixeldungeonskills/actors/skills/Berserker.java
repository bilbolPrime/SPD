package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Berserker extends PassiveSkillB3{


    {
        name = "Berserker";
        tier = 3;
        image = 129;
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
    public float incomingDamageModifier(){
        return level == 0 ? 1f : 0.9f;
    }

    @Override
    public String info()
    {
        return "Berserkers have no conern for their safety. They even become more aggressive when wounded.\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Berserker_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("+10% melee weapon damage."));
        sb.append("\n");
        sb.append(highlight("-10% damage taken."));
        sb.append("\n");
        sb.append("\n");
        sb.append("When under _40%_ health, hero does _+50%_ damage.");
        sb.append("\n");
        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Berserker Subclass";
        }

        return "";
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }
}
