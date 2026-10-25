package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryRecruitment;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

final class RebornLayout {
    private RebornLayout() {}

    static boolean build(RegularLevel level) {
        int[] x = {0, Random.IntRange(7, 9), Random.IntRange(15, 17), Random.IntRange(23, 25), 31};
        int[] y = {0, Random.IntRange(10, 11), Random.IntRange(20, 22), 31};
        boolean transpose = Random.Int(2) == 0;
        ArrayList<Room> all = new ArrayList<>();
        level.rooms = new LinkedHashSet<>();
        for (int row = 0; row < 3; row++) for (int col = 0; col < 4; col++) {
            Room room = new Room();
            if (transpose) room.set(y[row], x[col], y[row+1], x[col+1]);
            else room.set(x[col], y[row], x[col+1], y[row+1]);
            room.type = Room.Type.STANDARD;
            all.add(room);
            level.rooms.add(room);
        }
        for (int i = 0; i < all.size(); i++) for (int j = i+1; j < all.size(); j++)
            all.get(i).addNeigbour(all.get(j));

        int[] corners = {0, 3, 8, 11};
        int entry = corners[Random.Int(4)];
        level.roomEntrance = all.get(entry);
        level.roomExit = all.get(11-entry);
        level.roomEntrance.type = Room.Type.ENTRANCE;
        level.roomExit.type = Room.Type.EXIT;
        LinkedHashSet<Room> core = new LinkedHashSet<>(all);
        ArrayList<Room> leaves = new ArrayList<>();
        ArrayList<Room.Type> roles = new ArrayList<>();
        if (Dungeon.shopOnLevel()) roles.add(Room.Type.SHOP);
        if (!LocalCoop.active() && MercenaryRecruitment.campFloor(Dungeon.depth)) roles.add(Room.Type.MERCENARY);

        int specials = Level.pitRoomNeeded ? 3 : 2;
        for (int i = 0; i < specials; i++) roles.add(Room.Type.NULL);

        if (!reserve(core, leaves, level, roles, 0))
            throw new IllegalStateException("No connected Reborn room allocation");

        for (Room room : core) for (Room other : core)
            if (room.neigbours.contains(other)) room.connect(other);
        for (Room leaf : leaves) {
            ArrayList<Room> neighbours = new ArrayList<>();
            for (Room other : core) if (leaf.neigbours.contains(other)) neighbours.add(other);
            Room connection = leaf.type == Room.Type.SHOP ? level.roomEntrance : Random.element(neighbours);
            leaf.connect(connection);
        }
        level.assignRoomType();
        level.paint();
        level.paintWater();
        level.paintGrass();
        level.placeTraps();
        return true;
    }

    private static boolean reserve(LinkedHashSet<Room> core, ArrayList<Room> leaves,
                                   RegularLevel level, ArrayList<Room.Type> roles, int index) {
        if (index == roles.size()) return true;
        Room.Type type = roles.get(index);
        boolean nearEntrance = type == Room.Type.SHOP;
        ArrayList<Room> choices = new ArrayList<>();
        for (Room candidate : core) {
            if (candidate == level.roomEntrance || candidate == level.roomExit) continue;
            if (nearEntrance && !candidate.neigbours.contains(level.roomEntrance)) continue;
            LinkedHashSet<Room> remaining = new LinkedHashSet<>(core);
            remaining.remove(candidate);
            if (!connectedWithLoop(remaining)) continue;
            boolean accessible = true;
            for (Room leaf : leaves) {
                if (leaf.type == Room.Type.SHOP) continue;
                boolean adjacent = false;
                for (Room other : remaining) if (leaf.neigbours.contains(other)) adjacent = true;
                if (!adjacent) accessible = false;
            }
            if (accessible) choices.add(candidate);
        }
        while (!choices.isEmpty()) {
            Room chosen = choices.remove(Random.Int(choices.size()));
            core.remove(chosen);
            chosen.type = type;
            leaves.add(chosen);
            if (reserve(core, leaves, level, roles, index+1)) return true;
            leaves.remove(chosen);
            chosen.type = Room.Type.STANDARD;
            core.add(chosen);
        }
        return false;
    }

    private static boolean connectedWithLoop(HashSet<Room> core) {
        HashSet<Room> seen = new HashSet<>();
        ArrayList<Room> queue = new ArrayList<>();
        queue.add(core.iterator().next());
        seen.add(queue.get(0));
        int edges = 0;
        for (int i = 0; i < queue.size(); i++) for (Room next : queue.get(i).neigbours) {
            if (!core.contains(next)) continue;
            edges++;
            if (seen.add(next)) queue.add(next);
        }
        return seen.size() == core.size() && edges / 2 >= core.size();
    }

    static void assignSpecials(RegularLevel level) {
        ArrayList<Room.Type> choices = new ArrayList<>(Room.SPECIALS);
        Room.Type scheduled = RebornSideRooms.scheduled(Dungeon.depth);
        if (Dungeon.bossLevel(Dungeon.depth+1)) choices.remove(Room.Type.WEAK_FLOOR);
        for (Room room : level.rooms) if (room.type == Room.Type.NULL) {
            if (Level.pitRoomNeeded) {
                room.type = Room.Type.PIT;
                Level.pitRoomNeeded = false;

                choices.remove(Room.Type.ARMORY);
                choices.remove(Room.Type.CRYPT);
                choices.remove(Room.Type.LABORATORY);
                choices.remove(Room.Type.LIBRARY);
                choices.remove(Room.Type.STATUE);
                choices.remove(Room.Type.TREASURY);
                choices.remove(Room.Type.VAULT);
                choices.remove(Room.Type.WEAK_FLOOR);
            } else if (Dungeon.depth % 5 == 2 && choices.contains(Room.Type.LABORATORY)) {
                room.type = Room.Type.LABORATORY;
            } else if (scheduled != null) {
                room.type = scheduled;
                scheduled = null;
            } else room.type = choices.get(Math.min(Random.Int(choices.size()), Random.Int(choices.size())));
            if (room.type == Room.Type.WEAK_FLOOR) Level.weakFloorCreated = true;
            Room.useType(room.type);
            choices.remove(room.type);
        }
    }
}
