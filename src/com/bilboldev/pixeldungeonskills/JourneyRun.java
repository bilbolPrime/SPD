package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.tweeners.Tweener;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Fury;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.buffs.ArmorEvasion;
import com.bilboldev.pixeldungeonskills.actors.buffs.OffBalance;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.hero.Belongings;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.RebornTengu;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.JourneyCaptive;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Wandmaker;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.EquipableItem;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.JourneyCache;
import com.bilboldev.pixeldungeonskills.items.DewVial;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.bags.Bag;
import com.bilboldev.pixeldungeonskills.items.bags.ScrollHolder;
import com.bilboldev.pixeldungeonskills.items.bags.SeedPouch;
import com.bilboldev.pixeldungeonskills.items.bags.WandHolster;
import com.bilboldev.pixeldungeonskills.items.essences.Essence;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.keys.JourneyPass;
import com.bilboldev.pixeldungeonskills.items.keys.Key;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfHaggler;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.CavesBossLevel;
import com.bilboldev.pixeldungeonskills.levels.CityBossLevel;
import com.bilboldev.pixeldungeonskills.levels.PrisonBossLevel;
import com.bilboldev.pixeldungeonskills.levels.SewerBossLevel;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.JourneyScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.ui.GameLog;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.SparseArray;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;

public final class JourneyRun {
    public static final int FIRST_SLOT = 1, SLOT_COUNT = 6;
    public static final HeroClass[] HEROES = {HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE,
            HeroClass.HUNTRESS, HeroClass.NECROMANCER, HeroClass.MERCENARY};

    private static final int VERSION = 1;
    private static boolean active, finished, victory, returning, deathPending, leaving, discarded;
    private static JourneyRoute route;
    private static JourneyForces forces;
    private static JourneyRewards rewards;
    private static final EnumMap<HeroClass, Member> roster = new EnumMap<>(HeroClass.class);
    private static Member selected;
    private static Bag chest;
    private static JourneyShop shop;
    private static int current, deployed = -1, revision, slot;
    private static Level area;
    private static JourneyExpedition expedition;
    private static int nextFloor = -1;
    private static int entrance = -1, lastGround = -1;
    private static boolean recovering, reentering, completedExit, campSavePending;
    private static final LinkedHashMap<Integer, JourneyExpedition> fallen = new LinkedHashMap<>();
    private static Runnable restoreGlobals;

    public enum SlotState { EMPTY, SAVED, UNAVAILABLE }

    public static final class SlotInfo {
        public final int slot;
        public final SlotState state;
        public final long seed;
        public final boolean victory, custom;
        private final boolean[] living;
        private final boolean[] resting;
        private final int[] armor;

        private SlotInfo(int slot, SlotState state, long seed, boolean custom, boolean victory, boolean[] living, boolean[] resting, int[] armor) {
            this.slot = slot;
            this.state = state;
            this.seed = seed;
            this.custom = custom;
            this.victory = victory;
            this.living = living;
            this.resting = resting;
            this.armor = armor;
        }

        public boolean alive(HeroClass heroClass) {
            for (int i = 0; i < HEROES.length; i++) if (HEROES[i] == heroClass) return living[i];
            return false;
        }

        public int armorTier(HeroClass heroClass) {
            for (int i = 0; i < HEROES.length; i++) if (HEROES[i] == heroClass) return armor[i];
            return 0;
        }

        public boolean resting(HeroClass heroClass) {
            for (int i = 0; i < HEROES.length; i++) if (HEROES[i] == heroClass) return living[i] && resting[i];
            return false;
        }
    }

    private static final class Member {
        Hero hero;
        boolean dead, resting;
        int points, summons;
        float clock;
        Object[] slots;
        Char target;
        WandOfMagicCasting wand = new WandOfMagicCasting();

        void capture() {
            points = Skill.availableSkill;
            clock = Skill.skillTrack;
            slots = QuickSlot.values.clone();
            target = QuickSlot.lastTarget();
            summons = SummonedPet.summonedPets;
            wand = Hero.haxWand;
        }

        void activate() {
            Dungeon.hero = hero;
            StartScene.curClass = hero.heroClass;
            Skill.availableSkill = points;
            Skill.skillTrack = clock;
            QuickSlot.values = slots.clone();
            QuickSlot.lastTarget(target);
            SummonedPet.summonedPets = summons;
            Hero.haxWand = wand;
        }
    }

    private JourneyRun() { }

    public static boolean active() { return active; }

    public static int levelCap() {
        if (!active) return Integer.MAX_VALUE;
        for (JourneyRoute.Node node : route.nodes) if ((node.gate || node.finish) && !bossDefeated(node.id)) {
            switch (node.biome) {
                case SEWERS: return 10;
                case PRISON: return 15;
                case CAVES: return 21;
                default: return 25;
            }
        }
        return 25;
    }
    public static int slot() { return active ? slot : 0; }
    public static boolean inCamp() { return active && deployed < 0; }
    public static boolean finished() { return active && finished; }
    public static boolean victory() { return active && victory; }
    public static boolean dying() { return active && deathPending; }
    public static boolean recovering() { return active && deployed >= 0 && recovering; }
    public static int entrance() { return entrance; }
    public static JourneyRoute.Node deployedNode() { return deployed < 0 ? null : route.node(deployed); }
    public static int floorIndex() { return expedition == null ? 0 : expedition.floor; }
    public static int floorCount() { return deployed < 0 ? 0 : route.node(deployed).floors; }
    public static boolean canReturnToCamp() { return expedition != null && (recovering || expedition.retreat); }
    public static int guardsRemaining() { return expedition == null ? 0 : expedition.guardsRemaining(); }
    public static List<JourneyForces.Group> forces() {
        return active ? forces.groups() : Collections.emptyList();
    }
    public static boolean reinforcementLeaderPending() {
        return active && expedition != null && expedition.leaderPending();
    }
    public static boolean isReinforcementLeader(Mob mob) {
        return active && expedition != null && expedition.isLeader(mob);
    }
    public static void reinforcementSlain(Mob mob) {
        if (!active || expedition == null || !expedition.forceSlain(mob)) return;
        forces.remove(expedition.forceGroups);
        JourneyRoute.Node node = deployedNode();
        if (expedition.bossDefeated && (node.gate || node.finish)) node.completed = true;
    }
    public static boolean objectiveComplete() {
        JourneyRoute.Node node = deployedNode();
        if (node == null || guardsRemaining() > 0 || reinforcementLeaderPending()) return false;
        switch (node.objective) {
            case CHECKPOINT: return expedition.departureUnlocked || departurePass() != null;
            case SUPPLIES:
            case RESCUE: return expedition.claimed;
            default: return !(node.gate || node.finish) || bossDefeated(node.id);
        }
    }
    public static String objectiveExitHint() {
        if (reinforcementLeaderPending()) return "journey.leader_blocked";
        if (guardsRemaining() > 0) return "journey.guards_blocked";
        JourneyRoute.Node node = deployedNode();
        if (node != null && !bossDefeated(node.id) && (node.gate || node.finish))
            return node.finish ? "journey.final_objective" : "journey.boss_objective";
        if (node != null && !objectiveComplete()) switch (node.objective) {
            case CHECKPOINT: return "journey.pass_blocked";
            case SUPPLIES: return "journey.cache_blocked";
            case RESCUE: return "journey.rescue_blocked";
        }
        return "journey.exit_hint";
    }
    public static boolean isSupplyGuard(Mob mob) {
        return active && expedition != null && expedition.isGuard(mob);
    }
    public static void supplyGuardSlain(Mob mob) {
        if (active && expedition != null) expedition.guardSlain(mob);
    }
    public static boolean isOfficer(Mob mob) {
        return isSupplyGuard(mob) && deployedNode().objective == JourneyRoute.Objective.CHECKPOINT && mob.journeyGuard == 1;
    }
    public static boolean isCache(Heap heap) {
        return active && expedition != null && !expedition.claimed && expedition.cache == heap && heap != null;
    }
    public static boolean isCaptive(JourneyCaptive captive) {
        return active && expedition != null && !expedition.claimed && expedition.captive == captive && captive != null
                && area != null && area == Dungeon.level && area.mobs.contains(captive);
    }
    public static int campDiscount() {
        if (!active) return 0;
        int rescued = 0;
        for (JourneyRoute.Node node : route.nodes)
            if (node.completed && node.objective == JourneyRoute.Objective.RESCUE) rescued++;
        return Math.min(25, 5 * rescued);
    }
    public static boolean outpostCleared(JourneyRoute.Biome biome) {
        if (!active) return false;
        for (JourneyRoute.Node node : route.nodes)
            if (node.biome == biome && node.objective == JourneyRoute.Objective.OUTPOST && node.completed) return true;
        return false;
    }
    public static boolean bossArea() {
        JourneyRoute.Node node = deployedNode();
        return active && node != null && floorIndex() == node.floors - 1 && (node.gate || node.finish);
    }
    public static HeroClass selectedClass() { return selected == null ? null : selected.hero.heroClass; }
    public static Hero selectedHero() { return selected == null ? null : selected.hero; }
    public static Hero hero(HeroClass heroClass) {
        Member member = roster.get(heroClass);
        return member == null ? null : member.hero;
    }
    public static boolean alive(HeroClass heroClass) {
        Member member = roster.get(heroClass);
        return member != null && !member.dead;
    }
    public static Hero[] livingHeroes() {
        ArrayList<Hero> living = new ArrayList<>();
        for (HeroClass heroClass : HEROES) {
            Member member = roster.get(heroClass);
            if (member != null && !member.dead && member.hero.isAlive()) living.add(member.hero);
        }

        if (Dungeon.hero != null && !roster.containsKey(Dungeon.hero.heroClass) && Dungeon.hero.isAlive())
            living.add(Dungeon.hero);
        return living.toArray(new Hero[0]);
    }
    public static Bag chest() { return chest; }
    public static JourneyShop shop() { return shop; }
    public static int gold() { return Dungeon.gold; }
    public static JourneyRoute route() { return route; }
    public static Item reward(int node) { return active && rewards != null ? rewards.item(node) : null; }
    public static JourneyRoute.Node node(int id) { return route.node(id); }
    public static JourneyRoute.Node currentNode() { return route.node(current); }
    public static boolean resting(HeroClass heroClass) {
        Member member = active ? roster.get(heroClass) : null;
        return member != null && !member.dead && member.resting;
    }

