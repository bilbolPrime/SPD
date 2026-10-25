package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

public class Smite extends Smash{

    {
        name = "Smite";
        castText = Message.key("custom.generated.entry_smite_db8b402b39");
        tier = 1;
        image = 20;
        mana = 3;
        level = 0;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active2.active = false;
            hero.heroSkills.active3.active = false;
        }
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float meleeSpeedModifier()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return 1f;

        return 0.5f;
    }

    @Override
    public float damageModifier()
    {
        if(active == false || Dungeon.hero.MP < getManaCost() || coolDown())
            return 1f;
        else
        {
            Dungeon.hero.MP -= getManaCost();
            StatusPane.manaDropping += getManaCost();

            castTextYell();
            return 1f + 0.2f * level;
        }
    }

    @Override
    public String info()
    {
        return "A slow but devastating attack.\n\n"

                + extendedInfo()
                + requiresInfo() + costString();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= Skill.MAX_LEVEL; i++)
        {
            String levelDescription =  "Level " + i  + ": +" + (int)(0.2f * i * 100) + "% damage";
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
            return "\nRequires: Gladiator";
        }

        return "";
    }

    @Override
    public ArrayList<Class<? extends Skill>> getRequirements(){
        ArrayList<Class<? extends Skill>> toReturn = new ArrayList<>();
        toReturn.add(Gladiator.class);
        return toReturn;
    }
}
