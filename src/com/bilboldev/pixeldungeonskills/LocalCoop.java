package com.bilboldev.pixeldungeonskills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Light;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroAction;
import com.bilboldev.pixeldungeonskills.actors.mobs.ColdGirl;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.actors.skills.CurrentSkills;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.SkillContainer;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicCasting;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfBlink;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Callback;

public final class LocalCoop {
    public interface Action<T> {
        T run();
    }

    private static HeroClass secondClass;
    private static Hero[] party = new Hero[0];
    private static Hero selected;
    private static final IdentityHashMap<Hero, Controls> controls = new IdentityHashMap<>();
    private static final HashMap<Integer, Level> floors = new HashMap<>();
    private static boolean runningCommand;
    private static Hero clockOwner;
    private static int clockOffset;
    private static int actionClock;
    private static boolean teleporting;
    private static boolean gathering;
    private static boolean restoring;

    private static class Controls {
        int points = Skill.STARTING_SKILL;
        float clock;
        Object[] quickSlots = new Object[QuickSlot.MAX_SLOTS];
        Char target;
        WandOfMagicCasting wand = new WandOfMagicCasting();
        Runnable command;
        boolean pendingCommand;
        Skills skills;
        Bundle pendingActions;

        void capture() {
            points = Skill.availableSkill;
            clock = Skill.skillTrack;
            quickSlots = QuickSlot.values.clone();
            target = QuickSlot.lastTarget();
            wand = Hero.haxWand;
            if (skills == null) skills = new Skills();
            skills.capture(Dungeon.hero.heroSkills);
        }

        void restore() {
            Skill.availableSkill = points;
            Skill.skillTrack = clock;
            QuickSlot.values = quickSlots.clone();
            QuickSlot.lastTarget(target);
            Hero.haxWand = wand;
            if (skills != null) skills.restore(Dungeon.hero.heroSkills);
        }
    }

    private static class Skills {
        Skill branchPA, passiveA1, passiveA2, passiveA3;
        Skill branchPB, passiveB1, passiveB2, passiveB3;
        Skill branchA, active1, active2, active3, lastUsed;
        boolean unlocked;

        void capture(CurrentSkills current) {
            branchPA = current.branchPA;
            passiveA1 = current.passiveA1;
            passiveA2 = current.passiveA2;
            passiveA3 = current.passiveA3;
            branchPB = current.branchPB;
            passiveB1 = current.passiveB1;
            passiveB2 = current.passiveB2;
            passiveB3 = current.passiveB3;
            branchA = current.branchA;
            active1 = current.active1;
            active2 = current.active2;
            active3 = current.active3;
            lastUsed = current.lastUsed;
            unlocked = current.skillUnlocked;
        }

        void restore(CurrentSkills current) {
            current.branchPA = branchPA;
            current.passiveA1 = passiveA1;
            current.passiveA2 = passiveA2;
            current.passiveA3 = passiveA3;
            current.branchPB = branchPB;
            current.passiveB1 = passiveB1;
            current.passiveB2 = passiveB2;
            current.passiveB3 = passiveB3;
            current.branchA = branchA;
            current.active1 = active1;
            current.active2 = active2;
            current.active3 = active3;
            current.lastUsed = lastUsed;
            current.skillUnlocked = unlocked;
        }
    }

    private LocalCoop() { }

    public static void prepare(HeroClass first, HeroClass second) {
        if (first == null || second == null
                || Dungeon.rules().mode != GameplayMode.REBORN) {
            throw new IllegalArgumentException("A Reborn party needs two heroes");
        }
        clear();
        secondClass = second;
    }

    public static void clear() {
        secondClass = null;
        party = new Hero[0];
        selected = null;
        controls.clear();
        floors.clear();
        runningCommand = false;
        clockOwner = null;
        clockOffset = 0;
        actionClock = 0;
        teleporting = false;
        gathering = false;
        restoring = false;
    }

    public static boolean active() {
        return secondClass != null;
    }

    public static Hero[] heroes() {
        return party.clone();
    }

    public static Hero primary() {
        return party.length == 0 ? Dungeon.hero : party[0];
    }

    public static boolean contains(Char hero) {
        return controls.containsKey(hero);
    }

    public static Hero partner(Hero hero) {
        if (party.length != 2) return null;
        return hero == party[0] ? party[1] : party[0];
    }

