package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.LibraryCatalog;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Section;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Shelf;
import com.bilboldev.pixeldungeonskills.LibraryHeroBuild;
import com.bilboldev.pixeldungeonskills.LibraryCreatureStats;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild;
import com.bilboldev.pixeldungeonskills.Lore;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.LibraryStats;
import com.bilboldev.pixeldungeonskills.ui.LibraryRivalSkills;
import com.bilboldev.pixeldungeonskills.ui.LibraryRewardsPanel;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Window;
import com.bilboldev.pixeldungeonskills.windows.WndLibrary;
import com.bilboldev.pixeldungeonskills.windows.WndLibraryHero;
import com.bilboldev.pixeldungeonskills.windows.WndLibraryItem;
import com.bilboldev.pixeldungeonskills.windows.WndLibrarySkills;
import java.util.ArrayList;
import java.util.Locale;

final class LibraryPanel extends TitleHubPanel {
    private static Shelf shelf = Shelf.DEPTHS;
    private static Entry selected;
    private static LibraryItemBuild itemBuild;
    private static LibraryHeroBuild heroBuild;
    private static float listOffset;
    private Group body;
    private ScrollPane list;
    private boolean restoreEntry = selected != null;
    private final ArrayList<Entry> shown = new ArrayList<>();
    private final ArrayList<Component> rows = new ArrayList<>();

    LibraryPanel(TitleScene host) {
        super(host, msg("library"));
        cardW = portrait ? screenW - 24 : Math.min(370, screenW * .51f);
        cardY = portrait ? Math.max(82, screenH * .24f) : Math.max(86, 48 + target);
        cardH = screenH - cardY - 20;
        card();
        rebuild();
    }

