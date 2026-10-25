package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;

public final class RebornSideRooms {
    private RebornSideRooms() {}

    public static int floor(Room.Type type) {
        int seed = Dungeon.rebornEncounterSeed;
        switch (type) {
            case BARBECUE: return 2 + Math.floorMod(seed / 7, 3);
            case ECHO: return 6 + Math.floorMod(seed / 23, 4);
            case OFFERING: return 11 + Math.floorMod(seed / 97, 4);
            case LAST_WATCH: return 16 + Math.floorMod(seed / 389, 4);
            default: return -1;
        }
    }

    public static Room.Type scheduled(int depth) {
        if (Dungeon.rules().hasThemedDungeons())
            for (Room.Type type : new Room.Type[]{Room.Type.BARBECUE, Room.Type.ECHO, Room.Type.OFFERING, Room.Type.LAST_WATCH})
                if (floor(type) == depth) return type;
        return null;
    }

    public static String leave() {
        return Messages.get(RebornSideRooms.class, "leave");
    }

    public static boolean isSideRoom(Room.Type type) {
        return floor(type) >= 0;
    }

    public static void add(Mob mob) {
        if (Game.scene() instanceof GameScene) GameScene.add(mob);
        else Dungeon.level.mobs.add(mob);
    }

    public static void remove(Mob mob) {
        mob.HP = 0;
        Actor.remove(mob);
        Actor.freeCell(mob.pos);
        Dungeon.level.mobs.remove(mob);
    }
}
