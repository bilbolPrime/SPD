package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.windows.WndTitledMessage;
import java.util.ArrayList;

public final class LibraryChampionSkills extends Component {
    private static final int SIZE = 32;
    private static final int GAP = 4;
    private final ArrayList<Cell> cells = new ArrayList<>();

    public LibraryChampionSkills(float width, RebornChampion champion) {
        this.width = width;
        for (RebornChampion.Skill skill : champion.learnedSkills()) {
            Cell cell = new Cell(skill, champion.rank(skill));
            cells.add(cell);
            add(cell);
        }
        layout();
    }

    @Override
    protected void layout() {
        int columns = Math.max(1, ((int)width + GAP) / (SIZE + GAP));
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).setRect(x + i % columns * (SIZE + GAP), y + i / columns * (SIZE + GAP), SIZE, SIZE);
        }
        height = cells.isEmpty() ? 0 : cells.get(cells.size() - 1).bottom() - y;
    }

    public void onClick(float x, float y) {
        for (Cell cell : cells) {
            if (!cell.inside(x, y)) continue;
            Game.scene().add(new WndTitledMessage(new SkillSprite(cell.skill.icon), cell.skill.title(),
                    Messages.get("skills4.rank_row", cell.rank, cell.skill.description(cell.rank))));
            return;
        }
    }

    private static final class Cell extends Component {
        final RebornChampion.Skill skill;
        final int rank;
        final ColorBlock well;
        final SkillSprite icon;
        final BitmapText level;

        Cell(RebornChampion.Skill skill, int rank) {
            this.skill = skill;
            this.rank = rank;
            add(well = RebornSlot.create(SIZE, SIZE, 0xFF46404D));
            add(icon = new SkillSprite(skill.icon));
            icon.scale.set(1.5f);
            add(level = PixelScene.createText(Integer.toString(rank), 6));
            level.measure();
            level.hardlight(0xECD095);
        }

        @Override
        protected void layout() {
            well.x = x;
            well.y = y;
            icon.x = PixelScene.align(x + (width - icon.width()) / 2);
            icon.y = PixelScene.align(y + (height - icon.height()) / 2);
            level.x = PixelScene.align(right() - level.width() - 3);
            level.y = PixelScene.align(y + 2);
        }
    }
}
