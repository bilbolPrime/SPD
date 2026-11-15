package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Healing;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Wandmaker;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.TalentSkill;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.potions.Potion;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfAccuracy;
import com.bilboldev.pixeldungeonskills.items.scrolls.Scroll;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.TutorialLevel;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.scenes.InterlevelScene;
import com.bilboldev.pixeldungeonskills.scenes.StartScene;
import com.bilboldev.pixeldungeonskills.scenes.TitleScene;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.SparseArray;
import java.util.ArrayList;
import java.util.HashSet;

public final class TutorialRun {
    public static final int LESSONS = 8;
    public enum Guide { NONE, WAIT, INSPECT, INVENTORY, SKILLS, QUICKSLOT, SEARCH }
    private static boolean active, startRequested, retryRequested, leaving, passed, advanceRequested;
    private static int lesson, moves, smashReference = -1;
    private static boolean waited, normalInspected, championInspected, armorEquipped, ringIdentified;
    private static boolean quickslotUsed, smashLanded, drankHealing, ateFood;
    private static Skill selectedQuickslot;
    private static Runnable restoreDungeon, restoreDifficulty;
    private static Bundle statistics, journal, badges, identities, world;
    private static Object[] previousSlots;
    private static Char previousTarget;
    private static HeroClass previousClass;
    private static int previousSkin, previousSkills, previousSummons;
    private static float previousSkillTrack;
    private static boolean previousQualified, previousCompleted;
    private static int previousEncounterSeed, previousChallengeBosses;
    private static int previousStrengthPotions, previousUpgradeScrolls, previousEnchantmentScrolls, previousIronKeys;
    private static boolean previousDewVial;
    private static SparseArray<ArrayList<Item>> previousDropped;
    private static HashSet<Integer> previousChapters;
    private static String previousResult, previousCause;

    private TutorialRun() { }

    public static boolean active() { return active; }
    public static int lesson() { return lesson; }
    public static boolean completed() { return active && lesson == LESSONS - 1 && passed; }
    public static boolean canAdvance() { return acceptsInput() && passed && !advanceRequested && lesson < LESSONS - 1; }
    public static String objective() {
        String key = "tutorial.lesson_" + lesson + ".goal";
        return lesson == 4 ? Messages.get(key, (Object)Talent.SMASH.title()) : Messages.get(key);
    }
    public static boolean acceptsInput() {
        return active && !leaving && !retryRequested && Dungeon.hero != null && Dungeon.hero.isAlive();
    }
    public static TutorialLevel level() {
        return active && Dungeon.level instanceof TutorialLevel ? (TutorialLevel)Dungeon.level : null;
    }

    public static Guide guideAction() {
        TutorialLevel floor = level();
        if (floor == null || !acceptsInput() || passed || advanceRequested) return Guide.NONE;
        switch (lesson) {
            case 0: return waited ? Guide.NONE : Guide.WAIT;
            case 1: return !normalInspected || !championInspected ? Guide.INSPECT : Guide.NONE;
            case 3:
                return !armorEquipped && Dungeon.hero.belongings.backpack.items.contains(floor.leather)
                        || !ringIdentified && Dungeon.hero.belongings.backpack.items.contains(floor.identify)
                        ? Guide.INVENTORY : Guide.NONE;
            case 4:
                TalentSkill smash = Dungeon.hero.talents().skill(Talent.SMASH);
                if (smash.level == 0 || guideQuickslot() < 0) return Guide.SKILLS;
                if (smash.coolDown() || !Dungeon.hero.canUseSkill(smash.getManaCost())) return Guide.WAIT;
                return smash.active && quickslotUsed ? Guide.NONE : Guide.QUICKSLOT;
            case 5:
                if (!drankHealing && Dungeon.hero.belongings.backpack.items.contains(floor.healing)
                        || !ateFood && Dungeon.hero.belongings.backpack.items.contains(floor.food)) return Guide.INVENTORY;
                return drankHealing && ateFood ? Guide.WAIT : Guide.NONE;
            case 6:
                if (!floor.plantInspected || !floor.trapInspected) return Guide.INSPECT;
                return floor.awaitingWarning() ? Guide.WAIT : Guide.NONE;
            case 7:
                return floor.combatFinished() && !floor.doorSearched()
                        && Level.adjacent(Dungeon.hero.pos, TutorialLevel.SEARCH_DOOR)
                        ? Guide.SEARCH : Guide.NONE;
            default: return Guide.NONE;
        }
    }

