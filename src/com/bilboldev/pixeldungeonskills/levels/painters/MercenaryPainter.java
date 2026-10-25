package com.bilboldev.pixeldungeonskills.levels.painters;

import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryCamp;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;

public class MercenaryPainter extends Painter {
    public static void paint(Level level, Room room) {
        fill(level, room, Terrain.WALL);
        fill(level, room, 1, Terrain.EMPTY_SP);
        for (Room.Door door : room.connected.values()) door.set(Room.Door.Type.UNLOCKED);
        MercenaryCamp merc = new MercenaryCamp();
        merc.chooseOffer();
        merc.pos = (room.left + room.right)/2 + ((room.top + room.bottom)/2)*Level.WIDTH;
        level.mobs.add(merc);
    }
}
