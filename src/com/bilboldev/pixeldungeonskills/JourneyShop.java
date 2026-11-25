package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.armor.ClothArmor;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.armor.MailArmor;
import com.bilboldev.pixeldungeonskills.items.armor.PlateArmor;
import com.bilboldev.pixeldungeonskills.items.armor.ScaleArmor;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfInvisibility;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfLevitation;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfPurity;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfAccuracy;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfElements;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfEvasion;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfMagicMapping;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfRecharging;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfRemoveCurse;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfTeleportation;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfFirebolt;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfLightning;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfPoison;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.BattleAxe;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Dagger;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Glaive;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Knuckles;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Longsword;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Mace;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Quarterstaff;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.ShortSword;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Spear;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Sword;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.WarHammer;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JourneyShop {
    public static final class Entry {
        private final Item item;
        private final int price, slot;
        private boolean held;

        private Entry(Item item, int price, int slot) {
            this.item = item;
            this.price = price;
            this.slot = slot;
        }

        public Item item() { return item; }
        public int price() { return price; }
        public int slot() { return slot; }
        public boolean held() { return held; }
    }

    private final JourneyRoute route;
    private final ArrayList<Entry> stock = new ArrayList<>();
    private final boolean[] cleared;
    private JourneyRoute.Node highest;
    private int capacity = 6;

    JourneyShop(JourneyRoute route) {
        this.route = route;
        cleared = new boolean[route.nodes.size()];
        highest = route.node(0);
    }

    public List<Entry> stock() {
        ArrayList<Entry> offers = new ArrayList<>();
        for (Entry entry : stock) if (entry != null) offers.add(entry);
        return Collections.unmodifiableList(offers);
    }

    public int capacity() { return capacity; }

    void catchUp(JourneyRoute route) {
        if (cleared[0]) return;
        refresh(0);
        cleared[0] = true;
        for (JourneyRoute.Node node : route.nodes) if (node.completed) clear(node);
    }

    boolean clear(JourneyRoute.Node node) {
        if (node.id == 0 || cleared[node.id]) return false;
        if (node.layer > highest.layer || node.layer == highest.layer && node.depth > highest.depth) highest = node;
        for (JourneyRoute.Node completed : route.nodes)
            if (completed.completed && (completed.layer > highest.layer
                    || completed.layer == highest.layer && completed.depth > highest.depth)) highest = completed;
        if (node.restock) capacity += 4;
        refresh(node.id);
        cleared[node.id] = true;
        return true;
    }

    private void refresh(int node) {
        int held = 0, size = capacity;
        for (Entry entry : stock) if (entry != null && entry.held) {
            held++;
            size = Math.max(size, entry.slot + 1);
        }
        while (stock.size() > size) stock.remove(stock.size() - 1);
        while (stock.size() < size) stock.add(null);
        int remaining = Math.max(0, capacity - held);
        int depth = Dungeon.depth;
        try (Random.Scope random = Random.use(route.seed ^ 0x6E27C4A190BD835FL ^ node);
             Generator.Scope loot = Generator.isolate()) {
            Dungeon.depth = highest.depth;
            for (int i = 0; i < stock.size(); i++) {
                Entry entry = stock.get(i);
                if (entry != null && entry.held) continue;
                if (remaining-- <= 0) {
                    stock.set(i, null);
                    continue;
                }
                Item item = offer(i, node == 0);
                EquipmentAttribute.roll(item, true);
                item.randomPrefix();
                item.identify();
                stock.set(i, new Entry(item, item.price() * (3 + highest.biome.ordinal()), i));
            }
        } finally {
            Dungeon.depth = depth;
        }
    }

    private Item offer(int slot, boolean initial) {
        switch (slot) {
            case 0: return weapon(highest.biome);
            case 1: return armor(highest.biome);
            case 2: return new PotionOfHealing();
            case 3: return new PotionOfMana();
            case 4:
                if (initial) return new ScrollOfRemoveCurse();
                if (highest.biome == JourneyRoute.Biome.SEWERS || Random.Int(2) == 0) return utility();
                return Random.oneOf(new WandOfFirebolt(), new WandOfLightning(), new WandOfPoison(),
                        new RingOfAccuracy(), new RingOfEvasion(), new RingOfElements());
            case 5: return Generator.random(Generator.Category.ESSENCE);
            default:
                switch ((slot - 6) % 4) {
                    case 0: return new PotionOfHealing();
                    case 1: return new PotionOfMana();
                    case 2: return Generator.random(Generator.Category.ESSENCE);
                    default: return equipment(highest.biome);
                }
        }
    }

    private Item utility() {
        return Random.oneOf(new PotionOfInvisibility(), new PotionOfLevitation(), new PotionOfPurity(),
                new ScrollOfRemoveCurse(), new ScrollOfRecharging(), new ScrollOfTeleportation(), new ScrollOfMagicMapping());
    }

    private Item equipment(JourneyRoute.Biome biome) {
        switch (biome) {
            case SEWERS: return Random.oneOf(new ShortSword(), new Spear(), new ClothArmor());
            case PRISON: return Random.oneOf(new Quarterstaff(), new Spear(), new LeatherArmor());
            case CAVES: return Random.oneOf(new Sword(), new Mace(), new MailArmor());
            case CITY: return Random.oneOf(new Longsword(), new BattleAxe(), new ScaleArmor());
            case HALLS: return Random.oneOf(new Glaive(), new WarHammer(), new PlateArmor());
            default: throw new IllegalArgumentException("Invalid Journey biome");
        }
    }

    private Item weapon(JourneyRoute.Biome biome) {
        switch (biome) {
            case SEWERS: return Random.oneOf(new Dagger(), new Knuckles(), new ShortSword(), new Spear());
            case PRISON: return Random.oneOf(new Quarterstaff(), new Spear());
            case CAVES: return Random.oneOf(new Sword(), new Mace());
            case CITY: return Random.oneOf(new Longsword(), new BattleAxe());
            case HALLS: return Random.oneOf(new Glaive(), new WarHammer());
            default: throw new IllegalArgumentException("Invalid Journey biome");
        }
    }

    private Item armor(JourneyRoute.Biome biome) {
        switch (biome) {
            case SEWERS: return new ClothArmor();
            case PRISON: return new LeatherArmor();
            case CAVES: return new MailArmor();
            case CITY: return new ScaleArmor();
            case HALLS: return new PlateArmor();
            default: throw new IllegalArgumentException("Invalid Journey biome");
        }
    }

    int indexOf(Entry entry) { return stock.indexOf(entry); }
    void remove(int index) { stock.set(index, null); }
    void restore(int index, Entry entry) { stock.set(index, entry); }
    void hold(Entry entry, boolean held) { entry.held = held; }

    JourneyShop copy() {
        JourneyShop copy = new JourneyShop(route);
        copy.stock.addAll(stock);
        System.arraycopy(cleared, 0, copy.cleared, 0, cleared.length);
        copy.highest = highest;
        copy.capacity = capacity;
        return copy;
    }

    void store(Bundle bundle) {
        bundle.put("capacity", capacity);
        bundle.put("cleared", cleared);
        bundle.put("size", stock.size());
        for (int i = 0; i < stock.size(); i++) {
            Entry entry = stock.get(i);
            if (entry == null) continue;
            Bundle offer = new Bundle();
            offer.put("item", entry.item);
            offer.put("price", entry.price);
            offer.put("held", entry.held);
            bundle.put("entry_" + i, offer);
        }
    }

    static JourneyShop restore(Bundle bundle, JourneyRoute route) throws IOException {
        JourneyShop shop = new JourneyShop(route);
        boolean[] cleared = bundle.getBooleanArray("cleared");
        int size = bundle.getInt("size");
        int bonuses = 0;
        for (JourneyRoute.Node node : route.nodes) if (node.restock) bonuses++;
        if (cleared == null || cleared.length != shop.cleared.length || size < 0
                || size > route.nodes.size() + 29 + 4 * bonuses)
            throw new IOException("Invalid Journey shop progress");
        System.arraycopy(cleared, 0, shop.cleared, 0, cleared.length);
        shop.cleared[0] = true;
        for (JourneyRoute.Node node : route.nodes) if (node.id > 0 && cleared[node.id]) {
            if (!node.completed) throw new IOException("Invalid Journey shop progress");
            if (node.restock) shop.capacity += 4;
        }
        for (JourneyRoute.Node node : route.nodes)
            if (node.completed && (node.layer > shop.highest.layer
                    || node.layer == shop.highest.layer && node.depth > shop.highest.depth)) shop.highest = node;
        boolean legacy = !bundle.contains("capacity");
        if (legacy) {
            int regions = bundle.getInt("regions");
            if (regions < 1 || regions >= 1 << JourneyRoute.Biome.values().length)
                throw new IOException("Invalid Journey shop progress");
        } else if (bundle.getInt("capacity") != shop.capacity || size < shop.capacity) {
            throw new IOException("Invalid Journey shop capacity");
        }
        for (int i = 0; i < size; i++) {
            if (!bundle.contains("entry_" + i)) {
                if (legacy) throw new IOException("Missing Journey shop item");
                shop.stock.add(null);
                continue;
            }
            Bundle offer = bundle.getBundle("entry_" + i);
            Item item = (Item)offer.get("item");
            int price = offer.getInt("price");
            if (item == null || item.quantity() != 1 || price <= 0)
                throw new IOException("Invalid Journey shop item");
            Entry entry = new Entry(item, price, i);
            entry.held = offer.getBoolean("held");
            shop.stock.add(entry);
        }
        while (shop.stock.size() < shop.capacity) shop.stock.add(null);
        return shop;
    }
}
