package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Aggression extends PassiveSkillB2{


    {
        name = "Aggression";
        image = 9;
        tier = 2;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float damageModifier()
    {
        try
        {
            int damagedPercentage = (100 * (Dungeon.hero.HT - Dungeon.hero.HP)) / Dungeon.hero.HT;
            return 1f + 0.001f * level * damagedPercentage;
        }
      catch (Exception e){
            return 1f;
      }

    }

    @Override
    public String info()
    {
        return "The hero becomes even more violent when injured.\n\n"
                //   + costUpgradeInfo()
                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + String.format("%.1f", 100 * (0.001f * i)) + "% damage per 1% lost health.";
            if(i == 10){
                levelDescription =  "Level " + i  + ": +1% damage per 1% lost health.";
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
            return "\nRequires: Toughness and Berserker";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Berserker.class);
        toReturn.add(Toughness.class);
        return toReturn;
    }
}
