package com.bilboldev.pixeldungeonskills.thetiles;

import android.graphics.Bitmap;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.noosa.Tilemap;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.RegularLevel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;

public final class WallShadows extends Tilemap {
    private static final int[] SHADE = new int[32 * 16 * 16];
    private final Level level;
    private final boolean[] preserved;

    static {
        for (int mask = 0; mask < 32; mask++) for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
            int alpha = 0;
            if (mask >= 16) {
                if ((mask & 1) != 0) alpha = Math.max(alpha, Math.max(0, 5 - y) * 11);
                if ((mask & 2) != 0) alpha = Math.max(alpha, Math.max(0, x - 12) * 10);
                if ((mask & 4) != 0) alpha = Math.max(alpha, Math.max(0, y - 13) * 8);
                if ((mask & 8) != 0) alpha = Math.max(alpha, Math.max(0, 3 - x) * 12);
            } else {
                if ((mask & 1) != 0) alpha += falloff(7 - y, 2);
                if ((mask & 2) != 0) alpha += falloff(x - 11, 2);
                if ((mask & 4) != 0) alpha += falloff(y - 12, 2);
                if ((mask & 8) != 0) alpha += falloff(5 - x, 2);
                alpha = Math.min(120, alpha);
            }
            SHADE[mask * 256 + y * 16 + x] = alpha;
        }
    }

    public WallShadows(Level level) {
        super(texture(), new TextureFilm(texture(), 16, 16));
        this.level = level;
        preserved = new boolean[level.map.length];
        if (level instanceof RegularLevel) for (int cell = 0; cell < preserved.length; cell++) {
            if (preserved[cell]) continue;
            Room room = ((RegularLevel)level).room(cell);
            if (room == null || room.type != Room.Type.ALTAR && room.type != Room.Type.BLACKSMITH) continue;
            for (int y = room.top; y <= room.bottom; y++) for (int x = room.left; x <= room.right; x++)
                preserved[x + y * level.width()] = true;
        }
        map(new int[level.map.length], Level.WIDTH);
    }

    private static int falloff(int distance, int strength) {
        return distance <= 0 ? 0 : distance * distance * strength;
    }

    public boolean preserved(int cell) { return preserved[cell]; }

    private static SmartTexture texture() {
        if (!TextureCache.contains(WallShadows.class)) {
            int[] pixels = new int[32 * 16 * 16];
            for (int mask = 0; mask < 32; mask++) for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++)
                pixels[y * 512 + mask * 16 + x] = SHADE[mask * 256 + y * 16 + x] << 24;
            Bitmap bitmap = Bitmap.createBitmap(512, 16, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, 512, 0, 0, 512, 16);
            TextureCache.add(WallShadows.class, new SmartTexture(bitmap));
        }
        return TextureCache.get(WallShadows.class);
    }

    @Override public synchronized void updateMap() {
        for (int cell = 0; cell < data.length; cell++) data[cell] = mask(cell);
        super.updateMap();
    }

    @Override public synchronized void updateMapCell(int cell) {
        if (cell < 0 || cell >= data.length) return;
        int x = cell % mapWidth, y = cell / mapWidth;
        for (int row = Math.max(0, y - 1); row <= Math.min(mapHeight - 1, y + 1); row++) {
            for (int col = Math.max(0, x - 1); col <= Math.min(mapWidth - 1, x + 1); col++) {
                int pos = row * mapWidth + col;
                data[pos] = mask(pos);
                super.updateMapCell(pos);
            }
        }
    }

    private int mask(int cell) {
        int tile = level.map[cell];
        if (wall(cell) || (Terrain.flags[tile] & Terrain.PIT) != 0 || tile == Terrain.ALCHEMY) return 0;
        int mask = 0, x = cell % mapWidth;
        if (cell >= mapWidth && wall(cell - mapWidth)) mask |= 1;
        if (x + 1 < mapWidth && wall(cell + 1)) mask |= 2;
        if (cell + mapWidth < data.length && wall(cell + mapWidth)) mask |= 4;
        if (x > 0 && wall(cell - 1)) mask |= 8;
        return preserved[cell] ? mask | 16 : mask;
    }

    private boolean wall(int cell) {
        int tile = level.map[cell];
        return tile == Terrain.WALL || tile == Terrain.WALL_DECO || tile == Terrain.SECRET_DOOR
                || tile == Terrain.BOOKSHELF || tile == Terrain.COLLAPSED_CAVE;
    }

    public float opacity(int cell, int x, int y) { return SHADE[data[cell] * 256 + y * 16 + x] / 255f; }
    @Override protected boolean needsRender(int pos) { return (data[pos] & 15) != 0; }
}
