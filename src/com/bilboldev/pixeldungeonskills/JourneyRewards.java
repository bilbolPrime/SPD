package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.armor.ArmorPrefix;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.armor.MailArmor;
import com.bilboldev.pixeldungeonskills.items.armor.PlateArmor;
import com.bilboldev.pixeldungeonskills.items.armor.ScaleArmor;
import com.bilboldev.pixeldungeonskills.items.essences.Essence;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfStrength;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.WeaponEffect;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.BattleAxe;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Glaive;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Longsword;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Mace;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Quarterstaff;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Spear;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Sword;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.WarHammer;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.RebornDungeonBuilder;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

final class JourneyRewards {
    private static final long SEED = 0x4179656D6C6F6F74L;
    private final JourneyRoute route;
    private final Item[] items;
    private final boolean[] placed;

    private JourneyRewards(JourneyRoute route) {
        this.route = route;
        items = new Item[route.nodes.size()];
        placed = new boolean[items.length];
    }

    static JourneyRewards create(JourneyRoute route) {
        JourneyRewards rewards = new JourneyRewards(route);
        int depth = Dungeon.depth;
        java.util.Random placement = new java.util.Random(route.seed ^ SEED);
        try (Random.Scope random = Random.use(route.seed ^ ~SEED); Generator.Scope loot = Generator.isolate()) {
            for (JourneyRoute.Biome biome : JourneyRoute.Biome.values()) {
                ArrayList<JourneyRoute.Node> ordinary = new ArrayList<>(), special = new ArrayList<>();
                for (JourneyRoute.Node node : route.nodes) if (node.id > 0 && node.biome == biome && !node.gate && !node.finish) {
                    (node.objective == JourneyRoute.Objective.NONE ? ordinary : special).add(node);
                }
                Collections.shuffle(ordinary, placement);
                Collections.shuffle(special, placement);
                ordinary.addAll(special);
                int index = 0;
                for (int i = 0; i < (biome == JourneyRoute.Biome.HALLS ? 1 : 2); i++) {
                    Item potion = new PotionOfStrength().identify();
                    potion.levelKnown = potion.cursedKnown = true;
                    rewards.items[ordinary.get(index++).id] = potion;
                }
                JourneyRoute.Node node = ordinary.get(index);
                Dungeon.depth = node.depth(node.floors - 1);
                rewards.items[node.id] = equipment(biome.ordinal());
            }
            ArrayList<JourneyRoute.Node> ordinary = new ArrayList<>(), special = new ArrayList<>();
            for (JourneyRoute.Node node : route.nodes) if (node.id > 0 && !node.gate && !node.finish && rewards.items[node.id] == null)
                (node.objective == JourneyRoute.Objective.NONE ? ordinary : special).add(node);
            Collections.shuffle(ordinary, placement);
            Collections.shuffle(special, placement);
            ordinary.addAll(special);
            for (int i = 0; i < 5; i++) {
                JourneyRoute.Node node = ordinary.get(i);
                Dungeon.depth = node.depth(node.floors - 1);
                try {
                    rewards.items[node.id] = Random.element(Essence.Type.values()).itemClass.newInstance()
                            .maximumRoll(Math.min(Essence.MAX_GRADE, (node.layer + 1) / 2));
                } catch (ReflectiveOperationException failure) {
                    throw new IllegalStateException("Unable to create Journey Essence", failure);
                }
            }
        } finally {
            Dungeon.depth = depth;
        }
        return rewards;
    }

    private static Item equipment(int region) {
        Item item;
        boolean armor = Random.Int(2) == 0;
        switch (region) {
            case 0: item = armor ? new LeatherArmor() : Random.oneOf(new Quarterstaff(), new Spear()); break;
            case 1: item = armor ? new MailArmor() : Random.oneOf(new Sword(), new Mace()); break;
            case 2: item = armor ? new ScaleArmor() : Random.oneOf(new Longsword(), new BattleAxe()); break;
            default: item = armor ? new PlateArmor() : Random.oneOf(new Glaive(), new WarHammer()); break;
        }
        item.upgrade(region + 1);
        if (item instanceof Armor) {
            Armor piece = (Armor)item;
            piece.prefix = Random.element(ArmorPrefix.values());
            piece.inscribe();
        } else {
            item.weaponEffect = WeaponEffect.random(item);
            ((Weapon)item).enchant();
        }
        EquipmentAttribute.rollMaximum(item);
        item.levelKnown = item.cursedKnown = true;
        item.cursed = false;
        return item;
    }

