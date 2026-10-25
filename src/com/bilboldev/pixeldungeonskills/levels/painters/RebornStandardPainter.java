package com.bilboldev.pixeldungeonskills.levels.painters;

import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.utils.Random;

public class RebornStandardPainter extends Painter {
    public static void paint(Level level, Room room) {
        fill(level, room, Terrain.WALL);
        fill(level, room, 1, Terrain.EMPTY);
        for (Room.Door door : room.connected.values()) door.set(Room.Door.Type.REGULAR);
        switch (Random.Int(3)) {
            case 0:
                if (room.width() >= 7 && room.height() >= 7) {
                    set(level, room.left+3, room.top+3, Terrain.WALL);
                    set(level, room.right-3, room.bottom-3, Terrain.WALL);
                }
                break;
            case 1:
                for (int x = room.left+2; x <= room.right-2; x++) {
                    set(level, x, room.top+2, Terrain.HIGH_GRASS);
                    set(level, x, room.bottom-2, Terrain.HIGH_GRASS);
                }
                break;
            default: break;
        }
    }
}
