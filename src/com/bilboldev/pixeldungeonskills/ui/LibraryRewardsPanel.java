package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Badges.Badge;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryCreatureStats;
import com.bilboldev.pixeldungeonskills.LibraryRewards;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.LibraryRewards.Drop;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.effects.BadgeBanner;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.SkillSprite;
import com.bilboldev.pixeldungeonskills.windows.WndBadge;
import com.bilboldev.pixeldungeonskills.windows.WndTitledMessage;
import java.util.ArrayList;
import java.util.List;

public final class LibraryRewardsPanel extends Component {
    private final IconRow badges, skills, drops;
    private final boolean paired;

    public LibraryRewardsPanel(Entry entry, float width) {
        this(entry, width, 5);
    }

    public LibraryRewardsPanel(Entry entry, float width, int depth) {
        this.width = width;
        List<Badge> related = LibraryRewards.badges(entry);
        LibraryCreatureStats creature = entry.creatureStats(depth);
        RebornChampion.Skill[] abilities = creature == null ? new RebornChampion.Skill[0] : creature.championSkills();
        paired = Window.landscapeLayout() && !related.isEmpty() && abilities.length > 0;
        float columnWidth = paired ? (width - 8) / 2 : width;
        badges = new IconRow("badges", columnWidth);
        for (Badge badge : related) {
            badges.icon(BadgeBanner.image(badge.image), () -> Game.scene().add(new WndBadge(badge)));
        }
        add(badges);
        skills = new IconRow("champion_skills", columnWidth);
        for (RebornChampion.Skill ability : abilities) {
            Skill skill = LibraryChampionSkills.heroSkill(ability);
            skills.icon(new SkillSprite(skill.image), () -> Game.scene().add(new WndTitledMessage(
                    new SkillSprite(skill.image), Messages.capitalize(Messages.maybeTranslate(skill.name)),
                    LibraryChampionSkills.description(ability))));
        }
        add(skills);
        drops = new IconRow("drops", width);
        for (Drop drop : LibraryRewards.drops(entry, depth)) {
            boolean known = drop.itemKey == null || Lore.dropped(entry, depth, drop);
            Image image = drop.image();
            if (!known) image.hardlight(0x000000);
            drops.icon(image, () -> {
                Image icon = drop.image();
                if (!known) icon.hardlight(0x000000);
                Game.scene().add(new WndTitledMessage(icon, known ? drop.name() : "???",
                        known ? drop.info() : LibraryCatalog.text("unknown_drop")));
            });
        }
        add(drops);
        layout();
    }

    @Override
    protected void layout() {
        badges.setPos(x, y);
        skills.setPos(paired ? badges.right() + 8 : x,
                paired || badges.cells.isEmpty() || skills.cells.isEmpty() ? y : badges.bottom() + 8);
        float bottom = Math.max(badges.bottom(), skills.bottom());
        drops.setPos(x, bottom == y || drops.cells.isEmpty() ? bottom : bottom + 8);
        height = Math.max(bottom, drops.bottom()) - y;
    }

    public void onClick(float x, float y) {
        badges.onClick(x, y);
        skills.onClick(x, y);
        drops.onClick(x, y);
    }

    private static final class IconRow extends Component {
        private final HighlightedText title;
        private final ArrayList<IconCell> cells = new ArrayList<>();

        IconRow(String key, float width) {
            this.width = width;
            title = new HighlightedText(8);
            title.setColor(0xECD095, 0xECD095);
            title.text(LibraryCatalog.text(key), (int)width - 8, false);
            add(title);
        }

        void icon(Image image, Runnable action) {
            IconCell cell = new IconCell(image, action);
            cells.add(cell);
            add(cell);
        }

        @Override
        protected void layout() {
            visible = !cells.isEmpty();
            title.setPos(x + 4, y + 4);
            int columns = Math.max(1, ((int)width - 8) / 24);
            for (int i = 0; i < cells.size(); i++) {
                cells.get(i).setRect(x + 4 + i % columns * 24, title.bottom() + 4 + i / columns * 24, 24, 24);
            }
            height = cells.isEmpty() ? 0 : cells.get(cells.size() - 1).bottom() - y + 4;
        }

        void onClick(float x, float y) {
            for (IconCell cell : cells) {
                if (cell.inside(x, y)) {
                    cell.action.run();
                    return;
                }
            }
        }
    }

    private static final class IconCell extends Component {
        private final Image image;
        private final Runnable action;

        IconCell(Image image, Runnable action) {
            this.image = image;
            this.action = action;
            image.scale.set(20 / Math.max(image.width, image.height));
            add(image);
        }

        @Override
        protected void layout() {
            image.x = x + (width - image.width()) / 2;
            image.y = y + (height - image.height()) / 2;
        }
    }
}
