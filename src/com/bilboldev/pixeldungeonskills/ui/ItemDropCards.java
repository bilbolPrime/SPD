package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.LibraryItemBuild;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfAwareness;
import com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfHealth;
import com.bilboldev.pixeldungeonskills.actors.blobs.WaterOfTransmutation;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Bestiary;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyFoe;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDemon;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDefender;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyRaider;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyQuarry;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyAlly;
import com.bilboldev.pixeldungeonskills.actors.mobs.GauntletRatKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.essences.Essence;
import com.bilboldev.pixeldungeonskills.items.ItemStatusHandler;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.DailyLevel;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.PlantSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTerrainTilemap;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTilemapOld;
import com.bilboldev.utils.RectF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class ItemDropCards extends Gizmo {
    private static final float OPEN = .25f, HOLD = 2f, CLOSE = .5f;
    private static final int MAX_CARDS = 3;
    private static final HashSet<String> shownKinds = new HashSet<>();
    private static final HashMap<Integer, HashMap<String, Integer>> shownFloorItems = new HashMap<>();
    private static final HashMap<Integer, HashSet<Integer>> shownFloorChampions = new HashMap<>();
    private static int run = 1;
    private final GameScene scene;
    private final int session = run;
    private Level level;
    private int floor;
    private final LinkedHashMap<Long, Pile> piles = new LinkedHashMap<>();
    private final HashMap<Integer, Mob> mobsSeen = new HashMap<>();
    private final HashSet<Class<?>> mobTypesSeen = new HashSet<>();
    private final IdentityHashMap<Item, Integer> quiet = new IdentityHashMap<>();
    private final RectF candidate = new RectF();
    private float elapsed;
    private boolean[] terrainSeen;
    private Class<?>[] plantsSeen = new Class<?>[0];
    private float terrainScan;
    private int mode = Presentation.helperCards();

    public ItemDropCards(GameScene scene) {
        this.scene = scene;
        level = Dungeon.level;
        floor = Dungeon.levelKey();
        terrainSeen = new boolean[level == null ? 0 : level.map.length];
        camera = PixelScene.uiCamera;
        restoreFloorHistory();
    }

    public void ignore(Item item, int quantity) {
        settingsChanged();
        if (mode == Presentation.HELPER_CARDS_OFF) return;
        Integer count = quiet.get(item);
        quiet.put(item, quantity + (count == null ? 0 : count));
    }

    @Override
    public void update() {
        settingsChanged();
        if (Dungeon.level != level || Dungeon.levelKey() != floor) {
            clear();
            level = Dungeon.level;
            floor = Dungeon.levelKey();
            restoreFloorHistory();
        }
        if (level == null || Dungeon.rules().mode != GameplayMode.REBORN
                || mode == Presentation.HELPER_CARDS_OFF) return;
        elapsed = Math.min(.1f, Math.max(0, Game.elapsed));
        observe();
        observeMobs();
        boolean running = Dungeon.hero != null && Dungeon.hero.isAlive() && Window.topWindow() == null;
        terrainScan -= elapsed;
        if (running && terrainScan <= 0) {
            observeTerrain();
            observePlants();
            terrainScan = .2f;
        }
        for (Iterator<Pile> it = piles.values().iterator(); it.hasNext();) {
            Pile pile = it.next();
            boolean gone;
            if (pile.mob != null) {
                gone = !pile.mob.isAlive() || !level.mobs.contains(pile.mob)
                        || pile.rows.isEmpty() || (!pile.mob.hostile && !(pile.mob instanceof DailyAlly)
                        && !(pile.mob instanceof DailyDefender) && MobCardText.peopleHint(pile.mob) == null)
                        || (pile.activeRows() == 0 && !individualCard(pile.mob) && mobTypesSeen.contains(mobType(pile.mob)));
            } else if (pile.plant != null) {
                Plant current = level.plants.get(pile.pos);
                gone = current == null || current.getClass() != pile.plant.getClass() || pile.rows.isEmpty();
            } else if (pile.terrain >= 0) {
                gone = level.map[pile.pos] != pile.terrain || pile.rows.isEmpty() || terrainHint(pile.pos) == null;
            } else {
                Heap heap = level.heaps.get(pile.pos);
                gone = heap == null || heap.type != Heap.Type.HEAP && heap.type != Heap.Type.FOR_SALE
                        && chestType(heap) == null;
            }
            if (gone) {
                pile.destroy();
                it.remove();
                continue;
            }
            for (Iterator<Row> rows = pile.rows.iterator(); rows.hasNext();) {
                Row row = rows.next();
                if (row.age >= 0 && running && pile.onScreen && pile.placed) row.age += elapsed;
                if ((row.seen != null && !pile.items.contains(row.seen)) || row.age >= OPEN + HOLD + CLOSE
                        || row.age < 0 && row.suppressed()) {
                    row.destroy();
                    rows.remove();
                } else if (row.icon != null) {
                    if (row.mob != null && row.mob.sprite != null) {
                        row.icon.flipHorizontal = row.mob.sprite.flipHorizontal;
                        row.icon.copy(row.mob.sprite);
                        row.icon.rm = row.mob.sprite.rm;
                        row.icon.gm = row.mob.sprite.gm;
                        row.icon.bm = row.mob.sprite.bm;
                        row.icon.ra = row.mob.sprite.ra;
                        row.icon.ga = row.mob.sprite.ga;
                        row.icon.ba = row.mob.sprite.ba;
                    }
                    row.icon.update();
                    if (row.revision != ItemStatusHandler.revision()) row.refresh();
                }
            }
            if (pile.placed) {
                float difference = pile.bounds.width() - pile.shownWidth;
                float step = elapsed * 1200;
                pile.shownWidth += clamp(difference, -step, step);
            }
        }
    }

    private void observeTerrain() {
        if (terrainSeen.length != level.map.length) terrainSeen = new boolean[level.map.length];
        if (Dungeon.visible == null) return;
        for (int cell = 0; cell < level.map.length && cell < Dungeon.visible.length; cell++) {
            if (!Dungeon.visible[cell] || terrainSeen[cell] || terrainHint(cell) == null
                    || suppressed(terrainKind(cell))
                    || piles.containsKey(-1L - cell)) continue;
            piles.put(-1L - cell, new Pile(cell));
        }
    }

    private void observePlants() {
        if (plantsSeen.length != level.map.length) plantsSeen = new Class<?>[level.map.length];
        if (Dungeon.visible == null) return;
        for (Plant plant : level.plants.values()) {
            int cell = plant.pos;
            if (cell < 0 || cell >= plantsSeen.length || cell >= Dungeon.visible.length
                    || !Dungeon.visible[cell] || plantsSeen[cell] == plant.getClass()
                    || suppressed(plantKind(plant))) continue;
            long key = Long.MIN_VALUE + cell;
            if (!piles.containsKey(key)) piles.put(key, new Pile(plant));
        }
    }

    private void observeMobs() {
        for (Iterator<Mob> it = mobsSeen.values().iterator(); it.hasNext();) {
            if (!level.mobs.contains(it.next())) it.remove();
        }
        for (Mob mob : level.mobs) {
            if (!mob.hostile && !(mob instanceof DailyAlly) && !(mob instanceof DailyDefender) && MobCardText.peopleHint(mob) == null) continue;
            int id = mob.id();
            long key = -1L - level.map.length - id;
            Pile pile = piles.get(key);
            if (pile != null && pile.mob != mob) {
                pile.destroy();
                piles.remove(key);
                pile = null;
            }
            if (pile != null) {
                pile.pos = mob.pos;
            } else if (mobsSeen.get(id) != mob && mob.isAlive()
                    && !suppressed(mob)
                    && (individualCard(mob) || !mobTypesSeen.contains(mobType(mob)))) {
                piles.put(key, new Pile(mob));
            }
        }
    }

    private static boolean individualCard(Item item) {
        return item instanceof Essence || item.attributes.length > 0;
    }

    private static boolean individualCard(Mob mob) {
        return !mob.hostile || mob.champ > 0 || Bestiary.isBoss(mob) || mob instanceof GauntletRatKing
                || mob instanceof DailyQuarry
                || (mob instanceof DailyFoe && ((DailyFoe)mob).escapeCaptain());
    }

    private Class<?> mobType(Mob mob) {
        if (mob instanceof DailyDemon || mob instanceof DailyRaider
                || (mode == Presentation.HELPER_CARDS_AUTO && mob instanceof DailyFoe)) {
            return mob.spriteClass;
        }
        return mob.getClass();
    }

    private void observe() {
        for (Heap heap : level.heaps.values()) {
            Heap.Type chest = chestType(heap);
            if (heap.type != Heap.Type.HEAP && heap.type != Heap.Type.FOR_SALE && chest == null) continue;
            Pile pile = piles.get((long)heap.pos);
            if (pile != null && pile.chest != chest) {
                pile.destroy();
                piles.remove((long)heap.pos);
                pile = null;
            }
            if (pile == null) {
                if (chest != null && suppressed(chestKind(chest))) continue;
                pile = new Pile(heap);
                piles.put((long)heap.pos, pile);
            }
            pile.heap = heap;
            if (chest != null) continue;
            for (Item item : heap.items) {
                Seen seen = pile.find(item);
                boolean added = seen == null;
                if (added) {
                    seen = new Seen(item);
                    pile.items.add(seen);
                }
                int increase = item.quantity() - seen.quantity;
                Integer ignored = quiet.get(item);
                if (ignored != null) increase -= ignored;
                else if (added && item.droppedByHero) increase = 0;
                item.droppedByHero = false;
                if (item.droppedOnDeath) {
                    seen.afterDeath += Math.max(0, increase);
                    item.droppedOnDeath = false;
                    increase = 0;
                }
                seen.quantity = item.quantity();
                if (increase > 0 && !suppressed(item)) pile.rows.add(new Row(seen, increase));
            }
            for (Iterator<Seen> it = pile.items.iterator(); it.hasNext();) {
                if (!heap.items.contains(it.next().item)) it.remove();
            }
        }
        quiet.clear();
    }

    private static Heap.Type chestType(Heap heap) {
        switch (heap.type) {
            case CHEST: case MIMIC: return Heap.Type.CHEST;
            case LOCKED_CHEST: case CRYSTAL_CHEST: return heap.type;
            default: return null;
        }
    }

    @Override
    public void draw() {
        settingsChanged();
        if (Dungeon.level != level || Dungeon.rules().mode != GameplayMode.REBORN
                || mode == Presentation.HELPER_CARDS_OFF
                || Dungeon.hero == null || !Dungeon.hero.isAlive() || Window.topWindow() != null) return;
        for (Pile pile : piles.values()) {
            pile.onScreen = pile.mob != null ? scene.mobCardBounds(pile.mob, pile.anchor)
                    : pile.terrain >= 0 || pile.plant != null ? scene.terrainCardBounds(pile.pos, pile.anchor)
                    : scene.itemDropBounds(pile.heap, pile.anchor);
            if (!pile.onScreen) {
                pile.placed = false;
                for (Seen seen : pile.items) {
                    if (seen.afterDeath > 0) {
                        pile.rows.add(new Row(seen, Math.min(seen.afterDeath, seen.quantity)));
                        seen.afterDeath = 0;
                    }
                }
            }
        }

        int showing = 0;
        for (Pile pile : piles.values()) {
            if (!pile.onScreen || pile.activeRows() == 0 || showing >= MAX_CARDS) {
                pile.placed = false;
                continue;
            }
            pile.placed = place(pile, pile.width(), pile.fullHeight());
            if (pile.placed) showing++;
        }
        for (Pile pile : piles.values()) {
            if (!pile.onScreen) continue;
            for (Iterator<Row> rows = pile.rows.iterator(); rows.hasNext();) {
                Row row = rows.next();
                if (row.age >= 0) continue;
                if (row.suppressed()) {
                    row.destroy();
                    rows.remove();
                    continue;
                }
                if (pile.mob != null && !individualCard(pile.mob) && mobTypesSeen.contains(mobType(pile.mob))) break;
                if (pile.heap != null && pile.heap.sprite.isDropping()) break;
                if (!pile.placed && showing >= MAX_CARDS) break;
                row.prepare();
                float width = Math.max(pile.width(), row.width());
                float height = pile.fullHeight() + row.height();
                if (!place(pile, width, height)) break;
                if (!pile.placed) showing++;
                if (pile.activeRows() == 0) pile.shownWidth = width;
                row.age = 0;
                if (row.seen != null && individualCard(row.seen.item)) row.seen.item.helperCardRun = run;
                else if (row.mob != null && row.mob.champ > 0) row.mob.helperCardRun = run;
                else shownKinds.add(row.kind());
                if (pile.terrain >= 0) terrainSeen[pile.pos] = true;
                if (pile.plant != null) plantsSeen[pile.pos] = pile.plant.getClass();
                if (pile.mob != null) {
                    mobsSeen.put(pile.mob.id(), pile.mob);
                    if (!individualCard(pile.mob)) mobTypesSeen.add(mobType(pile.mob));
                }
                pile.placed = true;
            }
            if (pile.placed && pile.activeRows() > 0) pile.draw();
        }
    }

    private boolean place(Pile pile, float width, float height) {
        float cx = (pile.anchor.left + pile.anchor.right) / 2;
        float cy = (pile.anchor.top + pile.anchor.bottom) / 2;
        for (int attempt = 0; attempt < 24; attempt++) {
            int direction = (attempt + pile.direction) % 8;
            float gap = 9 + (attempt / 8) * 20;
            float x = direction == 0 || direction == 2 || direction == 4 ? pile.anchor.right + gap
                    : direction == 1 || direction == 3 || direction == 5 ? pile.anchor.left - gap - width : cx - width / 2;
            float y = direction <= 1 || direction == 6 ? pile.anchor.top - gap - height
                    : direction <= 3 || direction == 7 ? pile.anchor.bottom + gap : cy - height / 2;
            x = Math.round(Math.max(3, Math.min(camera.width - width - 3, x)));
            y = Math.round(Math.max(38, Math.min(scene.itemDropBottom() - height, y)));
            candidate.set(x, y, x + width, y + height);
            if (candidate.bottom > scene.itemDropBottom() || candidate.right > camera.width - 3
                    || scene.itemDropObstructed(candidate)) continue;
            boolean clear = true;
            for (Pile other : piles.values()) {
                if (other.onScreen && overlaps(candidate, other.anchor, 3)
                        || other != pile && other.placed && overlaps(candidate, other.bounds, 3)) {
                    clear = false;
                    break;
                }
            }
            if (!clear) continue;
            pile.bounds.set(candidate.left, candidate.top, candidate.right, candidate.bottom);
            pile.direction = direction;
            return true;
        }
        return false;
    }

    private static boolean overlaps(RectF a, RectF b, float gap) {
        return a.left < b.right + gap && a.right + gap > b.left
                && a.top < b.bottom + gap && a.bottom + gap > b.top;
    }

    public static List<Item> carried() {
        ArrayList<Item> result = new ArrayList<>();
        Hero[] heroes = LocalCoop.active() ? LocalCoop.heroes() : new Hero[]{Dungeon.hero};
        for (Hero hero : heroes) {
            if (hero == null) continue;
            for (Item item : hero.belongings) result.add(item);
            for (Item item : hero.storage) result.add(item);
        }
        return result;
    }

    public void rebind(List<Item> before) {
        settingsChanged();
        if (mode == Presentation.HELPER_CARDS_OFF) {
            rebindHistory(before);
            rememberFloor();
            level = Dungeon.level;
            floor = Dungeon.levelKey();
            restoreFloorHistory();
            return;
        }
        List<Item> after = carried();
        int[] available = new int[after.size()];
        for (int i = 0; i < available.length; i++) available[i] = after.get(i).quantity();
        int[] removed = new int[before.size()];
        for (int i = 0; i < removed.length; i++) {
            Item item = before.get(i);
            int remaining = item.quantity();
            for (int j = 0; j < available.length && remaining > 0; j++) {
                if (available[j] == 0 || !sameItem(item, after.get(j))) continue;
                int amount = Math.min(remaining, available[j]);
                if (item.helperCardRun == run) after.get(j).helperCardRun = run;
                available[j] -= amount;
                remaining -= amount;
            }
            removed[i] = remaining;
        }
        if (Dungeon.levelKey() != floor) {
            clear();
            level = Dungeon.level;
            floor = Dungeon.levelKey();
            restoreFloorHistory();
            if (level != null) observe();
            return;
        }
        ArrayList<Item> markedGround = new ArrayList<>();
        for (Pile pile : piles.values()) {
            for (Seen seen : pile.items) if (seen.item.helperCardRun == run) markedGround.add(seen.item);
        }
        boolean death = false;
        for (Hero hero : LocalCoop.heroes()) if (!hero.isAlive()) death = true;
        level = Dungeon.level;
        for (Mob mob : level.mobs) {
            int id = mob.id();
            Mob previous = mobsSeen.get(id);
            if (previous != null && sameMob(previous, mob)) {
                mob.helperCardRun = previous.helperCardRun;
                mobsSeen.put(id, mob);
            } else {
                mobsSeen.remove(id);
            }
            long key = -1L - level.map.length - id;
            Pile pile = piles.get(key);
            if (pile != null && !sameMob(pile.mob, mob)) {
                pile.destroy();
                piles.remove(key);
                continue;
            }
            if (pile != null) {
                pile.mob = mob;
                pile.pos = mob.pos;
                for (Row row : pile.rows) row.mob = mob;
            }
        }
        for (Heap heap : level.heaps.values()) {
            Pile pile = piles.get((long)heap.pos);
            ArrayList<Item> unmatched = new ArrayList<>(heap.items);
            if (pile != null) {
                pile.heap = heap;
                for (Seen seen : pile.items) {
                    for (Iterator<Item> it = unmatched.iterator(); it.hasNext();) {
                        Item next = it.next();
                        if (!sameItem(seen.item, next)) continue;
                        markedGround.remove(seen.item);
                        next.helperCardRun = seen.item.helperCardRun;
                        seen.item = next;
                        it.remove();
                        for (Row row : pile.rows) if (row.seen == seen && row.icon != null) row.refresh();
                        break;
                    }
                }
            }
            for (Item item : heap.items) {
                Seen seen = pile == null ? null : pile.find(item);
                int increase = item.quantity() - (seen == null ? 0 : seen.quantity);
                for (int i = 0; i < removed.length && increase > 0; i++) {
                    if (removed[i] == 0 || !sameItem(item, before.get(i))) continue;
                    int amount = Math.min(increase, removed[i]);
                    if (before.get(i).helperCardRun == run) item.helperCardRun = run;
                    if (death) item.droppedOnDeath = true;
                    else ignore(item, amount);
                    removed[i] -= amount;
                    increase -= amount;
                }
            }
        }
        for (Item old : markedGround) {
            for (Item item : after) {
                if (item.helperCardRun == run || !sameItem(old, item)) continue;
                item.helperCardRun = run;
                break;
            }
        }

        observe();
    }

    private void rebindHistory(List<Item> carriedBefore) {
        ArrayList<Item> before = new ArrayList<>(carriedBefore);
        ArrayList<Item> after = new ArrayList<>(carried());
        Level next = Dungeon.level;
        boolean sameFloor = level != null && next != null && floor == Dungeon.levelKey();
        if (sameFloor) {
            for (Heap heap : level.heaps.values()) before.addAll(heap.items);
        }
        if (next != null) {
            for (Heap heap : next.heaps.values()) after.addAll(heap.items);
        }
        for (Item previous : before) {
            if (!individualCard(previous)) continue;
            for (Iterator<Item> items = after.iterator(); items.hasNext();) {
                Item current = items.next();
                if (!sameItem(previous, current)) continue;
                if (previous.helperCardRun == run) current.helperCardRun = run;
                items.remove();
                break;
            }
        }
        if (sameFloor) {
            for (Mob previous : level.mobs) {
                if (previous.champ <= 0 || previous.helperCardRun != run) continue;
                for (Mob current : next.mobs) {
                    if (previous.id() == current.id() && sameMob(previous, current)) {
                        current.helperCardRun = run;
                        break;
                    }
                }
            }
        }
    }

    private static boolean sameItem(Item a, Item b) {
        if (a.getClass() != b.getClass() || a.level() != b.level() || a.cursed != b.cursed
                || type(a.weaponEffect) != type(b.weaponEffect)) return false;
        if (!EquipmentAttribute.same(a, b)) return false;
        if (a instanceof Essence) {
            Essence first = (Essence)a, second = (Essence)b;
            return first.grade() == second.grade() && first.bonus() == second.bonus() && first.skill() == second.skill();
        }
        if (a instanceof Weapon && type(((Weapon)a).enchantment()) != type(((Weapon)b).enchantment())) return false;
        return !(a instanceof Armor) || ((Armor)a).prefix == ((Armor)b).prefix
                && type(((Armor)a).glyph) == type(((Armor)b).glyph);
    }

    private static Class<?> type(Object value) {
        return value == null ? null : value.getClass();
    }

    private static boolean sameMob(Mob previous, Mob current) {
        return previous.getClass() == current.getClass() && previous.spriteClass == current.spriteClass
                && previous.champ == current.champ;
    }

    public static void resetRun() {
        shownKinds.clear();
        shownFloorItems.clear();
        shownFloorChampions.clear();
        run++;
    }

    public void settingsChanged() {
        int selected = Presentation.helperCards();
        if (mode == selected) return;
        clear();
        mode = selected;
        restoreFloorHistory();
    }

    private boolean suppressed(String kind) {
        return mode == Presentation.HELPER_CARDS_AUTO && shownKinds.contains(kind);
    }

    private boolean suppressed(Item item) {
        return mode == Presentation.HELPER_CARDS_AUTO && (individualCard(item)
                ? item.helperCardRun == run : shownKinds.contains(itemKind(item)));
    }

    private boolean suppressed(Mob mob) {
        return mode == Presentation.HELPER_CARDS_AUTO && (mob.champ > 0
                ? mob.helperCardRun == run : shownKinds.contains(mobKind(mob)));
    }

    private static String itemKind(Item item) {
        String unknown = unknownItemKind(item);
        return unknown == null ? "item:" + item.getClass().getName() : "unknown:" + unknown + ":" + item.image();
    }

    private String mobKind(Mob mob) {
        if (mob instanceof DailyAlly || mob instanceof DailyDefender || mob instanceof DailyFoe || mob instanceof DailyQuarry)
            return "mob:" + mob.getClass().getName() + ":" + mob.spriteClass.getName();
        return "mob:" + mobType(mob).getName() + ":" + MobCardText.peopleHint(mob);
    }

    private static String plantKind(Plant plant) {
        return "plant:" + plant.getClass().getName();
    }

    private static String chestKind(Heap.Type chest) {
        return "chest:" + chest;
    }

    private String terrainKind(int cell) {
        if (level instanceof DailyLevel) {
            String objective = ((DailyLevel)level).doorCardKind(cell);
            if (objective != null) return objective;
        }
        String hint = terrainHint(cell);
        return "terrain:" + hint + ("trap".equals(hint) ? ":" + level.map[cell] : "");
    }

    private static String floorItemKey(Heap heap, Item item) {
        StringBuilder key = new StringBuilder().append(heap.pos).append(':').append(item.getClass().getName())
                .append(':').append(item.level()).append(':').append(item.cursed)
                .append(':').append(type(item.weaponEffect))
                .append(':').append(Arrays.toString(EquipmentAttribute.pack(item.attributes)));
        if (item instanceof Essence) {
            Essence essence = (Essence)item;
            key.append(':').append(essence.grade()).append(':').append(essence.bonus()).append(':').append(essence.skill());
        }
        if (item instanceof Weapon) key.append(':').append(type(((Weapon)item).enchantment()));
        if (item instanceof Armor) key.append(':').append(((Armor)item).prefix).append(':').append(type(((Armor)item).glyph));
        return key.toString();
    }

    private void rememberFloor() {
        if (level == null || session != run) return;
        HashMap<String, Integer> items = new HashMap<>();
        for (Heap heap : level.heaps.values()) {
            for (Item item : heap.items) {
                if (!individualCard(item) || item.helperCardRun != run) continue;
                String key = floorItemKey(heap, item);
                Integer count = items.get(key);
                items.put(key, count == null ? 1 : count + 1);
            }
        }
        shownFloorItems.put(floor, items);
        HashSet<Integer> champions = new HashSet<>();
        for (Mob mob : level.mobs) {
            if (mob.champ > 0 && mob.helperCardRun == run) champions.add(mob.id());
        }
        shownFloorChampions.put(floor, champions);
    }

    private void restoreFloorHistory() {
        if (level == null) return;
        HashSet<Integer> champions = shownFloorChampions.get(floor);
        if (champions != null) {
            for (Mob mob : level.mobs) {
                if (mob.champ > 0 && champions.contains(mob.id())) mob.helperCardRun = run;
            }
        }
        HashMap<String, Integer> remembered = shownFloorItems.get(floor);
        if (remembered == null) return;
        HashMap<String, Integer> remaining = new HashMap<>(remembered);
        for (Heap heap : level.heaps.values()) {
            for (Item item : heap.items) {
                if (!individualCard(item)) continue;
                String key = floorItemKey(heap, item);
                Integer count = remaining.get(key);
                if (count == null || count == 0) continue;
                item.helperCardRun = run;
                remaining.put(key, count - 1);
            }
        }
    }

    private void clear() {
        rememberFloor();
        for (Pile pile : piles.values()) pile.destroy();
        piles.clear();
        quiet.clear();
        mobsSeen.clear();
        mobTypesSeen.clear();
        terrainSeen = new boolean[0];
        plantsSeen = new Class<?>[0];
        terrainScan = 0;
    }

    @Override
    public void destroy() {
        clear();
        level = null;
        super.destroy();
    }

    private static final class Seen {
        Item item;
        int quantity;
        int afterDeath;

        Seen(Item item) {
            this.item = item;
        }
    }

    private final class Pile {
        Heap heap;
        Mob mob;
        Plant plant;
        int pos;
        final int terrain;
        final Heap.Type chest;
        final ArrayList<Seen> items = new ArrayList<>();
        final ArrayList<Row> rows = new ArrayList<>();
        final RectF anchor = new RectF(), bounds = new RectF();
        final ColorBlock line = new ColorBlock(1, .5f, 0xFFACA593);
        final ColorBlock background = new ColorBlock(1, 1, 0xFF171A20);
        final RebornChrome frame = new RebornChrome(Chrome.Type.TOAST)
                .material(RebornChrome.Material.IRON).outlineOnly();
        boolean onScreen, placed;
        int direction;
        float shownWidth;

        Pile(Heap heap) {
            this.heap = heap;
            pos = heap.pos;
            terrain = -1;
            chest = chestType(heap);
            if (chest != null) rows.add(new Row(chest));
            line.camera = background.camera = frame.camera = camera;
        }

        Pile(int cell) {
            pos = cell;
            terrain = level.map[cell];
            chest = null;
            rows.add(new Row(cell));
            line.camera = background.camera = frame.camera = camera;
        }

        Pile(Mob mob) {
            this.mob = mob;
            pos = mob.pos;
            terrain = -1;
            chest = null;
            rows.add(new Row(mob));
            line.camera = background.camera = frame.camera = camera;
        }

        Pile(Plant plant) {
            this.plant = plant;
            pos = plant.pos;
            terrain = -1;
            chest = null;
            rows.add(new Row(plant));
            line.camera = background.camera = frame.camera = camera;
        }

        Seen find(Item item) {
            for (Seen seen : items) if (seen.item == item) return seen;
            return null;
        }

        int activeRows() {
            int count = 0;
            for (Row row : rows) if (row.age >= 0) count++;
            return count;
        }

        float width() {
            float width = 42;
            for (Row row : rows) if (row.age >= 0) width = Math.max(width, row.width());
            return width;
        }

        float fullHeight() {
            float height = 10;
            for (Row row : rows) if (row.age >= 0) height += row.height();
            return height;
        }

        void draw() {
            float height = 10, opacity = 0;
            for (Row row : rows) {
                if (row.age < 0) continue;
                height += row.height() * row.expansion();
                opacity = Math.max(opacity, row.life());
            }
            float width = shownWidth;
            float x = direction == 1 || direction == 3 || direction == 5 ? bounds.right - width
                    : direction >= 6 ? (bounds.left + bounds.right - width) / 2 : bounds.left;
            float y = direction <= 1 || direction == 6 ? bounds.bottom - height
                    : direction == 4 || direction == 5 ? (bounds.top + bounds.bottom - height) / 2 : bounds.top;
            float toX = clamp((anchor.left + anchor.right) / 2, x, x + width);
            float toY = clamp((anchor.top + anchor.bottom) / 2, y, y + height);
            float fromX = clamp(toX, anchor.left, anchor.right);
            float fromY = clamp(toY, anchor.top, anchor.bottom);
            toX = clamp(fromX, x, x + width);
            toY = clamp(fromY, y, y + height);
            float dx = toX - fromX, dy = toY - fromY;
            line.x = fromX;
            line.y = fromY;
            line.size((float)Math.hypot(dx, dy) * opacity, .5f);
            line.angle = (float)Math.toDegrees(Math.atan2(dy, dx));
            line.alpha(.7f * opacity);
            line.draw();
            background.x = x + 3;
            background.y = y + 3;
            background.size(width - 6, height - 6);
            background.alpha(.8f * opacity);
            background.draw();
            frame.x = x;
            frame.y = y;
            frame.size(width, height);
            frame.alpha(.9f * opacity);
            frame.draw();
            float rowY = y + 5;
            for (Row row : rows) {
                if (row.age < 0) continue;
                float alpha = row.age < OPEN ? smooth((row.age - .12f) / (OPEN - .12f))
                        : 1 - smooth((row.age - OPEN - HOLD) / (CLOSE / 2));
                if (row.expansion() >= .999f && alpha > 0) {
                    row.icon.x = x + 7 + (16 - row.icon.width()) / 2;
                    row.icon.y = rowY + (row.headerHeight() - row.icon.height()) / 2;
                    row.icon.alpha(alpha);
                    row.icon.draw();
                    row.title.setPos(x + 27, rowY + (row.headerHeight() - row.title.height()) / 2);
                    row.title.alpha(alpha);
                    row.title.draw();
                    row.separator.x = x + 7;
                    row.separator.y = rowY + row.headerHeight() + 2;
                    row.separator.size(width - 14, .35f);
                    row.separator.alpha(.25f * alpha);
                    row.separator.draw();
                    row.text.setPos(x + 7, rowY + row.headerHeight() + 6);
                    row.text.alpha(alpha);
                    row.text.draw();
                }
                rowY += row.height() * row.expansion();
            }
        }

        void destroy() {
            for (Row row : rows) row.destroy();
            line.destroy();
            frame.destroy();
            background.destroy();
        }
    }

    private final class Row {
        final Seen seen;
        final int count;
        final int cell;
        final Heap.Type chest;
        Mob mob;
        Plant plant;
        float age = -1;
        Image icon;
        RenderedTextMultiline title;
        RenderedTextMultiline text;
        ColorBlock separator;
        int revision;
        String shownCaption;

        Row(Seen seen, int count) {
            this.seen = seen;
            this.count = count;
            cell = -1;
            chest = null;
        }

        Row(int cell) {
            this.cell = cell;
            seen = null;
            count = 0;
            chest = null;
        }

        Row(Mob mob) {
            this.mob = mob;
            seen = null;
            count = 0;
            cell = -1;
            chest = null;
        }

        Row(Plant plant) {
            this.plant = plant;
            seen = null;
            count = 0;
            cell = plant.pos;
            chest = null;
        }

        Row(Heap.Type chest) {
            this.chest = chest;
            seen = null;
            count = 0;
            cell = -1;
        }

        String kind() {
            if (seen != null) return itemKind(seen.item);
            if (chest != null) return chestKind(chest);
            if (plant != null) return plantKind(plant);
            if (mob != null) return mobKind(mob);
            return terrainKind(cell);
        }

        boolean suppressed() {
            if (seen != null) return ItemDropCards.this.suppressed(seen.item);
            if (mob != null) return ItemDropCards.this.suppressed(mob);
            return ItemDropCards.this.suppressed(kind());
        }

        void prepare() {
            if (icon != null) return;
            if (seen != null) {
                icon = new ItemSprite(seen.item);
            } else if (chest != null) {
                icon = new ItemSprite(chest == Heap.Type.CHEST ? ItemSpriteSheet.CHEST
                        : chest == Heap.Type.CRYSTAL_CHEST ? ItemSpriteSheet.CRYSTAL_CHEST : ItemSpriteSheet.LOCKED_CHEST, null);
            } else if (plant != null) {
                icon = new PlantSprite(plant.image);
            } else if (mob != null) {
                icon = new Image(mob.sprite);
                icon.scale.set(Math.min(1, 16 / Math.max(icon.width(), icon.height())));
            } else {
                icon = Difficulties.is3d ? DungeonTerrainTilemap.tile(cell, level.map[cell])
                        : DungeonTilemapOld.tile(level.map[cell]);
            }
            icon.camera = camera;
            title = PixelScene.renderMultiline("", 6);
            title.hardlight(Window.titleColor());
            title.camera = camera;
            text = PixelScene.renderMultiline("", 6);
            text.setHighlightColor(Window.titleColor());
            text.camera = camera;
            separator = new ColorBlock(1, .35f, 0xFFACA593);
            separator.camera = camera;
            refresh();
        }

        void refresh() {
            if (seen != null) ((ItemSprite)icon).view(seen.item);
            int maxWidth = Math.max(36, Math.min(100, camera.width - 56));
            title.maxWidth(maxWidth);
            String caption = seen != null ? caption(seen.item, count)
                    : chest != null ? Messages.get("items.heap." + chest.name().toLowerCase(Locale.ROOT)) + "\n"
                    + Messages.get("ui.itemdropcards." + (chest == Heap.Type.CHEST ? "chest" : "locked_chest"))
                    : plant != null ? plant.displayName() + "\n" + Messages.get(plant, "card")
                    : mob != null ? caption(mob)
                    : caption(cell);
            if (!caption.equals(shownCaption)) {
                int titleEnd = caption.indexOf('\n');
                if (titleEnd < 0) titleEnd = caption.length();
                title.text(wrap(caption.substring(0, titleEnd).replace("_", ""), maxWidth), false);
                text.maxWidth(maxWidth + 20);
                text.text(wrap(caption.substring(titleEnd).trim(), maxWidth + 20), false);
                shownCaption = caption;
            }
            revision = ItemStatusHandler.revision();
        }

        float headerHeight() { return Math.max(16, title.height()); }
        float height() { return headerHeight() + text.height() + 11; }
        float width() { return Math.max(title.width() + 20, text.width()) + 14; }
        float life() { return age < OPEN ? smooth(age / OPEN) : 1 - smooth((age - OPEN - HOLD) / CLOSE); }
        float expansion() {
            return age < OPEN ? smooth(age / .12f) : 1 - smooth((age - OPEN - HOLD - CLOSE / 2) / (CLOSE / 2));
        }

        void destroy() {
            if (icon != null) icon.destroy();
            if (title != null) title.destroy();
            if (text != null) text.destroy();
            if (separator != null) separator.destroy();
        }
    }

    private static String caption(Item item, int count) {
        String unknown = unknownItemKind(item);
        String name = unknown == null ? Message.capitalize(Message.itemName(item)).toString()
                : Messages.get("ui.itemdropcards." + unknown);
        if (item.levelKnown && item.level() != 0) name += (item.level() > 0 ? " +" : " ") + item.level();
        if (count > 1) name += " x " + count;
        if (item instanceof Essence) return name + "\n" + ((Essence)item).cardInfo();
        if (item.isIdentified()) {
            if (item instanceof Weapon && !(item instanceof Bow)) {
                Weapon weapon = (Weapon)item;
                name += "\n" + Messages.get("attributes.weapon_card", weapon.min(), weapon.max(), weapon.STR());
            } else if (item instanceof Armor) {
                Armor armor = (Armor)item;
                name += "\n" + Messages.get("attributes.armor_card", armor.DR(), armor.STR);
            }
        }
        if (!item.isIdentified() || item instanceof Bow || !(item instanceof Weapon || item instanceof Armor)) {
            String description = unknown != null ? Messages.get("ui.itemdropcards.unknown_effect")
                    : Messages.get(item, "card_desc");
            if (Messages.NO_TEXT_FOUND.equals(description)) {
                description = Message.description(item).toString().replace("_", "").trim();
                int end = 0;
                while (end < description.length() && ".!?\u3002\uff01\uff1f\n".indexOf(description.charAt(end)) < 0) end++;
                description = description.substring(0, Math.min(description.length(), end + 1));
                if (description.codePointCount(0, description.length()) > 120) {
                    int limit = description.offsetByCodePoints(0, 117);
                    int space = description.lastIndexOf(' ', limit);
                    description = description.substring(0, space > limit - 15 ? space : limit).trim() + "\u2026";
                }
            }
            if (!description.isEmpty()) name += "\n" + description;
        }
        ArrayList<String> effects = new ArrayList<>();
        if (item.visiblyCursed()) effects.add("_" + Messages.get("library.cursed") + "_");
        if (item.isIdentified()) {
            for (String modifier : LibraryItemBuild.modifierKeys(item)) {
                String description = modifier.startsWith("enchantment_") || modifier.startsWith("glyph_")
                        ? Messages.get("library." + modifier + "_desc")
                        : Messages.get("ui.itemdropcards." + modifier.replace(':', '_').toLowerCase(Locale.ROOT));
                effects.add("_" + description.replace("_", "") + "_");
            }
        }
        for (EquipmentAttribute attribute : item.attributes) {
            effects.add(item.isIdentified() ? "_" + attribute.description() + "_"
                    : Messages.get("attributes.unidentified"));
        }
        for (String effect : effects) name += "\n" + (effects.size() > 1 ? "- " : "") + effect;
        return name;
    }

    private static String unknownItemKind(Item item) {
        return item instanceof Potion && !((Potion)item).isKnown() ? "potion"
                : item instanceof Scroll && !((Scroll)item).isKnown() ? "scroll"
                : item instanceof Ring && !((Ring)item).isKnown() ? "ring"
                : item instanceof Wand && !((Wand)item).isKnown() ? "wand" : null;
    }

    private static String caption(Mob mob) {
        String name = Messages.capitalize(mob.displayName());
        String hint = MobCardText.peopleHint(mob);
        return name + "\n" + (hint == null ? MobCardText.description(mob) : Messages.get("ui.itemdropcards." + hint));
    }

    private String caption(int cell) {
        if (level instanceof DailyLevel) {
            DailyLevel daily = (DailyLevel)level;
            String name = daily.doorName(cell);
            if (name != null) return name + "\n" + daily.doorDescription(cell);
        }
        return level.tileName(level.map[cell]) + "\n" + Messages.get("ui.itemdropcards." + terrainHint(cell));
    }

    private String terrainHint(int cell) {
        switch (level.map[cell]) {
            case Terrain.LOCKED_DOOR: return "locked_door";
            case Terrain.LOCKED_EXIT: return "locked_exit";
            case Terrain.ENTRANCE: return "stairs_up";
            case Terrain.EXIT:
            case Terrain.UNLOCKED_EXIT: return "stairs_down";
            case Terrain.ALCHEMY: return "alchemy";
            case Terrain.STORAGE: return "storage";
            case Terrain.BARRICADE: return "barricade";
            case Terrain.SIGN: return "sign";
            case Terrain.TOXIC_TRAP:
            case Terrain.FIRE_TRAP:
            case Terrain.PARALYTIC_TRAP:
            case Terrain.POISON_TRAP:
            case Terrain.ALARM_TRAP:
            case Terrain.LIGHTNING_TRAP:
            case Terrain.GRIPPING_TRAP:
            case Terrain.SUMMONING_TRAP: return "trap";
            case Terrain.WELL:
                for (Blob blob : level.blobs.values()) {
                    if (blob.cur == null || cell >= blob.cur.length || blob.cur[cell] <= 0) continue;
                    if (blob instanceof WaterOfHealth) return "healing_well";
                    if (blob instanceof WaterOfAwareness) return "awareness_well";
                    if (blob instanceof WaterOfTransmutation) return "transmutation_well";
                }
                return null;
            default: return null;
        }
    }

    private static String wrap(String value, int width) {
        if (value.indexOf('\n') >= 0) {
            StringBuilder lines = new StringBuilder();
            for (String line : value.split("\n", -1)) {
                if (lines.length() > 0) lines.append('\n');
                lines.append(wrap(line, width));
            }
            return lines.toString();
        }
        BitmapText measure = PixelScene.createText(6);
        StringBuilder result = new StringBuilder();
        try {
            for (String word : value.split(" ")) {
                if (result.length() > 0) result.append(' ');
                measure.text(word.replace("_", ""), false);
                measure.measure();
                if (measure.width() <= width) {
                    result.append(word);
                    continue;
                }
                StringBuilder line = new StringBuilder();
                for (int offset = 0; offset < word.length();) {
                    int code = word.codePointAt(offset);
                    offset += Character.charCount(code);
                    String next = new String(Character.toChars(code));
                    measure.text((line.toString() + next).replace("_", ""), false);
                    measure.measure();
                    if (measure.width() > width && line.length() > 0) {
                        result.append(line).append('\n');
                        line.setLength(0);
                    }
                    line.append(next);
                }
                result.append(line);
            }
            return result.toString();
        } finally {
            measure.destroy();
        }
    }

    private static float clamp(float value, float low, float high) {
        return Math.max(low, Math.min(high, value));
    }

    private static float smooth(float value) {
        float t = clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }
}
