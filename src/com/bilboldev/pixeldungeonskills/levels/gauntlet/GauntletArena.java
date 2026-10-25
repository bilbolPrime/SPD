package com.bilboldev.pixeldungeonskills.levels.gauntlet;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.buffs.*;
import com.bilboldev.pixeldungeonskills.actors.hero.GauntletHero;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.potions.GauntletPotions;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.scenes.GauntletScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.SparseArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GauntletArena extends Level {
    public enum Phase {
        PREPARING, FIGHTING, VICTORY, LOST
    }
    public static final int WAVES = 10, CENTER = 15, RADIUS = 10, STANDS_RADIUS = RADIUS + 3;
    public static final int SPECTATOR_COUNT = 40;
    public static final int FIRST_WAVE_STEPS = 3, INTERVAL_STEPS = 25;
    private static final int[] DEPTHS = {1, 3, 6, 8, 11, 13, 16, 18, 22, 24};
    private static final String[] TILES = {Assets.TILES_SEWERS_3D, Assets.TILES_PRISON_3D, Assets.TILES_CAVES_3D, Assets.TILES_CITY_3D, Assets.TILES_HALLS_3D};
    private static final String[] WATER = {Assets.WATER_SEWERS, Assets.WATER_PRISON, Assets.WATER_CAVES, Assets.WATER_CITY, Assets.WATER_HALLS};
    private static final String[] THEMES = {"sewers", "prison", "caves", "city", "halls"};
    private int wave = 1, completed, stepsRemaining = FIRST_WAVE_STEPS;
    private Phase phase = Phase.PREPARING;
    private boolean crowdDeparted;
    private final boolean[] spectatorSeats = new boolean[LENGTH];
    private final HashSet<Integer> rewardCells = new HashSet<>();

    public GauntletProfile profile() {
        return GauntletProfile.forHero(Dungeon.hero.heroClass);
    }

    private int coverBudget() {
        return profile().coverBudget(wave);
    }

    public int wave() {
        return wave;
    }

    public int completed() {
        return completed;
    }

    public int stepsRemaining() {
        return stepsRemaining;
    }

    public int radius() {
        return RADIUS;
    }

    public int theme() {
        return Math.min(4, (wave - 1) / 2);
    }

    public String themeName() {
        return Messages.get("gauntlet." + THEMES[theme()]);
    }

    public Phase phase() {
        return phase;
    }

    public boolean crowdDeparted() {
        return crowdDeparted;
    }

    public void finishVictoryAnimation() {
        if (phase == Phase.VICTORY) crowdDeparted = true;
    }

    public boolean acceptsInput() {
        return phase == Phase.PREPARING || phase == Phase.FIGHTING;
    }

    public ArrayList<Integer> rewardCells() {
        for (Iterator<Integer> cells = rewardCells.iterator(); cells.hasNext();) {
            Heap heap = heaps.get(cells.next());
            if (heap == null || heap.isEmpty()) cells.remove();
        }
        return new ArrayList<>(rewardCells);
    }

    public boolean isSpectatorTile(int cell) {
        return cell >= 0 && cell < LENGTH && spectatorSeats[cell];
    }

    public boolean isSpectatorGround(int cell) {
        return cell >= 0 && cell < LENGTH && Math.max(Math.abs(cell % WIDTH - CENTER),
                Math.abs(cell / WIDTH - CENTER)) > RADIUS;
    }

    public int enemyCount() {
        return profile().enemyCount(wave);
    }

    @Override
    public String tilesTex() {
        return TILES[theme()];
    }

    @Override
    public String waterTex() {
        return WATER[theme()];
    }

    @Override
    public void create() {
        map = new int[LENGTH];
        visited = new boolean[LENGTH];
        mapped = new boolean[LENGTH];
        heroFOV = new boolean[LENGTH];
        mobs = new HashSet<>();
        heaps = new SparseArray<>();
        blobs = new HashMap<>();
        plants = new SparseArray<>();
        entrance = CENTER + CENTER * WIDTH;
        exit = storage = -1;
        arrangeSpectators();
        reshape();
    }

    private void arrangeSpectators() {

        java.util.Random seating = new java.util.Random();
        Arrays.fill(spectatorSeats, false);
        for (int side = 0; side < 4; side++) {
            for (int along = -9; along <= 9; along += 2) {
                int row = RADIUS + 1 + seating.nextInt(STANDS_RADIUS - RADIUS);
                int x = side < 2 ? CENTER + along : CENTER + (side == 2 ? -row : row);
                int y = side < 2 ? CENTER + (side == 0 ? -row : row) : CENTER + along;
                spectatorSeats[x + y * WIDTH] = true;
            }
        }
    }

    @Override
    protected boolean build() {
        return true;
    }

    @Override
    protected void decorate() {}

    @Override
    protected void createMobs() {}

    @Override
    protected void createItems() {}

    @Override
    public Actor respawner() {
        return null;
    }

    @Override
    public int randomRespawnCell() {
        return entrance;
    }

    private boolean inside(int x, int y, int radius) {
        return Math.abs(x - CENTER) < radius && Math.abs(y - CENTER) < radius;
    }

    private void reshape() {
        Dungeon.depth = DEPTHS[wave - 1];
        GauntletScene.level = wave;
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
            int cell = x + y * WIDTH;
            if (inside(x, y, RADIUS)) {
                map[cell] = (x + 2 * y + wave) % 11 == 0 ? Terrain.EMPTY_DECO : Terrain.EMPTY;
            } else if (Math.max(Math.abs(x - CENTER), Math.abs(y - CENTER)) == RADIUS) {
                map[cell] = (x + y) % 4 == 0 ? Terrain.WALL_DECO : Terrain.WALL;
            } else map[cell] = Terrain.EMPTY_SP;
        }
        int placed = 0;
        for (int attempt = 0; attempt < 2000 && placed < coverBudget(); attempt++) {
            int startX = Random.IntRange(CENTER - RADIUS + 2, CENTER + RADIUS - 2);
            int startY = Random.IntRange(CENTER - RADIUS + 2, CENTER + RADIUS - 2);
            int length = attempt < 1000 ? Random.IntRange(2, 5) : 2;
            int step = Random.Int(2) == 0 ? 1 : WIDTH;
            ArrayList<Integer> segment = new ArrayList<>();
            for (int n = 0; n < length; n++) segment.add(startX + startY * WIDTH + n * step);
            if (!canPlaceSegment(segment)) continue;
            for (int cell : segment) map[cell] = Terrain.WALL;
            if (!connectedFloor()) {
                for (int cell : segment) map[cell] = Terrain.EMPTY;
                continue;
            }
            placed++;
        }

        ArrayList<Integer> trapCells = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++)
            if (inside(cell % WIDTH, cell / WIDTH, RADIUS) && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0
                    && !protectedCell(cell) && Level.distance(cell, Dungeon.hero.pos) > 2) trapCells.add(cell);
        int traps = wave == 1 || wave == WAVES ? 0 : wave + 1;
        for (int n = 0; n < traps && !trapCells.isEmpty(); n++) {
            int cell = trapCells.remove(Random.Int(trapCells.size()));
            map[cell] = wave < 5 ? Terrain.GRIPPING_TRAP : wave < 8 ? Terrain.POISON_TRAP : Terrain.LIGHTNING_TRAP;
        }

        Arrays.fill(visited, false);
        Arrays.fill(mapped, false);
        for (int cell : rewardCells()) mapped[cell] = true;
        refreshFlags();
    }

    private boolean protectedCell(int cell) {
        if (cell == entrance || cell == Dungeon.hero.pos || heaps.get(cell) != null || plants.get(cell) != null) return true;
        for (Mob mob : mobs) if (mob.isAlive() && mob.pos == cell) return true;
        return false;
    }

    private boolean canPlaceSegment(ArrayList<Integer> segment) {
        for (int cell : segment) {
            int x = cell % WIDTH, y = cell / WIDTH;
            if (cell < 0 || cell >= LENGTH || !inside(x, y, RADIUS - 1) || protectedCell(cell)
                    || Level.distance(cell, Dungeon.hero.pos) <= 1) return false;

            for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++)
                if (map[cell + dx + dy * WIDTH] == Terrain.WALL || map[cell + dx + dy * WIDTH] == Terrain.WALL_DECO)
                    return false;
        }
        return true;
    }

    private boolean connectedFloor() {
        boolean[] reached = new boolean[LENGTH];
        int[] queue = new int[LENGTH];
        int read = 0, write = 1;
        queue[0] = entrance;
        reached[entrance] = true;
        while (read < write) {
            int cell = queue[read++];
            for (int offset : new int[]{-1, 1, -WIDTH, WIDTH}) {
                int next = cell + offset;
                if (!reached[next] && inside(next % WIDTH, next / WIDTH, RADIUS)
                        && (Terrain.flags[map[next]] & Terrain.PASSABLE) != 0) {
                    reached[next] = true;
                    queue[write++] = next;
                }
            }
        }
        for (int cell = 0; cell < LENGTH; cell++)
            if (inside(cell % WIDTH, cell / WIDTH, RADIUS) && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0 && !reached[cell])
                return false;
        return true;
    }

    private void refreshFlags() {
        for (int cell = 0; cell < LENGTH; cell++) Level.set(cell, map[cell]);
        Arrays.fill(discoverable, true);
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
            if (!inside(x, y, radius())) {
                int cell = x + y * WIDTH;
                passable[cell] = avoid[cell] = false;
                solid[cell] = true;
            }
        }
    }

    @Override
    public boolean[] updateFieldOfView(Char viewer) {
        super.updateFieldOfView(viewer);
        if (viewer == Dungeon.hero) {

            for (int cell = 0; cell < LENGTH; cell++)
                if (isSpectatorGround(cell)) fieldOfView[cell] = true;
            heroFOV = fieldOfView.clone();
        }
        return fieldOfView;
    }

    public void heroStepped() {
        if (phase == Phase.PREPARING && stepsRemaining > 0 && --stepsRemaining == 0)
            Dungeon.hero.interrupt();
    }

    public void releaseWave() {
        if (phase != Phase.PREPARING || stepsRemaining > 0 || !Dungeon.hero.ready) return;
        if (completed == wave) {
            wave++;
            reshape();
        }

        ArrayList<Integer> candidates = new ArrayList<>();
        for (int y = CENTER - radius() + 1; y < CENTER + radius(); y++) {
            for (int x = CENTER - radius() + 1; x < CENTER + radius(); x++) {
                int cell = x + y * WIDTH;
                if (passable[cell] && Actor.findChar(cell) == null && heaps.get(cell) == null
                        && Level.distance(cell, Dungeon.hero.pos) >= profile().spawnDistance)
                    candidates.add(cell);
            }
        }
        if (candidates.size() < enemyCount()) throw new IllegalStateException("Not enough safe Gauntlet spawn positions");
        ArrayList<Integer> spawnCells = new ArrayList<>();
        for (int i = 0; i < enemyCount(); i++)
            spawnCells.add(candidates.remove(Random.Int(candidates.size())));
        for (int i = 0; i < spawnCells.size(); i++) {
            try {
                Mob mob = (Mob)profile().enemyType(wave, i).newInstance();
                mob.pos = spawnCells.get(i);
                mob.noLoot();
                mob.state = mob.HUNTING;
                mob.aggro(Dungeon.hero);
                GameScene.add(mob, 1f);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        phase = Phase.FIGHTING;
        if (wave == 3 || wave == 6) dropHealingPotion();
        ((GauntletHero)Dungeon.hero).ready();
        GameScene.updateMap();
        Dungeon.observe();
        GLog.h("gauntlet.wave", wave, WAVES, Message.key("gauntlet." + THEMES[theme()]));
    }

    private void dropHealingPotion() {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (inside(cell % WIDTH, cell / WIDTH, RADIUS) && passable[cell] && !avoid[cell]
                    && !protectedCell(cell) && Level.distance(cell, Dungeon.hero.pos) >= 3)
                candidates.add(cell);
        }
        if (candidates.isEmpty()) throw new IllegalStateException("No free Gauntlet healing potion tile");

        drop(new PotionOfHealing().identify(), Random.element(candidates)).sprite.drop();
    }

    public void checkCompletion() {
        if (phase != Phase.FIGHTING || !Dungeon.hero.isAlive() || wave == WAVES) return;
        for (Mob mob : mobs)
            if (mob.isAlive() && mob.hostile && !(mob instanceof SummonedPet) && !(mob instanceof HiredMerc)) return;
        completed = wave;
        phase = Phase.PREPARING;
        stepsRemaining = INTERVAL_STEPS;
        clearHazards();
        Buff.detach(Dungeon.hero, Poison.class);
        Buff.detach(Dungeon.hero, Burning.class);
        Buff.detach(Dungeon.hero, Bleeding.class);
        Buff.detach(Dungeon.hero, Weakness.class);
        Buff.detach(Dungeon.hero, Cripple.class);
        Buff.detach(Dungeon.hero, Paralysis.class);
        Buff.detach(Dungeon.hero, Blindness.class);
        Buff.detach(Dungeon.hero, Roots.class);
        Dungeon.hero.interrupt();
        GauntletPotions.applyRewards((GauntletHero)Dungeon.hero);
        Dungeon.hero.HP = Dungeon.hero.HT;
        Dungeon.hero.MP = Dungeon.hero.MMP;
        ((GauntletHero)Dungeon.hero).ready();
        for (Item item : profile().rewards(wave)) reward(item);
        refreshFlags();
        GameScene.updateMap();
        Dungeon.observe();
        GLog.p("gauntlet.cleared", wave);
    }

    private void reward(Item item) {
        ArrayList<Integer> candidates = new ArrayList<>();
        ArrayList<Integer> previousRewards = rewardCells();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!inside(cell % WIDTH, cell / WIDTH, RADIUS) || !passable[cell] || protectedCell(cell)
                    || Level.distance(cell, Dungeon.hero.pos) < 3) continue;
            boolean spaced = true;
            for (int other : previousRewards) if (Level.distance(cell, other) < 3) {
                spaced = false;
                break;
            }
            if (spaced) candidates.add(cell);
        }
        if (candidates.isEmpty()) {
            for (int cell = 0; cell < LENGTH; cell++)
                if (inside(cell % WIDTH, cell / WIDTH, RADIUS) && passable[cell] && !protectedCell(cell)) candidates.add(cell);
        }
        if (candidates.isEmpty()) throw new IllegalStateException("No free Gauntlet reward tile");
        int cell = Random.element(candidates);
        rewardCells.add(cell);
        mapped[cell] = true;
        drop(item, cell).sprite.drop();
    }

    private void clearHazards() {
        for (Blob blob : blobs.values()) {
            for (int cell = 0; cell < LENGTH; cell++) blob.clear(cell);
            Actor.remove(blob);
            if (blob.emitter != null) blob.emitter.on = false;
        }
        blobs.clear();
        for (int cell = 0; cell < LENGTH; cell++)
            if (map[cell] == Terrain.GRIPPING_TRAP || map[cell] == Terrain.POISON_TRAP || map[cell] == Terrain.LIGHTNING_TRAP)
                map[cell] = Terrain.INACTIVE_TRAP;
    }

    @Override
    public void press(int cell, Char ch) {
        if (phase == Phase.FIGHTING) super.press(cell, ch);
    }

    @Override
    public void mobPress(Mob mob) {
        if (phase == Phase.FIGHTING) super.mobPress(mob);
    }

    public void heroDied() {
        phase = Phase.LOST;
    }

    public void kingDied() {
        if (phase == Phase.FIGHTING && wave == WAVES && Dungeon.hero.isAlive()) {
            completed = WAVES;
            phase = Phase.VICTORY;
            Dungeon.hero.interrupt();
            Dungeon.hero.curAction = null;
            Dungeon.hero.ready = false;
        }
    }
}
