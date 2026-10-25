package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Section;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Shelf;
import com.bilboldev.pixeldungeonskills.LibraryRewards.Drop;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Prospector;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Wandmaker;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public final class Lore {
    private static final String DISCOVERIES = "lore_discoveries";
    private static Set<String> discoveries;
    private static boolean changed;
    private static boolean checkCompletion = true;
    private static Set<String> required;
    private static final List<Notice> notices = new ArrayList<>();

    private Lore() { }

    private static void load() {
        if (discoveries != null) return;
        discoveries = new TreeSet<>(Arrays.asList(Preferences.INSTANCE.getString(DISCOVERIES, "").split("\n")));
        discoveries.remove("");
    }

    private static boolean remember(String key) {
        load();
        if (!discoveries.add(key)) return false;
        changed = checkCompletion = true;
        return true;
    }

    private static void save() {
        if (changed) {
            StringBuilder saved = new StringBuilder();
            for (String entry : discoveries) {
                if (saved.length() > 0) saved.append('\n');
                saved.append(entry);
            }
            Preferences.INSTANCE.put(DISCOVERIES, saved.toString());
            changed = false;
        }
        if (checkCompletion) {
            checkCompletion = false;
            Badges.validateLore();
        }
    }

    public static synchronized boolean known(Entry entry) {
        if (entry.shelf.section == Section.HEROES || entry.shelf.section == Section.SKILLS || entry.knownAtStart()) return true;
        load();
        if (entry.shelf == Shelf.RIVALS) {
            for (int depth = 5; depth <= 20; depth += 5) {
                if (discoveries.contains(entry.id() + ":" + depth)) return true;
            }
            return false;
        }
        return discoveries.contains(entry.id());
    }

    public static synchronized boolean known(Entry rival, int depth) {
        load();
        return discoveries.contains(rival.id() + ":" + depth);
    }

    static synchronized boolean knownModifier(String key) {
        if (key == null) return true;
        load();
        return discoveries.contains("MODIFIER:" + key);
    }

    public static synchronized boolean dropped(Entry creature, int depth, String itemKey) {
        if (itemKey == null) return false;
        load();
        return known(LibraryCatalog.item(itemKey))
                && discoveries.contains(dropKey(creature, depth, itemKey));
    }

    public static synchronized boolean dropped(Entry creature, int depth, Drop drop) {
        load();
        return (drop.itemKey == null || known(LibraryCatalog.item(drop.itemKey)))
                && discoveries.contains(dropKey(creature, depth, drop.key()));
    }

    private static String dropKey(Entry creature, int depth, String itemKey) {
        return creature.id() + (creature.shelf == Shelf.RIVALS ? ":" + depth : "") + ">" + itemKey;
    }

    private static boolean solo() {
        return !GauntletRun.active() && !RunSeed.customRun() && !LocalCoop.active()
                && !PartySession.network() && !LocalCoop.isRestoring()
                && Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.level != null
                && Game.instance != null && Game.scene() instanceof GameScene;
    }

    private static boolean visible(int cell) {
        return cell >= 0 && cell < Dungeon.visible.length && Dungeon.visible[cell];
    }

    public static synchronized void observe() {
        if (!solo()) return;
        Ghost.Quest.updateLore();
        Wandmaker.Quest.updateLore();
        Blacksmith.Quest.updateLore();
        Imp.Quest.updateLore();
        Entry chapter = IceCaves.active() ? null : LibraryCatalog.chapter(Dungeon.depth);
        if (chapter != null) discover(chapter, 0);
        for (Mob mob : Dungeon.level.mobs) {
            if (mob instanceof Prospector) ((Prospector)mob).updateLore();
            if (visible(mob.pos) && mob.invisible == 0) see(mob);
        }
        for (Item item : Dungeon.hero.belongings) identify(item);
        identify(Dungeon.hero.belongings.backpack);
        if (Dungeon.hero.belongings.bow != null) identify(Dungeon.hero.belongings.bow);
        for (Heap heap : Dungeon.level.heaps.values()) {
            if (visible(heap.pos) && (heap.type == Heap.Type.HEAP || heap.type == Heap.Type.FOR_SALE)) {
                for (Item item : heap.items) identify(item);
            }
        }
        save();
    }

    private static void see(Mob mob) {
        Entry entry = LibraryCatalog.find(mob);
        if (entry == null) return;
        discover(entry, mob instanceof RivalBoss ? ((RivalBoss)mob).tier() * 5 : 0);
    }

    private static void discover(Entry entry, int depth) {
        if (entry.knownAtStart()) return;
        String key = entry.id() + (depth == 0 ? "" : ":" + depth);
        if (remember(key)) {
            String detail = depth == 0 ? entry.shelf.title() : new String[]{"I", "II", "III", "IV"}[depth / 5 - 1];
            notices.add(new Notice(entry.name(), detail, () -> entry.image(depth)));
        }
    }

    private static boolean typeKnown(Item item) {
        return item instanceof Potion ? ((Potion)item).isKnown()
                : item instanceof Scroll ? ((Scroll)item).isKnown()
                : item instanceof Ring ? ((Ring)item).isKnown()
                : item instanceof Wand ? ((Wand)item).isKnown() : item.isIdentified();
    }

    private static void identify(Item item) {
        boolean identified = typeKnown(item);
        Entry entry = LibraryCatalog.find(item);
        if (entry != null && (identified || item instanceof Weapon || item instanceof Armor)) discover(entry, 0);
        if (!identified) return;
        for (String modifier : LibraryItemBuild.modifierKeys(item)) {
            if (remember("MODIFIER:" + modifier)) {
                int image = item.image();
                notices.add(new Notice(LibraryItemBuild.modifierName(modifier), item.name(), () -> new ItemSprite(image, null)));
            }
        }
    }

    public static synchronized void used(Item item) {
        if (!solo()) return;
        identify(item);
        save();
    }

    public static synchronized void quest(String name, boolean completed) {
        if (!solo()) return;
        Entry entry = LibraryCatalog.find(Shelf.QUESTS, "library.quest." + name + ".name");
        discover(entry, 0);
        if (completed && remember(entry.id() + ":reward")) {
            notices.add(new Notice(entry.name(), LibraryCatalog.text("rewards"), entry::image));
        }
        save();
    }

    public static synchronized boolean rewardKnown(Entry quest) {
        load();
        return discoveries.contains(quest.id() + ":reward");
    }

    public static synchronized void dropped(Mob mob, Item item, int cell) {
        dropped(mob, item, cell, false);
    }

    public static synchronized void dropped(Mob mob, Item item, int cell, boolean championReward) {
        if (!solo() || mob == null || !visible(cell) || !visible(mob.pos) || mob.invisible > 0) return;
        Entry creature = LibraryCatalog.find(mob);
        if (creature == null) return;
        see(mob);
        if (!(item instanceof Weapon) && !(item instanceof Armor) && !typeKnown(item)) {
            save();
            return;
        }
        identify(item);
        int depth = mob instanceof RivalBoss ? ((RivalBoss)mob).tier() * 5 : 0;
        for (Drop drop : LibraryRewards.drops(creature, depth)) {
            if (drop.matches(item, mob, championReward) && remember(dropKey(creature, depth, drop.key()))) {
                if (drop.itemKey == null || known(LibraryCatalog.item(drop.itemKey))) {
                    notices.add(new Notice(creature.name(), drop.name(), drop::image));
                }
            }
        }
        save();
    }

    public static synchronized void brewed(AlchemyRecipe recipe) {
        if (!solo()) return;
        discover(LibraryCatalog.recipe(recipe), 0);
        identify(recipe.potion());
        save();
    }

    public static synchronized boolean complete() {
        load();
        if (required == null) {
            required = new TreeSet<>();
            for (Entry entry : LibraryCatalog.entries()) {
                if (entry.shelf.section == Section.HEROES || entry.shelf.section == Section.SKILLS) continue;
                if (entry.shelf == Shelf.RIVALS) {
                    for (int depth = 5; depth <= 20; depth += 5) {
                        required.add(entry.id() + ":" + depth);
                        for (Drop drop : LibraryRewards.drops(entry, depth)) required.add(dropKey(entry, depth, drop.key()));
                    }
                } else {
                    if (!entry.knownAtStart()) required.add(entry.id());
                    if (entry.shelf == Shelf.QUESTS) required.add(entry.id() + ":reward");
                    for (Drop drop : LibraryRewards.drops(entry)) required.add(dropKey(entry, 0, drop.key()));
                }
            }
            for (String modifier : LibraryItemBuild.modifiers()) required.add("MODIFIER:" + modifier);
        }
        return discoveries.containsAll(required);
    }

    public static synchronized boolean hasNotices() {
        return !notices.isEmpty();
    }

    public static synchronized List<Notice> takeNotices() {
        List<Notice> result = new ArrayList<>(notices);
        notices.clear();
        return result;
    }

    public static synchronized void clearNotices() {
        notices.clear();
    }

    public static final class Notice {
        public final String title, detail;
        private final Supplier<Image> image;

        private Notice(String title, String detail, Supplier<Image> image) {
            this.title = title;
            this.detail = detail;
            this.image = image;
        }

        public Image image() {
            return image.get();
        }
    }
}
