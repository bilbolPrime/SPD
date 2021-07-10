package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class KnockBack extends ActiveSkill2{


    {
        name = "KnockBack";
        castText = "KnockBack!";
        tier = 2;
        image = 18;
        mana = 5;
        useDelay = 5f;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active1.active = false; // Disable Smash
            hero.heroSkills.active3.active = false; // Disable Rampage
        }
    }

    @Override
    public float damageModifier()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return 1f;
        else
        {
            return 1f + 0.025f  * level;
        }
    }

    @Override
    public boolean knocksBack()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return false;
        else
        {
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();

            castTextYell();
            return true;
        }
    }


    @Override
    protected boolean upgrade()
    {
        return true;
    }




    @Override
    public String info()
    {
        return "Target is knocked back.\n\n"
                //    + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int)(0.025f * i * 100) + "% damage.";
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
            return "\nRequires: Toughness";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Toughness.class);
        return toReturn;
    }
}
