package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild.Property;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;

public final class WndLibraryItem extends WndLibrary {
    private static final int GAP = 4;

    private final LibraryItemBuild build;
    private final Runnable onClose;
    private Component heading;
    private ControlRow content;
    private ScrollPane pane;
    private boolean closed;

    public WndLibraryItem(LibraryItemBuild build, Runnable onClose) {
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

        int width = boundedWidth(landscapeLayout() ? 360 : 144);
        int bodyWidth = textWidth(width);
        float buttonHeight = Math.max(24, 48 * Game.density / camera.zoom);
        heading = InspectionTitle.create(build.image(), build.name());
        heading.setRect(0, 0, width, 0);
        content = new ControlRow();
        float top = 0;

        if (build.canUpgrade()) {
            HighlightedText level = label(LibraryCatalog.text("upgrade_level", build.level()),
                    bodyWidth - 2 * (buttonHeight + GAP));
            float rowHeight = Math.max(buttonHeight, level.height() + GAP);
            ItemButton minus = new ItemButton("-", () -> {
                build.level(build.level() - 1);
                rebuild();
            });
            minus.enable(build.level() > build.minimumLevel());
            minus.setRect(0, top, buttonHeight, rowHeight);
            content.add(minus);
            ItemButton plus = new ItemButton("+", () -> {
                build.level(build.level() + 1);
                rebuild();
            });
            plus.enable(build.level() < build.maximumLevel());
            plus.setRect(bodyWidth - buttonHeight, top, buttonHeight, rowHeight);
            content.add(plus);
            level.setPos((bodyWidth - level.width()) / 2, top + (rowHeight - level.height()) / 2);
            content.add(level);
            top += rowHeight + GAP;
        }

        if (build.properties().contains(Property.BASE_ARMOR)) {
            ItemButton armor = new ItemButton(Property.BASE_ARMOR.title() + ": "
                    + build.options(Property.BASE_ARMOR)[build.choice(Property.BASE_ARMOR)],
                    () -> choose(Property.BASE_ARMOR));
            armor.setRect(0, top, bodyWidth, buttonHeight);
            armor.setSize(bodyWidth, Math.max(buttonHeight, armor.reqHeight()));
            content.add(armor);
            top = armor.bottom() + GAP;
        }

        int count = build.properties().size() + (build.canCurse() ? 1 : 0)
                - (build.properties().contains(Property.BASE_ARMOR) ? 1 : 0);
        float cellWidth = (bodyWidth - GAP * (count - 1)) / count;
        float rowHeight = Math.max(32, buttonHeight);
        int column = 0;
        for (Property property : build.properties()) {
            if (property == Property.BASE_ARMOR) continue;
            String value = build.options(property)[build.choice(property)];
            ModifierButton button = new ModifierButton(value, () -> {
                if (property == Property.BALANCE) {
                    build.choose(property, (build.choice(property) + 1) % build.options(property).length);
                    rebuild();
                } else {
                    choose(property);
                }
            });
            button.setRect(column++ * (cellWidth + GAP), top, cellWidth, rowHeight);
            content.add(button);
        }
        if (build.canCurse()) {
            ModifierButton curse = new ModifierButton(LibraryCatalog.text(build.cursed() ? "cursed" : "pure"), () -> {
                build.cursed(!build.cursed());
                rebuild();
            });
            curse.setRect(column * (cellWidth + GAP), top, cellWidth, rowHeight);
            content.add(curse);
        }
        top += rowHeight + GAP;

        HighlightedText info = label(build.info(), bodyWidth - 8);
        info.setPos(4, top + GAP + 4);
        content.add(info);
        content.setSize(bodyWidth, info.bottom() + 4);
        layoutLibrary(width, heading, content, GAP);
        pane.scrollTo(0, offset);
    }

    private void choose(Property property) {
        parent.add(new WndLibraryItemOptions(build, property, this::rebuild));
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

    private final class ItemButton extends ActionButton {
        private final Runnable action;

        ItemButton(String label, Runnable action) {
            super(label);
            this.action = action;
            content.addControl(hotArea);
        }

        @Override
        protected void onClick() {
            action.run();
        }
    }

    private final class ModifierButton extends RedButton {
        private final Runnable action;
        private final float fontScale;

        ModifierButton(String label, Runnable action) {
            super(label);
            this.action = action;
            fontScale = text.scale.x;
            content.addControl(hotArea);
        }

        @Override
        protected void createChildren() {
            super.createChildren();
            remove(text);
            text.destroy();
            text = PixelScene.createMultiline(8);
            ((BitmapTextMultiline)text).maxWidth = Integer.MAX_VALUE;
            add(text);
        }

        @Override
        protected void layout() {
            text.scale.set(fontScale);
            text.measure();
            text.scale.set(fontScale * Math.min(1, (width - 4) / Math.max(1, text.width())));
            super.layout();
            text.y = y + (height - text.height()) / 2;
        }

        @Override
        protected void onClick() {
            action.run();
        }
    }
}
