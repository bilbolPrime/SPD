package com.bilboldev.pixeldungeonskills.actors.skills;


import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Wizard extends PassiveSkillB1{


    {
        name = "Wizard";
        image = 33;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public float wandRechargeSpeedReduction()
    {
        return 1f - 0.05f * level;
    }

    @Override
    public String info()
    {
        return "A true wizard can use his wands to the fullest.\n\n"
                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {

            String levelDescription =  "Level " + i  + ": -" + (int) (i * 5) + "% wand recharge time.";
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
            return "\nRequires: Spark";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Spark.class);
        return toReturn;
    }
}
