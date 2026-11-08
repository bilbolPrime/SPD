package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDefender;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyRaider;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class DailyOutpostLevel extends DailyLevel {
    private static final int[] WAVE_TIMES = {25, 100, 175, 250, 325};
    private static final String[] DIRECTIONS = {"north", "east", "south", "west"};
    private static final int[] NO_WARNINGS = new int[0];
    public int captainCell;
    public final int[] guardCells = new int[2];
    public final int[] approachCells = new int[4];
    public final int[] cellDoors = new int[2];
    public final int[] captiveCells = new int[2];
    public int[] cacheCells;
    private final int[] gateCells = {11 * WIDTH + 15, 15 * WIDTH + 19, 19 * WIDTH + 15, 15 * WIDTH + 11};
    private final int[] plannedKinds = new int[30], plannedApproaches = new int[30], plannedWaves = new int[30];
    private final int[] spawnOffsets = new int[30];
    private final int[] state = new int[30], pending = new int[30];
    private final float[] warningDue = new float[30], retryAfter = new float[30];
    private final DailyDefender[] captives = new DailyDefender[2];
    private DailyDefender captain;
    private int announced;
    private float lastUpdate = -1;

    private static class Chamber {
        int x, y, width, height, side, center;

        Chamber(int x, int y, int width, int height, int side) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.side = side;
        }
    }

    {
        color1 = 0x6a723d;
        color2 = 0x88924c;
    }

    @Override protected boolean build() {
        scenario = 5;
        feeling = Feeling.NONE;
        turnLimit = 420;
        Arrays.fill(map, Terrain.WALL);
        Arrays.fill(pending, -1);
        ArrayList<Chamber> chambers = new ArrayList<>();
        chambers.add(new Chamber(1, 1, 30, 9, 0));
        chambers.add(new Chamber(21, 10, 10, 11, 1));
        chambers.add(new Chamber(1, 21, 30, 10, 2));
        chambers.add(new Chamber(1, 10, 9, 11, 3));
        int count = Random.IntRange(8, 10);
        while (chambers.size() < count) {
            Chamber largest = null;
            for (Chamber room : chambers) {
                if (room.width < 14 && room.height < 14) continue;
                if (largest == null || room.width * room.height > largest.width * largest.height) largest = room;
            }
            if (largest == null) break;
            boolean vertical = largest.width >= 14 && (largest.height < 14 || Random.Int(2) == 0);
            int cut = Random.IntRange(7, (vertical ? largest.width : largest.height) - 7);
            if (vertical) {
                chambers.add(new Chamber(largest.x + cut, largest.y, largest.width - cut, largest.height, largest.side));
                largest.width = cut;
            } else {
                chambers.add(new Chamber(largest.x, largest.y + cut, largest.width, largest.height - cut, largest.side));
                largest.height = cut;
            }
        }
        for (Chamber room : chambers) {
            int width = Random.IntRange(5, Math.min(9, room.width - 2));
            int height = Random.IntRange(5, Math.min(9, room.height - 2));
            room.x += Random.IntRange(1, room.width - width - 1);
            room.y += Random.IntRange(1, room.height - height - 1);
            room.width = width;
            room.height = height;
            room.center = (room.y + height / 2) * WIDTH + room.x + width / 2;
            for (int y = room.y; y < room.y + height; y++) for (int x = room.x; x < room.x + width; x++)
                map[y * WIDTH + x] = Terrain.EMPTY;
        }
        Collections.sort(chambers, Comparator.comparingDouble(room ->
                Math.atan2(room.center / WIDTH - 15, room.center % WIDTH - 15)));
        for (int i = 0; i < chambers.size(); i++)
            passage(chambers.get(i).center, chambers.get((i + 1) % chambers.size()).center);
        for (int i = 0; i < 3; i++) {
            int first = Random.Int(chambers.size());
            int second = (first + Random.IntRange(2, chambers.size() - 2)) % chambers.size();
            passage(chambers.get(first).center, chambers.get(second).center);
        }
        for (int y = 12; y <= 18; y++) for (int x = 12; x <= 18; x++) map[y * WIDTH + x] = Terrain.EMPTY;
        ArrayList<Integer> unused = new ArrayList<>();
        for (Chamber room : chambers) unused.add(room.center);
        int[] outside = {10 * WIDTH + 15, 15 * WIDTH + 20, 20 * WIDTH + 15, 15 * WIDTH + 10};
        for (int side = 0; side < approachCells.length; side++) {
            ArrayList<Integer> candidates = new ArrayList<>();
            for (Chamber room : chambers) if (room.side == side) candidates.add(room.center);
            approachCells[side] = candidates.get(Random.Int(candidates.size()));
            unused.remove(Integer.valueOf(approachCells[side]));
            passage(approachCells[side], outside[side]);
            int across = side % 2 == 0 ? 1 : WIDTH;
            for (int offset = -1; offset <= 1; offset++) {
                map[gateCells[side] + offset * across] = Terrain.EMPTY_SP;
                map[outside[side] + offset * across] = Terrain.EMPTY;
            }
        }
        captainCell = 15 * WIDTH + 15;
        entrance = 16 * WIDTH + 15;
        guardCells[0] = 15 * WIDTH + 13;
        guardCells[1] = 15 * WIDTH + 17;
        for (int i = 0; i < captiveCells.length; i++) {
            captiveCells[i] = unused.remove(Random.Int(unused.size()));
            buildCell(i);
        }
        cacheCells = new int[Random.IntRange(2, Math.min(3, unused.size()))];
        for (int i = 0; i < cacheCells.length; i++) cacheCells[i] = unused.remove(Random.Int(unused.size()));
        map[entrance] = Terrain.ENTRANCE;
        goal = captainCell;
        supplyCell = cacheCells[0];
        exit = -1;
        planWaves();
        if (!routesOpen()) throw new IllegalStateException("Unreachable outpost approach");
        return true;
    }

    private static boolean inKeep(int cell) {
        int x = cell % WIDTH, y = cell / WIDTH;
        return x >= 11 && x <= 19 && y >= 11 && y <= 19;
    }

    private void passage(int from, int to) {
        int[] previous = new int[LENGTH], queue = new int[LENGTH];
        Arrays.fill(previous, -1);
        int read = 0, write = 0, first = Random.Int(4), direction = Random.Int(2) == 0 ? 1 : -1;
        previous[from] = from;
        queue[write++] = from;
        while (read < write && previous[to] < 0) {
            int cell = queue[read++];
            for (int i = 0; i < 4; i++) {
                int next = cell + NEIGHBOURS4[(first + direction * i + 4) % 4];
                int x = next % WIDTH, y = next / WIDTH;
                if (x <= 0 || x >= WIDTH - 1 || y <= 0 || y >= HEIGHT - 1
                        || inKeep(next) || previous[next] >= 0) continue;
                previous[next] = cell;
                queue[write++] = next;
            }
        }
        if (previous[to] < 0) throw new IllegalStateException("Outpost passage cannot reach its gate");
        for (int cell = to; cell != from; cell = previous[cell]) map[cell] = Terrain.EMPTY;
    }

    private void buildCell(int index) {
        int center = captiveCells[index];
        for (int y = -2; y <= 2; y++) for (int x = -2; x <= 2; x++)
            map[center + y * WIDTH + x] = Math.max(Math.abs(x), Math.abs(y)) == 1 ? Terrain.WALL : Terrain.EMPTY;
        int dx = 15 - center % WIDTH, dy = 15 - center / WIDTH;
        int toward = Math.abs(dx) > Math.abs(dy) ? Integer.signum(dx) : Integer.signum(dy) * WIDTH;
        cellDoors[index] = center + toward;
        map[cellDoors[index]] = Terrain.LOCKED_DOOR;
    }

    private boolean open(int cell) {
        return cell >= 0 && cell < LENGTH && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
    }

    private boolean routesOpen() {
        int[] reachable = distances(entrance);
        for (int i = 0; i < captiveCells.length; i++)
            if (reachable[captiveCells[i]] >= 0 || reachable[2 * cellDoors[i] - captiveCells[i]] < 0) return false;
        for (int cell = 0; cell < LENGTH; cell++) {
            if (cell == captiveCells[0] || cell == captiveCells[1]) continue;
            if (open(cell) && reachable[cell] < 0) return false;
        }
        return true;
    }

    private void planWaves() {
        int slot = 0, previous = -1;
        for (int wave = 0; wave < WAVE_TIMES.length; wave++) {
            int first = Random.Int(previous < 0 ? 4 : 3);
            if (previous >= 0 && first >= previous) first++;
            int second = Random.Int(3);
            if (second >= first) second++;
            int casters = 0;
            for (int i = 0; i < wave + 4; i++, slot++) {
                int kind = Random.Int(wave == 0 ? 2 : 4);
                if (kind == 3 && casters == 2) kind = Random.Int(3);
                if (kind == 3) casters++;
                plannedKinds[slot] = kind;
                plannedApproaches[slot] = wave >= 2 && i % 2 != 0 ? second : first;
                plannedWaves[slot] = wave;
                spawnOffsets[slot] = Random.Int(25);
            }
            previous = first;
        }
    }

    @Override protected void decorate() {
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] == Terrain.EMPTY && Random.Int(16) == 0) map[cell] = Terrain.EMPTY_DECO;
            else if (map[cell] == Terrain.WALL && open(cell + WIDTH) && Random.Int(5) == 0) map[cell] = Terrain.WALL_DECO;
        }
    }

    @Override protected void createMobs() {
        captain = new DailyDefender(0, captainCell, false);
        captain.pos = captainCell;
        mobs.add(captain);
        for (int i = 0; i < guardCells.length; i++) {
            DailyDefender guard = new DailyDefender(i + 1, guardCells[i], false);
            guard.pos = guardCells[i];
            mobs.add(guard);
        }
        for (int i = 0; i < captiveCells.length; i++) {
            captives[i] = new DailyDefender(i + 1, (i == 0 ? 13 : 17) * WIDTH + 15, true);
            captives[i].pos = captiveCells[i];
            mobs.add(captives[i]);
        }
    }

    @Override protected void createItems() {
        for (int i = 0; i < cacheCells.length; i++) {
            int cell = cacheCells[i];
            drop(new Food().identify(), cell);
            drop((Random.Int(2) == 0 ? new PotionOfHealing() : new PotionOfMana()).identify(), cell);
            if (i == 0) drop(armoryItem().identify(), cell);
            else if (Random.Int(2) == 0) drop(new ScrollOfUpgrade().identify(), cell);
            else drop(new PotionOfRejuvenation().identify(), cell);
            heaps.get(cell).type = Heap.Type.CHEST;
        }
    }

    private boolean current(Hero hero) {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level == this
                && hero != null && hero == Dungeon.hero && hero.isAlive();
    }

    public DailyDefender captain() { return captain; }

    public int entranceGate(int side) { return gateCells[side]; }

    public boolean insideOutpost(int cell) {
        int x = cell % WIDTH, y = cell / WIDTH;
        return x >= 12 && x <= 18 && y >= 12 && y <= 18;
    }

    public boolean captainAlive() {
        return captain != null && captain.isAlive() && mobs.contains(captain);
    }

    public int waveNumber() { return announced; }

    public int raidersRemaining() {
        int count = 0;
        for (Mob mob : mobs) if (mob instanceof DailyRaider && mob.isAlive()) count++;
        return count;
    }

    public boolean waveActive() {
        if (raidersRemaining() > 0) return true;
        for (int i = 0; i < state.length; i++) if (plannedWaves[i] < announced && state[i] != 2) return true;
        return false;
    }

    public int nextWaveTurns() {
        return announced < WAVE_TIMES.length ? Math.max(0, (int)Math.ceil(WAVE_TIMES[announced] - DailyRun.elapsedTurns())) : 0;
    }

    public void updateSiege() {
        if (!current(Dungeon.hero)) return;
        if (!captainAlive()) {
            DailyRun.fail("daily.outpost_lost");
            return;
        }
        float now = DailyRun.elapsedTurns();
        if (now >= turnLimit) {
            relieve();
            return;
        }
        if (now <= lastUpdate) return;
        lastUpdate = now;
        while (announced < WAVE_TIMES.length && now >= WAVE_TIMES[announced]) announceWave();
        int living = raidersRemaining();
        for (int i = 0; i < state.length; i++) {
            if (state[i] != 1 || now < warningDue[i]) continue;
            int cell = pending[i];
            pending[i] = -1;
            if (living >= 12 || !canSpawn(cell)) {
                state[i] = 0;
                retryAfter[i] = now + 2;
                continue;
            }
            DailyRaider raider = new DailyRaider(plannedKinds[i], gateCells[plannedApproaches[i]]);
            raider.pos = cell;
            state[i] = 2;
            living++;
            GameScene.add(raider, 1f);
        }
        int reserved = 0;
        for (int cell : pending) if (cell >= 0) reserved++;
        for (int i = 0; i < state.length && living + reserved < 12; i++) {
            if (state[i] != 0 || plannedWaves[i] >= announced || now < retryAfter[i]) continue;
            int cell = spawnCell(i);
            if (cell < 0) {
                retryAfter[i] = now + 2;
                continue;
            }
            state[i] = 1;
            pending[i] = cell;
            warningDue[i] = now + 2;
            reserved++;
        }
    }

    private void announceWave() {
        int first = -1, second = -1;
        for (int i = 0; i < plannedWaves.length; i++) if (plannedWaves[i] == announced) {
            if (first < 0) first = plannedApproaches[i];
            else if (plannedApproaches[i] != first) second = plannedApproaches[i];
        }
        announced++;
        Dungeon.hero.interrupt();
        Dungeon.hero.restoreHealth = false;
        if (second < 0) GLog.w("daily.outpost_wave", announced, Message.key("daily.outpost_" + DIRECTIONS[first]));
        else GLog.w("daily.outpost_wave_split", announced, Message.key("daily.outpost_" + DIRECTIONS[first]),
                Message.key("daily.outpost_" + DIRECTIONS[second]));
    }

    private int spawnCell(int slot) {
        int center = approachCells[plannedApproaches[slot]];
        for (int i = 0; i < 25; i++) {
            int offset = (spawnOffsets[slot] + i) % 25;
            int cell = center + (offset / 5 - 2) * WIDTH + offset % 5 - 2;
            if (canSpawn(cell)) return cell;
        }
        return -1;
    }

    private boolean canSpawn(int cell) {
        if (!open(cell) || inKeep(cell) || Actor.findChar(cell) != null || heaps.get(cell) != null
                || Level.distance(cell, Dungeon.hero.pos) <= 1) return false;
        for (int mark : pending) if (cell == mark) return false;
        for (int captive : captiveCells) if (cell == captive) return false;
        for (Mob mob : mobs) if (mob.isAlive() && mob.pos == cell) return false;
        return true;
    }

    public int[] warningCells() { return current(Dungeon.hero) ? pending : NO_WARNINGS; }

    public boolean isCellDoor(int cell) {
        for (int door : cellDoors) if (cell == door) return true;
        return false;
    }

    public void interactRescue(final Hero hero, final int door) {
        int found = -1;
        for (int i = 0; i < cellDoors.length; i++) if (door == cellDoors[i]) found = i;
        if (found < 0 || !canFree(hero, found)) return;
        final int captive = found;
        GameScene.show(new WndOptions(Messages.get("daily.outpost_cell"), Messages.get("daily.outpost_free_prompt"),
                Messages.get("daily.outpost_free"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !canFree(hero, captive)) return;
                setDoor(door, Terrain.OPEN_DOOR);
                captives[captive].free();
                hero.spendAndNext(1f);
            }
        });
    }

    private boolean canFree(Hero hero, int captive) {
        return current(hero) && captives[captive] != null && captives[captive].isAlive()
                && mobs.contains(captives[captive]) && captives[captive].captive()
                && map[cellDoors[captive]] == Terrain.LOCKED_DOOR && Level.adjacent(hero.pos, cellDoors[captive]);
    }

    public void relieve() {
        if (!DailyRun.active() || DailyRun.finished() || Dungeon.level != this) return;
        if (Dungeon.hero == null || !Dungeon.hero.isAlive()) {
            DailyRun.finish(false);
            return;
        }
        if (!captainAlive()) {
            DailyRun.fail("daily.outpost_lost");
            return;
        }
        if (DailyRun.elapsedTurns() < turnLimit) return;
        Arrays.fill(pending, -1);
        Arrays.fill(state, 2);
        for (Mob mob : new ArrayList<>(mobs)) if (mob instanceof DailyRaider) ((DailyRaider)mob).withdraw();
        DailyRun.finish(true);
    }

    @Override public String doorName(int cell) {
        if (cell == entrance) return Messages.get("daily.outpost_entrance");
        if (isCellDoor(cell) && map[cell] == Terrain.LOCKED_DOOR) return Messages.get("daily.outpost_cell");
        for (int side = 0; side < gateCells.length; side++) if (cell == gateCells[side])
            return Messages.get("daily.outpost_gate_" + DIRECTIONS[side]);
        return null;
    }

    @Override public String doorDescription(int cell) {
        if (cell == entrance) return Messages.get("daily.outpost_entrance_desc");
        if (isCellDoor(cell) && map[cell] == Terrain.LOCKED_DOOR) return Messages.get("daily.outpost_cell_desc");
        for (int gate : gateCells) if (cell == gate) return Messages.get("daily.outpost_gate_desc");
        return null;
    }

    @Override public String tileName(int tile) {
        return tile == Terrain.ENTRANCE ? Messages.get("daily.outpost_entrance") : super.tileName(tile);
    }

    @Override public String tileDesc(int tile) {
        return tile == Terrain.ENTRANCE ? Messages.get("daily.outpost_entrance_desc") : super.tileDesc(tile);
    }

    @Override public boolean insideBossRoom(int cell) { return false; }

    @Override public int randomRespawnCell() {
        int chosen = -1, count = 0;
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || Actor.findChar(cell) != null || cell == captiveCells[0] || cell == captiveCells[1]) continue;
            if (Random.Int(++count) == 0) chosen = cell;
        }
        return chosen;
    }

    @Override public String tilesTex() { return Difficulties.is3d ? Assets.TILES_PRISON_3D : Assets.TILES_PRISON; }
    @Override public String waterTex() { return Assets.WATER_PRISON; }
}
