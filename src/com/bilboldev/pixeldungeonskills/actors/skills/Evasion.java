package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Evasion extends PassiveSkillA2{


    {
        name = "Evasion";
        image = 152;
        tier = 2;
    }


    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int evasionDefenceBonus() {return level;}

    @Override
    public String info()
    {
        return "While not Free Runners themselves, assassins' reflexes should never be underestimated.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int) (i) + " defense on evasion.";
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
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Venom.class);
        toReturn.add(Assassin.class);
        return toReturn;
    }
}
