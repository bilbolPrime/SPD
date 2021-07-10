package com.bilboldev.pixeldungeonskills.actors.skills;


import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.Legend;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Enrage extends PassiveSkillA2{


    {
        name = "Enrage";
        castText = "Basic training";
        tier = 2;
        image = 136;

        useDelay = 2f;
    }


    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();
        if(active == false && level > 0)
            actions.add(AC_ACTIVATE);
        else if(level > 0)
            actions.add(AC_DEACTIVATE);

        if(hero.skillTree.canLevel(this)){
            actions.add(AC_ADVANCE);
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if(action == Skill.AC_ACTIVATE)
        {
            active = true;
        }
        else    if(action == Skill.AC_DEACTIVATE)
        {
            active = false;
        }
        else {
            super.execute(hero, action);
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
        return "Scrambles the mind of your minions making them attack recklessly but leaving them more open to attacks.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }


    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int)(0.05f * i * 100) + "% damage and -" + (int)(0.05f * i * 100) + "% defence.";
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
    public String costString(){
        return "\n\n" + highlight("This is a passive skill that can be enabled or disabled.");
    }

    @Override
    public float damageModifier()
    {
        return active ? 1f + 0.05f * level : 1f;
    }

    @Override
    public float incomingDamageModifier()
    {
        return active ? 1f + level * 0.05f : 1f;
    }

    @Override
    public String requiresInfo(){
        if(level == 0){
            return "\nRequires: Warlock";
        }

        return "";
    }


    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Warlock.class);
        return toReturn;
    }
}
