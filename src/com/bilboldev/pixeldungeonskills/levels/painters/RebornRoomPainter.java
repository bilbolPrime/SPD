package com.bilboldev.pixeldungeonskills.levels.painters;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.levels.*;
import com.bilboldev.utils.Random;
import java.util.Arrays;
import java.util.PriorityQueue;

public final class RebornRoomPainter extends Painter {
    private RebornRoomPainter() {}

    public static void paint(Level level, Room room, int style) {
        fill(level, room, Terrain.WALL);
        fill(level, room, 1, Terrain.EMPTY);
        for (Room.Door door : room.connected.values()) door.set(Room.Door.Type.REGULAR);
        Shape s = new Shape(level, room);
        if (level instanceof SewerLevel) sewers(s, style);
        else if (level instanceof PrisonLevel) prison(s, style);
        else if (level instanceof CavesLevel) caves(s, style);
        else if (level instanceof CityLevel) city(s, style);
        else halls(s, style);
        reconnect(level, room);
    }

    private static void sewers(Shape s, int style) {
        int x = s.w/2, y = s.h/2;
        switch (style) {
            case 0:
                s.box(1, 0, 1, s.h, Terrain.WATER);
                s.box(s.w-2, 0, 1, s.h, Terrain.WATER);
                s.box(0, y, s.w, 2, Terrain.EMPTY_DECO);
                s.set(0, 0, Terrain.WALL);
                break;
            case 1:
                s.ellipse(1, 1, s.w-2, s.h-2, Terrain.WATER);
                s.box(x-1, y-1, 2, 2, Terrain.EMPTY_DECO);
                s.set(s.w-1, s.h-1, Terrain.WALL);
                break;
            case 2:
                s.box(0, 1, s.w, 1, Terrain.WATER);
                s.box(1, y, Math.max(1, s.w-3), 1, Terrain.WALL);
                s.set(x, y, Terrain.EMPTY);
                s.scatter(Terrain.GRASS, 0.15f);
                break;
            case 3:
                s.box(0, 0, 2, Math.max(2, s.h-2), Terrain.WATER);
                s.box(s.w-2, 2, 2, s.h-2, Terrain.WATER);
                s.box(0, s.h-2, 2, 2, Terrain.HIGH_GRASS);
                s.scatter(Terrain.GRASS, 0.18f);
                break;
            case 4:
                s.box(x, 0, 1, y+1, Terrain.WATER);
                s.box(x, y, s.w-x, 1, Terrain.WATER);
                s.box(0, 0, Math.max(1, x-1), 1, Terrain.WALL);
                s.box(s.w-1, y+2, 1, s.h-y-2, Terrain.WALL);
                break;
            default:
                s.set(1, 1, Terrain.WALL);
                s.set(s.w-2, s.h-2, Terrain.WALL);
                s.box(0, s.h-1, s.w-1, 1, Terrain.WATER);
                s.box(s.w-1, 0, 1, s.h-1, Terrain.WATER);
                s.scatter(Terrain.EMPTY_DECO, 0.2f);
        }
    }

    private static void prison(Shape s, int style) {
        int x = s.w/2, y = s.h/2;
        switch (style) {
            case 0:
                for (int i = 1;i<s.h-1;i += 3) {
                    s.box(0, i, 2, 1, Terrain.WALL);
                    s.box(s.w-2, i+1, 2, 1, Terrain.WALL);
                }
                s.box(x, 0, 1, s.h, Terrain.EMPTY_DECO);
                break;
            case 1:
                s.box(1, y, Math.max(1, s.w-2), 1, Terrain.WALL);
                s.set(1, y, Terrain.EMPTY);
                s.set(s.w-2, y, Terrain.EMPTY);
                s.box(0, 0, 2, 1, Terrain.WATER);
                break;
            case 2:
                s.box(x-1, 0, 1, Math.max(1, y-1), Terrain.WALL);
                s.box(x+1, y+1, 1, s.h-y-1, Terrain.WALL);
                s.scatter(Terrain.EMPTY_DECO, 0.2f);
                break;
            case 3:
                s.corners(1, Terrain.WALL);
                s.set(1, y, Terrain.WALL);
                s.set(s.w-2, y, Terrain.WALL);
                s.box(x, 0, 2, 1, Terrain.WATER);
                break;
            case 4:
                s.box(0, 1, 1, Math.max(1, s.h-3), Terrain.BOOKSHELF);
                s.box(s.w-1, 2, 1, Math.max(1, s.h-3), Terrain.WALL);
                s.box(0, s.h-1, 2, 1, Terrain.WATER);
                break;
            default:
                s.box(0, y, Math.max(1, x-1), 1, Terrain.WALL);
                s.box(s.w-1, 0, 1, y, Terrain.WALL);
                s.box(0, s.h-1, x, 1, Terrain.EMPTY_DECO);
                s.set(s.w-2, s.h-1, Terrain.WATER);
        }
    }

