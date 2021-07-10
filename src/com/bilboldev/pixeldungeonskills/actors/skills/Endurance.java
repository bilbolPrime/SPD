package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Endurance extends PassiveSkillA1 {


    {
        name = "Endurance";
        image = 1;
    }

    @Override
    protected boolean upgrade()
    {
        Dungeon.hero.HT += 5;
        Dungeon.hero.HP += 5;
        return true;
    }


    @Override
    public String info()
    {
        return "Endurance increases the amount of damage a hero can buffer in combat.\n\n"
             //   + costUpgradeInfo()
                +     extendedInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (5 * i) + " max health.";
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
