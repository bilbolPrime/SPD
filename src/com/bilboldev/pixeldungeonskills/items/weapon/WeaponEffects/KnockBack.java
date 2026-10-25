package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.effects.particles.EarthParticle;

public class KnockBack extends WeaponEffect {
    {
        Name = "KnockBack";
    }

    @Override
    protected boolean canAffect(Char source, Char target) {
        return super.canAffect(source, target) && !Bestiary.isBoss(target)
                && Dungeon.level != null && Pushing.knockbackCell(source, target) != -1;
    }

    @Override
    public void onHit(Char source, Char target, int damageDealt) {
        if (!canAffect(source, target)) return;
        showHit(target, 0xDEC39A, EarthParticle.FACTORY, Assets.SND_HIT);
        Pushing.knockBack(source, target);
    }
}
