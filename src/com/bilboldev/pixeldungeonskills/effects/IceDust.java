package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.particles.PixelParticle;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.thetiles.WallShadows;
import java.util.ArrayList;
import java.util.Collections;

public final class IceDust extends Emitter {
    private final int cell;
    private float delay;

    private IceDust(int cell) {
        this.cell = cell;
        pos((cell % Level.WIDTH + .5f) * 16, (cell / Level.WIDTH + .65f) * 16);
        autoKill = false;
        delay = .4f + (cell % 31) * .06f;
    }

    public static void addTo(Group group, Level level, WallShadows shadows) {
        ArrayList<Integer> edges = new ArrayList<>();
        for (int cell = Level.WIDTH; cell < Level.LENGTH - Level.WIDTH; cell++) {
            int tile = level.map[cell];
            if (cell % Level.WIDTH == 0 || cell % Level.WIDTH == Level.WIDTH - 1
                    || shadows != null && shadows.preserved(cell)
                    || tile != Terrain.EMPTY && tile != Terrain.EMPTY_DECO) continue;
            if (wall(level.map[cell - Level.WIDTH]) || wall(level.map[cell + Level.WIDTH])
                    || wall(level.map[cell - 1]) || wall(level.map[cell + 1])) edges.add(cell);
        }
        java.util.Random random = new java.util.Random(31L * Dungeon.rebornEncounterSeed + Dungeon.levelKey());
        Collections.shuffle(edges, random);
        int[] placed = new int[12];
        int count = 0;
        for (int cell : edges) {
            boolean close = false;
            for (int i = 0; i < count; i++) {
                int dx = cell % Level.WIDTH - placed[i] % Level.WIDTH;
                int dy = cell / Level.WIDTH - placed[i] / Level.WIDTH;
                if (dx * dx + dy * dy < 16) { close = true; break; }
            }
            if (close) continue;
            group.add(new IceDust(cell));
            placed[count++] = cell;
            if (count == placed.length) break;
        }
    }

    private static boolean wall(int tile) { return tile == Terrain.WALL || tile == Terrain.WALL_DECO; }

    @Override public void update() {
        visible = Presentation.dungeonEffects() && Dungeon.visible[cell];
        if (!visible) return;
        super.update();
        if ((delay -= Game.elapsed) <= 0) {
            Dust particle = (Dust)recycle(Dust.class);
            particle.reset(x, y - 2, 0xC8E5EB, 1, 5);
            particle.speed.set(.55f, -.5f);
            particle.alpha(0);
            delay = 6 + cell % 5;
        }
    }

    public static class Dust extends PixelParticle {
        @Override public void update() {
            super.update();
            alpha(.32f * (float)Math.sin(Math.PI * Math.max(0, left) / lifespan));
        }
    }
}
