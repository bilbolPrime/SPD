package com.bilboldev.pixeldungeonskills.actors.skills;


/**
 * Created by Moussa on 20-Jan-17.
 */
public class Toughness extends PassiveSkillA3{


    {
        name = "Toughness";
        tier = 3;
        image = 3;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }

    @Override
    public float incomingDamageModifier()
    {
        return 1f - level * 0.1f;
    }

    @Override
    public String info()
    {
        return "Take less damage from enemy attacks.\n"
                + costUpgradeInfo();
    }
}
