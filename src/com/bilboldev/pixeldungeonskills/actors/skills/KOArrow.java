package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class KOArrow extends ActiveSkill2{


    {
        name = "KO Arrow";
        castText = "Goodnight sweet prince";
        tier = 2;
        image = 60;
        mana = 10;
    }

    boolean cast = false;

    @Override
    public boolean goToSleep()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
        {
            cast = false;
            return false;
        }

        cast = true;

        if(Random.Int(100) < 5 * level)
        {
            castTextYell();
            return true;
        }

        return false;
    }

    @Override
    public boolean slowTarget(){
        if(cast){
            cast = false;
            return true;
        }

        return  false;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Shoots an arrow filled with an anaesthetic agent numbing the target.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":  Slows target, " + (i * 5) + "% chance of KO.";

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
            return "\nRequires: Aimed Shot and Sniper";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(AimedShot.class);
        toReturn.add(Sniper.class);
        return toReturn;
    }
}
