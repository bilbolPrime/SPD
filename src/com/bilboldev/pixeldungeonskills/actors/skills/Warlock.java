package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Warlock extends PassiveSkillB3{


    {
        name = "Warlock";
        tier = 3;
        image = 131;
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
        return "Warlocks are mages who have chosen the dark path.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Warlock_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("Spark does 5 more points of damage."));
        sb.append("\n");
        sb.append(highlight("Warlocks can summon an extra minion."));
        sb.append("\n");
        sb.append("\n");
        sb.append(highlight("Warlocks feed on the souls of their enemies healing damage and satisfying hunger."));
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
        if(level == 0){
            return "\nRequires: Warlock Subclass";
        }

        return "";
    }

    @Override
    public int sparkBonusDamage(){
        return level == 0 ? 0 : 5;
    }
}
