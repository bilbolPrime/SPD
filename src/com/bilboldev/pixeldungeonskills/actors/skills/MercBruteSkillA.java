package com.bilboldev.pixeldungeonskills.actors.skills;

/**
 * Created by Moussa on 25-Jan-17.
 */
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
    public String info()
    {
        return "Take less damage from enemy attacks.\n";
    }
}
