package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.sprites.*;

public final class BossRemains extends Mob {
    private final int depth;
    private boolean fading;
    private float fade;

    public BossRemains(int depth, int cell) {
        this.depth = depth;
        HP = 0;
        pos = cell;
        state = PASSIVE;
        switch (depth) {
            case 5: spriteClass = GooSprite.class; break;
            case 10: spriteClass = TenguSprite.class; break;
            case 15: spriteClass = DM300Sprite.class; break;
            case 20: spriteClass = KingSprite.class; break;
            default: throw new IllegalArgumentException("No replaced boss on this floor");
        }
    }

    public void play() {
        sprite.listener = animation -> {
            if (fading) return;
            fading = true;
            if (depth == 15) sprite.emitter().burst(Speck.factory(Speck.WOOL), 15);
        };
        sprite.die();
    }

    public void update() {
        sprite.visible = Dungeon.visible[pos];
        if (fading) {
            fade += Game.elapsed;
            sprite.alpha(Math.max(0, 1 - fade / 3));
            if (fade >= 3) sprite.killAndErase();
        }
    }
}
