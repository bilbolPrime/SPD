package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class BattleMage extends PassiveSkillB3{


    {
        name = "Battle Mage";
        tier = 3;
        image = 130;
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
        return "Battle Mages are true masters of the wand.\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Battle_ _Mage_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("Spark has a 50% chance of blinding target!"));
        sb.append("\n");
        sb.append(highlight("+1 wand charge per successful melee attack using a wand."));

        sb.append("\n");
        sb.append("\n");
        sb.append("When attacking with a wand, do more damage based on _charges_ on equipped wand.");
        sb.append("\n");
        for(int i = 1; i <= 5; i++)
        {

            String levelDescription =  i + " charge: +" + (i) + " damage.";
            sb.append(levelDescription);
            sb.append("\n");
        }

        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Battle Mage Subclass";
        }

        return "";
    }
}
