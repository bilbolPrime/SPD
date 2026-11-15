package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.TutorialRun;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Gnoll;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Rat;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.TutorialInstructor;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.RebornSkillTree;
import com.bilboldev.pixeldungeonskills.items.armor.LeatherArmor;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfAccuracy;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfIdentify;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Sword;
import com.bilboldev.pixeldungeonskills.plants.Sorrowmoss;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.SparseArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class TutorialLevel extends Level {
    public static final int FIRE_TRAP = 11 + 23 * WIDTH;
    public static final int SEARCH_DOOR = 4 + 24 * WIDTH;
    private static final int[][] ROOMS = {{2, 7, 2, 12}, {9, 14, 2, 12}, {16, 21, 2, 12}, {23, 29, 2, 12},
            {23, 29, 17, 29}, {16, 21, 17, 29}, {9, 14, 17, 29}, {2, 7, 17, 29}};
    private static final int[] GATES = {8 + 7 * WIDTH, 15 + 7 * WIDTH, 22 + 7 * WIDTH, 26 + 14 * WIDTH,
            22 + 23 * WIDTH, 15 + 23 * WIDTH, 8 + 23 * WIDTH};
    private static final int[] ENTRIES = {4 + 10 * WIDTH, 10 + 7 * WIDTH, 17 + 7 * WIDTH, 24 + 7 * WIDTH,
            26 + 18 * WIDTH, 20 + 23 * WIDTH, 13 + 23 * WIDTH, 6 + 23 * WIDTH};
    private static final int[] INSTRUCTORS = {4 + 4 * WIDTH, 10 + 10 * WIDTH, 20 + 10 * WIDTH, 28 + 10 * WIDTH,
            28 + 26 * WIDTH, 20 + 26 * WIDTH, 13 + 26 * WIDTH, 6 + 19 * WIDTH};
    public Mob normalRat, championRat, skillRat;
    public LeatherArmor leather;
    public RingOfAccuracy ring;
    public ScrollOfIdentify identify;
    public PotionOfHealing healing;
    public Food food;
    public int injuredHealth;
    public boolean plantInspected, trapInspected;
    private boolean trapTriggered, extinguished, warningDodged, secondWave;
    private boolean doorSearched, doorPassed;
    private int warningCell = -1, warnedAt;
    private TutorialInstructor instructor;
    private IronKey exitKey;
    private final ArrayList<Mob> combat = new ArrayList<>();

    @Override
    public void create() {
        resizingNeeded = false;
        map = new int[LENGTH];
        seedUses = new int[LENGTH];
        visited = new boolean[LENGTH];
        mapped = new boolean[LENGTH];
        heroFOV = new boolean[LENGTH];
        mobs = new LinkedHashSet<>();
        heaps = new SparseArray<>();
        plants = new SparseArray<>();
        blobs = new HashMap<Class<? extends Blob>, Blob>();
        build();
        rebuildFlags();
        createMobs();
        beginLesson(TutorialRun.lesson());
    }

    @Override
    protected boolean build() {
        Arrays.fill(map, Terrain.WALL);
        for (int[] room : ROOMS) {
            for (int y = room[2]; y <= room[3]; y++) {
                for (int x = room[0]; x <= room[1]; x++) map[x + y * WIDTH] = Terrain.EMPTY;
            }
        }
        for (int y = 13; y <= 16; y++) map[26 + y * WIDTH] = Terrain.EMPTY;
        for (int i = 0; i < GATES.length; i++) map[GATES[i]] = i < TutorialRun.lesson() ? Terrain.DOOR : Terrain.WALL;
        for (int x = 16; x <= 21; x++) map[x + 6 * WIDTH] = x == 18 ? Terrain.DOOR : Terrain.WALL;
        for (int x = 2; x <= 7; x++) map[x + 24 * WIDTH] = x == 4 ? Terrain.SECRET_DOOR : Terrain.WALL;
        for (int x = 2; x <= 7; x++) map[x + 26 * WIDTH] = x == 4 ? Terrain.LOCKED_DOOR : Terrain.WALL;
        map[FIRE_TRAP] = Terrain.FIRE_TRAP;
        map[12 + 23 * WIDTH] = Terrain.WATER;
        map[13 + 23 * WIDTH] = Terrain.WATER;
        entrance = ENTRIES[0];
        storage = -1;
        exit = 4 + 27 * WIDTH;
        map[exit] = Terrain.EXIT;
        return true;
    }

    @Override protected void decorate() { }
    @Override protected void createItems() { }

    @Override
    protected void createMobs() {
        instructor = new TutorialInstructor();
        instructor.pos = INSTRUCTORS[TutorialRun.lesson()];
        mobs.add(instructor);
    }

    public static int entry(int lesson) { return ENTRIES[lesson]; }

    public static int room(int cell) {
        int x = cell % WIDTH, y = cell / WIDTH;
        for (int i = 0; i < ROOMS.length; i++) {
            int[] room = ROOMS[i];
            if (x >= room[0] && x <= room[1] && y >= room[2] && y <= room[3]) return i;
        }
        return -1;
    }

    public void openGate(int lesson) {
        if (lesson == 1) {
            removeExhibit(normalRat);
            removeExhibit(championRat);
        }
        Level.set(GATES[lesson], Terrain.DOOR);
        instructor.station(INSTRUCTORS[lesson + 1]);
        GameScene.updateMap(GATES[lesson]);
        Dungeon.observe();
    }

    private void removeExhibit(Mob mob) {
        if (mob == null) return;
        Actor.remove(mob);
        Actor.freeCell(mob.pos);
        mobs.remove(mob);
        if (mob.sprite != null) mob.sprite.killAndErase();
    }

    public void beginLesson(int lesson) {
        combat.clear();
        instructor.station(INSTRUCTORS[lesson]);
        switch (lesson) {
            case 1:
                normalRat = addRat(11, 5, true);
                championRat = addRat(13, 5, true);
                Bundle champion = new Bundle();
                champion.put("champion_skills", new String[]{RebornChampion.Skill.SHELL.name()});
                champion.put("champion_ranks", new int[]{1});
                RebornChampion shell = new RebornChampion();
                shell.restoreFromBundle(champion);
                shell.attachTo(championRat);
                break;
            case 2:
                combat.add(addRat(18, 4, false));
                break;
            case 3:
                leather = new LeatherArmor();
                leather.identify();
                ring = new RingOfAccuracy();
                identify = new ScrollOfIdentify();
                identify.identify();
                drop(leather, 25 + 5 * WIDTH);
                drop(new Sword().identify(), 27 + 5 * WIDTH);
                drop(ring, 25 + 9 * WIDTH);
                drop(identify, 27 + 9 * WIDTH);
                break;
            case 4:
                Dungeon.hero.skillTree = new RebornSkillTree(Dungeon.hero);
                Skill.availableSkill = 1;
                QuickSlot.resetSlots();
                Dungeon.hero.MP = Dungeon.hero.MMP;
                skillRat = addRat(25, 20, true);
                skillRat.HP = skillRat.HT = 40;
                break;
            case 5:
                injuredHealth = Dungeon.hero.HP = Math.max(1, Dungeon.hero.HT * 2 / 5);
                Dungeon.hero.buff(Hunger.class).satisfy(-Hunger.STARVING);
                healing = new PotionOfHealing();
                healing.identify();
                food = new Food();
                drop(healing, 18 + 20 * WIDTH);
                drop(food, 20 + 20 * WIDTH);
                break;
            case 6:
                plant(new Sorrowmoss.Seed(), 11 + 19 * WIDTH);
                break;
            case 7:
                combat.add(addRat(4, 23, false));
                Gnoll gnoll = new Gnoll();
                gnoll.pos = 4 + 20 * WIDTH;
                addPracticeMob(gnoll);
                combat.add(gnoll);
                exitKey = new IronKey();
                drop(exitKey, 3 + 25 * WIDTH);
                drop(new PotionOfHealing().identify(), 6 + 25 * WIDTH);
                break;
            default: break;
        }
        if (Dungeon.level == this) Dungeon.observe();
    }

    private Mob addRat(int x, int y, boolean stationary) {
        Rat rat = stationary ? new TrainingRat() : new Rat();
        rat.pos = x + y * WIDTH;
        addPracticeMob(rat);
        return rat;
    }

    private void addPracticeMob(Mob mob) {
        mob.noLoot();
        mob.challengeExtra = true;
        if (Dungeon.level == this) GameScene.add(mob);
        else mobs.add(mob);
    }

    public boolean combatFinished() {
        if (TutorialRun.lesson() == 2 && !secondWave) return false;
        for (Mob mob : combat) if (mob.isAlive()) return false;
        return !combat.isEmpty();
    }

    public boolean finalDoorOpen() { return map[4 + 26 * WIDTH] != Terrain.LOCKED_DOOR; }
    public boolean doorSearched() { return doorSearched; }
    public boolean searchFinished() { return doorSearched && doorPassed; }

    public void searched(Hero hero) {
        if (Level.adjacent(hero.pos, SEARCH_DOOR) && map[SEARCH_DOOR] != Terrain.SECRET_DOOR) doorSearched = true;
    }

    public boolean awaitingWarning() { return extinguished && !warningDodged && warningCell < 0; }

    public int guideCell() {
        if (TutorialRun.level() != this || !TutorialRun.acceptsInput() || TutorialRun.canAdvance() || TutorialRun.completed()) return -1;
        switch (TutorialRun.lesson()) {
            case 2:
            case 7:
                Mob nearest = null;
                for (int i = 0; i < combat.size(); i++) {
                    Mob mob = combat.get(i);
                    if (mob.isAlive() && (nearest == null || Level.distance(Dungeon.hero.pos, mob.pos)
                            < Level.distance(Dungeon.hero.pos, nearest.pos))) nearest = mob;
                }
                if (nearest != null) return nearest.pos;
                if (TutorialRun.lesson() != 7) return -1;
                if (!doorSearched) return SEARCH_DOOR - WIDTH;
                if (!doorPassed) return SEARCH_DOOR;
                if (!finalDoorOpen()) return !Dungeon.hero.belongings.backpack.items.contains(exitKey)
                        ? 3 + 25 * WIDTH : 4 + 26 * WIDTH;
                return exit;
            case 6:
                if (!plantInspected) return 11 + 19 * WIDTH;
                if (!trapInspected || !trapTriggered) return FIRE_TRAP;
                if (!extinguished) return 12 + 23 * WIDTH;
                return -1;
            default: return -1;
        }
    }

    public void updateExercise() {
        if (TutorialRun.lesson() == 2 && !secondWave && !combat.isEmpty() && !combat.get(0).isAlive()) {
            secondWave = true;
            combat.add(addRat(19, 3, false));
            combat.add(addRat(20, 4, false));
            Dungeon.observe();
        }
        if (TutorialRun.lesson() != 6 || warningDodged) return;
        if (extinguished) {
            if (warningCell < 0) {
                warningCell = Dungeon.hero.pos;
                warnedAt = Dungeon.hero.rebornActions;
                Dungeon.hero.interrupt();
                Dungeon.hero.restoreHealth = false;
                GLog.w("tutorial.warning");
            } else if (Dungeon.hero.rebornActions > warnedAt) {
                if (Dungeon.hero.pos != warningCell) {
                    warningDodged = true;
                    warningCell = -1;
                } else {
                    Dungeon.hero.damage(3, this);
                    warningCell = -1;
                }
            }
        } else if (trapTriggered && Dungeon.hero.buff(Burning.class) == null && map[FIRE_TRAP] != Terrain.FIRE_TRAP) {
            Fire fire = (Fire)blobs.get(Fire.class);
            if (fire == null || fire.cur[FIRE_TRAP] == 0) {
                Level.set(FIRE_TRAP, Terrain.FIRE_TRAP);
                GameScene.updateMap(FIRE_TRAP);
            }
        }
    }

    @Override
    public void press(int cell, Char ch) {
        boolean trainingFire = TutorialRun.lesson() == 6 && ch == Dungeon.hero && cell == FIRE_TRAP && map[cell] == Terrain.FIRE_TRAP;
        super.press(cell, ch);
        if (trainingFire) trapTriggered = true;
        if (ch == Dungeon.hero && cell == SEARCH_DOOR && doorSearched) doorPassed = true;
    }

    public void extinguished(Hero hero) {
        if (trapTriggered && hero == Dungeon.hero && Level.water[hero.pos]) extinguished = true;
    }

    public int[] warningCells() { return warningCell < 0 ? new int[0] : new int[]{warningCell}; }
    public boolean hazardsFinished() { return plantInspected && trapInspected && extinguished && warningDodged; }
    @Override public Actor respawner() { return null; }
    @Override public Actor mercRespawner() { return null; }
    @Override public int randomRespawnCell() { return -1; }
    @Override public String tilesTex() { return Difficulties.is3d ? Assets.TILES_SEWERS_3D : Assets.TILES_SEWERS; }
    @Override public String waterTex() { return Assets.WATER_SEWERS; }

    private static class TrainingRat extends Rat {
        { state = PASSIVE; }

        @Override protected boolean act() {
            if (TutorialRun.lesson() == 4 && HP < HT) {
                int healed = HT - HP;
                HP = HT;
                showRecovery(healed, false);
            }
            spend(TICK);
            return true;
        }

        @Override public void damage(int damage, Object source) {
            if (TutorialRun.lesson() == 4) super.damage(Math.min(damage, Math.max(0, HP - 1)), source);
        }
    }
}
