package com.bilboldev.pixeldungeonskills.actors.skills;

public class MercBruteSkillA extends Toughness {
    {
        tag = "mercA";
    }

    @Override
    protected boolean upgrade()
    {
        return false;
    }

    @Override
    protected float reductionPerLevel() {
        return 0.04f;
    }

    @Override
    public String info()
    {
        return "Take less damage from enemy attacks.\n";
    }
}
