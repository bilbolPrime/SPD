package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Gnoll;
import com.bilboldev.pixeldungeonskills.actors.mobs.Skeleton;
import com.bilboldev.pixeldungeonskills.actors.mobs.Brute;
import com.bilboldev.pixeldungeonskills.actors.mobs.Golem;
import com.bilboldev.pixeldungeonskills.actors.mobs.Scorpio;
import com.bilboldev.pixeldungeonskills.actors.mobs.Thief;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.JourneyCaptive;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.JourneyCache;
import com.bilboldev.pixeldungeonskills.items.keys.JourneyPass;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.HallsBossLevel;
import com.bilboldev.pixeldungeonskills.levels.PrisonBossLevel;
import com.bilboldev.pixeldungeonskills.levels.RebornDungeonBuilder;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.List;

final class JourneyExpedition {
    private final JourneyRoute.Node node;
    final Level[] levels;
    final int[] entrances;
    final Mob[] guards;
    final boolean[] defeated;
    int floor, actions;
    boolean retryArena, retreat, claimed, departureUnlocked;
    boolean bossDefeated;
    int[] forceGroups = new int[0];
    private JourneyForces.Troop[] forceTroops = new JourneyForces.Troop[0];
    private Mob[] forceMobs = new Mob[0];
    private boolean[] forceDefeated = new boolean[0];
    private final boolean[] forcePopulated;
    Heap cache;
    JourneyCaptive captive;

    JourneyExpedition(JourneyRoute.Node node) {
        this.node = node;
        levels = new Level[node.floors];
        entrances = new int[node.floors];
        Arrays.fill(entrances, -1);
        guards = new Mob[node.objective == JourneyRoute.Objective.NONE ? 0 : node.floors + node.biome.ordinal() / 2];
        defeated = new boolean[guards.length];
        forcePopulated = new boolean[node.floors];
        bossDefeated = (node.gate || node.finish) && node.completed;
    }

    void attachForces(List<JourneyForces.Group> groups) {
        if (groups.isEmpty()) return;
        forceGroups = new int[groups.size()];
        EnumSet<JourneyForces.Troop> types = EnumSet.noneOf(JourneyForces.Troop.class);
        JourneyForces.Troop leader = null;
        for (int i = 0; i < groups.size(); i++) {
            JourneyForces.Group group = groups.get(i);
            forceGroups[i] = group.id;
            for (JourneyForces.Troop troop : group.troops) {
                types.add(troop);
                if (leader == null || troop.ordinal() > leader.ordinal()) leader = troop;
            }
        }
        if (types.size() > 7) throw new IllegalStateException("Too many Journey reinforcement types");
        int count = Math.max(2 * Math.min(3, groups.size()), types.size() - 1);
        forceTroops = new JourneyForces.Troop[count + 1];
        forceMobs = new Mob[count + 1];
        forceDefeated = new boolean[count + 1];
        java.util.Random random = new java.util.Random(forceSeed());
        int required = 0;
        for (JourneyForces.Troop troop : types) if (troop != leader) forceTroops[required++] = troop;
        for (int i = required; i < count; i++) {
            List<JourneyForces.Troop> troops = groups.get(i % groups.size()).troops;
            forceTroops[i] = troops.get(random.nextInt(troops.size()));
        }
        forceTroops[count] = leader;
    }

    private long forceSeed() {
        long seed = JourneyRun.route().seed ^ (long)node.id * 0x4F72D936B8A1C50EL;
        for (int id : forceGroups) seed = seed * 31 + id;
        return seed;
    }

    boolean leaderPending() {
        return forceTroops.length > 0 && !forceDefeated[forceTroops.length - 1];
    }

    boolean isLeader(Mob mob) {
        return mob != null && forceMobs.length > 0 && mob.journeyForce == forceMobs.length
                && forceMobs[forceMobs.length - 1] == mob;
    }

    boolean forceSlain(Mob mob) {
        int index = mob.journeyForce - 1;
        if (index < 0 || index >= forceMobs.length || forceMobs[index] != mob || forceDefeated[index]) return false;
        forceDefeated[index] = true;
        return index == forceMobs.length - 1;
    }

