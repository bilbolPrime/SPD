package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfFrostLevel;
import com.bilboldev.utils.Bundle;
import java.io.IOException;

public final class IceCaves {
    public static final int FIRST = 11, LAST = 14;
    private static final int ACTIVE = 1, VISITED = 2, SCROLL_DROPPED = 4, BOSS_SLAIN = 8;
    private static int state;

    private IceCaves() { }

    public static boolean active() {
        return Dungeon.rules().mode == GameplayMode.REBORN && (state & ACTIVE) != 0;
    }

    public static boolean visited() {
        return (state & VISITED) != 0;
    }

    public static boolean bossSlain() {
        return (state & BOSS_SLAIN) != 0;
    }

    public static void defeatBoss() {
        state |= BOSS_SLAIN;
    }

    public static void enter() {
        state |= ACTIVE | VISITED;
    }

    public static void leave() {
        state &= ~ACTIVE;
    }

    public static void reset() {
        state = 0;
    }

    public static int levelKey(int depth) {
        return active() && depth >= FIRST && depth <= LAST ? 1000 + depth : depth;
    }

    public static int levelKey(Bundle world) {
        int depth = world.getInt("depth");
        return (world.getInt("ice_caves") & ACTIVE) != 0 && depth >= FIRST && depth <= LAST
                ? 1000 + depth : depth;
    }

    public static boolean branchKey(int key) {
        return key >= 1000 + FIRST && key <= 1000 + LAST;
    }

    public static boolean validLevelKey(int key) {
        return key >= 1 && key <= 26 || key == 1000 || branchKey(key);
    }

    public static void dropScroll(Mob boss) {
        if (Dungeon.rules().mode != GameplayMode.REBORN || Dungeon.depth != 10
                || (state & SCROLL_DROPPED) != 0) return;
        state |= SCROLL_DROPPED;
        Heap heap = Dungeon.level.drop(new ScrollOfFrostLevel(), boss.pos, boss);
        if (heap.sprite != null) heap.sprite.drop();
    }

    public static void store(Bundle bundle) {
        bundle.put("ice_caves", state);
    }

    public static void restore(Bundle bundle) throws IOException {
        int saved = bundle.getInt("ice_caves");
        if (saved < 0 || saved > 15 || (saved & (ACTIVE | BOSS_SLAIN)) != 0 && (saved & VISITED) == 0)
            throw new IOException("Invalid Ice Caves progress");
        state = Dungeon.rules().mode == GameplayMode.REBORN ? saved : 0;
    }
}