    private static void caves(Shape s, int style) {
        int x = s.w/2, y = s.h/2;
        s.corners(Math.min(2, Math.min(s.w, s.h)/3), Terrain.WALL);
        switch (style) {
            case 0:
                for (int i = 0;i<s.h;i++) s.set(Math.max(0, Math.min(s.w-1, x+(i%3)-1)), i, Terrain.WATER);
                s.box(0, y, 2, 1, Terrain.WALL);
                break;
            case 1:
                s.box(1, 1, Math.max(1, s.w/4), 1, Terrain.WALL);
                s.box(s.w-2, y, 1, Math.max(1, s.h/3), Terrain.WALL);
                s.scatter(Terrain.GRASS, 0.22f);
                break;
            case 2:
                s.box(x, 0, 1, s.h, Terrain.WALL);
                s.box(x, 1, 1, 2, Terrain.EMPTY);
                s.box(x, s.h-3, 1, 2, Terrain.EMPTY);
                s.box(0, s.h-2, 2, 1, Terrain.WATER);
                break;
            case 3:
                s.box(0, 1, 2, Math.max(1, s.h-2), Terrain.HIGH_GRASS);
                s.set(1, y, Terrain.WALL);
                s.set(s.w-2, 1, Terrain.WALL);
                s.scatter(Terrain.GRASS, 0.18f);
                break;
            case 4:
                s.box(0, 0, 1, Math.max(1, y), Terrain.CHASM);
                s.box(s.w-1, y, 1, s.h-y, Terrain.CHASM);
                s.set(1, s.h-2, Terrain.WALL);
                s.box(x, 0, 1, 2, Terrain.WATER);
                break;
            default:
                s.ellipse(0, 1, s.w-1, s.h-2, Terrain.WATER);
                s.box(s.w-2, 0, 2, Math.max(1, y), Terrain.WALL);
                s.box(x-1, y, 2, 2, Terrain.EMPTY_DECO);
        }
    }

    private static void city(Shape s, int style) {
        int x = s.w/2, y = s.h/2;
        switch (style) {
            case 0:
                s.box(1, 1, s.w-2, s.h-2, Terrain.WATER);
                s.box(x, 0, 2, s.h, Terrain.EMPTY_SP);
                s.box(0, y, s.w, 1, Terrain.EMPTY_SP);
                break;
            case 1:
                for (int i = 1;i<s.h-1;i += 3) {
                    s.set(1, i, Terrain.WALL);
                    s.set(s.w-2, i+1, Terrain.WALL);
                }
                s.box(x, 0, 1, s.h, Terrain.EMPTY_SP);
                break;
            case 2:
                s.box(1, 1, Math.max(1, x-1), 1, Terrain.WALL);
                s.box(x-1, 1, 1, Math.max(1, y-1), Terrain.WALL);
                s.box(s.w-2, s.h-2, 2, 2, Terrain.GRASS);
                break;
            case 3:
                s.box(0, 0, 1, s.h-1, Terrain.BOOKSHELF);
                s.box(s.w-1, 1, 1, s.h-1, Terrain.BOOKSHELF);
                s.box(1, y, Math.max(1, s.w-2), 1, Terrain.EMPTY_SP);
                break;
            case 4:
                s.box(0, 1, 2, 1, Terrain.WALL);
                s.box(s.w-2, s.h-2, 2, 1, Terrain.WALL);
                s.box(x, 0, 1, 1, Terrain.STATUE);
                s.scatter(Terrain.EMPTY_DECO, 0.22f);
                break;
            default:
                s.corners(1, Terrain.WALL);
                s.box(0, y, 1, 2, Terrain.HIGH_GRASS);
                s.box(s.w-1, 1, 1, 2, Terrain.HIGH_GRASS);
                s.box(1, y, s.w-2, 1, Terrain.EMPTY_SP);
                s.set(s.w-2, s.h-2, Terrain.STATUE);
        }
    }