    public static int guideQuickslot() {
        if (level() == null || !acceptsInput() || passed || advanceRequested || lesson != 4) return -1;
        TalentSkill smash = Dungeon.hero.talents().skill(Talent.SMASH);
        if (smash.level == 0) return -1;
        if (smashReference < 0) {
            Skill[] skills = LocalCoop.skills(Dungeon.hero);
            for (int i = 0; i < skills.length; i++) if (skills[i] == smash) smashReference = i;
        }
        for (int i = 0; i < QuickSlot.visibleSlots(); i++) {
            Object value = QuickSlot.values[i];
            if (value instanceof Integer && (Integer)value == smashReference) return i;
        }
        return -1;
    }

    public static int guideCell() {
        TutorialLevel floor = level();
        if (floor == null || !acceptsInput() || passed || advanceRequested) return -1;
        switch (lesson) {
            case 0:
                if (moves >= 3) return -1;
                int firstStep = 5 + 10 * TutorialLevel.WIDTH;
                return Dungeon.hero.pos == firstStep ? firstStep + 1 : firstStep;
            case 1: return !normalInspected ? floor.normalRat.pos : !championInspected ? floor.championRat.pos : -1;
            case 3:
                if (!armorEquipped && !Dungeon.hero.belongings.backpack.items.contains(floor.leather)) return 25 + 5 * TutorialLevel.WIDTH;
                if (!ringIdentified && !Dungeon.hero.belongings.backpack.items.contains(floor.ring)
                        && !floor.ring.isEquipped(Dungeon.hero)) return 25 + 9 * TutorialLevel.WIDTH;
                if (!ringIdentified && !Dungeon.hero.belongings.backpack.items.contains(floor.identify)) return 27 + 9 * TutorialLevel.WIDTH;
                return -1;
            case 4: return guideAction() == Guide.NONE ? floor.skillRat.pos : -1;
            case 5:
                if (!drankHealing && !Dungeon.hero.belongings.backpack.items.contains(floor.healing)) return 18 + 20 * TutorialLevel.WIDTH;
                if (!ateFood && !Dungeon.hero.belongings.backpack.items.contains(floor.food)) return 20 + 20 * TutorialLevel.WIDTH;
                return -1;
            default: return floor.guideCell();
        }
    }

    public static void start() {
        PartySession.cancelLobby();
        startRequested = true;
        InterlevelScene.mode = InterlevelScene.Mode.TUTORIAL;
        Game.switchScene(InterlevelScene.class);
    }

    public static boolean prepare() {
        if (active && !startRequested && !retryRequested) return false;
        boolean restart = active && retryRequested && !startRequested;
        if (!restart) {
            if (active) finish();
            DailyRun.leave();
            GauntletRun.finish();
            LocalCoop.clear();
            snapshot();
            active = true;
            lesson = 0;
        }
        startRequested = retryRequested = leaving = false;
        try {
            Dungeon.prepareNewRun(SaveSlots.FIRST, GameplayMode.REBORN, Difficulties.NORMAL.ordinal());
            Dungeon.seed = new RunSeed(0, true);
            StartScene.curClass = HeroClass.WARRIOR;
            HeroSkins.localChoice = 0;
            SummonedPet.summonedPets = 0;
            Dungeon.init();
            Dungeon.challenges = 0;
            Dungeon.depth = 1;
            Dungeon.hero.tutorial = true;
            Dungeon.hero.belongings.backpack.items.clear();
            Dungeon.hero.belongings.weapon.attributes = EquipmentAttribute.NONE;
            Dungeon.hero.belongings.armor.attributes = EquipmentAttribute.NONE;
            Skill.availableSkill = 0;
            if (lesson >= 4) Dungeon.hero.belongings.armor = (LeatherArmor)new LeatherArmor().identify();
            if (lesson >= 4) new RingOfAccuracy().identify().collect();
            if (lesson >= 5) {
                Dungeon.hero.talents().skill(Talent.SMASH).level = 1;
                QuickSlot.bind(Dungeon.hero.talents().skill(Talent.SMASH), 0);
            }
            resetExercise();
            TutorialLevel level = new TutorialLevel();
            level.create();
            Dungeon.switchLevel(level, TutorialLevel.entry(lesson));
            return true;
        } catch (RuntimeException failure) {
            finish();
            throw failure;
        }
    }

