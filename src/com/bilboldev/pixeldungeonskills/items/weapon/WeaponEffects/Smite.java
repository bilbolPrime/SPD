package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.effects.Speck;

public class Smite extends WeaponEffect {
    {
        Name = "Smite";
    }

    @Override
    public float damageMultiplier() {
        return 1.2f;
    }

    @Override
    public void onHit(Char source, Char target, int damageDealt) {
        showHit(target, 0xFFD36A, Speck.factory(Speck.LIGHT), Assets.SND_HIT);
    }
}
