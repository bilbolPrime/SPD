package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild.Slot;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild.Stats;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.LibraryStats;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.function.IntConsumer;

public final class WndLibraryHero extends WndLibrary {
    private static final int GAP = 4;
    private final LibraryHeroBuild build;
    private final Runnable onClose;
    private Component heading;
    private ControlRow content;
    private ScrollPane pane;
    private boolean closed;

    public WndLibraryHero(LibraryHeroBuild build, Runnable onClose) {
        this.build = build;
        this.onClose = onClose;
        rebuild();
    }

    private void rebuild() {
        float offset = pane == null ? 0 : pane.content().camera.scroll.y;
        if (pane != null) {
            remove(heading);
            heading.destroy();
            remove(pane);
            pane.destroy();
        }
        int width = boundedWidth(landscapeLayout() ? 392 : 144);
        int bodyWidth = textWidth(width);
        boolean wide = bodyWidth >= 320;
        int controlsWidth = wide ? bodyWidth * 43 / 100 : bodyWidth;
        int rowGap = wide ? 2 : GAP;
        float buttonHeight = Math.max(24, 48 * Game.density / camera.zoom);
        Component portrait = InspectionTitle.create(LibraryCatalog.heroImage(build.heroClass.spritesheet(), build.armorTier()),
                wide ? "" : LibraryCatalog.text("hero_equipment"));
        portrait.setRect(0, 0, width, 0);
        heading = new Component();
        heading.add(portrait);
        heading.setSize(width, portrait.height());
        content = new ControlRow();

        float top = 0;
        if (wide) {
            HighlightedText title = label(LibraryCatalog.text("hero_equipment"), width - 52);
            title.setColor(titleColor(), titleColor());
            title.setPos(52, 0);
            heading.add(title);
            float counterWidth = (width - 52 - GAP) / 2f;
            float counterTop = title.bottom() + GAP;
            float bottom = counter(heading, "hero_level", build.level, 1, LibraryHeroBuild.MAX_LEVEL,
                    value -> build.level = value, 52, counterTop, counterWidth, buttonHeight);
            bottom = Math.max(bottom, counter(heading, "hero_strength", build.strength, 1, LibraryHeroBuild.MAX_STRENGTH,
                    value -> build.strength = value, 52 + counterWidth + GAP, counterTop, counterWidth, buttonHeight));
            heading.setSize(width, Math.max(heading.height(), bottom + GAP));
        } else {
            top = counter(content, "hero_level", build.level, 1, LibraryHeroBuild.MAX_LEVEL,
                    value -> build.level = value, 0, top, controlsWidth, buttonHeight) + rowGap;
            top = counter(content, "hero_strength", build.strength, 1, LibraryHeroBuild.MAX_STRENGTH,
                    value -> build.strength = value, 0, top, controlsWidth, buttonHeight) + rowGap;
        }

        for (Slot slot : Slot.values()) {
            LibraryItemBuild item = build.get(slot);
            String value = item == null ? LibraryCatalog.text("none") : item.name();
            BuildButton select = new BuildButton(value, () -> chooseEquipment(slot));
            int emptyIcon = slot == Slot.WEAPON ? ItemSpriteSheet.SWORD
                    : slot == Slot.ARMOR ? ItemSpriteSheet.ARMOR_CLOTH : ItemSpriteSheet.RING_RUBY;
            select.icon(item == null ? new ItemSprite(emptyIcon, null) : item.image());
            float editWidth = Math.min(56, controlsWidth * .36f);
            select.setRect(0, top, controlsWidth - editWidth - GAP, buttonHeight);
            BuildButton edit = new BuildButton(LibraryCatalog.text("customize"), () -> {
                parent.add(new WndLibraryItem(build.get(slot), this::rebuild));
            });
            edit.enable(item != null);
            edit.setRect(controlsWidth - editWidth, top, editWidth, buttonHeight);
            float rowHeight = Math.max(buttonHeight, Math.max(select.reqHeight(), edit.reqHeight()));
            select.setSize(select.width(), rowHeight);
            edit.setSize(editWidth, rowHeight);
            content.add(select);
            content.add(edit);
            top += rowHeight + rowGap;
        }

        Stats stats = build.calculate();
        int statsWidth = wide ? bodyWidth - controlsWidth - 12 : bodyWidth;
        LibraryStats summary = LibraryStats.hero(statsWidth, stats);
        summary.setPos(wide ? controlsWidth + 12 : 0, wide ? 0 : top + GAP);
        content.add(summary);
        float bottom = summary.bottom();
        if (stats.firearm) {
            HighlightedText projectiles = label(LibraryCatalog.text("hero_projectiles", stats.projectiles), statsWidth);
            projectiles.setPos(summary.left(), summary.bottom() + GAP);
            content.add(projectiles);
            bottom = projectiles.bottom();
        }
        content.setSize(bodyWidth, Math.max(top - rowGap, bottom));
        layoutLibrary(width, heading, content, GAP);
        pane.scrollTo(0, offset);
    }

    private float counter(Component parent, String key, int value, int minimum, int maximum, IntConsumer change,
                          float left, float top, float width, float buttonHeight) {
        HighlightedText label = label(LibraryCatalog.text(key, value), width - 2 * (buttonHeight + GAP));
        float height = Math.max(buttonHeight, label.height() + GAP);
        BuildButton minus = new BuildButton("-", () -> {
            change.accept(value - 1);
            rebuild();
        }, parent == content);
        minus.enable(value > minimum);
        minus.setRect(left, top, buttonHeight, height);
        parent.add(minus);
        BuildButton plus = new BuildButton("+", () -> {
            change.accept(value + 1);
            rebuild();
        }, parent == content);
        plus.enable(value < maximum);
        plus.setRect(left + width - buttonHeight, top, buttonHeight, height);
        parent.add(plus);
        label.setPos(left + (width - label.width()) / 2, top + (height - label.height()) / 2);
        parent.add(label);
        return top + height;
    }

    private void chooseEquipment(Slot slot) {
        parent.add(new WndLibraryItemOptions(build, slot, this::rebuild));
    }

    private static HighlightedText label(String text, float width) {
        HighlightedText label = new HighlightedText(8);
        label.setColor(0xDDD4C5, 0xECD095);
        label.text(text, (int)width, false);
        return label;
    }

    @Override
    protected ScrollPane createScrollPane(Component body) {
        pane = content.scrollPane(body);
        return pane;
    }

    @Override
    public void hide() {
        if (closed) return;
        closed = true;
        super.hide();
        if (onClose != null) onClose.run();
    }

    private final class BuildButton extends ActionButton {
        private final Runnable action;

        BuildButton(String label, Runnable action) {
            this(label, action, true);
        }

        BuildButton(String label, Runnable action, boolean scrolls) {
            super(label);
            this.action = action;
            if (scrolls) content.addControl(hotArea);
        }

        @Override
        protected void layout() {
            super.layout();
            if (icon == null) return;
            ((BitmapTextMultiline)text).maxWidth = Math.max(1, (int)width - 24);
            text.measure();
            text.x = x + 22 + (width - 24 - text.width()) / 2;
            text.y = y + (height - text.height()) / 2;
            icon.x = x + 3;
            icon.y = y + (height - icon.height()) / 2;
        }

        @Override
        protected void onClick() {
            action.run();
        }
    }
}
