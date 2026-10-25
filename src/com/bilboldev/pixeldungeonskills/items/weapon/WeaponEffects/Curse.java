package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;

public class Curse extends WeaponEffect {
    {
        Name = "Curse";
    }

    @Override
    protected boolean canAffect(Char source, Char target) {
        return super.canAffect(source, target) && !target.immunities().contains(NecroWeakened.class);
    }

    @Override
    public void onHit(Char source, Char target, int damageDealt) {
        if (!canAffect(source, target)) return;
        NecroWeakened curse = Buff.prolong(target, NecroWeakened.class, 10f);
        if (curse.outgoingDamageModifier() > 0.95f) curse.setLevel(1);
        showHit(target, 0xBE8AE5, ShadowParticle.CURSE, Assets.SND_CURSED);
    }
}
