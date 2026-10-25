package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.levels.Level;
import java.util.ArrayDeque;

public final class RebornBossSpawn {
    private RebornBossSpawn() { }

    public static int companion(Level level, int boss, int hero) {
        boolean[] visited = new boolean[Level.LENGTH];
        ArrayDeque<Integer> cells = new ArrayDeque<>();
        cells.add(boss);
        visited[boss] = true;
        int result = -1, best = -1;
        while (!cells.isEmpty()) {
            int cell = cells.removeFirst();
            if (Actor.findChar(cell) == null && cell != hero && cell != level.exit && cell != level.entrance
                    && Level.distance(boss, cell) <= 6 && Level.distance(hero, cell) > best) {
                result = cell;
                best = Level.distance(hero, cell);
            }
            for (int offset : Level.NEIGHBOURS8) {
                int next = cell + offset;
                if (next >= 0 && next < Level.LENGTH && !visited[next] && Level.adjacent(cell, next)
                        && Level.passable[next] && !Level.avoid[next]) {
                    visited[next] = true;
                    cells.add(next);
                }
            }
        }
        return result;
    }

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