    public static void initialize() {
        if (!active()) return;
        Hero first = Dungeon.hero;
        first.heroSkills.skillUnlocked = false;
        Controls firstControls = new Controls();
        firstControls.capture();
        controls.put(first, firstControls);

        Hero second = new Hero();
        Controls secondControls = new Controls();
        controls.put(second, secondControls);
        party = new Hero[] { first, second };
        activate(second);
        second.difficulty = Dungeon.difficulty;
        second.live();
        secondClass.initHero(second);
        second.heroSkills.skillUnlocked = false;
        selected = first;
        activate(first);
        clockOwner = first;
    }

    public static void activate(Hero hero) {
        if (!contains(hero) || Dungeon.hero == hero) return;
        Controls previous = controls.get(Dungeon.hero);
        if (previous != null) previous.capture();
        Dungeon.hero = hero;
        controls.get(hero).restore();
    }

    public static void withHero(Hero hero, Runnable action) {
        Hero previous = Dungeon.hero;
        activate(hero);
        try {
            action.run();
        } finally {
            activate(previous);
        }
    }

    public static <T> T withHero(Hero hero, Action<T> action) {
        Hero previous = Dungeon.hero;
        activate(hero);
        try {
            return action.run();
        } finally {
            activate(previous);
        }
    }

    public static Callback callback(Callback callback) {
        if (callback == null || !contains(Dungeon.hero)) return callback;
        Hero owner = Dungeon.hero;
        return () -> withHero(owner, callback::call);
    }

    public static boolean acceptsInput() {
        if (PartySession.blocksLocalInput()) return false;
        if (PartySession.client()) {
            Hero hero = PartySession.localHero();
            return active() && hero != null && hero.isAlive() && !PartySession.applyingSnapshot();
        }
        Actor actor = Actor.current();
        return active() && (actor == null || actor instanceof Hero && ((Hero)actor).ready);
    }

    public static boolean isTurn(Hero hero) {
        return Actor.current() == hero && hero.ready;
    }

    public static void select(Hero hero) {
        select(hero, true);
    }

    public static void select(Hero hero, boolean focusCamera) {
        if (PartySession.network() && hero != PartySession.localHero()) return;
        if (!contains(hero) || !hero.isAlive() || !acceptsInput()) return;
        if (Game.scene() instanceof GameScene && !GameScene.canAcceptMovement()) return;
        selected = hero;
        activate(hero);
        GameScene.coopHeroChanged(focusCamera);
        Dungeon.observe();
    }

    public static void beforeActor(Actor actor) {
        if (actor instanceof Hero && contains((Hero)actor)) {
            Hero acting = (Hero)actor;
            if (!PartySession.network()) selected = acting;
            activate(acting);
            if (!PartySession.network() || acting == PartySession.localHero()) GameScene.coopHeroChanged(false);
        } else if (actor instanceof Buff && ((Buff)actor).target instanceof Hero) {
            activate((Hero)((Buff)actor).target);
        }
    }

    public static void afterProcess() {
        if (PartySession.network()) {
            Hero local = PartySession.localHero();
            selected = local != null ? local : primary();
        }
        if (selected != null) {
            activate(selected);
            GameScene.coopHeroChanged(false);
        }
    }

    public static boolean defer(Hero hero, Runnable command) {
        if (!contains(hero) || runningCommand || isTurn(hero)) return false;
        Controls state = controls.get(hero);
        state.command = command;
        hero.curAction = null;
        hero.restoreHealth = false;
        hero.ready = false;
        return true;
    }

    public static boolean runCommand(Hero hero) {
        Controls state = controls.get(hero);
        if (state == null || state.command == null) return false;
        Runnable command = state.command;
        state.command = null;
        runningCommand = true;
        hero.ready = true;
        try {
            command.run();
        } finally {
            runningCommand = false;
        }
        return true;
    }

    public static void cancelCommand(Hero hero) {
        Controls state = controls.get(hero);
        if (state != null) {
            state.command = null;
            state.pendingCommand = false;
        }
    }

    public static boolean hasCommand(Hero hero) {
        Controls state = controls.get(hero);
        return state != null && (state.command != null || state.pendingCommand);
    }

    public static boolean alive() {
        for (Hero hero : party) {
            if (hero.isAlive()) return true;
        }
        return false;
    }

