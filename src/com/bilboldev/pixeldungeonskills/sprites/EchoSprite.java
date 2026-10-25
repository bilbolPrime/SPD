package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.EchoDuel;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.EchoReflection;
import com.bilboldev.pixeldungeonskills.effects.particles.ShadowParticle;

public class EchoSprite extends MirrorSprite {
    @Override
    public void link(Char ch) {
        super.link(ch);
        if (ch instanceof EchoReflection) {
            appearance(Dungeon.hero.heroClass, ((EchoReflection)ch).armorTier);
        } else {
            EchoDuel duel = (EchoDuel)ch;
            appearance(duel.heroClass(), duel.armorTier);
        }
    }

    public void appearance(HeroClass hero, int tier) {
        texture(hero.spritesheet());
        updateArmor(tier);
        hardlight(.65f, .8f, 1f);
        alpha(.65f);
    }

    @Override
    public void die() {
        emitter().burst(ShadowParticle.CURSE, 8);
        super.die();
    }
}
