package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Gladiator extends PassiveSkillB3{


    {
        name = "Gladiator";
        tier = 3;
        image = 128;
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
    public int toHitBonus()
    {
        return level == 0 ? 0 : 3;
    }

    @Override
    public float meleeSpeedModifier()
    {
        return level == 0 ? 1f : 1.1f;
    }

    @Override
    public String info()
    {
        return "Gladiators rely on their weapon mastery to dominate in combat.\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Gladiator_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("+10% melee weapon attack speed."));
        sb.append("\n");
        sb.append(highlight("+3 melee weapon attack skill."));
        sb.append("\n");
        sb.append("\n");
        sb.append("Successful attacks start a _combo_ to deal more damage.");
        sb.append("\n");
        for(int i = 3; i <= 5; i++)
        {

            String levelDescription =  i + " hit combo: +" + (i - 2) * 20 + "% damage.";
            sb.append(levelDescription);
            sb.append("\n");
        }

        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Gladiator Subclass";
        }

        return "";
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }
}