    public static int actionClock() {
        if (clockOwner == null) return 0;
        if (!clockOwner.isAlive()) {
            for (Hero hero : party) {
                if (hero.isAlive()) {
                    actionClock = Math.max(actionClock, clockOwner.rebornActions + clockOffset);
                    clockOwner = hero;
                    clockOffset = actionClock - hero.rebornActions;
                    break;
                }
            }
        }
        actionClock = Math.max(actionClock, clockOwner.rebornActions + clockOffset);
        return actionClock;
    }

    public static void rememberLevel(int depth, Level level) {
        floors.put(depth, level);
    }

    public static Level level(int depth) {
        return floors.get(depth);
    }

    public static int[] floorDepths() {
        int[] depths = new int[floors.size()];
        int index = 0;
        for (Integer depth : floors.keySet()) depths[index++] = depth;
        Arrays.sort(depths);
        return depths;
    }

    public static void clearLevels() {
        floors.clear();
    }

    public static void storeParty(Bundle bundle) {
        if (party.length != 2) throw new IllegalStateException("There is no complete party");
        bundle.put("second_class", party[1].heroClass.name());
        bundle.put("clock_owner", clockOwner == party[1] ? 1 : 0);
        bundle.put("clock_offset", clockOffset);
        bundle.put("action_clock", actionClock);
        for (int i = 0; i < party.length; i++) {
            Hero hero = party[i];
            Bundle state = new Bundle();
            withHero(hero, () -> {
                QuickSlot.save(state);
                Bundle character = new Bundle();
                hero.storePartyRuntime(character);
                state.put("runtime", character);
                state.put("hero", hero);
                state.put("points", Skill.availableSkill);
                state.put("clock", Skill.skillTrack);
                state.put("ready", hero.ready);
                state.put("resting", hero.restoreHealth);
                state.put("reborn_actions", hero.rebornActions);
                state.put("command_pending", hasCommand(hero));
                state.put("action", storeAction(hero.curAction));
                state.put("last_action", storeAction(hero.lastAction));
                Char target = QuickSlot.lastTarget();
                state.put("target", target == null ? -1 : target.id());
                Skill[] skills = skills(hero);
                int lastUsed = -1;
                for (int index = 0; index < skills.length; index++) {
                    Skill skill = skills[index];
                    if (skill == hero.heroSkills.lastUsed) lastUsed = index;
                    Bundle runtime = new Bundle();
                    runtime.put("level", skill.level);
                    runtime.put("active", skill.active);
                    runtime.put("available_after", skill.availableAfter);
                    runtime.put("use_delay", skill.useDelay);
                    runtime.put("multi_target", skill.multiTargetActive);
                    runtime.put("quick_cast", skill.quickCast);
                    state.put("skill_" + index, runtime);
                }
                state.put("last_used", lastUsed);
            });
            bundle.put("hero_" + i, state);
        }
    }

    public static void restoreParty(Bundle bundle, int selectedIndex) {
        boolean previous = restoring;
        restoring = true;
        try {
            restorePartyState(bundle, selectedIndex);
        } finally {
            restoring = previous;
        }
    }

    public static boolean isRestoring() {
        return restoring;
    }

    public static void preparePartyRestore(HeroClass second) {
        if (second == null) throw new IllegalArgumentException("Missing party member");

        secondClass = second;
    }