    Item item(int node) { return items[node]; }

    void place(int node, int floor, Level level) {
        if (items[node] == null || placed[node] || floor != route.node(node).floors - 1) return;
        int[] fromEntrance = RebornDungeonBuilder.distances(level, level.entrance);
        int[] fromExit = RebornDungeonBuilder.distances(level, level.exit);
        ArrayList<Integer> cells = new ArrayList<>();

        int clearance = 0;
        for (int cell = 0; cell < Level.LENGTH; cell++) {
            if (cell == level.entrance || cell == level.exit || fromEntrance[cell] < 0 || fromExit[cell] < 1
                    || !RebornDungeonBuilder.walkable(level, cell)
                    || (Terrain.flags[level.map[cell]] & (Terrain.SECRET | Terrain.LOS_BLOCKING)) != 0
                    || level.heaps.get(cell) != null || level.plants.get(cell) != null) continue;
            boolean occupied = false;
            for (Mob mob : level.mobs) if (mob.pos == cell) occupied = true;
            if (occupied) continue;
            int distance = Math.min(3, Math.min(Level.distance(cell, level.entrance), Level.distance(cell, level.exit)));
            if (distance > clearance) {
                clearance = distance;
                cells.clear();
            }
            if (distance == clearance) cells.add(cell);
        }
        if (cells.isEmpty()) throw new IllegalStateException("No room for Journey reward");
        java.util.Random random = new java.util.Random(route.seed ^ SEED ^ node);
        Heap heap = new Heap();
        heap.pos = cells.get(random.nextInt(cells.size()));
        try (Random.Scope ignored = Random.use(route.seed ^ ~SEED ^ node)) {
            Bundle copy = new Bundle();
            copy.put("item", items[node]);
            heap.items.add((Item)copy.get("item"));
        }
        heap.revealed = true;
        level.heaps.put(heap.pos, heap);
        level.mapped[heap.pos] = true;
        placed[node] = true;
    }

    void validate(int node, JourneyExpedition expedition) throws IOException {
        if (items[node] != null && (expedition.levels[route.node(node).floors - 1] != null) != placed[node])
            throw new IOException("Invalid Journey reward placement");
    }

    void store(Bundle bundle) {
        ArrayList<Integer> nodes = new ArrayList<>();
        for (int node = 1; node < items.length; node++) if (items[node] != null) {
            nodes.add(node);
            Bundle reward = new Bundle();
            reward.put("item", items[node]);
            reward.put("placed", placed[node]);
            bundle.put("node_" + node, reward);
        }
        int[] ids = new int[nodes.size()];
        for (int i = 0; i < ids.length; i++) ids[i] = nodes.get(i);
        bundle.put("nodes", ids);
    }

    static JourneyRewards restore(JourneyRoute route, Bundle bundle) throws IOException {
        JourneyRewards rewards = new JourneyRewards(route);
        int[] nodes = bundle.getIntArray("nodes");
        if (!bundle.contains("nodes") || nodes == null || nodes.length >= route.nodes.size()) throw new IOException("Invalid Journey rewards");
        HashSet<Integer> used = new HashSet<>();
        try (Random.Scope ignored = Random.use(route.seed ^ ~SEED)) {
            for (int id : nodes) {
                if (id <= 0 || id >= rewards.items.length || !used.add(id)) throw new IOException("Invalid Journey reward node");
                JourneyRoute.Node node = route.node(id);
                if (node.gate || node.finish) throw new IOException("Journey reward on a boss");
                Bundle saved = bundle.getBundle("node_" + id);
                Item item = (Item)saved.get("item");
                if (item == null || item.quantity() != 1 || item.cursed || !item.isIdentified()
                        || !item.levelKnown || !item.cursedKnown || item.journeyRemains
                        || !(item.getClass() == PotionOfStrength.class || item instanceof Essence || item instanceof Armor || item instanceof MeleeWeapon))
                    throw new IOException("Invalid Journey reward item");
                rewards.items[id] = item;
                rewards.placed[id] = saved.getBoolean("placed");
            }
        }
        return rewards;
    }
}
