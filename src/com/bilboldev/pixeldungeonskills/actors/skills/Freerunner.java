package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Freerunner extends PassiveSkillB3{


    {
        name = "Free Runner";
        tier = 3;
        image = 153;
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
        return "Free runners are the fastest subclass.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Free_ _Runner_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("+60% movement speed when not starving."));
        sb.append("\n");
        sb.append(highlight("Double evasion when not starving."));
        sb.append("\n");
        sb.append("\n");

        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Free Runner Subclass";
        }

        return "";
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }
}
