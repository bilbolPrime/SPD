package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;

import java.util.ArrayList;

public class ExecutionerSkill extends PassiveSkillB3{

    {
        name = "Executioner";
        tier = 3;
        image = 160;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float gunRearmSpeedModifier() {
        return level == 0 ? 1f : 1.25f;
    }

    @Override
    public float gunAttackSpeedModifier() {
        if (level == 0 || Dungeon.hero == null || Dungeon.hero.HT <= 0) {
            return 1f;
        }

        return Dungeon.hero.HP * 1f / Dungeon.hero.HT < 0.40f ? 1.5f : 1f;
    }

    @Override
    public String info()
    {
        return "Executioners trade safety for overwhelming gun pressure.\n"
                + extendedInfo()
                + requiresInfo();
    }

    @Override
    public String extendedInfo(){
        StringBuilder sb = new StringBuilder();
        if (level == 0)
        {
            sb.append("Unlocked automatically when you choose the _Executioner_ path.");
            sb.append("\n");
            sb.append("\n");
        }

        sb.append(highlight("+25% rearm speed."));
        sb.append("\n");
        sb.append(highlight("+50% attack speed with guns when under 40% health."));
        sb.append("\n");
        return  sb.toString();
    }

    @Override
    public String requiresInfo(){
        if (level == 0){
            return "\nRequires: Executioner Subclass";
        }

        return "";
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<String>();

        return actions;
    }
}
