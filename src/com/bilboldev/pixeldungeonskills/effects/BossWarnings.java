package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.DailySealLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyOutpostLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyHoardLevel;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.Window;
import java.util.ArrayList;
import java.util.Arrays;

public class BossWarnings extends Group {
    private static final int ICE = 8, ARCANE = 9;
    private final SmartTexture stroke = TextureCache.createSolid(0xFFFFFFFF);
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
        if (Dungeon.level instanceof com.bilboldev.pixeldungeonskills.levels.TutorialLevel) {
            for (int cell : ((com.bilboldev.pixeldungeonskills.levels.TutorialLevel)Dungeon.level).warningCells())
                decorate(cell, cell, RebornBoss.ORDER, false, 1);
            if (Window.topWindow() == null) tutorialGuide(TutorialRun.guideCell());
        }
        if (Dungeon.level instanceof DailySealLevel) {
            DailySealLevel level = (DailySealLevel)Dungeon.level;
            for (int site = 0; site < level.siteCells.length; site++) {
                int cell = level.siteCells[site];
                if (!level.siteClosed(site)) ritual(cell);
            }
            for (int cell : level.warningCells())
                decorate(cell, cell, RebornBoss.ORDER, false, 1);
        }
        if (Dungeon.level instanceof DailyOutpostLevel) {
            for (int cell : ((DailyOutpostLevel)Dungeon.level).warningCells())
                decorate(cell, cell, RebornBoss.ORDER, false, 1);
        }
        if (Dungeon.level instanceof DailyHoardLevel) {
            for (int cell : ((DailyHoardLevel)Dungeon.level).warningCells())
                decorate(cell, cell, RebornBoss.ORDER, false, 1);
        }
        for (Mob mob : Dungeon.level.mobs) if (mob.isAlive()) {
            if (mob instanceof RebornBoss) {
                RebornBoss boss = (RebornBoss)mob;
                for (int cell : boss.residueCells()) if (!Level.water[cell]) decorate(cell, boss.pos, boss.residueKind(), true, 1);
                if (boss.showsWarning() && !boss.meleeWarning()) {
                    int kind = boss instanceof RivalBoss ? rivalKind((RivalBoss)boss) : boss.warningKind();
                    int[] warning = boss.warningCells();
                    int from = boss.warningOrigin();
                    float reach = 1;
                    for (int cell : warning) reach = Math.max(reach, Level.distance(from, cell));
                    for (int cell : warning) decorate(cell, from, kind, false, reach);
                }
                if (boss.opening()) opening(boss.pos);
            } else if (mob instanceof IceMob && !((IceMob)mob).meleeWarning()) {
                int[] warning = ((IceMob)mob).warningCells();
                float reach = 1;
                for (int cell : warning) reach = Math.max(reach, Level.distance(mob.pos, cell));
                for (int cell : warning) decorate(cell, mob.pos, ICE, false, reach);
            } else if (mob instanceof DailyDemon) {
                for (int cell : ((DailyDemon)mob).warningCells()) decorate(cell, mob.pos, RebornBoss.ORDER, false, 6);
            } else if (mob instanceof RebornTengu && ((RebornTengu)mob).opening()) opening(mob.pos);
        }
        for (int i = count; i < pool.size(); i++) pool.get(i).visible = false;
    }

    private boolean seen(int cell) {
        return RebornBoss.valid(cell) && Dungeon.visible[cell] && !Level.solid[cell];
    }

    private void tutorialGuide(int cell) {
        if (!seen(cell)) return;
        float opacity = .2f + .55f * (.5f - .5f * (float)Math.cos(time * Math.PI));
        for (int edge = 0; edge < 4; edge++) {
            boolean horizontal = edge < 2;
            float offset = edge % 2 == 0 ? -7 : 7;
            Mark mark = next(cell);
            mark.texture(stroke);
            mark.frame(0, 0, 1, 1);
            mark.place(cell, horizontal ? 14 : 1, horizontal ? 1 : 14, 0, 0xE6C57E,
                    opacity, horizontal ? 0 : offset, horizontal ? offset : 0);
        }
    }

    private int rivalKind(RivalBoss rival) {
        switch (rival.heroClass()) {
            case MAGE:
                return ARCANE;
            case NECROMANCER:
            case ROGUE:
                return RebornBoss.ORDER;
            case HUNTRESS:
                return rival.warningAbility() == 3 ? RebornBoss.EMBER : RebornBoss.DRILL;
            default:
                return RebornBoss.DRILL;
        }
    }

    private void opening(int cell) {
        if (!seen(cell)) return;
        float pulse = (float)Math.sin(time * 5);
        mark(cell, Assets.EFFECTS, 0, 0, 16, 16, 14 + pulse, 14 + pulse, 0, 0xFCE3A0, 0.55f, 0, 0);
    }

    private void ritual(int cell) {
        if (!seen(cell)) return;
        float pulse = (float)Math.sin(time * 3);
        speck(cell, Speck.LIGHT, 17 + pulse, 0, 0xBD86DF, 0.48f + pulse * 0.08f, 0, 0);
        speck(cell, Speck.STAR, 7, (float)(time * 15), 0xF0D5FF, 0.65f, 0, -1);
    }

    private void decorate(int cell, int from, int kind, boolean residue, float reach) {
        if (!seen(cell)) return;
        float distance = Level.distance(from, cell);
        double beat = time * 4 - distance * 0.5;
        float pulse = (float)Math.sin(beat), drift = (float)Math.cos(beat);
        float direction = (float)Math.toDegrees(Math.atan2(cell / Level.WIDTH - from / Level.WIDTH,
                cell % Level.WIDTH - from % Level.WIDTH));
        float opacity = residue ? 0.65f : 0.48f + pulse * 0.08f;
        float growth = from == cell ? 1 : Math.min(1, distance / reach);
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
                charge(cell, direction, distance, growth);
                break;
            case RebornBoss.ORDER:
                float circle = 8 + 7 * growth;
                sigil(cell, circle, 0xD8B2EE, 0.65f);
                speck(cell, Speck.STEAM, 4 + 3 * growth, (float)(time * 25), 0x75618F,
                        0.6f, -drift, pulse);
                speck(cell, Speck.STEAM, 3 + 2 * growth, (float)(-time * 35), 0xDFC6FA,
                        opacity, 2 * drift, -1 - pulse);
                break;
            case ICE:
                sigil(cell, 8 + 7 * growth, 0xA4D6E3, 0.6f);
                mark(cell, Assets.ICE_EFFECTS, 22, 5, 4, 6, 3 + 3 * growth, 5 + 4 * growth,
                        direction + 45 + 8 * pulse, 0xDBF6FF, 0.7f, 0, 0);
                speck(cell, Speck.STEAM, 4 + 2 * growth, 0, 0xC0DDEB, 0.35f,
                        drift, -1 - pulse);
                break;
            case ARCANE:
                line(cell, 6 + 8 * growth, direction, 0x8EABEB, 0.4f);
                mark(cell, Assets.EFFECTS, 16, 0, 16, 8, 6 + 8 * growth, 3 + 3 * growth,
                        direction + 8 * pulse, 0xC0CFFF, 0.65f, 0, 0);
                speck(cell, Speck.LIGHT, 3 + 3 * growth, direction, 0xE1D9FF,
                        0.55f + 0.15f * pulse, drift, 0);
                break;
            case RebornBoss.STEAM:
                speck(cell, Speck.STEAM, 10 + pulse, 0, 0xF3D9A2, opacity, 0, -pulse);
                speck(cell, Speck.STEAM, 5, 15, 0xFFFFFF, opacity * 0.65f, 2 * drift, -3);
                break;
            case RebornBoss.EMBER:
                speck(cell, Speck.LIGHT, residue ? 12 : 7 + 4 * growth, 0, 0xEF793D, opacity, 0, 1);
                speck(cell, Speck.STAR, 3 + 3 * growth, (float)(time * 40), 0xFFD389, opacity + 0.05f, drift, -2 - pulse);
                break;
            case RebornBoss.RIFT:
                mark(cell, Assets.EFFECTS, 16, 8, 16, 8, 13, 7 + pulse, direction + 12 * pulse,
                        0xD0A4F5, opacity + 0.12f, 0, 0);
                mark(cell, Assets.EFFECTS, 16, 0, 16, 8, 10, 5, direction - 12 * pulse,
                        0xE2C9FF, opacity * 0.8f, 0, 0);
                break;
        }
    }

    private void charge(int cell, float direction, float distance, float growth) {
        float dx = (float)Math.cos(Math.toRadians(direction));
        float dy = (float)Math.sin(Math.toRadians(direction));
        double travel = time * 1.7 - distance * 0.17;
        float step = (float)(travel - Math.floor(travel));
        float along = (step - 0.5f) * 6;
        float flicker = (float)Math.sin(step * Math.PI);

        line(cell, 7 + 8 * growth, direction, 0xD5A761, 0.55f);
        speck(cell, Speck.STEAM, 4 + 3 * growth, 0, 0xB7A080, 0.3f,
                -dx * 2 - dy, -dy * 2 + dx);
        speck(cell, Speck.STAR, 3 + 3 * growth, direction + 25 * flicker,
                0xFFD58A, 0.5f + 0.3f * flicker, dx * along, dy * along);
        speck(cell, Speck.LIGHT, 2 + 2 * growth, direction, 0xE8BD7C, 0.6f,
                -dx * along - dy * 2, -dy * along + dx * 2);
    }

    private void sigil(int cell, float size, int color, float alpha) {
        float offset = size * 11 / 32;
        mark(cell, Assets.EFFECTS, 0, 0, 16, 5, size, size * 5 / 16, 0, color, alpha, 0, -offset);
        mark(cell, Assets.EFFECTS, 0, 11, 16, 5, size, size * 5 / 16, 0, color, alpha, 0, offset);
    }

    private void line(int cell, float length, float angle, int color, float alpha) {
        Mark line = next(cell);
        line.texture(stroke);
        line.frame(0, 0, 1, 1);
        line.place(cell, length, 1, angle, color, alpha, 0, 0);
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
