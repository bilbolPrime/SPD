package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.levels.Level;

public final class RebornBossSpawn {
    private RebornBossSpawn() { }

    public static int farthest(Level level, int from, int minimumRow) {
        int result = -1, best = -1;
        for (int cell = 0; cell < Level.LENGTH; cell++) {
            if (cell / Level.WIDTH < minimumRow || !Level.passable[cell] || Level.avoid[cell]
                    || cell == level.entrance || cell == level.exit || Actor.findChar(cell) != null) continue;
            int distance = Level.distance(from, cell);
            if (distance > best) {
                best = distance;
                result = cell;
            }
        }
        if (result < 0) throw new IllegalStateException("Boss arena has no free floor");
        return result;
    }
}
