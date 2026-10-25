package com.bilboldev.pixeldungeonskills.items;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Corpse extends Item {

    {
        name = "corpse";
        image = ItemSpriteSheet.CORPSE;
        stackable = true;
        defaultAction = AC_THROW;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int price() {
        return quantity;
    }

    @Override
    public void doDrop( Hero hero ) {
        Sample.INSTANCE.play( Assets.SND_BONES );
        super.doDrop( hero );
    }

    @Override
    protected void onThrow( int cell ) {
        Sample.INSTANCE.play( Assets.SND_BONES );
        super.onThrow( cell );
    }

    @Override
    public String info() {
        return "A fresh corpse. It can be used for necromancy.";
    }
}