    public static boolean canDeploySelected() {
        return inCamp() && !finished && selected != null && !selected.dead && selected.hero.isAlive() && !selected.resting;
    }

    public static Item remains(Heap heap) {
        if (active && heap != null && !heap.isEmpty())
            for (Item item : heap.items) if (item.journeyRemains) return item;
        return null;
    }

    public static boolean hasRemains(int node) {
        if (!active || node < 0 || node >= route.nodes.size()) return false;
        JourneyExpedition cached = deployed == node ? expedition : fallen.get(node);
        if (cached != null) for (Level level : cached.levels)
            if (level != null) for (Heap heap : level.heaps.values()) if (remains(heap) != null) return true;
        return false;
    }

    public static boolean recovery(int node) {
        return hasRemains(node) && (victory || route.node(node).completed);
    }

    public static boolean available(int node) {
        if (!inCamp() || finished || node < 0 || node >= route.nodes.size()) return false;
        if (victory || route.node(node).completed) return recovery(node);
        for (JourneyRoute.Node previous : route.nodes) {
            if (!previous.completed) continue;
            for (int next : previous.next) if (next == node) return true;
        }
        return false;
    }

    public static int levelKey() {
        return 2000 + deployed * 3 + floorIndex();
    }

    private static int validateSlot(int slot) {
        if (slot < FIRST_SLOT || slot > SLOT_COUNT) throw new IllegalArgumentException("Invalid Journey slot");
        return slot;
    }

    private static String filename(int slot, int revision) {
        validateSlot(slot);
        return "journey" + (slot == FIRST_SLOT ? "" : "_slot" + slot) + "-" + revision % 2 + ".dat";
    }

    public static boolean exists() { return exists(active ? slot : FIRST_SLOT); }

    public static boolean exists(int slot) {
        validateSlot(slot);
        if (Game.instance == null) return false;
        for (String filename : Game.instance.fileList())
            if (filename(slot, 0).equals(filename) || filename(slot, 1).equals(filename)) return true;
        return false;
    }

    public static SlotInfo summary(int slot) {
        validateSlot(slot);
        boolean[] living = new boolean[HEROES.length];
        boolean[] resting = new boolean[HEROES.length];
        int[] armor = new int[HEROES.length];
        if (Game.instance == null) return new SlotInfo(slot, SlotState.UNAVAILABLE, 0, false, false, living, resting, armor);
        if (!exists(slot)) return new SlotInfo(slot, SlotState.EMPTY, 0, false, false, living, resting, armor);
        try {
            Bundle save = latestSave(slot);
            if (save.getBoolean("deleted")) return new SlotInfo(slot, SlotState.UNAVAILABLE, 0, false, false, living, resting, armor);
            for (int i = 0; i < HEROES.length; i++) {
                Bundle member = save.getBundle("hero_" + HEROES[i].name());
                living[i] = !member.getBoolean("dead");
                resting[i] = member.getBoolean("resting");
                armor[i] = Armor.previewTier(member.getBundle("hero").getBundle("armor"));
            }
            int survivors = 0;
            for (boolean alive : living) if (alive) survivors++;
            if (survivors <= 1) java.util.Arrays.fill(resting, false);
            return new SlotInfo(slot, SlotState.SAVED, Long.parseLong(save.getString("route_seed")),
                    save.getBundle("world").getBoolean("custom_seed"), save.getBoolean("victory"), living, resting, armor);
        } catch (IOException | RuntimeException unavailable) {
            return new SlotInfo(slot, SlotState.UNAVAILABLE, 0, false, false, living, resting, armor);
        }
    }

    public static void open() throws IOException {
        open(active ? slot : FIRST_SLOT);
    }

    public static void open(int slot) throws IOException {
        validateSlot(slot);
        if (active && JourneyRun.slot != slot) leave();
        if (!active) {
            if (exists(slot)) resume(slot);
            else create(slot, RunSeed.random().code);
        }
        if (!active) return;
        if (inCamp()) Game.switchScene(JourneyScene.class);
        else {
            InterlevelScene.mode = InterlevelScene.Mode.JOURNEY;
            Game.switchScene(InterlevelScene.class);
        }
    }

    public static synchronized void create() throws IOException {
        create(FIRST_SLOT, RunSeed.random().code);
    }

    public static synchronized void create(int slot, long seed) throws IOException {
        create(slot, seed, false);
    }

    public static synchronized void create(int slot, long seed, boolean custom) throws IOException {
        validateSlot(slot);
        if (!RunSeed.valid(seed)) throw new IllegalArgumentException("Invalid Journey seed");
        if (Game.instance == null) throw new IOException("Journey storage is unavailable");
        if (exists(slot)) throw new IOException("Journey slot is occupied");
        if (active) leave();
        begin();
        JourneyRun.slot = slot;
        try {
            revision = 0;
            route = new JourneyRoute(seed);
            forces = new JourneyForces(route);
            roster.clear();
            current = 0;
            deployed = -1;
            area = null;
            expedition = null;
            nextFloor = -1;
            fallen.clear();
            entrance = lastGround = -1;
            recovering = reentering = false;
            finished = victory = returning = deathPending = campSavePending = false;
            chest = new Bag();
            chest.size = Belongings.BACKPACK_SIZE;
            StartScene.curClass = HEROES[0];
            HeroSkins.localChoice = 0;
            Dungeon.prepareNewRun(SaveSlots.FIRST, GameplayMode.REBORN, Difficulties.NORMAL.ordinal());
            Dungeon.seed = new RunSeed(seed, custom);
            Hero.haxWand = new WandOfMagicCasting();
            Dungeon.init();
            for (int i = 0; i < HEROES.length; i++) {
                if (i > 0) {
                    try (Random.Scope random = RunSeed.scope(RunSeed.INITIAL, i)) {
                        Actor.clear();
                        QuickSlot.resetSlots();
                        Skill.skillTrack = 0;
                        SummonedPet.summonedPets = 0;
                        Hero.haxWand = new WandOfMagicCasting();
                        Dungeon.hero = new Hero();
                        Dungeon.hero.difficulty = Dungeon.difficulty;
                        Dungeon.hero.live();
                        HEROES[i].initHero(Dungeon.hero);
                    }
                }
                new SeedPouch().collect();
                new ScrollHolder().collect();
                new WandHolster().collect();
                Member member = new Member();
                member.hero = Dungeon.hero;
                member.capture();
                roster.put(HEROES[i], member);
            }
            selected = roster.get(HEROES[0]);
            selected.activate();
            Actor.clear();
            Dungeon.level = null;
            shop = new JourneyShop(route);
            shop.catchUp(route);
            rewards = JourneyRewards.create(route);
            save();
        } catch (IOException | RuntimeException failure) {
            close();
            throw failure;
        }
    }