    private static void halls(Shape s, int style) {
        int x = s.w/2, y = s.h/2;
        switch (style) {
            case 0:
                s.corners(Math.min(2, Math.min(s.w, s.h)/3), Terrain.CHASM);
                s.box(0, y, 1, 2, Terrain.WALL);
                s.box(s.w-1, 1, 1, 2, Terrain.WALL);
                break;
            case 1:
                for (int i = 1;i<s.h-1;i += 3) {
                    s.set(1, i, Terrain.WALL);
                    s.set(s.w-2, i+1, Terrain.STATUE);
                }
                s.scatter(Terrain.EMPTY_DECO, 0.16f);
                break;
            case 2:
                s.box(0, 1, s.w, 1, Terrain.WATER);
                s.box(0, s.h-2, s.w, 1, Terrain.WATER);
                s.set(1, 1, Terrain.WALL);
                s.set(s.w-2, s.h-2, Terrain.WALL);
                break;
            case 3:
                s.box(1, 1, s.w-2, 1, Terrain.WALL);
                s.box(1, s.h-2, s.w-2, 1, Terrain.WALL);
                s.set(x, 1, Terrain.EMPTY);
                s.set(x-1, s.h-2, Terrain.EMPTY);
                s.box(x-1, y, 2, 1, Terrain.EMPTY_DECO);
                break;
            case 4:
                for (int i = 0;i<s.h;i += 3) s.box(0, i, 1+(i%2), 1, Terrain.WALL);
                s.box(s.w-1, y, 1, s.h-y, Terrain.HIGH_GRASS);
                s.box(1, s.h-1, Math.max(1, x-1), 1, Terrain.WATER);
                break;
            default:
                s.corners(1, Terrain.WALL);
                s.box(x-1, y-1, 1, 2, Terrain.WALL);
                s.box(0, 0, 1, y, Terrain.WATER);
                s.box(s.w-1, y, 1, s.h-y, Terrain.CHASM);
                s.scatter(Terrain.GRASS, 0.10f);
        }
    }

    public static void reconnect(Level level, Room room) {
        int cx = (room.left+room.right)/2, cy = (room.top+room.bottom)/2;

        int best = Integer.MAX_VALUE, hubX = cx, hubY = cy;
        for (int y = room.top+1;y<room.bottom-1;y++) for (int x = room.left+1;x<room.right-1;x++) {
            int cost = Math.abs(x-cx)+Math.abs(y-cy);
            for (int dx = 0;dx<2;dx++) for (int dy = 0;dy<2;dy++)
                if (!RebornDungeonBuilder.walkable(level, x+dx+(y+dy)*Level.WIDTH)) cost += 20;
            if (cost<best) {
                best = cost;
                hubX = x;
                hubY = y;
            }
        }
        cx = hubX;
        cy = hubY;
        for (int x = cx;x<=cx+1;x++) for (int y = cy;y<=cy+1;y++) clear(level, room, x, y);
        for (int door : room.rebornDoors) {
            int x = door%Level.WIDTH, y = door/Level.WIDTH;
            boolean horizontal = x==room.left || x==room.right;
            if (horizontal) x += Integer.compare(cx, x);
            else y += Integer.compare(cy, y);
            clear(level, room, x, y);
            if (horizontal) clear(level, room, x, y+(y<=cy?1:-1));
            else clear(level, room, x+(x<=cx?1:-1), y);
            connect(level, room, x+y*Level.WIDTH, cx+cy*Level.WIDTH);
        }
        boolean[] seen = new boolean[Level.LENGTH];
        int[] queue = new int[Level.LENGTH];
        int start = cx+cy*Level.WIDTH, tail = 1;
        queue[0] = start;
        seen[start] = true;
        for (int head = 0;head<tail;head++) for (int n:Level.NEIGHBOURS4) {
            int cell = queue[head]+n;
            if (RebornDungeonBuilder.validStep(queue[head], cell) && room.inside(cell) && !seen[cell]
                    && RebornDungeonBuilder.walkable(level, cell)) {
                        seen[cell] = true;
                        queue[tail++] = cell;
                    }
        }
        for (int y = room.top+1;y<room.bottom;y++) for (int x = room.left+1;x<room.right;x++) {
            int cell = x+y*Level.WIDTH;
            if (!seen[cell] && RebornDungeonBuilder.walkable(level, cell)) level.map[cell] = Terrain.WALL;
        }
    }

