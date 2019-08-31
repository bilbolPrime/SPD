package com.bilboldev.pixeldungeonskills.actors.skills;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.ElmoParticle;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.ui.StatusPane;

import java.util.ArrayList;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class AimedShot extends ActiveSkill1{


    boolean cast;
    {
        name = "Aimed Shot";
        castText = "I see him";
        image = 93;
        tier = 1;
        mana = 3;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);
        if(action == Skill.AC_ACTIVATE)
        {
            hero.heroSkills.active2.active = false; // Disable Double shot
            hero.heroSkills.active3.active = false; // Disable Bombvoyage
        }
    }

    @Override
    public float rangedDamageModifier()
    {
        float toReturn = 1f;
        toReturn += cast ? 0.2f * level : 0;
        cast = false;
        return toReturn;
    }

    @Override
    public boolean aimedShot()
    {
        if(active == false || Dungeon.hero.MP < getManaCost())
        {
            cast = false;
            return false;
        }

        cast = true;

        castTextYell();
        Dungeon.hero.MP -= getManaCost();
        StatusPane.manaDropping += getManaCost();
        return true;
    }

    @Override
    protected boolean upgrade()
    {
        return true;
    }


    @Override
    public String info()
    {
        return "Stronger ranged attack that never misses.\n"
                + costUpgradeInfo();
    }
}
