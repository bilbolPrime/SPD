package com.bilboldev.pixeldungeonskills.actors.skills;

import java.util.ArrayList;

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
        return necroMercInfo("meditation_desc", extendedInfo())
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

            String levelDescription = necroMercText("level_s_regeneration", i, (int) (100 * (-regenerationDelayAfter + regenerationDelayBase)/ regenerationDelayBase));
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
            return "\n" + necroMercText("requires_spirituality");
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