    private void rebuild() {
        if (body != null) {
            controls.removeIf(control -> control.parent == body);
            remove(body);
            body.destroy();
        }
        add(body = new Group());
        float left = cardX + 10, width = cardW - 20, top = cardY + 10;
        Section[] sections = Section.values();
        float tabGap = portrait ? 2 : 4;
        float tabWidth = (width - tabGap * (sections.length - 1)) / sections.length;
        for (int i = 0; i < sections.length; i++) {
            final Section choice = sections[i];
            HubButton tab = button(body, LibraryCatalog.text(choice.name().toLowerCase(Locale.ROOT)), shelf.section == choice, () -> {
                for (Shelf first : Shelf.values()) {
                    if (first.section == choice) {
                        select(first);
                        break;
                    }
                }
            });
            tab.icon(choice.image());
            tab.setRect(left + i * (tabWidth + tabGap), top, tabWidth, target);
        }
        top += target + 6;
        if (shelf.section == Section.ITEMS || shelf.section == Section.CREATURES) {
            button(body, "<", false, () -> cycleShelf(-1)).setRect(left, top, target, target);
            button(body, ">", false, () -> cycleShelf(1)).setRect(left + width - target, top, target, target);
            HighlightedText category = label(body, shelf.title(), 10, left, top, width - 2 * (target + 8));
            category.setPos(left + (width - category.width()) / 2, top + (target - category.height()) / 2);
            top += target + 6;
        }
        shown.clear();
        for (Entry entry : LibraryCatalog.entries()) {
            if (entry.shelf == shelf) shown.add(entry);
        }
        LibraryCatalog.sort(shown);
        text(body, LibraryCatalog.text("count", shown.size()), 7, 0xBCA98B, left, top);
        top += 16;
        Component content = new Component();
        rows.clear();
        int columns = width >= 260 ? 2 : 1;
        float cellWidth = width / columns, rowHeight = Math.max(40, target + 8);
        for (int i = 0; i < shown.size(); i++) {
            Entry entry = shown.get(i);
            float x = i % columns * cellWidth, y = i / columns * rowHeight;
            Component row = new Component();
            row.setRect(x, y, cellWidth - 4, rowHeight - 4);
            ColorBlock fill = new ColorBlock(row.width(), row.height(), 0x801F1C29);
            fill.x = x;
            fill.y = y;
            row.add(fill);
            Icon icon = new Icon(entry);
            icon.setRect(x + 3, y + 3, 35, row.height() - 6);
            row.add(icon);
            HighlightedText name = label(row, Lore.known(entry) ? entry.name() : "???", 9, x + 44, y + 4, cellWidth - 52);
            name.setPos(name.left(), y + Math.max(4, (row.height() - name.height()) / 2));
            content.add(row);
            rows.add(row);
        }
        content.setSize(width, (float)Math.ceil(shown.size() / (float)columns) * rowHeight);
        list = new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).inside(x, y)) {
                        Sample.INSTANCE.play(Assets.SND_CLICK);
                        open(shown.get(i));
                        return;
                    }
                }
            }
        };
        body.add(list);
        list.setRect(left, top, width, Math.max(20, cardY + cardH - top - 10));
        list.scrollTo(0, listOffset);
    }

    private void select(Shelf choice) {
        shelf = choice;
        listOffset = 0;
        rebuild();
    }

    private void cycleShelf(int direction) {
        Shelf[] shelves = Shelf.values();
        int index = shelf.ordinal();
        do {
            index = (index + direction + shelves.length) % shelves.length;
        } while (shelves[index].section != shelf.section);
        select(shelves[index]);
    }

    private void open(Entry entry) {
        selected = entry;
        itemBuild = null;
        heroBuild = null;
        showSelected();
    }

    private void showSelected() {
        if (itemBuild != null) {
            host.add(new WndLibraryItem(itemBuild, LibraryPanel::clearSelection));
        } else if (heroBuild != null) {
            host.add(new WndLibraryHero(heroBuild, LibraryPanel::clearSelection));
        } else {
            Entry entry = selected;
            host.add(new EntryWindow(entry, () -> {
                selected = entry;
                if (entry.heroClass != null) {
                    heroBuild = new LibraryHeroBuild();
                    heroBuild.chooseClass(entry.heroClass);
                } else {
                    itemBuild = new LibraryItemBuild(entry);
                }
                showSelected();
            }));
        }
    }

    private static void clearSelection() {
        selected = null;
        itemBuild = null;
        heroBuild = null;
    }

    @Override
    public void update() {
        super.update();
        if (restoreEntry && !host.transitioning() && Window.topWindow() == null) {
            restoreEntry = false;
            showSelected();
        }
    }

    @Override
    public void destroy() {
        listOffset = list.content().camera.scroll.y;
        super.destroy();
    }

    private static HighlightedText label(Group group, String value, int size, float x, float y, float width) {
        HighlightedText text = new HighlightedText(size);
        text.setColor(0xDDD4C5, 0xECD095);
        text.text(value, (int)width, false);
        text.setPos(x, y);
        group.add(text);
        return text;
    }

    private static final class EntryWindow extends WndLibrary {
        private final LibraryRewardsPanel rewards;
        private final ControlRow content = new ControlRow() {
            @Override protected void onClick(float x, float y) {
                if (rewards != null) rewards.onClick(x, y);
            }
        };

        EntryWindow(Entry entry, Runnable customize) {
            this(entry, customize, firstDepth(entry));
        }

        EntryWindow(Entry entry, Runnable customize, int depth) {
            int width = boundedWidth(landscapeLayout() ? 400 : 144);
            int bodyWidth = textWidth(width);
            boolean wide = bodyWidth >= 320;
            int statsWidth = wide ? 156 : bodyWidth;
            boolean revealed = Lore.known(entry);
            boolean known = revealed && (entry.shelf != Shelf.RIVALS || Lore.known(entry, depth));
            Component heading = new Component();
            Icon icon = new Icon(entry, true, depth);
            icon.setRect(0, 0, 40, 40);
            heading.add(icon);
            HighlightedText name = label(heading, revealed ? entry.name() : "???", 9, 48, 0, width - 48);
            name.setColor(titleColor(), titleColor());
            name.setPos(48, Math.max(0, (40 - name.height()) / 2));
            heading.setSize(width, Math.max(40, name.bottom()));
            if (entry.shelf == Shelf.RIVALS && revealed) {
                if (wide) name.setPos(48, 0);
                float left = wide ? 48 : 0;
                float y = wide ? name.bottom() + 4 : heading.height() + 6;
                float cell = (width - left - 9) / 4f;
                String[] tiers = {"I", "II", "III", "IV"};
                for (int i = 0; i < 4; i++) {
                    final int choice = (i + 1) * 5;
                    RedButton button = new ActionButton(tiers[i]) {
                        @Override protected void onClick() {
                            Group owner = EntryWindow.this.parent;
                            EntryWindow.this.hide();
                            selected = entry;
                            owner.add(new EntryWindow(entry, customize, choice));
                        }
                    };
                    button.enable(choice != depth);
                    button.setRect(left + i * (cell + 3), y, cell, Math.max(24, 48 * Game.density / camera.zoom));
                    heading.add(button);
                    heading.setSize(width, Math.max(heading.height(), button.bottom()));
                }
            }
            String info;
            if (known) info = entry.info();
            else if (entry.shelf == Shelf.DEPTHS) info = LibraryCatalog.text("unknown_depth");
            else if (entry.shelf == Shelf.QUESTS) info = LibraryCatalog.text("unknown_quest");
            else if (entry.shelf == Shelf.RECIPES) info = LibraryCatalog.text("unknown_recipe");
            else if (entry.shelf == Shelf.RIVALS && revealed) info = LibraryCatalog.text("unknown_rival_depth");
            else if (entry.shelf.section == Section.ITEMS) info = LibraryCatalog.text("unknown_item");
            else info = LibraryCatalog.text("unknown_creature");
            if (entry.shelf == Shelf.QUESTS) {
                info += "\n\n_" + LibraryCatalog.text("rewards") + ":_ "
                        + (known && Lore.rewardKnown(entry) ? entry.reward() : "???");
            }
            HighlightedText description = new HighlightedText(8);
            description.setColor(0xDDD4C5, 0xECD095);
            float buttonHeight = Math.max(24, 48 * Game.density / camera.zoom);
            float top = 0;
            LibraryCreatureStats creature = known ? entry.creatureStats(depth) : null;
            LibraryStats stats = null;
            if (creature != null) {
                stats = LibraryStats.creature(statsWidth, creature);
            } else if (entry.heroClass != null) {
                LibraryHeroBuild hero = new LibraryHeroBuild();
                hero.chooseClass(entry.heroClass);
                stats = LibraryStats.hero(statsWidth, hero.calculate());
            }
            if (stats != null) {
                stats.setPos(0, top);
                content.add(stats);
                top = stats.bottom() + (wide ? 0 : 8);
            }
            if (entry.heroClass != null) {
                RedButton equipment = heroAction("hero_equipment", () -> {
                    EntryWindow.this.hide();
                    customize.run();
                });
                RedButton skills = heroAction("skill_tree", () -> parent.add(new WndLibrarySkills(entry.heroClass)));
                float cell = (statsWidth - 4) / 2f;
                equipment.setRect(0, stats.bottom() + 6, cell, buttonHeight);
                skills.setRect(cell + 4, equipment.top(), cell, buttonHeight);
                float height = Math.max(buttonHeight, Math.max(equipment.reqHeight(), skills.reqHeight()));
                equipment.setSize(cell, height);
                skills.setSize(cell, height);
                top = equipment.bottom() + 8;
            }
            if (creature != null && !creature.note().isEmpty()) info = creature.note() + "\n\n" + info;
            boolean split = wide && stats != null;
            int descriptionWidth = split ? bodyWidth - statsWidth - 12 : bodyWidth;
            description.text(info, descriptionWidth - 8, false);
            description.setPos((split ? statsWidth + 12 : 0) + 4, (split ? 0 : top) + 4);
            content.add(description);
            top = Math.max(top, description.bottom() + 4);
            if (known && entry.shelf == Shelf.RIVALS) {
                LibraryRivalSkills abilities = new LibraryRivalSkills(descriptionWidth, entry.rivalClass(), depth);
                abilities.setPos(split ? statsWidth + 12 : 0, (split ? description.bottom() : top) + 10);
                content.add(abilities);
                top = Math.max(top, abilities.bottom());
            }
            Image plant = known ? entry.plantImage() : null;
            if (plant != null) {
                HighlightedText title = label(content, LibraryCatalog.text("plant"), 8, 4, top + 8, bodyWidth - 8);
                title.setColor(0xECD095, 0xECD095);
                plant.scale.set(2);
                plant.x = 4;
                plant.y = title.bottom() + 4;
                content.add(plant);
                top = plant.y + plant.height() + 4;
            }
            rewards = known ? new LibraryRewardsPanel(entry, bodyWidth, depth) : null;
            if (rewards != null && rewards.height() > 0) {
                rewards.setPos(0, top + (wide ? 6 : 10));
                content.add(rewards);
                top = rewards.bottom();
            }
            content.setSize(bodyWidth, top);
            if (known && entry.customizable()) {
                RedButton edit = new ActionButton(LibraryCatalog.text("customize")) {
                    @Override
                    protected void onClick() {
                        EntryWindow.this.hide();
                        customize.run();
                    }
                };
                edit.setSize(width, buttonHeight);
                layoutLibrary(width, heading, content, 6, edit);
            } else {
                layoutLibrary(width, heading, content, 6);
            }
        }

        private RedButton heroAction(String key, Runnable action) {
            RedButton button = new ActionButton(LibraryCatalog.text(key)) {
                { content.addControl(hotArea); }

                @Override protected void onClick() {
                    action.run();
                }
            };
            content.add(button);
            return button;
        }

        @Override
        protected ScrollPane createScrollPane(Component body) {
            return content.scrollPane(body);
        }

        @Override
        public void hide() {
            clearSelection();
            super.hide();
        }
    }

    private static final class Icon extends Component {
        private final Image image;
        private Emitter cloud;

        Icon(Entry entry) {
            this(entry, false);
        }

        Icon(Entry entry, boolean selected) {
            this(entry, selected, firstDepth(entry));
        }

        Icon(Entry entry, boolean selected, int depth) {
            boolean known = Lore.known(entry) && (entry.shelf != Shelf.RIVALS || Lore.known(entry, depth));
            if (entry.shelf == Shelf.DEPTHS && !known) {
                image = new ItemSprite(ItemSpriteSheet.SMTH, null);
            } else if (selected && entry.heroClass != null) {
                image = LibraryCatalog.heroImage(entry.heroClass.spritesheet(), 0);
            } else {
                image = entry.image(depth);
            }
            if (!known) image.hardlight(0x000000);
            add(image);
            if (known && entry.key.equals("actors.mobs.fetidrat.name")) {
                cloud = new Emitter();
                cloud.pour(Speck.factory(Speck.PARALYSIS), .7f);
                add(cloud);
            }
        }

        @Override
        protected void layout() {
            super.layout();
            float size = Math.min(width, height) * .85f;
            image.scale.set(size / Math.max(image.width, image.height));
            image.x = PixelScene.align(x + (width - image.width()) / 2);
            image.y = PixelScene.align(y + (height - image.height()) / 2);
            if (cloud != null) cloud.pos(image.x, image.y, image.width(), image.height());
        }
    }

    private static int firstDepth(Entry entry) {
        if (entry.shelf == Shelf.RIVALS) {
            for (int depth = 5; depth <= 20; depth += 5) {
                if (Lore.known(entry, depth)) return depth;
            }
        }
        return 5;
    }
}
