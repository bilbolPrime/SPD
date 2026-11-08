package com.bilboldev.pixeldungeonskills.scenes;

import android.opengl.GLES20;
import com.bilboldev.gltextures.BufferTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfHealth;
import com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfAwareness;
import com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfTransmutation;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.RegularLevel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.thetiles.WallShadows;
import javax.microedition.khronos.opengles.GL10;

final class DungeonLighting extends Image {
    private static final int SAMPLES = 2;
    private static final int ALPHA = 64;
    private static final float BASE = 1 - ALPHA / 255f;
    private Level level;
    private final WallShadows shadows;
    private final BufferTexture lightmap;
    private final float[] red, green, blue;
    private final int[] previous;
    private final int[] roomColor;
    private final int columns, rows;
    private final Glow[] spells = new Glow[12];
    private final boolean torches;
    private float clock, delay;
    private boolean upload = true;
    private boolean enabled;
    private int revision;

    DungeonLighting(Level level, WallShadows shadows) {
        this.level = level;
        this.shadows = shadows;
        columns = level.width() * SAMPLES;
        rows = level.height() * SAMPLES;
        lightmap = new BufferTexture(columns, rows);
        TextureCache.add(lightmap, lightmap);
        texture(lightmap);
        scale.set(16f / SAMPLES);
        red = new float[columns * rows];
        green = new float[red.length];
        blue = new float[red.length];
        previous = new int[red.length];
        roomColor = new int[level.map.length];
        String tiles = level.tilesTex();
        torches = tiles.equals(Assets.TILES_PRISON) || tiles.equals(Assets.TILES_PRISON_3D);
        boolean ice = tiles.equals(Assets.TILES_ICE_CAVES) || tiles.equals(Assets.TILES_ICE_CAVES_3D);
        for (int cell = 0; cell < roomColor.length; cell++) {
            int color = ice ? 0xDEF0FC : 0xF0F2F7;
            Room room = level instanceof RegularLevel ? ((RegularLevel)level).room(cell) : null;
            if (room != null) switch (room.type) {
                case LIBRARY: color = 0xF6F0E5; break;
                case GARDEN: color = 0xE9F4E8; break;
                case POOL: color = 0xE5F0F6; break;
                case CRYPT: color = 0xE6E8F0; break;
                default: break;
            }
            roomColor[cell] = color;
        }
        for (int i = 0; i < spells.length; i++) spells[i] = new Glow();
        enabled(Presentation.dungeonEffects());
    }

    void enabled(boolean value) {
        enabled = visible = value;
        revision++;
        for (Glow glow : spells) {
            glow.following = null;
            glow.left = 0;
        }
        if (enabled) refresh();
    }

    void follow(Emitter missile, int color) {
        if (!enabled) return;
        Glow glow = freeGlow(missile);
        glow.following = missile;
        glow.color = color;
        glow.left = .16f;
        glow.duration = .16f;
        glow.radius = 2.6f;
        glow.strength = .85f;
        delay = 0;
    }

    void burst(float x, float y, float radius, int color) {
        if (!enabled) return;
        Glow glow = freeGlow(null);
        glow.following = null;
        glow.x = x / 16;
        glow.y = y / 16;
        glow.color = color;
        glow.left = glow.duration = .38f;
        glow.radius = radius;
        glow.strength = 1.4f;
        delay = 0;
    }

    private Glow freeGlow(Emitter following) {
        Glow oldest = spells[0];
        if (following != null) for (Glow glow : spells) if (glow.following == following) return glow;
        for (Glow glow : spells) {
            if (glow.left <= 0) return glow;
            if (glow.left < oldest.left) oldest = glow;
        }
        return oldest;
    }

    void rebind(Level level) {
        this.level = level;
        delay = 0;
    }

