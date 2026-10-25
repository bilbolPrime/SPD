package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

public class Rampage extends ActiveSkill3{

    {
        name = "Rampage";
        castText = Message.key("custom.generated.entry_rampage_d7bdc4fc19");
        tier = 3;
        image = 19;
        mana = 5;
        useDelay = 5f;
    }

    @Override
    public float damageModifier()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return 1f;
        else
        {
            return 1f + 0.1f * level;
        }
    }

    @Override
    public boolean AoEDamage()
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
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active1.active = false;
            hero.heroSkills.active2.active = false;
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
        return "Hits all nearby targets.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (i * 10) + "% damage";

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
            return "\nRequires: Berserker";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Berserker.class);
        return toReturn;
    }
}
