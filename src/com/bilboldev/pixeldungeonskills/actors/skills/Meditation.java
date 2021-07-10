package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Meditation extends PassiveSkillA2{


    {
        name = "Meditation";
        image = 26;
        tier = 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public int manaRegenerationBonus() {return level;}

    @Override
    public String info()
    {
        return "A true wizard master can keep calm in the most dire situations.\n\n"
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

            String levelDescription =  "Level " + i  + ": +" + (int) (100 * (-regenerationDelayAfter + regenerationDelayBase)/ regenerationDelayBase) + "% regeneration.";
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
            return "\nRequires: Spirituality";
        }

        return "";
    }


    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Spirituality.class);
        return toReturn;
    }
}
