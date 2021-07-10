package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class DoubleShot extends ActiveSkill2{


    {
        name = "Double Shot";
        castText = "Two for one";
        image = 90;
        tier = 2;
        mana = 5;
        useDelay = 4f;
    }

    private boolean onDouble = false; // prevent infinite loop

    private int damageBoost = 0;

    @Override
    public float rangedDamageModifier()
    {
        if(damageBoost < 1)
            return 1f;
        else
        {
            damageBoost--;
            return 1f + 0.1f * (level - 3);
        }
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active1.active = false; // Disable Aimed Shot
            hero.heroSkills.active3.active = false; // Disable Bombvoyage
        }
    }

    @Override
    public boolean doubleShot()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return false;
        else if(onDouble == false)
        {
            onDouble = true;
            damageBoost = 2;
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();

            castTextYell();
            return true;
        }
        onDouble = false;
        return false;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Shoots two arrows at the same time.\n\n"
                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (i - 3) * (10) + "% damage per arrow";

            if(i > 2) {
                levelDescription =  "Level " + i  + ": +" + (i - 3) * (10) + "% damage per arrow";
            }

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
            return "\nRequires: Warden";
        }

        return "";
    }
}
