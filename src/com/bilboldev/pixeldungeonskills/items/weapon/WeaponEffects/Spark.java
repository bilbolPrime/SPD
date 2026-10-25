package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.particles.SparkParticle;
import com.bilboldev.utils.Random;

public class Spark extends WeaponEffect {
    {
        Name = "Spark";
    }

    @Override
    public void onHit(Char source, Char target, int damageDealt) {
        if (!canAffect(source, target)) return;
        showHit(target, 0x8BCFFF, SparkParticle.FACTORY, Assets.SND_LIGHTNING);
        target.damage(Random.IntRange(4, 8), this, 0x8BCFFF);
    }
}
