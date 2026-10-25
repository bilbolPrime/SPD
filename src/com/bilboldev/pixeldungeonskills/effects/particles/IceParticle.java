package com.bilboldev.pixeldungeonskills.effects.particles;

import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.particles.PixelParticle;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.utils.Random;

public class IceParticle extends PixelParticle {
    public static final Emitter.Factory SHARDS = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            ((IceParticle)emitter.recycle(IceParticle.class)).reset(x, y, false);
        }
    };

    public static final Emitter.Factory SPIKES = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            ((IceParticle)emitter.recycle(IceParticle.class)).reset(x, y, true);
        }
    };

    public static final Emitter.Factory MIST = new Emitter.Factory() {
        @Override public void emit(Emitter emitter, int index, float x, float y) {
            Speck mist = (Speck)emitter.recycle(Speck.class);
            mist.reset(index, x, y, Speck.STEAM);
            mist.hardlight(0xA9DDEB);
            mist.speed.set(Random.Float(-3, 3), -3);
        }
    };

    private boolean spike;
    private float ground;

    public IceParticle() {
        texture(Assets.ICE_EFFECTS);
        origin.set(8);
    }

    private void reset(float x, float y, boolean spike) {
        revive();
        this.spike = spike;
        ground = y;
        this.x = x - 8;
        this.y = y - 8;
        frame(spike ? 0 : 16, 0, 16, 16);
        resetColor();
        speed.set(0);
        acc.set(0);
        angle = angularSpeed = 0;
        left = lifespan = spike ? .65f : Random.Float(.45f, .7f);
        if (spike) {
            scale.set(1, .1f);
            this.y -= 8 * scale.y;
        } else {
            scale.set(Random.Float(.65f, 1));
            speed.polar(Random.Float(-3.1415926f, 0), Random.Float(24, 48));
            acc.y = 80;
            angularSpeed = Random.Float(-150, 150);
        }
    }

    @Override
    public void update() {
        super.update();
        if (spike) {
            scale.y = Math.min(1, .1f + (lifespan - left) / .12f);
            y = ground - 8 - 8 * scale.y;
        }
        alpha(Math.max(0, Math.min(1, left / .2f)));
    }
}