    private int forceFloor(int index) {
        return index == forceTroops.length - 1 ? levels.length - 1 : index % levels.length;
    }

    void populateForces(Level level) {
        if (forcePopulated[floor]) return;
        if (forceTroops.length == 0) {
            forcePopulated[floor] = true;
            return;
        }
        long seed = forceSeed() ^ (long)floor * 0x6D41C85A7BE9032FL;
        java.util.Random random = new java.util.Random(seed);
        try (Random.Scope ignored = Random.use(seed)) {
            boolean arena = (node.gate || node.finish) && floor == levels.length - 1;
            ArrayList<Integer> cells = spawnCells(level, arena);
            for (int i = 0; i < forceTroops.length; i++) if (forceFloor(i) == floor) {
                if (cells.isEmpty()) throw new IllegalStateException("No room for Journey reinforcements");
                Mob mob = forceTroops[i].create();
                mob.pos = cells.remove(random.nextInt(cells.size()));
                mob.journeyForce = i + 1;
                mob.rebornPack = -node.id;
                mob.state = mob.WANDERING;
                if (i == forceTroops.length - 1) RebornChampion.grant(mob, false, random);
                forceMobs[i] = mob;
                level.mobs.add(mob);
                if (level.map[mob.pos] == Terrain.HIGH_GRASS) level.map[mob.pos] = Terrain.GRASS;
            }
        }
        forcePopulated[floor] = true;
    }

    void clearForces() {
        for (Level level : levels) if (level != null)
            for (Mob mob : new ArrayList<>(level.mobs)) if (mob.journeyForce > 0) {
                if (mob instanceof Thief && ((Thief)mob).item != null) recoverStolenItem(level, (Thief)mob);
                level.mobs.remove(mob);
                Actor.remove(mob);
                if (Actor.findChar(mob.pos) == mob) Actor.freeCell(mob.pos);

                mob.sprite = null;
            }
        forceGroups = new int[0];
        forceTroops = new JourneyForces.Troop[0];
        forceMobs = new Mob[0];
        forceDefeated = new boolean[0];
        Arrays.fill(forcePopulated, false);
    }

    private static void recoverStolenItem(Level level, Thief thief) {
        int destination = -1, distance = Integer.MAX_VALUE;
        for (int cell = 0; cell < Level.LENGTH; cell++) {
            Heap existing = level.heaps.get(cell);
            if (!RebornDungeonBuilder.walkable(level, cell) || existing != null && existing.type != Heap.Type.HEAP) continue;
            int away = Level.distance(thief.pos, cell);
            if (away < distance) {
                destination = cell;
                distance = away;
            }
        }
        if (destination < 0) throw new IllegalStateException("No room for stolen Journey belongings");
        Heap heap = level.heaps.get(destination);
        if (heap == null) {
            heap = new Heap();
            heap.pos = destination;
            level.heaps.put(destination, heap);
        }
        thief.item.journeyRemains = true;
        thief.item.droppedByHero = thief.item.droppedOnDeath = true;
        heap.items.addFirst(thief.item);
        thief.item = null;
    }

    void validateForces(JourneyForces forces) throws IOException {
        List<JourneyForces.Group> defending = forces.at(node.id);
        HashSet<Integer> ids = new HashSet<>();
        EnumSet<JourneyForces.Troop> types = EnumSet.noneOf(JourneyForces.Troop.class);
        JourneyForces.Troop leader = null;
        for (JourneyForces.Group group : defending) {
            ids.add(group.id);
            for (JourneyForces.Troop troop : group.troops) {
                types.add(troop);
                if (leader == null || troop.ordinal() > leader.ordinal()) leader = troop;
            }
        }
        if (leaderPending()) {
            if (ids.size() != forceGroups.length) throw new IOException("Invalid deployed Journey forces");
            for (int id : forceGroups) if (!ids.remove(id)) throw new IOException("Missing deployed Journey force");
            for (JourneyForces.Troop troop : forceTroops)
                if (!types.contains(troop)) throw new IOException("Changed deployed Journey troops");
            if (!EnumSet.copyOf(Arrays.asList(forceTroops)).equals(types)) throw new IOException("Missing deployed Journey troop type");
            if (forceTroops[forceTroops.length - 1] != leader) throw new IOException("Changed Journey reinforcement leader");
        } else {
            if (!JourneyRun.victory() && forceGroups.length == 0 && !defending.isEmpty())
                throw new IOException("Missing deployed Journey forces");
            for (int id : forceGroups) if (ids.contains(id)) throw new IOException("Defeated Journey force returned");
        }
    }

