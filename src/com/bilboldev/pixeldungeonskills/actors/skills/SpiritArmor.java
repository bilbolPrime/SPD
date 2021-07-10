package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.utils.GLog;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class SpiritArmor extends PassiveSkillA3{


    {
        name = "Spirit Armor";
        tier = 3;
        image = 27;
        level = 0;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
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
    public int incomingDamageReduction(int damage)
    {
        if(active == false)
            return 0;
        int maxReduction = (int) (damage * 0.1f * level);
        if(maxReduction == 0 && damage > 0)
            maxReduction = 1;

        if(Dungeon.hero.MP > maxReduction)
            Dungeon.hero.MP -= maxReduction;
        else
        {
            maxReduction = Dungeon.hero.MP;
            Dungeon.hero.MP = 0;
        }

        if(maxReduction != 0)
            GLog.p(" (Spirit Armor absorbed " + maxReduction + " damage) ");

        return maxReduction;
    }

    @Override
    public String info()
    {
        return "When activated, a portion of damage is taken from mana _when_ _possible._ \n\n"
                + extendedInfo()
                + requiresInfo()
                + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": " + (int) (100 * (0.1 * i)) + "% damage absorbed.";
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
            return "\nRequires: Battle Mage";
        }

        return "";
    }

    @Override
    public String costString(){
        return "\n\n" + highlight("This is a passive skill that can be enabled or disabled.");
    }


    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(BattleMage.class);
        return toReturn;
    }
}
