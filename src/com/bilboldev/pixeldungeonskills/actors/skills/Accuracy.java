package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Accuracy extends PassiveSkillB1{


    {
        name = "Accuracy";
        image = 81;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int toHitModifier(){return level;}

    @Override
    public String info()
    {
        return "Increases chance to hit when using a ranged weapon.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + i + " attack skill.";
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

}
