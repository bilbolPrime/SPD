package com.bilboldev.pixeldungeonskills.actors.skills;



/**
 * Created by Moussa on 20-Jan-17.
 */
public class Bandit extends PassiveSkillA1{


    {
        name = "Bandit";
        image = 49;
        tier = 1;
    }

    @Override
    public int lootBonus(int gold)
    {
        return (int) (gold * 0.1f * level);
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Bonus to gold.\n"
                + "Becomes immune to Mimic gold steal.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (i * 10) + "% bonus gold.";
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