    int guardsRemaining() {
        int count = 0;
        for (boolean dead : defeated) if (!dead) count++;
        return count;
    }

    boolean isGuard(Mob mob) {
        return mob != null && mob.journeyGuard > 0 && mob.journeyGuard <= guards.length
                && guards[mob.journeyGuard - 1] == mob;
    }

    void guardSlain(Mob mob) {
        if (!isGuard(mob) || defeated[mob.journeyGuard - 1]) return;
        defeated[mob.journeyGuard - 1] = true;
        if (node.objective == JourneyRoute.Objective.CHECKPOINT && mob.journeyGuard == 1) {
            JourneyPass pass = new JourneyPass();
            pass.depth = JourneyRun.levelKey() + node.floors - 1 - floor;
            Heap heap = Dungeon.level.drop(pass, mob.pos);
            if (heap.sprite != null) heap.sprite.drop();
        }
    }

    void populateGuards(JourneyRoute.Node node, Level level) {
        try (Random.Scope random = RunSeed.scope(RunSeed.CHALLENGES, JourneyRun.levelKey())) {
            if (floor == levels.length - 1 && !claimed) {
                if (node.objective == JourneyRoute.Objective.SUPPLIES) {
                    cache = new Heap();
                    cache.pos = Random.element(spawnCells(level, false));
                    cache.type = Heap.Type.CHEST;
                    cache.items.add(new JourneyCache());
                    level.heaps.put(cache.pos, cache);
                } else if (node.objective == JourneyRoute.Objective.RESCUE) {
                    captive = new JourneyCaptive();
                    captive.pos = Random.element(spawnCells(level, false));
                    level.mobs.add(captive);
                }
            }
            ArrayList<Integer> cells = spawnCells(level, false);
            for (int i = 0; i < guards.length; i++) {
                if (i % levels.length != floor) continue;
                if (cells.isEmpty()) throw new IllegalStateException("No room for Journey objective guards");
                Mob mob = guard(node.biome);
                mob.pos = cells.remove(Random.Int(cells.size()));
                mob.journeyGuard = i + 1;
                mob.state = mob.WANDERING;
                guards[i] = mob;
                level.mobs.add(mob);
                if (level.map[mob.pos] == Terrain.HIGH_GRASS) level.map[mob.pos] = Terrain.GRASS;
            }
            if ((node.gate || node.finish) && floor == levels.length - 1 && !JourneyRun.outpostCleared(node.biome)) {
                cells = spawnCells(level, true);
                int count = 2 + node.biome.ordinal() / 2;
                if (cells.size() < count) throw new IllegalStateException("No room for Journey boss garrison");
                for (int i = 0; i < count; i++) {
                    Mob mob = guard(node.biome);
                    mob.pos = cells.remove(Random.Int(cells.size()));
                    mob.journeyGuard = -1;
                    mob.state = mob.WANDERING;
                    level.mobs.add(mob);
                }
            }
        }
    }

    private static Mob guard(JourneyRoute.Biome biome) {
        switch (biome) {
            case SEWERS: return new Gnoll();
            case PRISON: return new Skeleton();
            case CAVES: return new Brute();
            case CITY: return new Golem();
            case HALLS: return new Scorpio();
            default: throw new IllegalArgumentException("Invalid Journey biome");
        }
    }

