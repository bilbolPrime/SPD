package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Sorcerer extends PassiveSkillB2{


    {
        name = "Sorcerer";
        image = 34;
        tier = 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public float wandDamageBonus()
    {
        return 1f + 0.1f * level;
    }

    @Override
    public String info()
    {
        return "A Battle Mage does more damage with his wands.\n\n"
                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            float regenerationDelayBase = 10f;
            float regenerationDelayAfter = (float)(10f / Math.pow(1.2, i));

            String levelDescription =  "Level " + i  + ": +" + (int) (100 * (0.1 * i)) + "% damage.";
            if(i == level){
                sb.append(highlight(levelDescription));
            }
            else {
                sb.append(levelDescription);
            }
            sb.append("\n");
        }
        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Wizard and Battle Mage";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Wizard.class);
        toReturn.add(BattleMage.class);
        return toReturn;
    }
}
