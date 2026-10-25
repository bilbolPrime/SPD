package com.bilboldev.pixeldungeonskills.effects;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.Group;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.DeathBomb;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.ArrayList;
import java.util.Arrays;

public class DeathBombs extends Group {
    private final ArrayList<Mark> marks = new ArrayList<>();
    private final int[] fuses = new int[Level.LENGTH];

    @Override
    public void update() {
        super.update();
        Arrays.fill(fuses, 0);
        for (DeathBomb bomb : Dungeon.level.deathBombs) {
            if (fuses[bomb.pos] == 0 || bomb.turns < fuses[bomb.pos]) fuses[bomb.pos] = bomb.turns;
        }
        int count = 0;
        for (int cell = 0; cell < fuses.length; cell++) {
            if (fuses[cell] == 0 || !Dungeon.visible[cell]) continue;
            if (count == marks.size()) {
                Mark mark = new Mark();
                marks.add(mark);
                add(mark);
            }
            Mark mark = marks.get(count++);
            mark.visible = true;
            mark.bomb.place(cell);
            if (mark.zoom != Camera.main.zoom) {
                mark.zoom = Camera.main.zoom;
                PixelScene.chooseFont(9, mark.zoom);
                mark.number.font(PixelScene.font);
                mark.number.scale.set(PixelScene.scale);
                mark.number.measure();
            }
            if (mark.turns != fuses[cell]) {
                mark.turns = fuses[cell];
                mark.number.text(Integer.toString(mark.turns));
                mark.number.measure();
            }
            mark.number.x = mark.bomb.x + (mark.bomb.width() - mark.number.width()) / 2;
            mark.number.y = mark.bomb.y - mark.number.height();
        }
        for (int i = count; i < marks.size(); i++) marks.get(i).visible = false;
    }

    private static class Mark extends Group {
        int turns;
        float zoom;
        final ItemSprite bomb = new ItemSprite(ItemSpriteSheet.BOMB, null);
        final BitmapText number = new BitmapText(PixelScene.font);

        Mark() {
            add(bomb);
            add(number);
            number.hardlight(0xFFAA44);
        }
    }
}
