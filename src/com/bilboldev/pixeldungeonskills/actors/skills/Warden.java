package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Warden extends PassiveSkillB3{


    {
        name = "Warden";
        tier = 3;
        image = 145;
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
        return "Wardens have a strong connection with nature.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Warden_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("Gains armor buff when trampling high grass."));
        sb.append("\n");
        sb.append(highlight("20% chance to get dew drops and seeds from plants."));
        sb.append("\n");
        sb.append("\n");

        return  sb.toString();
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Warden Subclass";
        }

        return "";
    }
}
