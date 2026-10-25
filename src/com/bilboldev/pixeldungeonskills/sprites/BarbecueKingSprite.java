package com.bilboldev.pixeldungeonskills.sprites;

import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.BarbecueKing;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.FlameParticle;

public class BarbecueKingSprite extends RatKingSprite {
    private Emitter fire;
    private int grill;
    @Override
    public void link(Char ch) {
        super.link(ch);
        grill = ((BarbecueKing)ch).grill;
        fire = CellEmitter.center(grill);
        fire.pour(FlameParticle.FACTORY, .1f);
    }

    @Override
    public void update() {
        super.update();
        if (fire != null) fire.visible = Dungeon.visible[grill];
    }

    @Override
    public void kill() {
        if (fire != null) {
            fire.on = false;
            fire = null;
        }
        super.kill();
    }
}
