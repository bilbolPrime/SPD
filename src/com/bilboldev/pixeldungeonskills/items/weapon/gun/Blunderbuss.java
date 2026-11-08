package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.messages.Messages;
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
        return Messages.get("custom.generated.necro_merc.blunderbuss_desc");
    }
}