    public static synchronized void resume() throws IOException {
        resume(active ? slot : FIRST_SLOT);
    }

    public static synchronized void resume(int slot) throws IOException {
        validateSlot(slot);
        if (active && JourneyRun.slot == slot) return;
        if (active) leave();
        Bundle save = latestSave(slot);
        if (save.getBoolean("deleted") || save.getBoolean("finished") && !save.getBoolean("victory")) {
            deleteSlot(slot);
            return;
        }
        begin();
        JourneyRun.slot = slot;
        try {
            revision = save.getInt("revision");
            route = JourneyRoute.restore(save);
            forces = JourneyForces.restore(route, save.getBundle("forces"));
            current = save.getInt("current");
            deployed = save.getInt("deployed");
            route.node(current);
            if (deployed != -1) route.node(deployed);
            finished = save.getBoolean("finished");
            victory = save.getBoolean("victory");
            returning = save.getBoolean("returning");
            deathPending = save.getBoolean("death_pending");
            Dungeon.hero = null;
            Dungeon.level = null;
            Dungeon.restorePartyWorld(save.getBundle("world"));
            if (Dungeon.seed == null || Dungeon.seed.code != route.seed) throw new IOException("Invalid Journey run seed");
            Dungeon.challenges = 0;
            roster.clear();
            for (HeroClass heroClass : HEROES) {
                Bundle state = save.getBundle("hero_" + heroClass.name());
                QuickSlot.restore(state);
                Dungeon.hero = null;
                Skill.skillTrack = 0;
                Hero.haxWand = new WandOfMagicCasting();
                Member member = new Member();
                member.hero = (Hero)state.get("hero");
                if (member.hero == null || member.hero.heroClass != heroClass || member.hero.skillSystem != 4)
                    throw new IOException("Invalid Journey hero");
                Dungeon.hero = member.hero;
                member.dead = state.getBoolean("dead");
                member.resting = state.getBoolean("resting");
                member.points = Skill.availableSkill;
                member.clock = state.getFloat("clock");
                member.hero.talents().shiftClock(member.clock);
                member.slots = QuickSlot.values.clone();
                member.summons = state.getInt("summons");
                roster.put(heroClass, member);
            }
            selected = roster.get(HeroClass.valueOf(save.getString("selected")));
            if (selected == null) throw new IOException("Missing Journey selection");
            clearRestForLastHero();
            selected.activate();
            QuickSlot.resetSlots();
            chest = new Bag();
            chest.size = Belongings.BACKPACK_SIZE;
            chest.restoreFromBundle(save.getBundle("chest"));
            selected.activate();
            Actor.clear();
            QuickSlot.resetSlots();
            rewards = JourneyRewards.restore(route, save.getBundle("rewards"));
            expedition = deployed < 0 ? null : JourneyExpedition.restore(route.node(deployed), save.getBundle("expedition"), returning);
            area = expedition == null ? null : expedition.levels[expedition.floor];
            entrance = expedition == null ? -1 : expedition.entrances[expedition.floor];
            lastGround = save.getInt("last_ground");
            nextFloor = -1;
            recovering = save.getBoolean("recovering");
            reentering = save.getBoolean("reentering");
            completedExit = save.getBoolean("completed_exit");
            fallen.clear();
            int count = save.getInt("fallen_count");
            if (count < 0 || count > HEROES.length) throw new IOException("Invalid Journey areas");
            for (int i = 0; i < count; i++) {
                Bundle cached = save.getBundle("fallen_" + i);
                int node = cached.getInt("node");
                if (node <= 0 || node >= route.nodes.size() || node == deployed || fallen.containsKey(node))
                    throw new IOException("Invalid Journey area");
                fallen.put(node, JourneyExpedition.restore(route.node(node), cached));
                if (fallen.get(node).forceGroups.length != 0) throw new IOException("Unresolved cached Journey forces");
            }
            if (expedition != null) expedition.validateForces(forces);
            if (expedition != null) rewards.validate(deployed, expedition);
            for (Map.Entry<Integer, JourneyExpedition> cached : fallen.entrySet()) rewards.validate(cached.getKey(), cached.getValue());
            int levelCap = levelCap();
            for (Member member : roster.values())
                if (!member.dead && member.hero.lvl >= levelCap) member.hero.exp = 0;
            Actor.clear();
            Dungeon.level = null;
            selected.activate();
            Dungeon.depth = deployed < 0 ? 0 : route.node(deployed).depth(expedition.floor);
            if (save.contains("shop")) shop = JourneyShop.restore(save.getBundle("shop"), route);
            else {
                shop = new JourneyShop(route);
                shop.catchUp(route);
                save();
            }
            if (returning) finishArea();
            if (victory) Badges.validateJourneyComplete();
        } catch (IOException | RuntimeException failure) {
            close();
            if (failure instanceof IOException) throw (IOException)failure;
            throw new IOException("Unable to restore Journey", failure);
        }
    }

    private static void begin() {
        TutorialRun.finish();
        DailyRun.leave();
        GauntletRun.finish();
        PartySession.cancelLobby();
        LocalCoop.clear();
        restoreGlobals = snapshot();
        active = true;
        leaving = false;
        discarded = false;
        SummonedPet.summonedPets = 0;
    }

    public static synchronized void select(HeroClass heroClass) {
        if (!inCamp() || finished) throw new IllegalStateException("Journey selection requires camp");
        Member member = roster.get(heroClass);
        if (member == null || member.dead) throw new IllegalArgumentException("Unavailable Journey hero");
        selected.capture();
        selected = member;
        selected.activate();
    }

    private static void clearRestForLastHero() {
        int living = 0;
        for (Member member : roster.values()) if (!member.dead && member.hero.isAlive()) living++;
        if (living <= 1) for (Member member : roster.values()) member.resting = false;
    }

    public static boolean subclassesUnlocked() {
        if (!active) return false;
        for (JourneyRoute.Node node : route.nodes)
            if (node.biome == JourneyRoute.Biome.PRISON && node.gate && bossDefeated(node.id)) return true;
        return false;
    }

    public static boolean bossDefeated(int id) {
        if (!active) return false;
        JourneyRoute.Node node = route.node(id);
        if (!(node.gate || node.finish)) return false;
        if (node.completed) return true;
        JourneyExpedition saved = deployed == id ? expedition : fallen.get(id);
        return saved != null && saved.bossDefeated;
    }

    private static HashSet<Integer> defeatedBosses() {
        HashSet<Integer> defeated = new HashSet<>();
        for (JourneyRoute.Node node : route.nodes)
            if ((node.gate || node.finish) && bossDefeated(node.id)) defeated.add(node.id);
        return defeated;
    }

    public static HeroSubClass[] subclassChoices(HeroClass heroClass) {
        switch (heroClass) {
            case WARRIOR: return new HeroSubClass[]{HeroSubClass.GLADIATOR, HeroSubClass.BERSERKER};
            case MAGE: return new HeroSubClass[]{HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK};
            case ROGUE: return new HeroSubClass[]{HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER};
            case HUNTRESS: return new HeroSubClass[]{HeroSubClass.SNIPER, HeroSubClass.WARDEN};
            case NECROMANCER: return new HeroSubClass[]{HeroSubClass.SPIRIT_BINDER, HeroSubClass.LICH};
            case MERCENARY: return new HeroSubClass[]{HeroSubClass.MARSHAL, HeroSubClass.EXECUTIONER};
            default: throw new IllegalArgumentException("Unsupported Journey hero class");
        }
    }

    public static boolean canChooseSubclass(Hero hero) {
        return inCamp() && !finished && selected != null && !selected.dead && selected.hero == hero
                && hero.isAlive() && hero.subClass == HeroSubClass.NONE && subclassesUnlocked();
    }

