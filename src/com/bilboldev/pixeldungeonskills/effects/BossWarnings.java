package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import com.bilboldev.utils.RectF;
import java.util.ArrayList;
import java.util.Arrays;

public class BossWarnings extends Group {
    private final RectF attentionFrame = Icons.get(Icons.ALERT).frame();
    private final ArrayList<Mark> pool = new ArrayList<>();
    private final Mark[] cells = new Mark[Level.LENGTH];
    private int count;
    private double time;
    @Override
    public void update() {
        super.update();
        time = (time + Game.elapsed) % 360;
    }

    public int count() {
        return count;
    }

    public Mark at(int index) {
        return pool.get(index);
    }

    public Mark atCell(int cell) {
        return cells[cell];
    }

    public void refresh() {
        count = 0;
        Arrays.fill(cells, null);
        for (Mob mob : Dungeon.level.mobs) if (mob.isAlive()) {
            if (mob instanceof RebornBoss) {
                RebornBoss boss = (RebornBoss)mob;
                for (int cell : boss.residueCells()) if (!Level.water[cell]) decorate(cell, boss.pos, boss.residueKind(), true, 1);
                if (boss.showsWarning() && !(boss instanceof RivalBoss && ((RivalBoss)boss).meleeWarning())) {
                    int kind = boss.warningKind();
                    int[] warning = boss.warningCells();
                    int from = boss.warningOrigin();
                    float reach = 1;
                    if (kind == RebornBoss.DRILL || kind == RebornBoss.ORDER)
                        for (int cell : warning) reach = Math.max(reach, Level.distance(from, cell));
                    for (int cell : warning) decorate(cell, from, kind, false, reach);
                }
                if (boss.opening()) opening(boss.pos);
            } else if (mob instanceof RebornTengu && ((RebornTengu)mob).opening()) opening(mob.pos);
        }
        for (int i = count; i < pool.size(); i++) pool.get(i).visible = false;
    }

    private boolean seen(int cell) {
        return RebornBoss.valid(cell) && Dungeon.visible[cell] && !Level.solid[cell];
    }

    private void opening(int cell) {
        if (!seen(cell)) return;
        float pulse = (float)Math.sin(time * 5);
        mark(cell, Assets.EFFECTS, 0, 0, 16, 16, 14 + pulse, 14 + pulse, 0, 0xFCE3A0, 0.55f, 0, 0);
    }

    private void decorate(int cell, int from, int kind, boolean residue, float reach) {
        if (!seen(cell)) return;
        float distance = Level.distance(from, cell);
        double beat = time * 4 - distance * 0.5;
        float pulse = (float)Math.sin(beat), drift = (float)Math.cos(beat);
        float direction = (float)Math.toDegrees(Math.atan2(cell / Level.WIDTH - from / Level.WIDTH,
                cell % Level.WIDTH - from % Level.WIDTH));
        float opacity = residue ? 0.65f : 0.48f + pulse * 0.08f;
        switch (kind) {
            case RebornBoss.SLIME:
            case RebornBoss.ROT:

                int green = kind == RebornBoss.SLIME ? 0xA9D878 : 0x91C58B;
                float ring = residue ? 13 : 11 + 2 * pulse;
                mark(cell, Assets.EFFECTS, 0, 0, 16, 16, ring, ring * 0.8f, 0, green, opacity, 0, 1);
                item(cell, ItemSpriteSheet.DEWDROP, residue ? 7 : 6, 0, green, opacity + 0.1f,
                        residue ? 0 : drift, residue ? 0 : -1 - pulse);
                break;
            case RebornBoss.DRILL:
            case RebornBoss.ORDER:

                float size = 7 + 6 * Math.min(1, distance / reach);
                float sway = 0.3f * drift;
                Mark attention = next(cell);
                attention.texture(Assets.ICONS);
                attention.frame(attentionFrame);
                attention.place(cell, size, size, 12 * pulse, 0xFFFFFF, 0.72f + pulse * 0.08f,
                        -(float)Math.sin(Math.toRadians(direction)) * sway,
                        (float)Math.cos(Math.toRadians(direction)) * sway);
                break;
            case RebornBoss.STEAM:
                speck(cell, Speck.STEAM, 10 + pulse, 0, 0xF3D9A2, opacity, 0, -pulse);
                speck(cell, Speck.STEAM, 5, 15, 0xFFFFFF, opacity * 0.65f, 2 * drift, -3);
                break;
            case RebornBoss.EMBER:
                speck(cell, Speck.LIGHT, residue ? 12 : 10 + pulse, 0, 0xEF793D, opacity, 0, 1);
                speck(cell, Speck.STAR, 6, (float)(time * 40), 0xFFD389, opacity + 0.05f, drift, -2 - pulse);
                break;
            case RebornBoss.RIFT:

                mark(cell, Assets.EFFECTS, 16, 8, 16, 8, 13, 7 + pulse, direction + 12 * pulse,
                        0xD0A4F5, opacity + 0.12f, 0, 0);
                mark(cell, Assets.EFFECTS, 16, 0, 16, 8, 10, 5, direction - 12 * pulse,
                        0xE2C9FF, opacity * 0.8f, 0, 0);
                break;
        }
    }

    private void item(int cell, int index, float size, float angle, int color, float alpha, float dx, float dy) {

        Mark m = next(cell);
        m.texture(Assets.ITEMS);
        int columns = m.texture.width / 16;
        m.frame(index % columns * 16, index / columns * 16, 16, 16);
        m.place(cell, size, size, angle, color, alpha, dx, dy);
    }

    private void speck(int cell, int index, float size, float angle, int color, float alpha, float dx, float dy) {
        Mark m = next(cell);
        m.texture(Assets.SPECKS);
        int columns = m.texture.width / 7;
        m.frame(index % columns * 7, index / columns * 7, 7, 7);
        m.place(cell, size, size, angle, color, alpha, dx, dy);
    }
    private void mark(int cell, String texture, int x, int y, int w, int h, float sx, float sy,
                      float angle, int color, float alpha, float dx, float dy) {
        Mark m = next(cell);
        m.texture(texture);
        m.frame(x, y, w, h);
        m.place(cell, sx, sy, angle, color, alpha, dx, dy);
    }

    private Mark next(int cell) {
        if (count == pool.size()) {
            Mark m = new Mark();
            pool.add(m);
            add(m);
        }
        Mark m = pool.get(count++);
        m.next = null;

        if (cells[cell] == null) cells[cell] = m;
        else {
            Mark last = cells[cell];
            while (last.next != null) last = last.next;
            last.next = m;
        }
        return m;
    }

    public static class Mark extends Image {
        public Mark next;
        public int cell;
        public float centerX, centerY, sizeX, sizeY, cosine, sine;
        private void place(int cell, float sx, float sy, float rotation, int color, float opacity, float dx, float dy) {
            this.cell = cell;
            sizeX = sx;
            sizeY = sy;
            origin.set(width / 2, height / 2);
            centerX = (cell % Level.WIDTH + 0.5f) * 16 + dx;
            centerY = (cell / Level.WIDTH + 0.5f) * 16 + dy;
            x = centerX - origin.x;
            y = centerY - origin.y;
            scale.set(sx / width, sy / height);
            angle = rotation;
            cosine = (float)Math.cos(Math.toRadians(rotation));
            sine = (float)Math.sin(Math.toRadians(rotation));
            hardlight(color);
            alpha(opacity);
            visible = true;
        }
    }
}