    private static void snapshot() {
        previousClass = StartScene.curClass;
        previousSkin = HeroSkins.localChoice;
        previousSkills = Skill.availableSkill;
        previousSkillTrack = Skill.skillTrack;
        previousSummons = SummonedPet.summonedPets;
        previousSlots = QuickSlot.values.clone();
        previousTarget = QuickSlot.lastTarget();
        previousQualified = Statistics.qualifiedForNoKilling;
        previousCompleted = Statistics.completedWithNoKilling;
        previousEncounterSeed = Dungeon.rebornEncounterSeed;
        previousChallengeBosses = Dungeon.challengeBosses;
        previousStrengthPotions = Dungeon.potionOfStrength;
        previousUpgradeScrolls = Dungeon.scrollsOfUpgrade;
        previousEnchantmentScrolls = Dungeon.scrollsOfEnchantment;
        previousDewVial = Dungeon.dewVial;
        previousIronKeys = IronKey.curDepthQuantity;
        previousDropped = Dungeon.droppedItems;
        previousChapters = Dungeon.chapters;
        previousResult = Dungeon.resultDescription;
        previousCause = Dungeon.resultCauseClassName;
        statistics = new Bundle();
        Statistics.storeInBundle(statistics);
        journal = null;
        if (Journal.records != null) {
            journal = new Bundle();
            Journal.storeInBundle(journal);
        }
        badges = new Bundle();
        Badges.saveLocal(badges);
        identities = null;
        if (Dungeon.hero != null) {
            identities = new Bundle();
            Scroll.save(identities);
            Potion.save(identities);
            Wand.save(identities);
            Ring.save(identities);
        }
        world = new Bundle();
        Room.storeRoomsInBundle(world);
        RivalEncounter.store(world);
        IceCaves.store(world);
        Ghost.Quest.storeInBundle(world);
        Wandmaker.Quest.storeInBundle(world);
        Blacksmith.Quest.storeInBundle(world);
        Imp.Quest.storeInBundle(world);
        restoreDifficulty = Difficulties.NORMAL.useDefaultsTemporarily();
        restoreDungeon = Dungeon.enterTemporaryRun();
    }

    public static void leave() {
        if (!active) return;
        leaving = true;
        Dungeon.hero.interrupt();
        Dungeon.hero.ready = false;
        Game.switchScene(TitleScene.class);
    }

    public static void finish() {
        if (!active) return;
        Actor.clear();
        LocalCoop.clear();
        Dungeon.hero = null;
        Dungeon.level = null;
        restoreDungeon.run();
        restoreDifficulty.run();
        Statistics.restoreFromBundle(statistics);
        Statistics.qualifiedForNoKilling = previousQualified;
        Statistics.completedWithNoKilling = previousCompleted;
        if (journal != null) Journal.restoreFromBundle(journal);
        else Journal.records = null;
        Badges.loadLocal(badges);
        if (identities != null) {
            Scroll.restore(identities);
            Potion.restore(identities);
            Wand.restore(identities);
            Ring.restore(identities);
        }
        Room.restoreRoomsFromBundle(world);
        RivalEncounter.restore(world);
        try { IceCaves.restore(world); }
        catch (java.io.IOException invalid) { throw new IllegalStateException(invalid); }
        Ghost.Quest.restoreFromBundle(world);
        Wandmaker.Quest.restoreFromBundle(world);
        Blacksmith.Quest.restoreFromBundle(world);
        Imp.Quest.restoreFromBundle(world);
        Dungeon.rebornEncounterSeed = previousEncounterSeed;
        Dungeon.challengeBosses = previousChallengeBosses;
        Dungeon.potionOfStrength = previousStrengthPotions;
        Dungeon.scrollsOfUpgrade = previousUpgradeScrolls;
        Dungeon.scrollsOfEnchantment = previousEnchantmentScrolls;
        Dungeon.dewVial = previousDewVial;
        IronKey.curDepthQuantity = previousIronKeys;
        Dungeon.droppedItems = previousDropped;
        Dungeon.chapters = previousChapters;
        Dungeon.resultDescription = previousResult;
        Dungeon.resultCauseClassName = previousCause;
        StartScene.curClass = previousClass;
        HeroSkins.localChoice = previousSkin;
        Skill.availableSkill = previousSkills;
        Skill.skillTrack = previousSkillTrack;
        SummonedPet.summonedPets = previousSummons;
        QuickSlot.resetSlots();
        System.arraycopy(previousSlots, 0, QuickSlot.values, 0, previousSlots.length);
        QuickSlot.lastTarget(previousTarget);
        active = startRequested = retryRequested = leaving = false;
        restoreDungeon = restoreDifficulty = null;
        statistics = journal = badges = identities = world = null;
        previousSlots = null;
        previousTarget = null;
        selectedQuickslot = null;
        previousDropped = null;
        previousChapters = null;
    }

    public static void retry() {
        if (!active || leaving) return;
        retryRequested = true;
        Dungeon.hero.interrupt();
        Dungeon.hero.ready = false;
        InterlevelScene.mode = InterlevelScene.Mode.TUTORIAL;
        Game.switchScene(InterlevelScene.class);
    }