    private static void restorePartyState(Bundle bundle, int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex > 1) throw new IllegalArgumentException("Invalid party member");
        secondClass = HeroClass.valueOf(bundle.getString("second_class"));
        controls.clear();
        Hero[] restored = new Hero[2];
        party = new Hero[0];
        selected = null;
        runningCommand = false;
        for (int i = 0; i < restored.length; i++) {
            Bundle state = bundle.getBundle("hero_" + i);
            Dungeon.hero = null;
            Skill.skillTrack = 0;
            Hero.haxWand = new WandOfMagicCasting();
            QuickSlot.restore(state);
            Hero hero = (Hero)state.get("hero");
            if (hero == null) throw new IllegalArgumentException("Missing party member");
            Dungeon.hero = hero;
            hero.restorePartyRuntime(state.getBundle("runtime"));
            Skill.availableSkill = state.getInt("points");
            Skill.skillTrack = state.getFloat("clock");
            hero.ready = state.getBoolean("ready");
            hero.restoreHealth = state.getBoolean("resting");
            hero.rebornActions = state.getInt("reborn_actions");
            Skill[] skills = skills(hero);
            for (int index = 0; index < skills.length; index++) {
                Skill skill = skills[index];
                Bundle runtime = state.getBundle("skill_" + index);
                skill.level = runtime.getInt("level");
                skill.active = runtime.getBoolean("active");
                skill.availableAfter = runtime.getFloat("available_after");
                skill.useDelay = runtime.getFloat("use_delay");
                skill.multiTargetActive = runtime.getBoolean("multi_target");
                skill.quickCast = runtime.getBoolean("quick_cast");
            }
            int lastUsed = state.getInt("last_used");
            hero.heroSkills.lastUsed = lastUsed >= 0 && lastUsed < skills.length ? skills[lastUsed] : null;
            QuickSlot.lastTarget(null);
            Controls owner = new Controls();
            owner.capture();
            owner.pendingCommand = state.getBoolean("command_pending");
            owner.pendingActions = state;
            controls.put(hero, owner);
            restored[i] = hero;
        }
        party = restored;
        clockOwner = party[bundle.getInt("clock_owner") == 1 ? 1 : 0];
        clockOffset = bundle.getInt("clock_offset");
        actionClock = bundle.getInt("action_clock");
        selected = party[selectedIndex];
        activate(selected);
    }

    public static void restoreActions() {
        for (Hero hero : party) {
            Controls owner = controls.get(hero);
            Bundle state = owner.pendingActions;
            if (state == null) continue;
            hero.curAction = restoreAction(state.getBundle("action"));
            hero.lastAction = restoreAction(state.getBundle("last_action"));
            owner.target = findCharacter(state.getInt("target"));
            owner.pendingActions = null;
        }
        if (selected != null) controls.get(selected).restore();
    }

    private static Skill[] skills(Hero hero) {
        CurrentSkills current = hero.heroSkills;
        ArrayList<Skill> skills = new ArrayList<>();
        Collections.addAll(skills, current.branchPA, current.passiveA1, current.passiveA2, current.passiveA3,
                current.branchPB, current.passiveB1, current.passiveB2, current.passiveB3,
                current.branchA, current.active1, current.active2, current.active3);
        for (SkillContainer slot : hero.skillTree.getSkills())
            skills.add(slot.skill);
        return skills.toArray(new Skill[skills.size()]);
    }

    private static Bundle storeAction(HeroAction action) {
        Bundle bundle = new Bundle();
        if (action == null) return bundle;
        String type;
        Char target = null;
        if (action instanceof HeroAction.Move) type = "move";
        else if (action instanceof HeroAction.PickUp) type = "pickup";
        else if (action instanceof HeroAction.OpenChest) type = "chest";
        else if (action instanceof HeroAction.Buy) type = "buy";
        else if (action instanceof HeroAction.Unlock) type = "unlock";
        else if (action instanceof HeroAction.Descend) type = "descend";
        else if (action instanceof HeroAction.Ascend) type = "ascend";
        else if (action instanceof HeroAction.Cook) type = "cook";
        else if (action instanceof HeroAction.Storage) type = "storage";
        else if (action instanceof HeroAction.Attack) {
            type = "attack";
            target = ((HeroAction.Attack)action).target;
        } else if (action instanceof HeroAction.Interact) {
            type = "interact";
            target = ((HeroAction.Interact)action).npc;
        } else if (action instanceof HeroAction.Discuss) {
            type = "discuss";
            target = ((HeroAction.Discuss)action).coldGirl;
        } else throw new IllegalArgumentException("Unknown hero action");
        bundle.put("type", type);
        bundle.put("cell", action.dst);
        bundle.put("target", target == null ? -1 : target.id());
        return bundle;
    }

    private static HeroAction restoreAction(Bundle bundle) {
        if (bundle.isNull()) return null;
        int cell = bundle.getInt("cell");
        Char target = findCharacter(bundle.getInt("target"));
        switch (bundle.getString("type")) {
            case "move": return new HeroAction.Move(cell);
            case "pickup": return new HeroAction.PickUp(cell);
            case "chest": return new HeroAction.OpenChest(cell);
            case "buy": return new HeroAction.Buy(cell);
            case "unlock": return new HeroAction.Unlock(cell);
            case "descend": return new HeroAction.Descend(cell);
            case "ascend": return new HeroAction.Ascend(cell);
            case "cook": return new HeroAction.Cook(cell);
            case "storage": return new HeroAction.Storage(cell);
            case "attack": return target == null ? null : new HeroAction.Attack(target);
            case "interact": return target instanceof NPC ? new HeroAction.Interact((NPC)target) : null;
            case "discuss": return target instanceof ColdGirl ? new HeroAction.Discuss((ColdGirl)target) : null;
            default: return null;
        }
    }

    private static Char findCharacter(int id) {
        if (id < 0) return null;
        for (Hero hero : party) {
            if (hero.id() == id) return hero;
        }
        if (Dungeon.level != null) {
            for (Mob mob : Dungeon.level.mobs) {
                if (mob.id() == id) return mob;
            }
        }
        return null;
    }

    public static void placeParty(Level level, int entrance) {
        Hero leader = Dungeon.hero;
        leader.pos = entrance;
        Hero other = partner(leader);
        if (other != null && other.isAlive()) {
            int cell = nearby(entrance, level);
            if (cell == -1) throw new IllegalStateException("No space for the second hero");
            other.pos = cell;
        }
        for (Hero hero : party) {
            hero.curAction = null;
            hero.restoreHealth = false;
            cancelCommand(hero);
            Light light = hero.buff(Light.class);
            hero.viewDistance = light == null ? level.viewDistance : Math.max(Light.DISTANCE, level.viewDistance);
        }
    }

    private static int nearby(int origin, Level level) {
        for (int distance = 1; distance < Level.WIDTH; distance++) {
            for (int cell = 0; cell < Level.LENGTH; cell++) {
                if (Level.distance(origin, cell) != distance || !Level.passable[cell] || Level.pit[cell]) continue;
                boolean occupied = false;
                for (Mob mob : level.mobs) {
                    if (mob.pos == cell && mob.isAlive()) {
                        occupied = true;
                        break;
                    }
                }
                if (!occupied) return cell;
            }
        }
        return -1;
    }

    public static boolean gather(Hero leader, int destination) {
        Hero other = partner(leader);
        if (other == null || !other.isAlive()) return true;
        if (destination < 0 || destination >= Level.LENGTH || destination == leader.pos
                || !Level.passable[destination] || Level.pit[destination]) return false;
        Char occupant = Actor.findChar(destination);
        if (occupant != null && occupant != other) return false;
        other.interrupt();
        other.restoreHealth = false;
        boolean previous = teleporting;
        boolean previousGathering = gathering;
        teleporting = true;
        gathering = true;
        try {
            withHero(other, () -> appear(other, destination));
        } finally {
            teleporting = previous;
            gathering = previousGathering;
        }
        GameScene.coopHeroChanged();
        return true;
    }

    public static boolean gathering() {
        return gathering;
    }

    public static boolean teleportTogether(Char character, int destination) {
        if (teleporting || !contains(character) || character.pos == destination) return false;
        Hero leader = (Hero)character;
        Hero other = partner(leader);
        if (other == null || !other.isAlive()) return false;
        int partnerCell = teleportNeighbour(destination, other);
        if (partnerCell == -1) {
            GLog.w("custom.generated.entry_strong_magic_aura_of_this_place_prevents_you_fro_266e1f0419");
            return true;
        }
        teleporting = true;
        try {
            withHero(leader, () -> appear(leader, destination));
            if (leader.pos != destination) partnerCell = teleportNeighbour(leader.pos, other);
            if (partnerCell != -1) gather(leader, partnerCell);
        } finally {
            teleporting = false;
        }
        Dungeon.observe();
        GameScene.coopHeroChanged();
        return true;
    }

    private static int teleportNeighbour(int destination, Hero other) {
        for (int offset : Level.NEIGHBOURS8) {
            int cell = destination + offset;
            if (cell < 0 || cell >= Level.LENGTH || !Level.adjacent(cell, destination)
                    || !Level.passable[cell] || Level.pit[cell] || Level.avoid[cell] || Level.secret[cell]
                    || Dungeon.level.plants.get(cell) != null) continue;
            Char occupant = Actor.findChar(cell);
            if (occupant == null || occupant == other) return cell;
        }
        return -1;
    }

    private static void appear(Hero hero, int destination) {
        if (hero.sprite != null && hero.sprite.parent != null) {
            WandOfBlink.appear(hero, destination);
        } else {
            if (Actor.findChar(hero.pos) == hero) Actor.freeCell(hero.pos);
            hero.pos = destination;
            Actor.occupyCell(hero);
        }
    }
}
