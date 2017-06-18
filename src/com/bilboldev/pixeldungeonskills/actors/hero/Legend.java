package com.bilboldev.pixeldungeonskills.actors.hero;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.buffs.ManaRegeneration;
import com.bilboldev.pixeldungeonskills.actors.buffs.Regeneration;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.PlateArmor;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Longsword;
import com.bilboldev.pixeldungeonskills.levels.MovieLevel;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.utils.Bundle;

/**
 * Created by Moussa on 04-Feb-17.
 */
public class Legend extends Hero {

    {
        heroClass = HeroClass.HATSUNE;
    }


    @Override
    public boolean act() {
            super.act();

            if(MissionScene.scenePause == true)
            {
                spendAndNext( 1f );
            }

        return false;
    }

    @Override
    public boolean isStarving() {
        return false;
    }

    @Override
    public void live() {
        Buff.affect(this, ManaRegeneration.class);
        Buff.affect(this, ManaRegeneration.class);
        Buff.affect(this, ManaRegeneration.class);

        Buff.affect( this, Regeneration.class );
        Buff.affect( this, Regeneration.class );

        lvl = 100;
        HP = HT = 100;
        STR = 25;
        MP = MMP = 100;
        attackSkill = 40;
        defenseSkill = 25;
        Item tmp = new Longsword().identify();
        belongings.weapon = (Weapon)tmp;
        tmp = new PlateArmor().identify();
        belongings.armor = (Armor)tmp;
    }

    @Override
    public void die(Object reason)
    {
        super.die(reason);
        MissionScene.scenePause = true;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {



    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

    }
}
