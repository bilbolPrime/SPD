package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Assassin extends PassiveSkillB3{


    {
        name = "Assassin";
        tier = 3;
        image = 154;
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
            sb.append("Unlocked automatically when you choose the _Assassin_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("Bonus damage to surprise attacks."));
        sb.append("\n");
        sb.append(highlight("+1 Stealth."));
        sb.append("\n");
        sb.append("\n");

        return  sb.toString();
    }

    @Override
    public int stealthBonus(){return level > 0 ? 1 : 0;}

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Assassin Subclass";
        }

        return "";
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }
}
