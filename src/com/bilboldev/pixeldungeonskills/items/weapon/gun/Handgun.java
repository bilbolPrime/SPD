package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Handgun extends Gun {

    {
        name  = "Handgun";
        image = ItemSpriteSheet.HANDGUN;
    }

    public Handgun() {
        super(1);
    }

    @Override
    protected int initialCharges() {
        return 1;
    }

    @Override
    protected float turnsPerCharge() {
        return Math.max(5f, 14f - level() * 0.5f);
    }

    @Override
    public String desc() {
        return "A small, compact handgun. Easy to carry, low damage — but it fires and recharges quickly.";
    }
}
