package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild.Slot;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild.Property;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WndLibraryItemOptions extends WndLibrary {
    private static final int GAP = 4;
    private final ArrayList<Choice> choices = new ArrayList<>();
    private final Runnable onSelect;
    private final String unknownMessage;

    public WndLibraryItemOptions(LibraryItemBuild build, Property property, Runnable onSelect) {
        this.onSelect = onSelect;
        unknownMessage = property == Property.BASE_ARMOR ? "unknown_item" : "unknown_modifier";
        String[] names = build.options(property);
        List<Integer> known = build.knownOptions(property);
        for (int i = 0; i < names.length; i++) {
            final int index = i;
            boolean revealed = known.contains(i);
            Item item = revealed || property == Property.BASE_ARMOR ? build.optionItem(property, i) : build.item();
            choices.add(new Choice(item, names[i], revealed, () -> build.choose(property, index)));
        }
        layoutChoices(property.title());
    }

    public WndLibraryItemOptions(LibraryHeroBuild build, Slot slot, Runnable onSelect) {
        this.onSelect = onSelect;
        unknownMessage = "unknown_item";
        choices.add(new Choice(null, LibraryCatalog.text("none"), true, () -> build.set(slot, null)));
        ArrayList<Entry> entries = new ArrayList<>();
        for (Entry entry : LibraryCatalog.entries()) {
            if (entry.customizable() && build.accepts(slot, entry.newItem())) entries.add(entry);
        }
        LibraryCatalog.sort(entries);
        for (Entry entry : entries) {
            boolean known = Lore.known(entry);
            choices.add(new Choice(entry.newItem(), known ? entry.name() : "???", known,
                    () -> build.set(slot, new LibraryItemBuild(entry))));
        }
        layoutChoices(LibraryCatalog.text(slot == Slot.RING1 || slot == Slot.RING2
                ? "rings" : slot.name().toLowerCase(Locale.ROOT)));
    }

    private void layoutChoices(String title) {
        int width = boundedWidth(landscapeLayout() ? 260 : 144);
        int columns = landscapeLayout() ? 4 : 2;
        float cellWidth = (textWidth(width) - GAP * (columns - 1)) / (float)columns;
        HighlightedText heading = new HighlightedText(9);
        heading.setColor(titleColor(), titleColor());
        heading.text(title, width, false);
        Component content = new Component();
        float top = 0;
        for (int row = 0; row < choices.size(); row += columns) {
            int end = Math.min(row + columns, choices.size());
            float rowHeight = Math.max(56, 64 * Game.density / camera.zoom);
            for (int i = row; i < end; i++) rowHeight = Math.max(rowHeight, choices.get(i).requiredHeight(cellWidth));
            for (int i = row; i < end; i++) {
                Choice choice = choices.get(i);
                choice.setRect((i - row) * (cellWidth + GAP), top, cellWidth, rowHeight);
                content.add(choice);
            }
            top += rowHeight + GAP;
        }
        content.setSize(textWidth(width), top - GAP);
        layoutLibrary(width, heading, content, GAP);
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (Choice choice : choices) {
                    if (choice.inside(x, y)) {
                        Sample.INSTANCE.play(Assets.SND_CLICK);
                        if (!choice.known) {
                            ItemSprite image = new ItemSprite(choice.imageIndex, null);
                            image.hardlight(0x000000);
                            Game.scene().add(new WndTitledMessage(image, "???", LibraryCatalog.text(unknownMessage)));
                            return;
                        }
                        choice.select.run();
                        hide();
                        onSelect.run();
                        return;
                    }
                }
            }
        };
    }

    private static final class Choice extends Component {
        final boolean known;
        final int imageIndex;
        final Runnable select;
        private final NinePatch background;
        private final ItemSprite image;
        private final HighlightedText name;

        Choice(Item item, String label, boolean known, Runnable select) {
            this.known = known;
            this.select = select;
            imageIndex = item == null ? ItemSpriteSheet.SMTH : ItemSpriteSheet.identifiedImage(item.getClass(), item.image());
            image = new ItemSprite(imageIndex, item != null && known ? item.glowing() : null);
            if (!known) image.hardlight(0x000000);
            background = Chrome.get(Chrome.Type.BUTTON);
            add(background);
            add(image);
            name = new HighlightedText(8);
            name.setColor(0xDDD4C5, 0xECD095);
            name.text(known ? label : "???", 200, false);
            add(name);
        }

        float requiredHeight(float width) {
            name.text(name.text(), (int)width - GAP * 2, false);
            return name.height() + 32 + GAP * 3;
        }

        @Override
        protected void layout() {
            background.x = x;
            background.y = y;
            background.size(width, height);
            name.text(name.text(), (int)width - GAP * 2, false);
            name.setPos(x + (width - name.width()) / 2, bottom() - name.height() - GAP);
            float size = Math.min(width - GAP * 2, name.top() - y - GAP * 2);
            image.scale.set(Math.min(2, size / ItemSprite.SIZE));
            image.x = PixelScene.align(x + (width - image.width()) / 2);
            image.y = PixelScene.align(y + (name.top() - y - image.height()) / 2);
        }
    }
}
