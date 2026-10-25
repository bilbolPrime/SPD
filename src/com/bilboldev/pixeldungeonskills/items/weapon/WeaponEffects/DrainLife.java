package com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Random;

public class DrainLife extends WeaponEffect {
    {
        Name = "DrainLife";
    }

    @Override
    public void onHit(Char source, Char target, int damageDealt) {
        if (!canAffect(source, target)) return;
        showHit(target, 0xCC80A8, ShadowParticle.UP, Assets.SND_ZAP);
        int before = target.HP;
        target.damage(Random.IntRange(8, 16), this, 0xCC80A8);
        int drained = Math.max(0, before - Math.max(0, target.HP));
        if (!source.isAlive()) return;
        int healed = Math.min(drained, source.HT - source.HP);
        if (healed <= 0) return;
        source.HP += healed;
        if (source.sprite != null && source.sprite.visible) {
            source.sprite.showStatus(CharSprite.POSITIVE, "+" + healed);
            source.sprite.centerEmitter().burst(Speck.factory(Speck.HEALING), 3);
        }
    }
}
