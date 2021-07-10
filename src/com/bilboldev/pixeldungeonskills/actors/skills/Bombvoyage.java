package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;
import com.bilboldev.utils.Random;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Bombvoyage extends ActiveSkill3{


    {
        name = "Bombvoyage";
        castText = "Bombvoyage";
        image = 91;
        tier = 3;
        mana = 15;
        useDelay = 30f;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active1.active = false; // Disable Aimed Shot
            hero.heroSkills.active2.active = false; // Disable Double Arrow
        }
    }

    private boolean cast = false;

    public float paralysisDuration(){
        return paralysisDuration(level);
    }

    public float paralysisDuration(int level){
        return !cast ? 0f : 1f + level * 0.1f;
    }

    public int damage(){
        return damage(level);
    }

    public int damage(int level){
        return !cast ? 0 : level;
    }

    @Override
    public boolean arrowToBomb()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
        {
            cast = false;
            return false;
        }
        else
        {
            cast = true;
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
        return "Attaches a bomb to a standard arrow.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ":   +" + i + " damage and " + String.format("%.1f", (1f + i * 0.1f)) + " paralysis.";

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
            return "\nRequires: Sniper";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Sniper.class);
        return toReturn;
    }
}
