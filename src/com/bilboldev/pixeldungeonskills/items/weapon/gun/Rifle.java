package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Rifle extends Gun {

    {
        name  = "Rifle";
        image = ItemSpriteSheet.RIFLE;
        ACU   = 1.5f;
    }

    public Rifle() {
        super(4);
    }

    @Override
    protected int initialCharges() {
        return 3;
    }

    @Override
    protected float turnsPerCharge() {
        return Math.max(5f, 18f - level() * 0.5f);
    }

    @Override
    public String desc() {
        return "A long-barrelled precision firearm. Highly accurate and powerful, but slow to swing in melee.";
    }
}
