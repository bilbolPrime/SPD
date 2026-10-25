package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.thetiles.FogOfWarNew;

final class PerspectiveFog {
    private Level level;
    private final int[] colors;
    private final boolean[] terrain;
    private boolean initialized;

    PerspectiveFog(Level level) {
        this.level = level;
        colors = new int[level.length()];
        terrain = new boolean[colors.length];
    }

    void rebind(Level level) {
        this.level = level;
    }

    boolean update(boolean[] visible, int brightness) {
        boolean changed = !initialized;
        initialized = true;
        for (int cell = 0; cell < colors.length; cell++) {
            int color = FogOfWarNew.color(visible[cell], level.visited[cell], level.mapped[cell], brightness);
            if (colors[cell] != color) {
                colors[cell] = color;
                changed = true;
            }
        }
        if (changed) {
            for (int y = 0; y < level.height(); y++) {
                for (int x = 0; x < level.width(); x++) {
                    boolean drawn = false;

                    for (int oy = -1; oy <= 1 && !drawn; oy++) {
                        for (int ox = -1; ox <= 1; ox++) {
                            if ((at(x + ox, y + oy) >>> 24) < 255) {
                                drawn = true;
                                break;
                            }
                        }
                    }
                    terrain[x + y * level.width()] = drawn;
                }
            }
        }
        return changed;
    }

    boolean hasTerrain(int cell) {
        return cell >= 0 && cell < terrain.length && terrain[cell];
    }

    int apply(int color, double x, double y) {
        int fog = sample(x, y);
        int alpha = fog >>> 24;
        if (alpha == 0) return color;
        int clear = 255 - alpha;

        int r = (((color >> 16) & 255) * clear + (fog & 255) * alpha) / 255;
        int g = (((color >> 8) & 255) * clear + ((fog >> 8) & 255) * alpha) / 255;
        int b = ((color & 255) * clear + ((fog >> 16) & 255) * alpha) / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    int sample(double x, double y) {

        double tx = x * 2 - 0.5, ty = y * 2 - 0.5;
        int ix = (int)Math.floor(tx), iy = (int)Math.floor(ty);
        int left = ix >> 1, right = (ix + 1) >> 1;
        int top = iy >> 1, bottom = (iy + 1) >> 1;
        int a = at(left, top), b = at(right, top);
        int c = at(left, bottom), d = at(right, bottom);
        if (a == b && a == c && a == d) return a;
        double fx = tx - ix, fy = ty - iy;
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            double upper = ((a >>> shift) & 255) * (1 - fx) + ((b >>> shift) & 255) * fx;
            double lower = ((c >>> shift) & 255) * (1 - fx) + ((d >>> shift) & 255) * fx;
            result |= (int)Math.round(upper * (1 - fy) + lower * fy) << shift;
        }
        return result;
    }

    private int at(int x, int y) {
        return x >= 0 && y >= 0 && x < level.width() && y < level.height()
                ? colors[x + y * level.width()] : 0xFF000000;
    }
}
