package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Sniper extends PassiveSkillB3{


    {
        name = "Sniper";
        tier = 3;
        image = 146;
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
    public float rangedDamageModifier(){
        return level == 0 ? 1f : 1.2f;
    }

    @Override
    public String info()
    {
        return "Snipers are very accurate and capable of detecting weaknesses in the strongest foes.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if(level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Sniper_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("+20% damage from ranged weapons."));
        sb.append("\n");
        sb.append(highlight("Detects weak spots in targets bypassing their armor."));
        sb.append("\n");
        sb.append("\n");

        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Sniper Subclass";
        }

        return "";
    }
}
