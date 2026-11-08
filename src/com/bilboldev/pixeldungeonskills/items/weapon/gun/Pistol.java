package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Pistol extends Gun {

    {
        name  = "Pistol";
        image = ItemSpriteSheet.PISTOL;
    }

    public Pistol() {
        super(2);
    }

    @Override
    protected int initialCharges() {
        return 2;
    }

    @Override
    protected float turnsPerCharge() {
        return Math.max(5f, 16f - level() * 0.5f);
    }

    @Override
    public String desc() {
        return Messages.get("custom.generated.necro_merc.pistol_desc");
    }
}
