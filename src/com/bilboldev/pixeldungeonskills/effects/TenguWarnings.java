package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornTengu;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.ArrayList;
import java.util.Arrays;

public class TenguWarnings extends Group {
    public static final float OPACITY = 0.5f;
    private static final float MIN_SIZE = 8f, MAX_SIZE = 16f;
    private final ArrayList<WarningShuriken> shurikens = new ArrayList<>();
    private final WarningShuriken[] cells = new WarningShuriken[Level.LENGTH];
    private int count;
    private double phase;

    @Override
    public void update() {
        super.update();
        phase = (phase + Game.elapsed * 4) % (Math.PI * 2);
    }

    public void refresh() {
        count = 0;
        Arrays.fill(cells, null);
        for (Mob mob : Dungeon.level.mobs) {
            if (!(mob instanceof RebornTengu) || !mob.isAlive()) continue;
            RebornTengu boss = (RebornTengu)mob;
            int[] warning = boss.warningCells();
            if (warning.length == 0) continue;
            int from = boss.warningOrigin(), end = warning[warning.length - 1];
            if (from < 0 || from >= Level.LENGTH || end < 0 || end >= Level.LENGTH) continue;
            float dx = end % Level.WIDTH - from % Level.WIDTH;
            float dy = end / Level.WIDTH - from / Level.WIDTH;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) continue;
            for (int cell : warning) {
                if (cell < 0 || cell >= Level.LENGTH || Level.solid[cell]) break;

                if (!Dungeon.visible[cell]) continue;
                if (count == shurikens.size()) {
                    WarningShuriken shuriken = new WarningShuriken();
                    shurikens.add(shuriken);
                    add(shuriken);
                }
                WarningShuriken shuriken = shurikens.get(count++);
                shuriken.place(from, cell, dx / distance, dy / distance, distance, phase);
                cells[cell] = shuriken;
            }
        }
        for (int i = count; i < shurikens.size(); i++) shurikens.get(i).visible = false;
    }

    public int count() {
        return count;
    }

    public WarningShuriken at(int index) {
        return shurikens.get(index);
    }

    public WarningShuriken atCell(int cell) {
        return cells[cell];
    }

    public static final class WarningShuriken extends Image {
        public int cell;
        public float centerX, centerY, size, cosine, sine;

        private WarningShuriken() {
            super(Assets.ITEMS);
            int columns = texture.width / ItemSprite.SIZE;
            frame(ItemSpriteSheet.SHURIKEN % columns * ItemSprite.SIZE,
                    ItemSpriteSheet.SHURIKEN / columns * ItemSprite.SIZE, ItemSprite.SIZE, ItemSprite.SIZE);
            origin.set(width / 2, height / 2);
            alpha(OPACITY);
        }

        private void place(int from, int cell, float directionX, float directionY, float fullDistance, double phase) {
            this.cell = cell;
            float dx = cell % Level.WIDTH - from % Level.WIDTH;
            float dy = cell / Level.WIDTH - from / Level.WIDTH;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            size = MIN_SIZE + (MAX_SIZE - MIN_SIZE) * Math.min(1, distance / fullDistance);

            float sway = 0.6f * (float)Math.sin(phase + distance * 0.65);
            centerX = (cell % Level.WIDTH + 0.5f) * 16 - directionY * sway;
            centerY = (cell / Level.WIDTH + 0.5f) * 16 + directionX * sway;
            x = centerX - origin.x;
            y = centerY - origin.y;
            angle = 12f * (float)Math.sin(phase + distance * 0.45);
            cosine = (float)Math.cos(Math.toRadians(angle));
            sine = (float)Math.sin(Math.toRadians(angle));
            scale.set(size / width);
            visible = true;
        }
    }
}
