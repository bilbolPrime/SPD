package com.bilboldev.pixeldungeonskills.actors.skills;


import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Mastery extends PassiveSkillB3{


    {
        name = "Mastery";
        tier = 3;
        image = 11;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int  weaponLevelBonus()
    {
        if(level == 0){
            return  0;
        }

        if(level < 5){
            return  1;
        }

        if(level < 8){
            return  2;
        }

        if(level == 10){
            return  4;
        }

        return 3;
    }

    @Override
    public float meleeSpeedModifier()
    {
        return 1f + 0.05f * level;
    }


    @Override
    public String info()
    {
        return "Increases mastery of melee weapons.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int) (i * 5) + "% speed " + " and +" + (int) Math.max(1, i / 2.5) + " level.";
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
            return "\nRequires: Firm Hand and Gladiator";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(FirmHand.class);
        toReturn.add(Gladiator.class);
        return toReturn;
    }
}