    public static synchronized boolean chooseSubclass(Hero hero, HeroSubClass way) throws IOException {
        if (!canChooseSubclass(hero)) return false;
        HeroSubClass[] choices = subclassChoices(hero.heroClass);
        if (way != choices[0] && way != choices[1]) return false;
        Bundle skills = new Bundle();
        hero.skillTree.storeInBundle(skills);
        int points = Skill.availableSkill, health = hero.HP, mana = hero.MP;
        Fury fury = hero.buff(Fury.class);
        try {
            hero.subClass = way;
            if (fury == null && (way == HeroSubClass.BERSERKER || way == HeroSubClass.EXECUTIONER)
                    && hero.HP <= hero.HT * Fury.LEVEL) {
                Fury gained = Buff.affect(hero, Fury.class);

                Actor.remove(gained);
            }
            hero.skillTree.subclassChosen(way);
            save();
            return true;
        } catch (IOException | RuntimeException failure) {
            hero.subClass = HeroSubClass.NONE;
            hero.skillTree.restoreSkillsFromBundle(skills);
            hero.talents().shiftClock(Skill.skillTrack);
            Skill.availableSkill = points;
            if (fury == null) Buff.detach(hero, Fury.class);
            hero.refreshBonuses();
            hero.HP = health;
            hero.MP = mana;
            QuickSlot.refresh();
            selected.capture();
            throw failure;
        }
    }

    public static synchronized boolean deposit(Item item) throws IOException {
        if (!inCamp() || finished || item == null || item instanceof Bag || selected.dead || item.isEquipped(selected.hero)
                || !selected.hero.belongings.backpack.contains(item) || chest.contains(item)
                || !hasRoom(chest, item)) return false;
        return transfer(item, selected.hero.belongings.backpack, chest);
    }

    public static synchronized boolean withdraw(Item item) throws IOException {
        if (!inCamp() || finished || item == null || item instanceof Bag || selected.dead || !chest.contains(item)
                || !hasRoom(selected.hero.belongings.backpack, item)) return false;
        return transfer(item, chest, selected.hero.belongings.backpack);
    }

    public static synchronized boolean changeEquipment(Item item, boolean equip, Ring replace) throws IOException {
        Hero hero = selectedHero();
        if (!canTrade(hero) || !(item instanceof EquipableItem)) return false;
        Belongings gear = hero.belongings;
        if (equip ? !gear.backpack.contains(item) || item.isEquipped(hero) : !item.isEquipped(hero)) return false;
        if (!item.actions(hero).contains(equip ? EquipableItem.AC_EQUIP : EquipableItem.AC_UNEQUIP)) return false;
        if (equip && item instanceof IceWeapon && ((IceWeapon)item).heroClass() != hero.heroClass) return false;

        int slot;
        if (item instanceof Bow) slot = 4;
        else if (item instanceof KindOfWeapon) slot = 0;
        else if (item instanceof Armor) slot = 1;
        else if (item instanceof Ring) {
            if (!equip) slot = gear.ring1 == item ? 2 : 3;
            else if (gear.ring1 == null) slot = 2;
            else if (gear.ring2 == null) slot = 3;
            else if (replace == gear.ring1) slot = 2;
            else if (replace == gear.ring2) slot = 3;
            else return false;
        } else return false;
        Item[] worn = {gear.weapon, gear.armor, gear.ring1, gear.ring2, gear.bow};
        Item previous = worn[slot];
        if (previous != null && previous.cursed && !(previous instanceof Bow)) return false;

        Bag source = equip ? container(gear.backpack, item) : null;
        LinkedHashMap<Bag, ArrayList<Item>> contents = new LinkedHashMap<>();
        if (source != null) {
            contents.put(source, new ArrayList<>(source.items));
            source.items.remove(item);
        }
        Bag target = previous == null ? null : destination(gear.backpack, previous);
        boolean room = target == null || target.canHold(previous);
        if (source != null) {
            source.items.clear();
            source.items.addAll(contents.get(source));
        }
        if (!room) return false;
        if (target != null && !contents.containsKey(target)) contents.put(target, new ArrayList<>(target.items));
        Item stack = null;
        if (previous != null && previous.stackable) {
            for (Item carried : target.items) {
                if (carried != item && carried.getClass() == previous.getClass()
                        && (long)carried.quantity() + previous.quantity() <= Integer.MAX_VALUE) {
                    stack = carried;
                    break;
                }
            }
        }
        int quantity = stack == null ? 0 : stack.quantity();

        int health = hero.HP, mana = hero.MP;
        boolean curseKnown = item.cursedKnown;
        Object[] slots = QuickSlot.values.clone();
        Char enemy = QuickSlot.lastTarget();
        Buff offBalance = slot == 0 ? hero.buff(OffBalance.class) : null;
        Buff armorEvasion = slot == 1 ? hero.buff(ArmorEvasion.class) : null;
        try {
            if (equip) item.detachAll(gear.backpack);
            setEquipment(hero, slot, equip ? item : null);
            if (previous != null && !previous.collect(gear.backpack)) {
                throw new IllegalStateException("Journey inventory capacity changed");
            }
            if (equip) item.cursedKnown = true;
            if (offBalance != null) offBalance.detach();
            if (armorEvasion != null) armorEvasion.detach();
            hero.refreshBonuses();
            QuickSlot.refresh();
            save();
            return true;
        } catch (IOException | RuntimeException failure) {
            if (previous != null && gear.backpack.contains(previous)) previous.detachAll(gear.backpack);
            setEquipment(hero, slot, previous);
            for (Map.Entry<Bag, ArrayList<Item>> entry : contents.entrySet()) {
                entry.getKey().items.clear();
                entry.getKey().items.addAll(entry.getValue());
            }
            if (stack != null) stack.quantity(quantity);
            if (equip && item instanceof Wand) ((Wand)item).charge(hero);
            if (offBalance != null) offBalance.attachTo(hero);
            if (armorEvasion != null) armorEvasion.attachTo(hero);
            item.cursedKnown = curseKnown;
            hero.refreshBonuses();
            hero.HP = health;
            hero.MP = mana;
            QuickSlot.values = slots;
            QuickSlot.refresh();
            QuickSlot.lastTarget(enemy);
            selected.capture();
            throw failure;
        } finally {
            for (Buff buff : hero.buffs()) Actor.remove(buff);
        }
    }

    private static void setEquipment(Hero hero, int slot, Item item) {
        Belongings gear = hero.belongings;
        switch (slot) {
            case 0:
                gear.weapon = (KindOfWeapon)item;
                if (gear.weapon != null) gear.weapon.activate(hero);
                break;
            case 1: gear.armor = (Armor)item; break;
            case 2:
                if (gear.ring1 != null) gear.ring1.deactivate();
                gear.ring1 = (Ring)item;
                if (gear.ring1 != null) gear.ring1.activate(hero);
                break;
            case 3:
                if (gear.ring2 != null) gear.ring2.deactivate();
                gear.ring2 = (Ring)item;
                if (gear.ring2 != null) gear.ring2.activate(hero);
                break;
            case 4: gear.bow = (Bow)item; break;
            default: throw new IllegalArgumentException("Unknown Journey equipment slot");
        }
    }

    private static boolean canTrade(Hero hero) {
        return inCamp() && !finished && selected != null && !selected.dead
                && selected.hero == hero && hero.isAlive();
    }

    public static synchronized int purchasePrice(JourneyShop.Entry entry, Hero buyer) {
        if (!canTrade(buyer) || entry == null || shop.indexOf(entry) < 0) return 0;
        int price = Math.max(1, (int)((long)entry.price() * (100 - campDiscount()) / 100));
        if (buyer.buff(RingOfHaggler.Haggling.class) != null && price >= 2) price /= 2;
        return price;
    }

    public static synchronized boolean hold(JourneyShop.Entry entry, boolean held) throws IOException {
        if (!canTrade(selectedHero()) || entry == null || shop.indexOf(entry) < 0) return false;
        boolean previous = entry.held();
        if (previous == held) return true;
        shop.hold(entry, held);
        try {
            save();
            return true;
        } catch (IOException | RuntimeException failure) {
            shop.hold(entry, previous);
            throw failure;
        }
    }

