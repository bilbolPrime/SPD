package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Fletching extends PassiveSkillA1{


    {
        name = "Fletching";
        image = 73;
        tier = 1;
    }

    @Override
    public int fletching() { return level == 0 ? 0 : 100 - 5 * level;}

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Creates arrows with time.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": Fletches arrows with time.";

            if(i != 1){
                levelDescription =  "Level " + i  + ": " + (i * 5) + "% faster fletching.";
            }

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
        return "\nRequires: Accuracy";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Accuracy.class);
        return toReturn;
    }
}
