package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.effects.particles.PoisonParticle;

public class Venom extends WeaponEffect {
    {
        Name = "Venom";
    }

    @Override
    protected boolean canAffect(Char source, Char target) {
        return super.canAffect(source, target) && !target.immunities().contains(Poison.class);
    }

    @Override
    public void onHit(Char source, Char target, int damageDealt) {
        if (!canAffect(source, target)) return;
        Buff.affect(target, Poison.class).extendItemDuration(Poison.durationFactor(target));
        showHit(target, 0x88DD66, PoisonParticle.SPLASH, Assets.SND_PLANT);
    }
}
