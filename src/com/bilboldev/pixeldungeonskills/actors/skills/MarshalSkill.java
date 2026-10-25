package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

public class MarshalSkill extends PassiveSkillB3{

    {
        name = "Marshal";
        tier = 3;
        image = 161;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public int gunAmmoBonus() {
        return level == 0 ? 0 : 5;
    }

    @Override
    public int gunToHitBonus() {
        return level == 0 ? 0 : 1;
    }

    @Override
    public String info()
    {
        return "Mercenaries who are tired of breaking the law become marshals.\n"
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if (level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Marshal_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("+5 ammo capacity."));
        sb.append("\n");
        sb.append(highlight("+1 attack skill with guns."));

        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if (level == 0){
            return "\nRequires: Marshal Subclass";
        }

        return "";
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }
}
