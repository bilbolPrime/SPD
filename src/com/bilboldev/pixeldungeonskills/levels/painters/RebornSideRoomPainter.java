package com.bilboldev.pixeldungeonskills.levels.painters;

import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.*;
import com.bilboldev.pixeldungeonskills.items.food.RoyalDinner;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.levels.*;

public class RebornSideRoomPainter extends Painter {
    public static void paint(Level level, Room room) {
        fill(level, room, Terrain.WALL);
        fill(level, room, 1, Terrain.EMPTY_SP);
        for (Room.Door door : room.connected.values()) door.set(Room.Door.Type.REGULAR);
        int x = (room.left + room.right) / 2, y = (room.top + room.bottom) / 2;
        int center = x + y * Level.WIDTH;
        switch (room.type) {
            case ECHO:

                set(level, room.left+1, room.top+1, Terrain.WATER);
                set(level, room.right-1, room.top+1, Terrain.WATER);
                EchoReflection echo = new EchoReflection();
                echo.configure(room, center);
                level.mobs.add(echo);
                break;
            case BARBECUE:
                BarbecueKing king = new BarbecueKing();
                king.pos = center-1;
                king.grill = center+1;
                set(level, center, Terrain.EMBERS);
                set(level, king.grill, Terrain.INACTIVE_TRAP);
                set(level, x+1, y-1, Terrain.WATER);
                level.mobs.add(king);
                level.drop(new RoyalDinner(), king.grill);
                break;
            case OFFERING:
                set(level, center, Terrain.PEDESTAL);
                set(level, room.left+1, room.top+1, Terrain.STATUE_SP);
                set(level, room.right-1, room.top+1, Terrain.STATUE_SP);
                ForgottenOffering offering = new ForgottenOffering();
                offering.pos = center;
                level.mobs.add(offering);
                break;
            case LAST_WATCH:

                for (int xx = room.left+1; xx < room.right; xx++)
                    set(level, xx, room.top+2, xx == x ? Terrain.EMBERS : Terrain.WALL_DECO);
                set(level, room.left+1, room.bottom-1, Terrain.EMBERS);
                set(level, room.right-1, room.bottom-1, Terrain.INACTIVE_TRAP);
                WatchRemains remains = new WatchRemains();
                remains.pos = x + (room.bottom-2) * Level.WIDTH;
                level.mobs.add(remains);
                level.drop(new Arrow(8), remains.pos+1);
                break;
            default: throw new IllegalArgumentException("Unsupported side room " + room.type);
        }
        for (Room.Door door : room.connected.values()) {
            int step = door.x == room.left ? 1 : door.x == room.right ? -1
                    : door.y == room.top ? Level.WIDTH : -Level.WIDTH;
            for (int i = 1; i <= 2; i++) {
                int cell = door.x + door.y * Level.WIDTH + i * step;

                if ((Terrain.flags[level.map[cell]] & Terrain.PASSABLE) == 0)
                    set(level, cell, Terrain.EMPTY_SP);
            }
        }
    }
}
