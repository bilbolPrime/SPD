package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.*;

public final class PartyParticles {
    private static final Emitter.Factory[] FACTORIES = {
            MagicMissile.MagicParticle.FACTORY, FlameParticle.FACTORY, MagicMissile.EarthParticle.FACTORY,
            PurpleParticle.MISSILE, MagicMissile.WhiteParticle.FACTORY, WoolParticle.FACTORY,
            PoisonParticle.MISSILE, LeafParticle.GENERAL, MagicMissile.SlowParticle.FACTORY,
            MagicMissile.ForceParticle.FACTORY, MagicMissile.ColdParticle.FACTORY, ShadowParticle.MISSILE,
            BlastParticle.FACTORY, BloodParticle.FACTORY, EarthParticle.FACTORY, ElmoParticle.FACTORY,
            EnergyParticle.FACTORY, FlowParticle.FACTORY, LeafParticle.LEVEL_SPECIFIC,
            SacrificialParticle.FACTORY, PoisonParticle.SPLASH, PurpleParticle.BURST, ShadowParticle.CURSE,
            ShadowParticle.UP, ShaftParticle.FACTORY, SmokeParticle.FACTORY, SnowParticle.FACTORY,
            SparkParticle.FACTORY, WebParticle.FACTORY, WindParticle.FACTORY
    };

    private PartyParticles() { }

    static int id(Emitter.Factory factory) {
        for (int i = 0; i < FACTORIES.length; i++) if (FACTORIES[i] == factory) return i;
        int type = Speck.factoryType(factory);
        return type < 0 ? -1 : 100 + type;
    }

    static boolean valid(int id) {
        return id >= 0 && id < FACTORIES.length || id >= 100 && id <= 114 || id >= 201 && id <= 211;
    }

    public static Emitter.Factory factory(int id, boolean light) {
        if (!valid(id)) throw new IllegalArgumentException("Unknown party particle");
        if (id < FACTORIES.length) return FACTORIES[id];
        final int type = id - 100;

        return new Emitter.Factory() {
            @Override public void emit(Emitter emitter, int index, float x, float y) {
                ((Speck)emitter.recycle(Speck.class)).reset(index, x, y, type);
            }
            @Override public boolean lightMode() { return light; }
        };
    }

    public static class BurstEmitter extends Emitter {
        @Override public void start(Factory factory, float interval, int quantity) {
            PartyEffects.particles(factory, interval, quantity, target == null ? x : target.x,
                    target == null ? y : target.y, target == null ? width : target.width,
                    target == null ? height : target.height, renderAnchor());
            super.start(factory, interval, quantity);
        }
    }
}