    @Override public void update() {
        if (!enabled) return;
        clock += Game.elapsed;
        for (Glow glow : spells) {
            if (glow.left <= 0) continue;
            if (glow.following != null && glow.following.exists && glow.following.on) {
                glow.x = (glow.following.x + glow.following.width / 2) / 16;
                glow.y = (glow.following.y + glow.following.height / 2) / 16 + (Difficulties.is3d ? .4f : 0);
            } else {
                glow.following = null;
                glow.left = Math.max(0, glow.left - Game.elapsed);
            }
        }
        if ((delay -= Game.elapsed) <= 0) {
            delay = 1f / 30;
            refresh();
        }
    }

    void refresh() {

        for (int cell = 0; cell < level.map.length; cell++) {
            boolean lit = Dungeon.visible[cell] && !shadows.preserved(cell);
            float r = lit ? (roomColor[cell] >> 16 & 255) / 255f : 1;
            float g = lit ? (roomColor[cell] >> 8 & 255) / 255f : 1;
            float b = lit ? (roomColor[cell] & 255) / 255f : 1;
            int start = cell / level.width() * SAMPLES * columns + cell % level.width() * SAMPLES;
            for (int y = 0; y < SAMPLES; y++) for (int x = 0; x < SAMPLES; x++) {
                int p = start + y * columns + x;
                red[p] = r;
                green[p] = g;
                blue[p] = b;
            }
        }
        for (Glow glow : spells) if (glow.left > 0) {
            float fade = glow.following == null ? glow.left / glow.duration : 1;
            illuminate(glow.x, glow.y, glow.radius, glow.color, glow.strength * fade * fade);
        }
        Fire fire = (Fire)level.blobs.get(Fire.class);
        WaterOfHealth health = (WaterOfHealth)level.blobs.get(WaterOfHealth.class);
        WaterOfAwareness awareness = (WaterOfAwareness)level.blobs.get(WaterOfAwareness.class);
        WaterOfTransmutation transmutation = (WaterOfTransmutation)level.blobs.get(WaterOfTransmutation.class);
        int sources = 0;
        for (int cell = 0; cell < level.map.length && sources < 24; cell++) {
            if (!Dungeon.visible[cell] || shadows.preserved(cell)) continue;
            int tile = level.map[cell];
            int color = tile == Terrain.ALCHEMY ? 0xE3C99C
                    : tile != Terrain.WELL ? 0
                    : health != null && health.cur[cell] > 0 ? 0xE8BFA8
                    : awareness != null && awareness.cur[cell] > 0 ? 0xC7D9A6
                    : transmutation != null && transmutation.cur[cell] > 0 ? 0xC4ACED : 0;
            if (color != 0) {
                illuminate(cell % level.width() + .5f, cell / level.width() + .5f, 2.5f, color, .32f);
                sources++;
                continue;
            }
            boolean flame = fire != null && fire.cur[cell] > 0;
            if (!flame && !(torches && level.map[cell] == Terrain.WALL_DECO && level.wallIsExposed(cell))) continue;
            float flicker = .90f + .06f * (float)Math.sin(clock * 5.1f + cell)
                    + .04f * (float)Math.sin(clock * 8.3f + cell * .7f);
            illuminate(cell % level.width() + .5f, cell / level.width() + .5f,
                    flame ? 2.7f : 3.6f, 0xFFD09A, (flame ? .75f : .65f) * flicker);
            sources++;
        }
        boolean changed = false;
        for (int p = 0; p < red.length; p++) {
            int color = ALPHA << 24 | channel(blue[p]) << 16 | channel(green[p]) << 8 | channel(red[p]);
            if (previous[p] != color) changed = true;
            previous[p] = color;
        }
        if (changed) {
            lightmap.pixels.position(0);
            lightmap.pixels.put(previous);
            upload = true;
            revision++;
        }
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round((value - BASE) * 255)));
    }

    private void illuminate(float sx, float sy, float radius, int color, float strength) {
        int sourceX = (int)sx, sourceY = (int)sy;
        if (sourceX < 0 || sourceY < 0 || sourceX >= level.width() || sourceY >= level.height()
                || !Dungeon.visible[sourceX + sourceY * level.width()]) return;
        float r = (color >> 16 & 255) / 255f * strength;
        float g = (color >> 8 & 255) / 255f * strength;
        float b = (color & 255) / 255f * strength;
        for (int cy = Math.max(0, (int)(sy - radius)); cy <= Math.min(level.height() - 1, (int)(sy + radius)); cy++) {
            for (int cx = Math.max(0, (int)(sx - radius)); cx <= Math.min(level.width() - 1, (int)(sx + radius)); cx++) {
                int cell = cx + cy * level.width();
                if (!Dungeon.visible[cell] || shadows.preserved(cell) || !reaches(sourceX, sourceY, cx, cy)) continue;
                int start = cy * SAMPLES * columns + cx * SAMPLES;
                for (int y = 0; y < SAMPLES; y++) for (int x = 0; x < SAMPLES; x++) {
                    float dx = cx + (x + .5f) / SAMPLES - sx, dy = cy + (y + .5f) / SAMPLES - sy;
                    float falloff = Math.max(0, 1 - (dx * dx + dy * dy) / (radius * radius));
                    falloff *= falloff;
                    int p = start + y * columns + x;
                    red[p] += r * falloff;
                    green[p] += g * falloff;
                    blue[p] += b * falloff;
                }
            }
        }
    }

    private boolean reaches(int x, int y, int tx, int ty) {
        int dx = Math.abs(tx - x), dy = Math.abs(ty - y);
        int sx = x < tx ? 1 : -1, sy = y < ty ? 1 : -1, error = dx - dy;
        while (x != tx || y != ty) {
            int e = error * 2;
            boolean horizontal = e > -dy, vertical = e < dx;
            if (horizontal && vertical && blocked(x + sx, y) && blocked(x, y + sy)) return false;
            if (horizontal) { error -= dy; x += sx; }
            if (vertical) { error += dx; y += sy; }
            if (x == tx && y == ty) return true;
            if (blocked(x, y)) return false;
        }
        return true;
    }

    private boolean blocked(int x, int y) {
        return x < 0 || y < 0 || x >= level.width() || y >= level.height()
                || (Terrain.flags[level.map[x + y * level.width()]] & Terrain.LOS_BLOCKING) != 0;
    }

    int revision() { return revision; }

    int tint(int color, int cell, double wx, double wy) {
        if (!enabled || cell < 0 || cell >= level.map.length || !Dungeon.visible[cell] || shadows.preserved(cell)) return color;
        float x = (float)Math.max(0, Math.min(columns - 1, wx * SAMPLES - .5));
        float y = (float)Math.max(0, Math.min(rows - 1, wy * SAMPLES - .5));
        int left = (int)x, top = (int)y, right = Math.min(left + 1, columns - 1), bottom = Math.min(top + 1, rows - 1);
        int a = left + top * columns, b = right + top * columns, c = left + bottom * columns, d = right + bottom * columns;
        float u = x - left, v = y - top;
        int r = Math.min(255, (int)((color >> 16 & 255) * sample(red, a, b, c, d, u, v)));
        int g = Math.min(255, (int)((color >> 8 & 255) * sample(green, a, b, c, d, u, v)));
        int blueValue = Math.min(255, (int)((color & 255) * sample(blue, a, b, c, d, u, v)));
        return color & 0xFF000000 | r << 16 | g << 8 | blueValue;
    }

    private static float sample(float[] values, int a, int b, int c, int d, float x, float y) {
        float top = values[a] + (values[b] - values[a]) * x;
        float bottom = values[c] + (values[d] - values[c]) * x;
        return Math.min(BASE + 1, top + (bottom - top) * y);
    }

    @Override public void draw() {
        if (upload) {
            lightmap.update();
            upload = false;
        }
        GLES20.glBlendFunc(GL10.GL_DST_COLOR, GL10.GL_ONE_MINUS_SRC_ALPHA);
        super.draw();
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override public void destroy() {
        for (Glow glow : spells) glow.following = null;
        TextureCache.remove(lightmap);
        super.destroy();
    }

    private static final class Glow {
        Emitter following;
        float x, y, radius, strength, left, duration;
        int color;
    }
}
