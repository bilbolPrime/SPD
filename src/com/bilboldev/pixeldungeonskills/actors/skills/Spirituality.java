package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Spirituality extends PassiveSkillA1{


    {
        name = "Spirituality";
        image = 25;
        tier = 1;
    }

    @Override
    protected boolean upgrade()
    {
        Dungeon.hero.MP += 10;
        Dungeon.hero.MMP += 10;
        return true;
    }


    @Override
    public String info()
    {
        return "The hero trains his mind and soul as he trains his body.\n\n"
                +     extendedInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (10 * i) + " max mana.";
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