    private ArrayList<Integer> spawnCells(Level level, boolean arena) {

        int entrance = entrances[floor];
        int[] fromEntrance = RebornDungeonBuilder.distances(level, entrance);
        int[] fromExit = RebornDungeonBuilder.distances(level, level.exit);
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell = 0; cell < Level.LENGTH; cell++) {
            boolean accessible = fromEntrance[cell] >= 4;
            if (arena && level instanceof PrisonBossLevel) accessible = ((PrisonBossLevel)level).insideArena(cell);
            else if (arena && level instanceof HallsBossLevel) accessible = fromExit[cell] >= 4;
            if (!accessible || fromExit[cell] < 4 || cell == entrance
                    || !RebornDungeonBuilder.walkable(level, cell) || level.heaps.get(cell) != null) continue;
            int tile = level.map[cell];
            if (tile != Terrain.EMPTY && tile != Terrain.EMPTY_DECO && tile != Terrain.EMPTY_SP
                    && tile != Terrain.GRASS && tile != Terrain.HIGH_GRASS && tile != Terrain.WATER) continue;
            boolean occupied = false;
            for (Mob mob : level.mobs) if (mob.pos == cell) occupied = true;
            if (!occupied) cells.add(cell);
        }
        return cells;
    }

    void removeGarrison() {
        for (Level level : levels) if (level != null) {
            for (Mob mob : new ArrayList<>(level.mobs)) if (mob.journeyGuard == -1) {
                level.mobs.remove(mob);
                Actor.remove(mob);
                if (Actor.findChar(mob.pos) == mob) Actor.freeCell(mob.pos);
                if (mob.sprite != null) mob.sprite.killAndErase();
            }
        }
    }

    void store(Bundle bundle) {
        bundle.put("floor", floor);
        bundle.put("entrances", entrances);
        bundle.put("actions", actions);
        bundle.put("retry_arena", retryArena);
        bundle.put("retreat", retreat);
        bundle.put("claimed", claimed);
        bundle.put("departure_unlocked", departureUnlocked);
        bundle.put("guards_defeated", defeated);
        bundle.put("boss_defeated", bossDefeated);
        bundle.put("force_groups", forceGroups);
        String[] troops = new String[forceTroops.length];
        for (int i = 0; i < troops.length; i++) troops[i] = forceTroops[i].name();
        bundle.put("force_troops", troops);
        bundle.put("force_defeated", forceDefeated);
        bundle.put("force_populated", forcePopulated);
        for (int i = 0; i < levels.length; i++) if (levels[i] != null) bundle.put("floor_" + i, levels[i]);
    }

    static JourneyExpedition restore(JourneyRoute.Node node, Bundle bundle) throws IOException {
        return restore(node, bundle, false);
    }

    static JourneyExpedition restore(JourneyRoute.Node node, Bundle bundle, boolean returning) throws IOException {
        JourneyExpedition expedition = new JourneyExpedition(node);
        expedition.floor = bundle.getInt("floor");
        if (expedition.floor < 0 || expedition.floor >= node.floors) throw new IOException("Invalid Journey floor");
        int[] entrances = bundle.getIntArray("entrances");
        boolean[] defeated = bundle.getBooleanArray("guards_defeated");
        if (entrances.length != node.floors || defeated.length != expedition.guards.length)
            throw new IOException("Invalid Journey expedition");
        System.arraycopy(entrances, 0, expedition.entrances, 0, entrances.length);
        System.arraycopy(defeated, 0, expedition.defeated, 0, defeated.length);
        expedition.actions = bundle.getInt("actions");
        expedition.retryArena = bundle.getBoolean("retry_arena");
        expedition.retreat = bundle.getBoolean("retreat");
        expedition.claimed = bundle.getBoolean("claimed");
        expedition.departureUnlocked = bundle.getBoolean("departure_unlocked");
        expedition.bossDefeated = bundle.getBoolean("boss_defeated");
        expedition.forceGroups = bundle.getIntArray("force_groups");
        String[] troops = bundle.getStringArray("force_troops");
        expedition.forceDefeated = bundle.getBooleanArray("force_defeated");
        boolean[] populated = bundle.getBooleanArray("force_populated");
        if (expedition.forceGroups == null || troops == null || troops.length > 7
                || (expedition.forceGroups.length == 0 ? troops.length != 0
                : troops.length < 2 * Math.min(3, expedition.forceGroups.length) + 1)
                || expedition.forceDefeated == null || expedition.forceDefeated.length != troops.length
                || populated == null || populated.length != node.floors
                || expedition.bossDefeated && !(node.gate || node.finish))
            throw new IOException("Invalid Journey reinforcements");
        HashSet<Integer> groups = new HashSet<>();
        for (int id : expedition.forceGroups) if (id <= 0 || !groups.add(id)) throw new IOException("Invalid Journey force ID");
        expedition.forceTroops = new JourneyForces.Troop[troops.length];
        expedition.forceMobs = new Mob[troops.length];
        try {
            for (int i = 0; i < troops.length; i++) {
                expedition.forceTroops[i] = JourneyForces.Troop.valueOf(troops[i]);
                if (expedition.forceTroops[i].biome.ordinal() > node.biome.ordinal() + 1)
                    throw new IOException("Journey reinforcements crossed their region boundary");
            }
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid Journey reinforcement type", invalid);
        }
        if (troops.length > 0 && troops.length != Math.max(2 * Math.min(3, expedition.forceGroups.length),
                EnumSet.copyOf(Arrays.asList(expedition.forceTroops)).size() - 1) + 1)
            throw new IOException("Invalid Journey reinforcement count");
        System.arraycopy(populated, 0, expedition.forcePopulated, 0, populated.length);
        for (int i = 0; i < node.floors; i++) {
            if (!bundle.contains("floor_" + i)) continue;
            Dungeon.depth = node.depth(i);
            expedition.levels[i] = (Level)bundle.get("floor_" + i);
            if (expedition.levels[i] == null || entrances[i] < 0 || entrances[i] >= Level.LENGTH)
                throw new IOException("Invalid Journey floor");
            Actor.clear();
            Level level = expedition.levels[i];
            for (Mob mob : new ArrayList<>(level.mobs)) {
                if (mob.journeyForce != 0) {
                    int force = mob.journeyForce - 1;
                    if (force < 0 || force >= expedition.forceMobs.length || expedition.forceFloor(force) != i
                            || mob.pos < 0 || mob.pos >= Level.LENGTH
                            || !populated[i] || expedition.forceDefeated[force] || expedition.forceMobs[force] != null
                            || mob.getClass() != expedition.forceTroops[force].mobClass || mob.journeyGuard != 0
                            || force == troops.length - 1 && mob.buff(RebornChampion.class) == null)
                        throw new IOException("Invalid Journey reinforcement");
                    expedition.forceMobs[force] = mob;
                }
                if (mob instanceof JourneyCaptive) {
                    if (node.objective != JourneyRoute.Objective.RESCUE || i != node.floors - 1 || expedition.captive != null)
                        throw new IOException("Invalid Journey captive");
                    if (expedition.claimed) level.mobs.remove(mob);
                    else expedition.captive = (JourneyCaptive)mob;
                }
                int index = mob.journeyGuard - 1;
                if (index < 0) continue;
                if (index >= expedition.guards.length || index % node.floors != i
                        || expedition.guards[index] != null || defeated[index])
                    throw new IOException("Invalid Journey objective guard");
                expedition.guards[index] = mob;
            }
            for (Heap heap : new ArrayList<>(level.heaps.values())) {
                for (Item item : new ArrayList<>(heap.items)) if (item instanceof JourneyCache) {
                    if (node.objective != JourneyRoute.Objective.SUPPLIES || i != node.floors - 1 || expedition.cache != null)
                        throw new IOException("Invalid Journey supply cache");
                    if (expedition.claimed) {
                        heap.items.remove(item);
                        if (heap.isEmpty()) level.heaps.remove(heap.pos);
                        else heap.type = Heap.Type.HEAP;
                    } else expedition.cache = heap;
                }
            }
        }
        for (int i = 0; i < expedition.guards.length; i++)
            if (!defeated[i] && expedition.levels[i % node.floors] != null && expedition.guards[i] == null)
                throw new IOException("Missing Journey objective guard");
        for (int i = 0; i < node.floors; i++) if (populated[i] && expedition.levels[i] == null)
            throw new IOException("Missing Journey reinforcement floor");

        for (int i = 0; i < troops.length; i++)
            if (!returning && populated[expedition.forceFloor(i)] && !expedition.forceDefeated[i] && expedition.forceMobs[i] == null)
                throw new IOException("Missing Journey reinforcement");
        return expedition;
    }
}