    public static synchronized boolean buy(JourneyShop.Entry entry, Hero buyer) throws IOException {
        int price = purchasePrice(entry, buyer);
        if (price <= 0 || Dungeon.gold < price || !hasRoom(buyer.belongings.backpack, entry.item())) return false;
        Item item = entry.item();
        Bag backpack = buyer.belongings.backpack;
        Bag target = destination(backpack, item);
        ArrayList<Item> items = new ArrayList<>(target.items);
        Item stack = null;
        for (Item carried : target.items) if (item.stackable && carried.getClass() == item.getClass()
                && (long)carried.quantity() + item.quantity() <= Integer.MAX_VALUE) {
            stack = carried;
            break;
        }
        int quantity = stack == null ? 0 : stack.quantity();
        int gold = Dungeon.gold, health = buyer.HP, mana = buyer.MP;
        Object[] slots = QuickSlot.values.clone();
        Char enemy = QuickSlot.lastTarget();
        int index = shop.indexOf(entry);
        shop.remove(index);
        Dungeon.gold -= price;
        try {
            if (!item.collect(backpack)) throw new IllegalStateException("Journey inventory capacity changed");
            save();
            return true;
        } catch (IOException | RuntimeException failure) {
            if (backpack.contains(item)) item.detachAll(backpack);
            target.items.clear();
            target.items.addAll(items);
            if (stack != null) stack.quantity(quantity);
            Essence.inventoryChanged(target, item);
            buyer.HP = health;
            buyer.MP = mana;
            buyer.belongings.countIronKeys();
            QuickSlot.values = slots;
            QuickSlot.refresh();
            QuickSlot.lastTarget(enemy);
            Dungeon.gold = gold;
            shop.restore(index, entry);
            selected.capture();
            throw failure;
        }
    }

    public static synchronized int salePrice(Item item, int amount) {
        if (!canTrade(selectedHero()) || item == null || item instanceof Bag || item instanceof Gold
                || item.isEquipped(selected.hero) || !selected.hero.belongings.backpack.contains(item)
                || amount <= 0 || amount > item.quantity() || !item.stackable && item.quantity() != 1) return 0;
        int quantity = item.quantity();
        int unitPrice;
        item.quantity(1);
        try {
            unitPrice = item.price();
        } finally {
            item.quantity(quantity);
        }
        long price = (long)unitPrice * amount;
        return price > 0 && price <= Integer.MAX_VALUE && price + Dungeon.gold <= Integer.MAX_VALUE ? (int)price : 0;
    }

    public static synchronized boolean sell(Item item, int amount, Hero seller) throws IOException {
        if (!canTrade(seller)) return false;
        int price = salePrice(item, amount);
        if (price <= 0) return false;
        Bag source = container(seller.belongings.backpack, item);
        ArrayList<Item> items = new ArrayList<>(source.items);
        int quantity = item.quantity(), gold = Dungeon.gold, health = seller.HP, mana = seller.MP;
        Object[] slots = QuickSlot.values.clone();
        Char enemy = QuickSlot.lastTarget();
        if (amount == quantity) item.detachAll(seller.belongings.backpack);
        else {
            item.quantity(quantity - amount);
            item.updateQuickslot();
        }
        Dungeon.gold += price;
        try {
            save();
            return true;
        } catch (IOException | RuntimeException failure) {
            source.items.clear();
            source.items.addAll(items);
            item.quantity(quantity);
            if (item instanceof Wand && source.owner != null) ((Wand)item).charge(source.owner);
            Essence.inventoryChanged(source, item);
            seller.HP = health;
            seller.MP = mana;
            seller.belongings.countIronKeys();
            QuickSlot.values = slots;
            QuickSlot.refresh();
            QuickSlot.lastTarget(enemy);
            Dungeon.gold = gold;
            selected.capture();
            throw failure;
        }
    }

    private static boolean transfer(Item item, Bag sourceRoot, Bag targetRoot) throws IOException {
        Bag source = container(sourceRoot, item);
        Bag target = destination(targetRoot, item);
        ArrayList<Item> sourceItems = new ArrayList<>(source.items);
        ArrayList<Item> targetItems = new ArrayList<>(target.items);
        Item stack = null;
        for (Item carried : target.items) {
            if (item.stackable && carried.getClass() == item.getClass()
                    && (long)carried.quantity() + item.quantity() <= Integer.MAX_VALUE) {
                stack = carried;
                break;
            }
        }
        int quantity = stack == null ? 0 : stack.quantity();
        int health = selected.hero.HP, mana = selected.hero.MP;
        Object[] slots = QuickSlot.values.clone();
        Char targetEnemy = QuickSlot.lastTarget();
        item.detachAll(sourceRoot);
        if (!item.collect(targetRoot)) throw new IllegalStateException("Journey inventory capacity changed");
        selected.capture();
        try {
            save();
            return true;
        } catch (IOException failure) {
            if (targetRoot.contains(item)) item.detachAll(targetRoot);
            source.items.clear();
            source.items.addAll(sourceItems);
            target.items.clear();
            target.items.addAll(targetItems);
            if (stack != null) stack.quantity(quantity);
            if (item instanceof Wand && source.owner != null) ((Wand)item).charge(source.owner);
            Essence.inventoryChanged(source, item);
            Essence.inventoryChanged(target, item);
            selected.hero.HP = health;
            selected.hero.MP = mana;
            selected.hero.belongings.countIronKeys();
            QuickSlot.values = slots;
            QuickSlot.refresh();
            QuickSlot.lastTarget(targetEnemy);
            selected.capture();
            throw failure;
        }
    }

    private static Bag destination(Bag bag, Item item) {
        Bag target = bag;
        while (true) {
            Bag nested = null;
            for (Item carried : target.items) {
                if (carried instanceof Bag && ((Bag)carried).grab(item) && ((Bag)carried).canHold(item)) {
                    nested = (Bag)carried;
                    break;
                }
            }
            if (nested == null) break;
            target = nested;
        }
        return target;
    }

    private static Bag container(Bag bag, Item item) {
        for (Item carried : bag.items) {
            if (carried == item) return bag;
            if (carried instanceof Bag && ((Bag)carried).contains(item)) return container((Bag)carried, item);
        }
        throw new IllegalArgumentException("Item is not in this Journey inventory");
    }

    private static boolean hasRoom(Bag bag, Item item) {
        return destination(bag, item).canHold(item);
    }

    public static synchronized void deploy(int node) throws IOException {
        if (!available(node) || !canDeploySelected()) throw new IllegalStateException("Unavailable Journey area");
        selected.capture();
        Hunger hunger = Buff.affect(selected.hero, Hunger.class);
        Bundle hungerState = new Bundle();
        hunger.storeInBundle(hungerState);
        recovering = recovery(node);
        JourneyExpedition cached = fallen.remove(node);
        int position = selected.hero.pos, actions = selected.hero.rebornActions;
        deployed = node;
        expedition = cached == null ? new JourneyExpedition(route.node(node)) : cached;
        expedition.floor = 0;
        nextFloor = -1;
        area = expedition.levels[0];
        entrance = lastGround = expedition.entrances[0];
        reentering = cached != null;
        if (cached != null) {
            selected.hero.pos = entrance;
            selected.hero.rebornActions = cached.actions;
            expedition.retryArena = true;
        }
        Dungeon.level = null;
        JourneyForces previousForces = forces;
        boolean[] previousRest = new boolean[HEROES.length];
        int[] previousHealth = new int[HEROES.length], previousMana = new int[HEROES.length];
        for (int i = 0; i < HEROES.length; i++) {
            Member member = roster.get(HEROES[i]);
            previousRest[i] = member.resting;
            previousHealth[i] = member.hero.HP;
            previousMana[i] = member.hero.MP;
        }
        try {
            hunger.satisfy();
            if (!victory) {
                forces = forces.copy();
                forces.advance(defeatedBosses());
                expedition.attachForces(forces.at(node));
            }
            for (Member member : roster.values()) {
                member.resting = false;
                if (member == selected || member.dead || !member.hero.isAlive()) continue;
                Hero hero = member.hero;
                hero.HP = (int)Math.min(hero.HT, hero.HP + (long)hero.HT * 33 / 100);
                hero.MP = (int)Math.min(hero.MMP, hero.MP + (long)hero.MMP * 33 / 100);
            }
            save();
        } catch (IOException | RuntimeException failure) {
            forces = previousForces;
            for (int i = 0; i < HEROES.length; i++) {
                Member member = roster.get(HEROES[i]);
                member.resting = previousRest[i];
                member.hero.HP = previousHealth[i];
                member.hero.MP = previousMana[i];
            }
            expedition.clearForces();
            hunger.restoreFromBundle(hungerState);
            deployed = -1;
            area = null;
            expedition = null;
            recovering = reentering = false;
            entrance = lastGround = -1;
            selected.hero.pos = position;
            selected.hero.rebornActions = actions;
            if (cached != null) fallen.put(node, cached);
            throw failure;
        }
        InterlevelScene.mode = InterlevelScene.Mode.JOURNEY;
        Game.switchScene(InterlevelScene.class);
    }

