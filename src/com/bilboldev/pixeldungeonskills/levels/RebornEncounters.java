package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Iterator;

public final class RebornEncounters {
    private RebornEncounters() {}

    public static int packs(int depth) {
        if (depth < 2 || depth > 24 || depth % 5 == 0 || depth == 21) return 0;
        return depth < 16 ? 1 : 2;
    }

    public static int population(int depth) {
        return 5 + depth % 5 + (depth-1)/10;
    }

    public static boolean championPair(int depth, int pack) {
        int index = pack;
        for (int d = 2; d < depth; d++) index += packs(d);
        return Math.floorMod(Dungeon.rebornEncounterSeed + index, 5) == 0;
    }

    static void populate(RegularLevel level) {
        ArrayList<Room> rooms = new ArrayList<>();
        for (Room room : level.rooms) if (room.type == Room.Type.STANDARD) rooms.add(room);
        ArrayList<Room> unused = new ArrayList<>(rooms);
        int[] walking = level.themedLayout ? RebornDungeonBuilder.distances(level, level.entrance) : null;
        int count = 0;
        for (int pack = 0; pack < packs(Dungeon.depth); pack++) {
            boolean pair = championPair(Dungeon.depth, pack);
            int members = pair ? 2 : Dungeon.depth < 11 ? 2 : 3;
            ArrayList<Integer> cells = new ArrayList<>();
            Room chosen = null;
            if (level.themedLayout) {
                int best = -1;
                for (Room candidate : unused) {
                    ArrayList<Integer> options = encounterCells(level, candidate, walking);
                    if (options.size() < members) continue;

                    int score = Math.min(30, options.size()) + Math.min(3, candidate.rebornDoors.length)*4 + Random.Int(9);
                    if (score > best) {
                        chosen = candidate;
                        cells = options;
                        best = score;
                    }
                }
                if (chosen != null) unused.remove(chosen);
            }
            while (chosen == null && !unused.isEmpty()) {
                Room candidate = unused.remove(Random.Int(unused.size()));
                cells = safeCells(level, candidate, 7);
                if (cells.size() >= members) {
                    chosen = candidate;
                    break;
                }
            }
            if (chosen == null) throw new IllegalStateException("Reborn encounter has no safe room");
            int anchor = level.themedLayout ? encounterAnchor(level, chosen, cells) : cells.get(Random.Int(cells.size()));
            cells.remove(Integer.valueOf(anchor));
            final int center = anchor;
            final int[] approach = level.themedLayout ? RebornDungeonBuilder.distances(level, anchor) : null;
            java.util.Collections.sort(cells, (a, b) -> Integer.compare(approach == null ? Level.distance(center, a) : approach[a],
                    approach == null ? Level.distance(center, b) : approach[b]));
            Mob leader = Bestiary.mob(Dungeon.depth);
            leader.pos = anchor;
            leader.rebornPack = pack+1;
            RebornChampion.grant(leader, pair);
            add(level, leader);
            for (int member = 1; member < members; member++) {
                Mob mob;
                try {
                    mob = leader.getClass().newInstance();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                mob.pos = cells.remove(0);
                mob.rebornPack = pack+1;
                if (pair) RebornChampion.grant(mob, true);
                add(level, mob);
            }
            count += members;
        }
        ArrayList<Integer> cells = new ArrayList<>();
        for (Room room : rooms) cells.addAll(safeCells(level, room, 4));
        while (count < population(Dungeon.depth) && !cells.isEmpty()) {
            Mob mob = Bestiary.mob(Dungeon.depth);
            mob.pos = cells.remove(Random.Int(cells.size()));
            add(level, mob);
            count++;
        }
    }

    private static ArrayList<Integer> encounterCells(RegularLevel level, Room room, int[] walking) {
        ArrayList<Integer> result = safeCells(level, room, 7);
        for (Iterator<Integer> cells = result.iterator(); cells.hasNext();) {
            int cell = cells.next();
            boolean tooClose = walking[cell] < 10;
            for (int door : room.rebornDoors) {
                if (Level.distance(cell, door) < 2) tooClose = true;
            }
            for (Mob mob : level.mobs) {
                if (mob.rebornPack != 0 && Level.distance(cell, mob.pos) < 5) tooClose = true;
            }
            if (tooClose) cells.remove();
        }
        return result;
    }

    private static int encounterAnchor(RegularLevel level, Room room, ArrayList<Integer> cells) {
        int chosen = cells.get(0), best = -1;
        for (int cell : cells) {
            int space = 0, cover = 0, doorDistance = 10;
            for (int n : Level.NEIGHBOURS8) {
                int other = cell+n;
                if (other<0 || other>=Level.LENGTH || !Level.adjacent(cell, other)) continue;
                if (RebornDungeonBuilder.walkable(level, other)) space++;
                else if (Level.solid[other]) cover++;
            }
            for (int door : room.rebornDoors) doorDistance = Math.min(doorDistance, Level.distance(cell, door));
            int score = space*3 + Math.min(2, cover)*2 + Math.min(4, doorDistance)*2 + Random.Int(4);
            if (score>best) {
                best = score;
                chosen = cell;
            }
        }
        return chosen;
    }

    private static ArrayList<Integer> safeCells(RegularLevel level, Room room, int distance) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int y = room.top+1; y < room.bottom; y++) for (int x = room.left+1; x < room.right; x++) {
            int p = x + y*Level.WIDTH;
            if (Level.distance(p, level.entrance) < distance || !Level.passable[p]
                    || Actor.findChar(p) != null || level.heaps.get(p) != null) continue;
            boolean occupied = false;
            for (Mob mob : level.mobs) if (mob.pos == p) occupied = true;
            int tile = level.map[p];
            if (!occupied && (tile == Terrain.EMPTY || tile == Terrain.EMPTY_DECO || tile == Terrain.EMPTY_SP
                    || tile == Terrain.GRASS || tile == Terrain.HIGH_GRASS || tile == Terrain.WATER)) result.add(p);
        }
        return result;
    }

    private static void add(RegularLevel level, Mob mob) {
        level.mobs.add(mob);
        Actor.occupyCell(mob);
        if (level.map[mob.pos] == Terrain.HIGH_GRASS) {
            level.map[mob.pos] = Terrain.GRASS;
            Level.losBlocking[mob.pos] = false;
        }
    }
}
