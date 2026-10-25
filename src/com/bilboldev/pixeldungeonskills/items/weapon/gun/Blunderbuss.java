package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Blunderbuss extends Gun {

    {
        name  = "Blunderbuss";
        image = ItemSpriteSheet.BLUNDERBUSS;
    }

    public Blunderbuss() {
        super(3);
    }

    @Override
    protected int initialCharges() {
        return 1;
    }

    @Override
    protected int shotsPerFire() {
        return 3;
    }

    @Override
    protected float turnsPerCharge() {
        return Math.max(5f, 18f - level() * 0.5f);
    }

    @Override
    public String desc() {
        return "A wide-mouthed shotgun that unleashes three projectiles in a burst. "
             + "Each pellet deals reduced damage, but together they can devastate close targets.";
    }
}