    public static synchronized void prepareArea() throws IOException {
        if (!active || deployed < 0 || finished || selected.dead)
            throw new IOException("No deployed Journey hero");
        selected.activate();
        boolean descending = nextFloor > expedition.floor;
        boolean changingFloor = nextFloor >= 0;
        if (changingFloor) {
            Actor.fixTime();
            selected.capture();
            expedition.actions = selected.hero.rebornActions;
            for (Mob mob : area.mobs) mob.sprite = null;
            expedition.floor = nextFloor;
            nextFloor = -1;
            area = expedition.levels[expedition.floor];
        }
        Actor.clear();
        Dungeon.depth = route.node(deployed).depth(expedition.floor);
        if (area == null) {
            area = Dungeon.newJourneyLevel(route.node(deployed), expedition.floor);
            expedition.levels[expedition.floor] = area;
            expedition.entrances[expedition.floor] = area.entrance;
            expedition.populateGuards(route.node(deployed), area);
            selected.hero.pos = area.entrance;
        }
        entrance = expedition.entrances[expedition.floor];
        boolean overChasm = (Terrain.flags[area.map[selected.hero.pos]] & Terrain.PIT) != 0;
        area.removeChasms();
        area.rebuildFlags();
        if (overChasm && !reentering) {
            selected.hero.pos = landing(entrance, selected.hero.pos);
            lastGround = selected.hero.pos;
        }
        if (reentering) {
            try {
                for (int floor = 0; floor < expedition.levels.length; floor++) {
                    Level level = expedition.levels[floor];
                    if (level == null) continue;
                    Dungeon.level = level;
                    Dungeon.depth = route.node(deployed).depth(floor);
                    level.rebuildFlags();
                    for (Mob mob : level.mobs) {
                        mob.sprite = null;
                        mob.restorePartyEnemy(null);
                        RebornChampion champion = mob.buff(RebornChampion.class);
                        if (champion != null) champion.finishAttack();
                    }
                    for (Mob mob : level.mobs) {
                        if (mob instanceof RebornBoss) ((RebornBoss)mob).interruptSkill();
                        if (mob instanceof RebornTengu) ((RebornTengu)mob).shadowBroken();
                    }
                }
            } finally {
                Dungeon.level = area;
                Dungeon.depth = route.node(deployed).depth(expedition.floor);
                area.rebuildFlags();
            }
        }
        if (bossArea() && expedition.retryArena) {
            Dungeon.level = area;
            if (area instanceof SewerBossLevel) ((SewerBossLevel)area).unseal();
            if (area instanceof PrisonBossLevel) ((PrisonBossLevel)area).reenterJourney();
            if (area instanceof CavesBossLevel) ((CavesBossLevel)area).reenterJourney();
            if (area instanceof CityBossLevel) ((CityBossLevel)area).reenterJourney();
            expedition.retryArena = false;
        }
        if (changingFloor || reentering) {
            int stairs = changingFloor && !descending ? area.exit : entrance;
            selected.hero.pos = landing(stairs, stairs);
        }
        reentering = false;
        if ((route.node(deployed).gate || route.node(deployed).finish) && outpostCleared(route.node(deployed).biome))
            expedition.removeGarrison();
        expedition.populateForces(area);
        rewards.place(deployed, expedition.floor, area);
        lastGround = selected.hero.pos;
        SummonedPet.summonedPets = 0;
        for (Mob mob : area.mobs) if (mob instanceof SummonedPet && mob.isAlive()) SummonedPet.summonedPets++;
        GameLog.wipe();
        Dungeon.switchLevel(area, selected.hero.pos);
        save();
    }

    public static boolean descend() {
        if (floorIndex() + 1 == floorCount()) return completeArea();
        changeFloor(floorIndex() + 1);
        return true;
    }

    public static boolean ascend() {
        if (floorIndex() > 0) changeFloor(floorIndex() - 1);
        else if (canReturnToCamp()) requestCamp(false);
        else return false;
        return true;
    }

    private static void changeFloor(int floor) {
        selected.capture();
        selected.hero.curAction = selected.hero.lastAction = null;
        selected.target = null;
        QuickSlot.lastTarget(null);
        nextFloor = floor;
        InterlevelScene.mode = InterlevelScene.Mode.JOURNEY;
        Game.switchScene(InterlevelScene.class);
    }

    public static void rememberPosition(int cell) {
        if (active && area != null && safeGround(cell)) lastGround = cell;
    }

    public static void fall(int cell) {
        Hero hero = selected.hero;
        int destination = landing(safeGround(lastGround) ? lastGround : entrance, cell);
        if (Actor.findChar(hero.pos) == hero) Actor.freeCell(hero.pos);
        hero.pos = destination;
        Actor.occupyCell(hero);
        hero.sprite.place(destination);
        hero.interrupt();
        Dungeon.observe();
    }

    private static boolean safeGround(int cell) {
        return cell >= 0 && cell < Level.LENGTH && Level.passable[cell] && !Level.avoid[cell] && !Level.pit[cell];
    }

    private static int landing(int anchor, int target) {
        boolean[] visited = new boolean[Level.LENGTH];
        int[] queue = new int[Level.LENGTH];
        int first = 0, last = 1, best = -1;
        queue[0] = anchor;
        visited[anchor] = true;
        while (first < last) {
            int cell = queue[first++];
            boolean occupied = false;
            for (Mob mob : area.mobs) if (mob.isAlive() && mob.pos == cell) occupied = true;
            if (!occupied && safeGround(cell) && (best < 0 || Level.distance(cell, target) < Level.distance(best, target)))
                best = cell;
            for (int offset : Level.NEIGHBOURS8) {
                int next = cell + offset;
                if (next < 0 || next >= Level.LENGTH || visited[next] || !Level.adjacent(cell, next) || !safeGround(next)) continue;
                visited[next] = true;
                queue[last++] = next;
            }
        }
        if (best < 0) throw new IllegalStateException("No safe Journey landing");
        return best;
    }

    public static boolean completeArea() {
        if (active && deployed >= 0) {
            if (reinforcementLeaderPending()) {
                return false;
            }
            JourneyRoute.Node node = route.node(deployed);
            if (floorIndex() != node.floors - 1 || !recovering && !objectiveComplete()) {
                return false;
            }
            if (!recovering && node.objective == JourneyRoute.Objective.CHECKPOINT && !expedition.departureUnlocked) {
                departurePass().detachAll(selected.hero.belongings.backpack);
                expedition.departureUnlocked = true;
            }
        }
        completedExit = true;
        requestCamp(false);
        return true;
    }

    private static JourneyPass departurePass() {
        return selected.hero.belongings.getKey(JourneyPass.class, levelKey() - floorIndex() + floorCount() - 1);
    }

    public static synchronized boolean secureCache(Heap heap, Hero hero) throws IOException {
        if (!isCache(heap) || hero != selectedHero() || !hero.isAlive() || area != Dungeon.level
                || area.heaps.get(heap.pos) != heap || !Level.adjacent(hero.pos, heap.pos) && hero.pos != heap.pos) return false;
        if (guardsRemaining() > 0) {
            GLog.w("journey.guards_blocked");
            return false;
        }
        claimObjective();
        for (Item item : new ArrayList<>(heap.items)) if (item instanceof JourneyCache) heap.items.remove(item);
        expedition.cache = null;
        if (heap.isEmpty()) heap.destroy();
        else {
            heap.type = Heap.Type.HEAP;
            if (heap.sprite != null) heap.sprite.link();
        }
        GLog.p("journey.cache_secured");
        return true;
    }

    public static synchronized boolean rescue(JourneyCaptive captive) throws IOException {
        if (!isCaptive(captive) || !selected.hero.isAlive() || !Level.adjacent(selected.hero.pos, captive.pos)
                || guardsRemaining() > 0) return false;
        claimObjective();
        area.mobs.remove(captive);
        Actor.remove(captive);
        if (Actor.findChar(captive.pos) == captive) Actor.freeCell(captive.pos);
        if (captive.sprite != null) captive.sprite.killAndErase();
        expedition.captive = null;
        GLog.p("journey.captive_freed");
        return true;
    }

    private static void claimObjective() throws IOException {
        expedition.claimed = true;
        try {
            save();
        } catch (IOException | RuntimeException failure) {
            expedition.claimed = false;
            throw failure;
        }
    }

