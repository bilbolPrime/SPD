package com.bilboldev.pixeldungeonskills.items.weapon.gun;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Roots;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;

public class Mortar extends Gun {

    {
        name  = "Mortar";
        image = ItemSpriteSheet.MORTAR;
        DLY   = 2.0f;
    }

    public Mortar() {
        super(5);
    }

    @Override
    protected int initialCharges() {
        return 1;
    }

    @Override
    protected float turnsPerCharge() {
        return Math.max(8f, 22f - level() * 0.5f);
    }

    @Override
    public String desc() {
        return "A massive shoulder-mounted cannon. Devastating power, extremely slow reload. "
             + "Direct hits can knock enemies off their feet.";
    }
}
