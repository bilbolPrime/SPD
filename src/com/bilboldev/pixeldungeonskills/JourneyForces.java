package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.mobs.Bat;
import com.bilboldev.pixeldungeonskills.actors.mobs.Brute;
import com.bilboldev.pixeldungeonskills.actors.mobs.Crab;
import com.bilboldev.pixeldungeonskills.actors.mobs.Elemental;
import com.bilboldev.pixeldungeonskills.actors.mobs.Eye;
import com.bilboldev.pixeldungeonskills.actors.mobs.Gnoll;
import com.bilboldev.pixeldungeonskills.actors.mobs.Golem;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Monk;
import com.bilboldev.pixeldungeonskills.actors.mobs.Rat;
import com.bilboldev.pixeldungeonskills.actors.mobs.Scorpio;
import com.bilboldev.pixeldungeonskills.actors.mobs.Shaman;
import com.bilboldev.pixeldungeonskills.actors.mobs.Skeleton;
import com.bilboldev.pixeldungeonskills.actors.mobs.Spinner;
import com.bilboldev.pixeldungeonskills.actors.mobs.Succubus;
import com.bilboldev.pixeldungeonskills.actors.mobs.Thief;
import com.bilboldev.pixeldungeonskills.actors.mobs.Warlock;
import com.bilboldev.pixeldungeonskills.sprites.BatSprite;
import com.bilboldev.pixeldungeonskills.sprites.BruteSprite;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.sprites.CrabSprite;
import com.bilboldev.pixeldungeonskills.sprites.ElementalSprite;
import com.bilboldev.pixeldungeonskills.sprites.EyeSprite;
import com.bilboldev.pixeldungeonskills.sprites.GnollSprite;
import com.bilboldev.pixeldungeonskills.sprites.GolemSprite;
import com.bilboldev.pixeldungeonskills.sprites.MonkSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.ScorpioSprite;
import com.bilboldev.pixeldungeonskills.sprites.ShamanSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.SpinnerSprite;
import com.bilboldev.pixeldungeonskills.sprites.SuccubusSprite;
import com.bilboldev.pixeldungeonskills.sprites.ThiefSprite;
import com.bilboldev.pixeldungeonskills.sprites.WarlockSprite;
import com.bilboldev.utils.Bundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class JourneyForces {
    public enum Troop {
        RAT(JourneyRoute.Biome.SEWERS, Rat.class, RatSprite.class),
        GNOLL(JourneyRoute.Biome.SEWERS, Gnoll.class, GnollSprite.class),
        CRAB(JourneyRoute.Biome.SEWERS, Crab.class, CrabSprite.class),
        SKELETON(JourneyRoute.Biome.PRISON, Skeleton.class, SkeletonSprite.class),
        THIEF(JourneyRoute.Biome.PRISON, Thief.class, ThiefSprite.class),
        SHAMAN(JourneyRoute.Biome.PRISON, Shaman.class, ShamanSprite.class),
        BAT(JourneyRoute.Biome.CAVES, Bat.class, BatSprite.class),
        BRUTE(JourneyRoute.Biome.CAVES, Brute.class, BruteSprite.class),
        SPINNER(JourneyRoute.Biome.CAVES, Spinner.class, SpinnerSprite.class),
        ELEMENTAL(JourneyRoute.Biome.CITY, Elemental.class, ElementalSprite.class),
        MONK(JourneyRoute.Biome.CITY, Monk.class, MonkSprite.class),
        WARLOCK(JourneyRoute.Biome.CITY, Warlock.class, WarlockSprite.class),
        GOLEM(JourneyRoute.Biome.CITY, Golem.class, GolemSprite.class),
        SUCCUBUS(JourneyRoute.Biome.HALLS, Succubus.class, SuccubusSprite.class),
        EYE(JourneyRoute.Biome.HALLS, Eye.class, EyeSprite.class),
        SCORPIO(JourneyRoute.Biome.HALLS, Scorpio.class, ScorpioSprite.class);

        public final JourneyRoute.Biome biome;
        public final Class<? extends Mob> mobClass;
        public final Class<? extends CharSprite> spriteClass;

        Troop(JourneyRoute.Biome biome, Class<? extends Mob> mobClass, Class<? extends CharSprite> spriteClass) {
            this.biome = biome;
            this.mobClass = mobClass;
            this.spriteClass = spriteClass;
        }

        Mob create() {
            try {
                return mobClass.newInstance();
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Unable to create Journey reinforcement", failure);
            }
        }
    }

    public static final class Group {
        public final int id, source, node, previous, target;
        public final boolean traveling, settled;
        public final JourneyRoute.Biome biome;
        public final List<Troop> troops;

        private Group(int id, int source, int node, int previous, int target, boolean traveling,
                      JourneyRoute.Biome biome, List<Troop> troops) {
            this.id = id;
            this.source = source;
            this.node = node;
            this.previous = previous;
            this.target = target;
            this.traveling = traveling;
            settled = !traveling && node == target;
            this.biome = biome;
            this.troops = Collections.unmodifiableList(new ArrayList<>(troops));
        }
    }

    private final JourneyRoute route;
    private List<Group> groups = Collections.emptyList();
    private int advances, nextId = 1;

    JourneyForces(JourneyRoute route) {
        this.route = route;
    }

    List<Group> groups() { return groups; }

    JourneyForces copy() {
        JourneyForces copied = new JourneyForces(route);
        copied.groups = groups;
        copied.advances = advances;
        copied.nextId = nextId;
        return copied;
    }

    List<Group> at(int node) {
        ArrayList<Group> found = new ArrayList<>();
        for (Group group : groups) if (group.node == node) found.add(group);
        return found;
    }

    void remove(int[] ids) {
        HashSet<Integer> removed = new HashSet<>();
        for (int id : ids) removed.add(id);
        ArrayList<Group> remaining = new ArrayList<>();
        for (Group group : groups) if (!removed.contains(group.id)) remaining.add(group);
        groups = Collections.unmodifiableList(remaining);
    }

    void advance(Set<Integer> defeatedBosses) {
        java.util.Random random = new java.util.Random(route.seed ^ 0x623DE9A147B85CF0L * ++advances);
        HashSet<Integer> reserved = new HashSet<>();
        ArrayList<Group> moved = new ArrayList<>();
        ArrayList<Group> blocked = new ArrayList<>();
        HashSet<Integer> occupied = new HashSet<>();
        for (Group group : groups) {
            if (group.settled && route.node(group.node).completed) continue;
            if (!group.settled && route.node(group.previous).completed) {
                blocked.add(group);
                continue;
            }
            int node = group.traveling ? group.previous : group.node;
            boolean traveling = !group.traveling && node != group.target;
            int previous = traveling ? group.previous : -1;
            moved.add(new Group(group.id, group.source, node, previous, group.target, traveling, group.biome, group.troops));
            reserved.add(group.target);
            occupied.add(node);
        }
        for (Group group : blocked) {
            if (route.node(group.node).completed || occupied.contains(group.node) || !reserved.add(group.node)) continue;
            moved.add(new Group(group.id, group.source, group.node, -1, group.node, false, group.biome, group.troops));
            occupied.add(group.node);
        }
        int firstDispatch = nextId;
        for (JourneyRoute.Node source : route.nodes) if ((source.gate || source.finish)
                && !source.completed && !defeatedBosses.contains(source.id)) {
            int target = chooseTarget(source.id, reserved, random);
            if (target < 0) continue;
            reserved.add(target);
            ArrayList<Troop> pool = new ArrayList<>();
            for (Troop troop : Troop.values()) if (troop.biome == source.biome) pool.add(troop);
            Collections.shuffle(pool, random);
            int types = 1 + random.nextInt(Math.min(3, pool.size()));
            moved.add(new Group(nextId++, source.id, source.id, -1, target, true, source.biome, pool.subList(0, types)));
        }
        ArrayList<Group> assigned = new ArrayList<>();
        Map<Long, Integer> routes = new HashMap<>();
        Map<Integer, Integer> destinations = new HashMap<>();
        for (Group group : moved) {
            if (group.settled) destinations.put(group.node, destinations.getOrDefault(group.node, 0) + 1);
            else if (group.previous >= 0) {
                long edge = edge(group.node, group.previous);
                routes.put(edge, routes.getOrDefault(edge, 0) + 1);
                destinations.put(group.previous, destinations.getOrDefault(group.previous, 0) + 1);
            }
        }
        for (Group group : moved) {
            int previous = group.previous;
            if (!group.settled && previous < 0) {
                previous = choosePrevious(group.node, group.target, routes, destinations, random, group.id >= firstDispatch);
                if (previous < 0) continue;
                long edge = edge(group.node, previous);
                routes.put(edge, routes.getOrDefault(edge, 0) + 1);
                destinations.put(previous, destinations.getOrDefault(previous, 0) + 1);
            }
            assigned.add(new Group(group.id, group.source, group.node, previous, group.target,
                    group.traveling, group.biome, group.troops));
        }
        groups = Collections.unmodifiableList(assigned);
    }

    private int chooseTarget(int source, Set<Integer> reserved, java.util.Random random) {
        JourneyRoute.Node boss = route.node(source);
        int biome = Math.max(0, boss.biome.ordinal() - 1), nearest = -1;
        ArrayList<Integer> choices = new ArrayList<>();
        for (JourneyRoute.Node node : route.nodes) {
            if (node.id == 0 || node.completed || reserved.contains(node.id) || node.biome.ordinal() != biome
                    || node.layer >= boss.layer || !reachable(source, node.id, true)) continue;
            if (node.layer > nearest) {
                nearest = node.layer;
                choices.clear();
            }
            if (node.layer == nearest) choices.add(node.id);
        }
        return choices.isEmpty() ? -1 : choices.get(random.nextInt(choices.size()));
    }

    private int choosePrevious(int node, int target, Map<Long, Integer> routes,
                               Map<Integer, Integer> destinations, java.util.Random random, boolean uncleared) {
        ArrayList<Integer> choices = new ArrayList<>();
        int leastOnRoute = Integer.MAX_VALUE, leastAtNode = Integer.MAX_VALUE;
        for (int previous : predecessors(node)) {
            if (uncleared && route.node(previous).completed || !reachable(previous, target, uncleared)) continue;
            int onRoute = routes.getOrDefault(edge(node, previous), 0);
            int atNode = destinations.getOrDefault(previous, 0);
            if (onRoute < leastOnRoute || onRoute == leastOnRoute && atNode < leastAtNode) {
                choices.clear();
                leastOnRoute = onRoute;
                leastAtNode = atNode;
            }
            if (onRoute == leastOnRoute && atNode == leastAtNode) choices.add(previous);
        }
        return choices.isEmpty() ? -1 : choices.get(random.nextInt(choices.size()));
    }

    private ArrayList<Integer> predecessors(int node) {
        ArrayList<Integer> found = new ArrayList<>();
        for (JourneyRoute.Node previous : route.nodes) if (previous.id > 0)
            for (int next : previous.next) if (next == node) found.add(previous.id);
        return found;
    }

    private boolean reachable(int from, int target) {
        return reachable(from, target, false);
    }

    private boolean reachable(int from, int target, boolean uncleared) {
        if (uncleared && route.node(target).completed) return false;
        if (from == target) return true;
        if (route.node(target).layer >= route.node(from).layer) return false;
        boolean[] reached = new boolean[route.nodes.size()];
        reached[target] = true;
        for (JourneyRoute.Node node : route.nodes) if (reached[node.id] && (!uncleared || !node.completed))
            for (int next : node.next) reached[next] = true;
        return reached[from];
    }

    private static long edge(int node, int previous) {
        return (long)node << 32 | (previous & 0xFFFFFFFFL);
    }

    void store(Bundle bundle) {
        bundle.put("advances", advances);
        bundle.put("next_id", nextId);
        int[] ids = new int[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            ids[i] = group.id;
            Bundle state = new Bundle();
            state.put("source", group.source);
            state.put("node", group.node);
            state.put("previous", group.previous);
            state.put("target", group.target);
            state.put("traveling", group.traveling);
            state.put("biome", group.biome.name());
            String[] troops = new String[group.troops.size()];
            for (int j = 0; j < troops.length; j++) troops[j] = group.troops.get(j).name();
            state.put("troops", troops);
            bundle.put("group_" + group.id, state);
        }
        bundle.put("ids", ids);
    }

    static JourneyForces restore(JourneyRoute route, Bundle bundle) throws IOException {
        JourneyForces forces = new JourneyForces(route);
        forces.advances = bundle.getInt("advances");
        forces.nextId = bundle.getInt("next_id");
        int[] ids = bundle.getIntArray("ids");
        if (forces.advances < 0 || forces.advances == Integer.MAX_VALUE || forces.nextId < 1
                || forces.nextId > Integer.MAX_VALUE - 5 || forces.nextId > 5L * forces.advances + 1
                || ids == null || ids.length > 5L * forces.advances || ids.length >= route.nodes.size())
            throw new IOException("Invalid Journey forces");
        ArrayList<Group> restored = new ArrayList<>();
        HashSet<Integer> used = new HashSet<>();
        HashSet<Integer> targets = new HashSet<>();
        try {
            for (int id : ids) {
                if (id <= 0 || id >= forces.nextId || !used.add(id)) throw new IOException("Invalid Journey force ID");
                Bundle state = bundle.getBundle("group_" + id);
                int source = state.getInt("source"), node = state.getInt("node"), previous = state.getInt("previous");
                int target = state.getInt("target");
                boolean traveling = state.getBoolean("traveling");
                boolean settled = !traveling && node == target;
                JourneyRoute.Biome biome = JourneyRoute.Biome.valueOf(state.getString("biome"));
                if (source <= 0 || source >= route.nodes.size() || !(route.node(source).gate || route.node(source).finish)
                        || route.node(source).biome != biome || node <= 0 || node >= route.nodes.size()
                        || target <= 0 || target >= route.nodes.size() || !targets.add(target)
                        || route.node(target).biome.ordinal() < Math.max(0, biome.ordinal() - 1)
                        || route.node(target).biome.ordinal() > (settled ? biome.ordinal() : Math.max(0, biome.ordinal() - 1))
                        || route.node(target).layer > route.node(source).layer || !state.contains("traveling")
                        || !forces.reachable(source, node) || !forces.reachable(node, target))
                    throw new IOException("Invalid Journey force position");
                if (traveling && node == target || (settled ? previous != -1
                        : !forces.predecessors(node).contains(previous) || !forces.reachable(previous, target)))
                    throw new IOException("Invalid Journey force route");
                String[] names = state.getStringArray("troops");
                if (names == null || names.length < 1 || names.length > 3) throw new IOException("Invalid Journey troops");
                ArrayList<Troop> troops = new ArrayList<>();
                for (String name : names) {
                    Troop troop = Troop.valueOf(name);
                    if (troop.biome != biome || troops.contains(troop)) throw new IOException("Invalid Journey troop type");
                    troops.add(troop);
                }
                restored.add(new Group(id, source, node, previous, target, traveling, biome, troops));
            }
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid Journey forces", invalid);
        }
        forces.groups = Collections.unmodifiableList(restored);
        return forces;
    }
}