    public static void bossSlain() {
        if (!bossArea()) return;
        JourneyRoute.Node node = route.node(deployed);
        expedition.bossDefeated = true;
        if (!expedition.leaderPending()) node.completed = true;
    }

    public static void died() {
        requestCamp(true);
    }

    private static void requestCamp(boolean died) {
        if (!active || deployed < 0 || returning) return;
        returning = true;
        deathPending = died;
        selected.hero.interrupt();
        selected.hero.ready = false;
        selected.hero.next();
        if (died) {
            selected.dead = true;
            leaveBelongings();
            expedition.retreat = true;
            Sample.INSTANCE.play(Assets.SND_DEATH);
            Game.scene().add(new Tweener(selected.hero.sprite, 2.8f) {
                @Override protected void updateValues(float progress) { }
                @Override protected void onComplete() { returnToCamp(); }
            });
        } else returnToCamp();
    }

    private static void returnToCamp() {
        InterlevelScene.mode = InterlevelScene.Mode.JOURNEY_CAMP;
        Game.switchScene(InterlevelScene.class);
    }

    public static synchronized void finishArea() throws IOException {
        if (active && campSavePending) {
            save();
            campSavePending = false;
            return;
        }
        if (!active || deployed < 0 || !returning) throw new IOException("No Journey area to finish");
        boolean rest = completedExit && !deathPending && !recovering && !route.node(deployed).finish;
        Actor.fixTime();
        selected.capture();
        int previousCurrent = current;
        current = deployed;
        if (!recovering && (completedExit || route.node(deployed).completed)) {
            JourneyShop previousShop = shop;
            JourneyRoute.Node node = route.node(current);
            boolean completed = node.completed, previousVictory = victory;
            try {
                shop = shop.copy();
                boolean refreshed = shop.clear(node);
                node.completed = true;
                if (node.finish) victory = true;

                if (refreshed) save();
            } catch (IOException | RuntimeException failure) {
                shop = previousShop;
                current = previousCurrent;
                node.completed = completed;
                victory = previousVictory;
                throw failure;
            }
        }
        expedition.actions = selected.hero.rebornActions;
        expedition.clearForces();
        if (deathPending || !route.node(deployed).completed || hasRemains(deployed)) fallen.put(deployed, expedition);
        clearArea();
        if (selected.dead) {
            Member survivor = null;
            for (HeroClass heroClass : HEROES) if (!roster.get(heroClass).dead) {
                survivor = roster.get(heroClass);
                break;
            }
            if (survivor == null) finished = true;
            else selected = survivor;
        }
        if (!selected.dead) {
            boolean remains = false;
            for (int node : fallen.keySet()) if (hasRemains(node)) remains = true;
            finished = victory && !remains;
        }
        selected.activate();
        if (rest) selected.resting = true;
        clearRestForLastHero();
        campSavePending = true;
        save();
        campSavePending = false;
    }

    private static void clearArea() {
        if (route.node(deployed).completed) {
            int firstKey = levelKey() - floorIndex();
            ArrayList<Item> keys = new ArrayList<>();
            Bag backpack = selected.hero.belongings.backpack;
            for (Item item : backpack) if (item instanceof Key && ((Key)item).depth >= firstKey
                    && ((Key)item).depth < firstKey + floorCount()) keys.add(item);
            for (Item item : keys) item.detachAll(backpack);
        }
        deployed = -1;
        area = null;
        expedition = null;
        nextFloor = -1;
        returning = deathPending = false;
        recovering = reentering = completedExit = false;
        entrance = lastGround = -1;
        Actor.clear();
        Dungeon.level = null;
        for (Member member : roster.values()) {
            member.hero.sprite = null;
            member.hero.curAction = member.hero.lastAction = null;
            member.hero.restoreHealth = false;
            member.summons = 0;
            member.target = null;
        }
        IronKey.curDepthQuantity = 0;
    }

    private static void leaveBelongings() {
        Hero hero = selected.hero;
        Belongings belongings = hero.belongings;
        LinkedHashSet<Item> items = new LinkedHashSet<>(belongings.backpack.items);
        for (Item item : new Item[]{belongings.weapon, belongings.armor, belongings.ring1, belongings.ring2, belongings.bow})
            if (item != null) items.add(item);
        belongings.weapon = null;
        belongings.armor = null;
        belongings.ring1 = belongings.ring2 = null;
        belongings.bow = null;
        belongings.backpack.items.clear();
        for (Item item : new ArrayList<>(items)) {
            if (Bag.isHolder(item)) {
                Bag bag = (Bag)item;
                bag.onDetach();
                items.remove(item);
                items.addAll(bag.items);
                bag.items.clear();
            } else if (item instanceof DewVial) {
                items.remove(item);
            }
        }
        for (Item item : items) {
            if (item instanceof Ring) ((Ring)item).deactivate();
            if (item instanceof Wand) ((Wand)item).stopCharging();
            if (item instanceof Bag) {
                ((Bag)item).onDetach();
                ((Bag)item).owner = null;
                for (Item content : (Bag)item) {
                    if (content instanceof Bag) {
                        ((Bag)content).onDetach();
                        ((Bag)content).owner = null;
                    }
                    if (content instanceof Wand) ((Wand)content).stopCharging();
                }
            }
        }
        QuickSlot.resetSlots();
        selected.capture();
        selected.target = null;
        for (Level level : expedition.levels) if (level != null)
            for (Mob mob : new ArrayList<>(level.mobs)) if (mob instanceof SummonedPet) {
                level.mobs.remove(mob);
                if (level == area) {
                    Actor.remove(mob);
                    if (Actor.findChar(mob.pos) == mob) Actor.freeCell(mob.pos);
                }
            }
        if (items.isEmpty()) return;
        Heap heap = area.heaps.get(hero.pos);
        if (heap == null) {
            heap = new Heap();
            heap.pos = hero.pos;
            area.heaps.put(hero.pos, heap);
        }
        for (Item item : items) {
            item.journeyRemains = true;
            item.droppedOnDeath = item.droppedByHero = true;
            heap.items.addFirst(item);
        }
        if (Game.scene() instanceof GameScene) {
            GameScene.deathDrops(heap, items);
        }
    }

    public static boolean shareExperience(Hero source, int experience) {
        if (!active || source != selectedHero() || deployed < 0) return false;
        selected.capture();
        Member deployedHero = selected;
        try {
            for (HeroClass heroClass : HEROES) {
                Member member = roster.get(heroClass);
                if (member.dead) continue;
                member.activate();
                member.hero.gainExperience(experience, member == deployedHero);
                member.capture();
            }
        } finally {
            deployedHero.activate();
        }
        return true;
    }

    public static synchronized void save() throws IOException {
        if (!active || leaving || discarded) return;
        if (selected == null) throw new IOException("Journey roster is not ready");
        if (finished && !victory) {
            deleteSlot(slot);
            discarded = true;
            return;
        }
        selected.capture();
        Bundle save = new Bundle();
        save.put("journey_version", VERSION);
        save.put("journey_slot", slot);
        save.put("revision", revision + 1);
        route.store(save);
        Bundle storedForces = new Bundle();
        forces.store(storedForces);
        save.put("forces", storedForces);
        Bundle storedRewards = new Bundle();
        rewards.store(storedRewards);
        save.put("rewards", storedRewards);
        save.put("current", current);
        save.put("deployed", deployed);
        save.put("finished", finished);
        save.put("victory", victory);
        save.put("returning", returning);
        save.put("death_pending", deathPending);
        save.put("entrance", entrance);
        save.put("last_ground", lastGround);
        save.put("recovering", recovering);
        save.put("reentering", reentering);
        save.put("completed_exit", completedExit);
        save.put("selected", selectedClass().name());
        Bundle world = new Bundle();
        Dungeon.storePartyWorld(world);
        save.put("world", world);
        Member activeHero = selected;
        try {
            for (HeroClass heroClass : HEROES) {
                Member member = roster.get(heroClass);
                member.activate();
                Bundle state = new Bundle();
                QuickSlot.save(state);
                state.put("hero", member.hero);
                state.put("dead", member.dead);
                state.put("resting", member.resting);
                state.put("clock", member.clock);
                state.put("summons", member.summons);
                save.put("hero_" + heroClass.name(), state);
            }
            QuickSlot.resetSlots();
            Bundle storedChest = new Bundle();
            chest.storeInBundle(storedChest);
            save.put("chest", storedChest);
            Bundle storedShop = new Bundle();
            shop.store(storedShop);
            save.put("shop", storedShop);
            if (expedition != null) {
                Bundle state = new Bundle();
                expedition.store(state);
                save.put("expedition", state);
            }
            save.put("fallen_count", fallen.size());
            int index = 0;
            for (Map.Entry<Integer, JourneyExpedition> entry : fallen.entrySet()) {
                Bundle state = new Bundle();
                state.put("node", entry.getKey());
                entry.getValue().store(state);
                save.put("fallen_" + index++, state);
            }
        } finally {
            activeHero.activate();
        }
        try (OutputStream output = Game.instance.openFileOutput(filename(slot, revision + 1), Game.MODE_PRIVATE)) {
            if (!Bundle.write(save, output)) throw new IOException("Unable to write Journey save");
        }
        revision++;
        if (victory) Badges.validateJourneyComplete();
    }

