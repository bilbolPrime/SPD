package com.bilboldev.pixeldungeonskills.actors.hero;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.buffs.ManaRegeneration;
import com.bilboldev.pixeldungeonskills.actors.buffs.Regeneration;
import com.bilboldev.pixeldungeonskills.items.Amulet;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.PlateArmor;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Longsword;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.MissionScene;
import com.bilboldev.pixeldungeonskills.scenes.SurfaceScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndGauntletDie;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.utils.Bundle;

/**
 * Created by Moussa on 04-Feb-17.
 */
public class GauntletHero extends Hero {

    {
        heroClass = HeroClass.GAUNTLET;
        lvl = 1;
    }


    @Override
    public boolean act() {
            super.act();
        return false;
    }

    @Override
    public void live() {
        Buff.affect(this, ManaRegeneration.class);
        Buff.affect( this, Regeneration.class );
        Buff.affect( this, Hunger.class );

        lvl = 1;
        HP = HT = 20;
        STR = 11;
        MP = MMP = 20;
        attackSkill = 5;
        defenseSkill = 5;
       //Item tmp = new Longsword().identify();
       //belongings.weapon = (Weapon)tmp;
       //tmp = new PlateArmor().identify();
       //belongings.armor = (Armor)tmp;
    }

    @Override
    public void die(Object reason)
    {
        super.die(reason);
        GauntletScene.show(new WndGauntletDie());
        GLog.n( "%s: \"%s\" ", "Rat King", "You have served your purpose... " + GauntletScene.level + " waves... pathetic." );
    }

    @Override
    public void storeInBundle( Bundle bundle ) {



    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {

    }

    @Override
    protected boolean actAscend( HeroAction.Ascend action ) {
        int stairs = action.dst;
        if (pos == stairs && pos == Dungeon.level.entrance) {
            ready();
            return false;
        }

        return super.actAscend(action);
    }

    @Override
    protected boolean actDescend( HeroAction.Descend action ){
        int stairs = action.dst;

        if (pos == stairs && pos == Dungeon.level.exit) {
            GauntletScene.nextLevel();
            ready();
            return false;
        }

        return super.actDescend(action);
    }

    protected boolean actStorage( HeroAction.Storage action ) {
        int stairs = action.dst;

        if (pos == stairs && pos == Dungeon.level.storage) {
            ready();
            return false;
        }

        return super.actStorage(action);
    }
}
