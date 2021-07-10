package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

/**
 * Created by Moussa on 22-Jan-17.
 */
public class ActiveSkill extends Skill {

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
            Dungeon.hero.heroSkills.lastUsed = this;
            hero.skillTree.activate(this);
        }
        else    if(action == Skill.AC_DEACTIVATE)
        {
            active = false;
        }

        if(action == Skill.AC_ADVANCE){
            hero.skillTree.advance(this);
        }
    }

    @Override
    public String costString(){
        String toReturn = "\n\n";
        toReturn += highlight("Requires " + mana + " mana to cast.");

        return toReturn;
    }
}