    private static Bundle readSave(String filename, int slot) throws IOException {
        Bundle bundle = Dungeon.gameBundle(filename);
        if (bundle.getInt("journey_version") != VERSION || bundle.getInt("revision") <= 0)
            throw new IOException("Unsupported Journey save");
        if (bundle.contains("journey_slot") ? bundle.getInt("journey_slot") != slot : slot != FIRST_SLOT)
            throw new IOException("Invalid Journey slot");
        if (bundle.getBoolean("deleted")) return bundle;
        JourneyRoute.restore(bundle);
        for (HeroClass heroClass : HEROES) {
            Bundle member = bundle.getBundle("hero_" + heroClass.name());
            if (member.isNull() || !member.contains("dead") || member.getBundle("hero").isNull())
                throw new IOException("Missing Journey roster");
        }
        return bundle;
    }

    private static Bundle latestSave(int slot) throws IOException {
        Bundle latest = null;
        IOException failure = null;
        for (int index = 0; index < 2; index++) {
            try {
                Bundle save = readSave(filename(slot, index), slot);
                if (latest == null || save.getInt("revision") > latest.getInt("revision")) latest = save;
            } catch (IOException invalid) {
                failure = invalid;
            }
        }
        if (latest == null) throw new IOException("Unable to read Journey save", failure);
        return latest;
    }

    private static int latestRevision(int slot) {
        try { return latestSave(slot).getInt("revision"); }
        catch (IOException absent) { return 0; }
    }

    public static synchronized void delete(int slot) throws IOException {
        validateSlot(slot);
        deleteSlot(slot);
        if (active && JourneyRun.slot == slot) {
            discarded = true;
            close();
        }
    }

    private static void deleteSlot(int slot) throws IOException {
        if (Game.instance == null) throw new IOException("Journey storage is unavailable");
        if (!exists(slot)) return;
        int markerRevision = latestRevision(slot) + 1;
        Bundle marker = new Bundle();
        marker.put("journey_version", VERSION);
        marker.put("journey_slot", slot);
        marker.put("revision", markerRevision);
        marker.put("deleted", true);
        String markerFile = filename(slot, markerRevision), olderFile = filename(slot, markerRevision + 1);
        try (OutputStream output = Game.instance.openFileOutput(markerFile, Game.MODE_PRIVATE)) {
            if (!Bundle.write(marker, output)) throw new IOException("Unable to delete Journey save");
        }

        deleteFile(olderFile);
        deleteFile(markerFile);
    }

    private static void deleteFile(String filename) throws IOException {
        for (String file : Game.instance.fileList()) if (filename.equals(file)) {
            if (!Game.instance.deleteFile(filename)) throw new IOException("Unable to delete Journey save");
            return;
        }
    }

    public static synchronized void leave() throws IOException {
        if (!active) return;
        if (!leaving) save();
        close();
    }

    public static synchronized void prepareLeave() throws IOException {
        if (!active || leaving) return;
        Actor.fixTime();
        save();
        leaving = true;
    }

    private static void close() {
        Actor.clear();
        Dungeon.hero = null;
        Dungeon.level = null;
        active = false;
        leaving = false;
        Badges.clearJourneyNotification();
        if (restoreGlobals != null) restoreGlobals.run();
        restoreGlobals = null;
        selected = null;
        roster.clear();
        chest = null;
        shop = null;
        route = null;
        forces = null;
        rewards = null;
        area = null;
        expedition = null;
        nextFloor = -1;
        fallen.clear();
        recovering = reentering = false;
        campSavePending = false;
        entrance = lastGround = -1;
        deployed = -1;
        slot = 0;
        discarded = false;
        InterlevelScene.mode = InterlevelScene.Mode.NONE;
    }

    private static Runnable snapshot() {
        final Runnable dungeon = Dungeon.enterTemporaryRun();
        final Runnable difficulty = Difficulties.NORMAL.useDefaultsTemporarily();
        final int points = Skill.availableSkill, summons = SummonedPet.summonedPets;
        final float clock = Skill.skillTrack;
        final HeroClass heroClass = StartScene.curClass;
        final int skin = HeroSkins.localChoice;
        final Object[] slots = QuickSlot.values.clone();
        final Char target = QuickSlot.lastTarget();
        final WandOfMagicCasting wand = Hero.haxWand;
        final Bundle statistics = new Bundle(), journal = new Bundle(), badges = new Bundle();
        final Bundle identities = new Bundle(), world = new Bundle();
        final boolean hadJournal = Journal.records != null, hadHero = Dungeon.hero != null;
        final boolean qualified = Statistics.qualifiedForNoKilling, completed = Statistics.completedWithNoKilling;
        final int encounterSeed = Dungeon.rebornEncounterSeed, challengeBosses = Dungeon.challengeBosses;
        final int strength = Dungeon.potionOfStrength, upgrades = Dungeon.scrollsOfUpgrade;
        final int enchantments = Dungeon.scrollsOfEnchantment, keys = IronKey.curDepthQuantity;
        final boolean dew = Dungeon.dewVial;
        final SparseArray<ArrayList<Item>> dropped = Dungeon.droppedItems;
        final HashSet<Integer> chapters = Dungeon.chapters;
        final String result = Dungeon.resultDescription, cause = Dungeon.resultCauseClassName;
        Statistics.storeInBundle(statistics);
        if (hadJournal) Journal.storeInBundle(journal);
        Badges.saveLocal(badges);
        if (hadHero) {
            Scroll.save(identities);
            Potion.save(identities);
            Wand.save(identities);
            Ring.save(identities);
        }
        Room.storeRoomsInBundle(world);
        RivalEncounter.store(world);
        IceCaves.store(world);
        Ghost.Quest.storeInBundle(world);
        Wandmaker.Quest.storeInBundle(world);
        Blacksmith.Quest.storeInBundle(world);
        Imp.Quest.storeInBundle(world);
        return () -> {
            dungeon.run();
            difficulty.run();
            Skill.availableSkill = points;
            Skill.skillTrack = clock;
            SummonedPet.summonedPets = summons;
            StartScene.curClass = heroClass;
            HeroSkins.localChoice = skin;
            QuickSlot.values = slots;
            QuickSlot.lastTarget(target);
            Hero.haxWand = wand;
            Statistics.restoreFromBundle(statistics);
            Statistics.qualifiedForNoKilling = qualified;
            Statistics.completedWithNoKilling = completed;
            if (hadJournal) Journal.restoreFromBundle(journal);
            else Journal.records = null;
            Badges.loadLocal(badges);
            if (hadHero) {
                Scroll.restore(identities);
                Potion.restore(identities);
                Wand.restore(identities);
                Ring.restore(identities);
            }
            Room.restoreRoomsFromBundle(world);
            RivalEncounter.restore(world);
            try { IceCaves.restore(world); }
            catch (IOException invalid) { throw new IllegalStateException(invalid); }
            Ghost.Quest.restoreFromBundle(world);
            Wandmaker.Quest.restoreFromBundle(world);
            Blacksmith.Quest.restoreFromBundle(world);
            Imp.Quest.restoreFromBundle(world);
            Dungeon.rebornEncounterSeed = encounterSeed;
            Dungeon.challengeBosses = challengeBosses;
            Dungeon.potionOfStrength = strength;
            Dungeon.scrollsOfUpgrade = upgrades;
            Dungeon.scrollsOfEnchantment = enchantments;
            Dungeon.dewVial = dew;
            IronKey.curDepthQuantity = keys;
            Dungeon.droppedItems = dropped;
            Dungeon.chapters = chapters;
            Dungeon.resultDescription = result;
            Dungeon.resultCauseClassName = cause;
        };
    }
}