    private static void connect(Level level, Room room, int from, int to) {
        int[] costs = new int[Level.LENGTH], previous = new int[Level.LENGTH];
        Arrays.fill(costs, Integer.MAX_VALUE);
        costs[from] = 0;
        previous[from] = from;
        PriorityQueue<int[]> open = new PriorityQueue<>(11, (a, b)->Integer.compare(a[1], b[1]));
        open.add(new int[]{from, 0});
        while (!open.isEmpty()) {
            int[] entry = open.remove();
            int cell = entry[0];
            if (entry[1]!=costs[cell])continue;
            if (cell==to)break;
            for (int offset:Level.NEIGHBOURS4) {
                int next = cell+offset;
                if (!RebornDungeonBuilder.validStep(cell, next) || !room.inside(next))continue;
                int cost = entry[1]+(RebornDungeonBuilder.walkable(level, next)?1:12);
                if (cost<costs[next]) {costs[next] = cost;previous[next] = cell;open.add(new int[]{next, cost});}
            }
        }
        for (int cell = to;cell!=from;cell = previous[cell]) clear(level, room, cell%Level.WIDTH, cell/Level.WIDTH);
    }

    private static void clear(Level level, Room room, int x, int y) {
        int cell = x+y*Level.WIDTH;
        if (!room.inside(cell)) return;
        if (!RebornDungeonBuilder.walkable(level, cell)) level.map[cell] = Terrain.EMPTY;
        else if (level.map[cell]==Terrain.HIGH_GRASS) level.map[cell] = Terrain.GRASS;
    }

    private static final class Shape {
        final Level level;
        final Room room;
        final boolean transpose, flipX, flipY;
        final int w, h;
        Shape(Level level, Room room) {
            this.level = level;
            this.room = room;
            transpose = Random.Int(2)==0;
            flipX = Random.Int(2)==0;
            flipY = Random.Int(2)==0;
            w = (transpose?room.height():room.width())-1;
            h = (transpose?room.width():room.height())-1;
        }
        int cell(int x, int y) {
            if (flipX) x = w-1-x;
            if (flipY) y = h-1-y;
            return room.left+1+(transpose?y:x) + (room.top+1+(transpose?x:y))*Level.WIDTH;
        }
        void set(int x, int y, int tile) {
            if (x<0 || y<0 || x>=w || y>=h) return;
            if (tile==Terrain.CHASM && Dungeon.bossLevel(Dungeon.depth+1)) tile = Terrain.WATER;
            level.map[cell(x, y)] = tile;
        }
        void box(int x, int y, int width, int height, int tile) {
            for (int j = y;j<y+height;j++) for (int i = x;i<x+width;i++) set(i, j, tile);
        }
        void corners(int size, int tile) {
            for (int x = 0;x<size;x++) for (int y = 0;y<size-x;y++) {
                set(x, y, tile);
                set(w-1-x, y, tile);
                set(x, h-1-y, tile);
                set(w-1-x, h-1-y, tile);
            }
        }
        void ellipse(int x, int y, int width, int height, int tile) {
            for (int j = 0;j<height;j++) for (int i = 0;i<width;i++) {
                float dx = (2*i+1-width)/(float)Math.max(1, width), dy = (2*j+1-height)/(float)Math.max(1, height);
                if (dx*dx+dy*dy<=1) set(x+i, y+j, tile);
            }
        }
        void scatter(int tile, float chance) {
            for (int y = 0;y<h;y++) for (int x = 0;x<w;x++)
                if (level.map[cell(x, y)]==Terrain.EMPTY && Random.Float()<chance) set(x, y, tile);
        }
    }
}
