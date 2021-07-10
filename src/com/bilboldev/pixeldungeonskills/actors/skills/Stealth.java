package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Stealth extends PassiveSkillA2{


    {
        name = "Stealth";
        image = 50;
        tier = 2;
    }

    @Override
    public int stealthBonus(){return level / 4;}

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Harder to detect.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (i * 0.25) + " stealth.";
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