    public static void died(Hero hero) {
        if (active && Dungeon.hero == hero) retry();
    }

    public static boolean valid(Hero hero) {
        return !hero.tutorial || active && !leaving && !retryRequested && Dungeon.hero == hero;
    }

    public static void advance() {
        if (!canAdvance()) return;
        advanceRequested = true;
        level().openGate(lesson);
    }

    private static void resetExercise() {
        passed = advanceRequested = false;
        moves = 0;
        smashReference = -1;
        waited = normalInspected = championInspected = armorEquipped = ringIdentified = false;
        quickslotUsed = smashLanded = drankHealing = ateFood = false;
        selectedQuickslot = null;
    }

    public static void update() {
        TutorialLevel floor = level();
        if (floor == null || !acceptsInput()) return;
        if (advanceRequested && TutorialLevel.room(Dungeon.hero.pos) == lesson + 1) {
            lesson++;
            resetExercise();
            floor.beginLesson(lesson);
        }
        floor.updateExercise();
        if (passed) return;
        switch (lesson) {
            case 0: passed = moves >= 3 && waited; break;
            case 1: passed = normalInspected && championInspected; break;
            case 2: passed = floor.combatFinished(); break;
            case 3: passed = armorEquipped && ringIdentified; break;
            case 4: passed = smashLanded; break;
            case 5:
                passed = drankHealing && ateFood && Dungeon.hero.buff(Healing.class) == null
                        && Dungeon.hero.HP > floor.injuredHealth;
                break;
            case 6: passed = floor.hazardsFinished(); break;
            case 7: passed = floor.combatFinished() && floor.searchFinished() && floor.finalDoorOpen()
                    && Dungeon.hero.pos == floor.exit; break;
        }
    }

    public static void moved(Hero hero, int from) {
        if (valid(hero) && active && hero == Dungeon.hero && from != hero.pos && lesson == 0) moves++;
    }

    public static void waited(Hero hero) {
        if (active && valid(hero) && hero == Dungeon.hero && lesson == 0) waited = true;
    }

    public static void inspect(Mob mob) {
        TutorialLevel floor = level();
        if (floor == null || lesson != 1) return;
        if (mob == floor.normalRat) normalInspected = true;
        if (mob == floor.championRat) championInspected = true;
    }

    public static void inspect(Plant plant) {
        TutorialLevel floor = level();
        if (floor != null && lesson == 6 && floor.plants.get(plant.pos) == plant) floor.plantInspected = true;
    }

    public static void inspect(int cell) {
        TutorialLevel floor = level();
        if (floor != null && lesson == 6 && cell == TutorialLevel.FIRE_TRAP) floor.trapInspected = true;
    }

    public static void equipped(Hero hero, Item item) {
        TutorialLevel floor = level();
        if (floor != null && valid(hero) && lesson == 3 && item == floor.leather && floor.leather.STR <= hero.STR()) armorEquipped = true;
    }

    public static void identified(Hero hero, Item item, Item scroll) {
        TutorialLevel floor = level();
        if (floor != null && valid(hero) && lesson == 3 && item == floor.ring && scroll == floor.identify) ringIdentified = true;
    }

    public static void quickslot(Hero hero, Skill skill) {
        if (active && valid(hero) && lesson == 4) selectedQuickslot = skill;
    }

    public static void skillActivated(Hero hero, TalentSkill skill) {
        if (!active || !valid(hero) || lesson != 4) return;
        quickslotUsed = skill == selectedQuickslot && skill.talent == Talent.SMASH;
        selectedQuickslot = null;
    }

    public static void skillHit(Hero hero, Talent talent, Char target, int before) {
        TutorialLevel floor = level();
        if (floor != null && valid(hero) && lesson == 4 && quickslotUsed && talent == Talent.SMASH
                && target == floor.skillRat && before > 0 && target.isAlive() && floor.mobs.contains(target)) smashLanded = true;
    }

    public static void searched(Hero hero) {
        TutorialLevel floor = level();
        if (floor != null && valid(hero) && lesson == 7) floor.searched(hero);
    }

    public static void healing(Hero hero, Item item) {
        TutorialLevel floor = level();
        if (floor != null && valid(hero) && lesson == 5 && item == floor.healing) {
            drankHealing = true;
        }
    }

    public static void ate(Hero hero, Item item) {
        TutorialLevel floor = level();
        if (floor != null && valid(hero) && lesson == 5 && item == floor.food) ateFood = true;
    }
}
