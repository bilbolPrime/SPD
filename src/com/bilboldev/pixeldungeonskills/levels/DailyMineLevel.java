package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyAlly;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDM300;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyFoe;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class DailyMineLevel extends DailyLevel {
    public int[] minerCells = new int[3];
    public int[] minerDoors = new int[3];
    private final int[] minerState = new int[3];
    private final int[] sites = new int[9];
    private final int[] caches = new int[2];
    private final int[] shortcuts = new int[2];
    private final int[] keys = new int[2];
    private int[] patrol;

    @Override protected boolean build() {
        scenario = 2;
        feeling = Feeling.NONE;
        Arrays.fill(map, Terrain.WALL);
        int side = Random.Int(4), along = Random.IntRange(6, 25);
        entrance = side == 0 ? 5 * WIDTH + along : side == 1 ? 26 * WIDTH + along
                : side == 2 ? along * WIDTH + 5 : along * WIDTH + 26;
        sites[0] = entrance;
        for (int i = 1; i < sites.length; i++) sites[i] = nextSite(i);
        for (int site : sites) excavate(site);
        boolean[] joined = new boolean[sites.length];
        joined[0] = true;
        for (int count = 1; count < sites.length; count++) {
            int from = -1, to = -1, shortest = Integer.MAX_VALUE;
            for (int a = 0; a < sites.length; a++) if (joined[a]) {
                for (int b = 0; b < sites.length; b++) if (!joined[b]) {
                    int cost = distance(sites[a], sites[b]) + Random.Int(10);
                    if (cost < shortest) {
                        shortest = cost;
                        from = a;
                        to = b;
                    }
                }
            }
            tunnel(sites[from], sites[to]);
            joined[to] = true;
        }
        for (int i = 0; i < 4; i++) {
            int from = Random.Int(sites.length), to = Random.Int(sites.length - 1);
            if (to >= from) to++;
            tunnel(sites[from], sites[to]);
        }
        for (int i = 0; i < minerCells.length; i++) {
            minerCells[i] = sites[i + 1];
            buildDigSite(i);
        }
        rescueCell = minerCells[0];
        rescueDoor = minerDoors[0];
        caches[0] = sites[4];
        caches[1] = sites[5];
        supplyCell = caches[0];
        goal = sites[6];
        for (int i = 7; i < sites.length; i++)
            if (distance(sites[i], entrance) > distance(goal, entrance)) goal = sites[i];
        patrol = Arrays.copyOfRange(sites, 4, sites.length);
        buildShortcuts();
        int[] reachable = distances(entrance);
        ArrayList<Integer> occupied = new ArrayList<>();
        for (int cache : caches) occupied.add(cache);
        for (int i = 0; i < keys.length; i++) {
            keys[i] = itemCell(reachable, occupied);
            occupied.add(keys[i]);
        }
        for (int i = 0; i < 7; i++) {
            int cell = itemCell(reachable, occupied);
            if (!clearPatch(cell, 1)) continue;
            map[cell] = Terrain.WALL;
            if (!routesOpen()) map[cell] = Terrain.EMPTY;
        }
        map[entrance] = Terrain.ENTRANCE;
        exit = -1;
        calculateBudget();
        return true;
    }

    private int nextSite(int count) {
        int furthest = -1;
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int y = 5; y <= 26; y++) for (int x = 5; x <= 26; x++) {
            int cell = y * WIDTH + x, nearest = Integer.MAX_VALUE;
            for (int i = 0; i < count; i++) nearest = Math.min(nearest, Level.distance(cell, sites[i]));
            if (count > 3) {
                boolean nearMiner = false;
                for (int i = 1; i <= 3; i++) if (Level.distance(cell, sites[i]) < 5) nearMiner = true;
                if (nearMiner) continue;
            }
            if (nearest > furthest) {
                furthest = nearest;
                candidates.clear();
            }
            if (nearest == furthest) candidates.add(cell);
        }
        return candidates.get(Random.Int(candidates.size()));
    }

    private void excavate(int center) {
        int rx = Random.IntRange(4, 5), ry = Random.IntRange(3, 5);
        for (int dy = -ry; dy <= ry; dy++) for (int dx = -rx; dx <= rx; dx++) {
            float edge = dx * dx / (float)(rx * rx) + dy * dy / (float)(ry * ry);
            if (edge < .75f || edge <= 1.15f && Random.Int(4) != 0) dig(center % WIDTH + dx, center / WIDTH + dy);
        }
    }

    private void tunnel(int start, int end) {
        int x = start % WIDTH, y = start / WIDTH;
        int endX = end % WIDTH, endY = end / WIDTH;
        while (x != endX || y != endY) {
            if (x != endX && (y == endY || Random.Int(2) == 0)) x += Integer.signum(endX - x);
            else y += Integer.signum(endY - y);
            dig(x, y);
            if (Random.Int(3) == 0) dig(x + 1, y);
        }
    }

    private void dig(int x, int y) {
        if (x > 0 && x < WIDTH - 1 && y > 0 && y < HEIGHT - 1) map[y * WIDTH + x] = Terrain.EMPTY;
    }

    private void buildDigSite(int index) {
        int center = minerCells[index];
        for (int y = -2; y <= 2; y++) for (int x = -2; x <= 2; x++)
            map[center + y * WIDTH + x] = (Math.abs(x) == 1 && Math.abs(y) <= 1)
                    || (Math.abs(y) == 1 && Math.abs(x) <= 1) ? Terrain.WALL : Terrain.EMPTY;
        int direction = NEIGHBOURS4[Random.Int(4)];
        minerDoors[index] = center + direction;
        map[minerDoors[index]] = Terrain.LOCKED_DOOR;
    }

    private void buildShortcuts() {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] != Terrain.WALL || reserved(cell)) continue;
            boolean horizontal = open(cell - 1) && open(cell + 1) && !open(cell - WIDTH) && !open(cell + WIDTH);
            boolean vertical = open(cell - WIDTH) && open(cell + WIDTH) && !open(cell - 1) && !open(cell + 1);
            if (horizontal || vertical) candidates.add(cell);
        }
        for (int i = 0; i < shortcuts.length; i++) {
            if (!candidates.isEmpty()) {
                shortcuts[i] = candidates.remove(Random.Int(candidates.size()));
                map[shortcuts[i]] = Terrain.LOCKED_DOOR;
                continue;
            }
            int center = sites[6 + i];
            int door = center + 1;
            for (int dy = -2; dy <= 2; dy++) for (int dx = -2; dx <= 2; dx++) dig(door % WIDTH + dx, door / WIDTH + dy);
            map[door - WIDTH] = map[door + WIDTH] = Terrain.WALL;
            map[door] = Terrain.LOCKED_DOOR;
            shortcuts[i] = door;
        }
    }

    private boolean clearPatch(int center, int radius) {
        if (reserved(center)) return false;
        for (int y = -radius; y <= radius; y++) for (int x = -radius; x <= radius; x++)
            if (!open(center + y * WIDTH + x)) return false;
        return true;
    }

    private boolean reserved(int cell) {
        if (Level.distance(cell, entrance) <= 2) return true;
        for (int center : minerCells) if (Level.distance(cell, center) <= 2) return true;
        for (int site : sites) if (cell == site) return true;
        return false;
    }

    private boolean open(int cell) {
        return cell >= 0 && cell < LENGTH && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
    }

    private boolean routesOpen() {
        int[] distance = distances(entrance);
        for (int i = 0; i < minerCells.length; i++)
            if (distance[minerDoors[i] + minerDoors[i] - minerCells[i]] < 0 || distance[minerCells[i]] >= 0) return false;
        for (int cell : patrol) if (distance[cell] < 0) return false;
        for (int cell : keys) if (cell > 0 && distance[cell] < 0) return false;
        return true;
    }

    private int itemCell(int[] reachable, ArrayList<Integer> occupied) {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++)
            if (reachable[cell] > 0 && open(cell) && !reserved(cell) && !occupied.contains(cell)) candidates.add(cell);
        return candidates.get(Random.Int(candidates.size()));
    }

    private void calculateBudget() {
        ArrayList<Integer> stops = new ArrayList<>();
        for (int i = 0; i < minerCells.length; i++) stops.add(minerDoors[i] + minerDoors[i] - minerCells[i]);
        for (int cell : caches) stops.add(cell);
        int route = 0, current = entrance;
        while (!stops.isEmpty()) {
            int[] distance = distances(current);
            int closest = 0;
            for (int i = 1; i < stops.size(); i++) if (distance[stops.get(i)] < distance[stops.get(closest)]) closest = i;
            int next = stops.remove(closest);
            if (distance[next] < 0) throw new IllegalStateException("Unreachable mine worksite");
            route += distance[next];
            current = next;
        }
        int home = distances(current)[entrance];
        if (home < 0 || !routesOpen()) throw new IllegalStateException("The mine lift cannot be reached");
        turnLimit = Math.max(800, (route + home) * 4 + 400);
    }

    @Override protected void decorate() {
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] == Terrain.EMPTY && Random.Int(14) == 0) map[cell] = Terrain.EMPTY_DECO;
            else if (map[cell] == Terrain.WALL && open(cell + WIDTH) && Random.Int(4) == 0) map[cell] = Terrain.WALL_DECO;
        }
    }

    @Override protected void createMobs() {
        for (int i = 0; i < minerCells.length; i++) {
            DailyAlly miner = new DailyAlly(2, i);
            miner.pos = minerCells[i];
            mobs.add(miner);
            addGuard(minerDoors[i] + minerDoors[i] - minerCells[i], 9);
        }
        for (int i = 4; i < sites.length; i++) {
            int count = Random.IntRange(1, 3);
            for (int j = 0; j < count; j++) addGuard(freeCellNear(sites[i]), Random.IntRange(8, 10));
        }
        DailyDM300 machine = new DailyDM300();
        machine.pos = goal;
        mobs.add(machine);
    }

    private void addGuard(int cell, int kind) {
        DailyFoe foe = new DailyFoe(kind);
        foe.pos = cell;
        mobs.add(foe);
    }

    private int freeCellNear(int center) {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || Level.distance(cell, center) > 4 || reserved(cell) || cell == goal) continue;
            boolean occupied = false;
            for (Mob mob : mobs) if (mob.pos == cell) occupied = true;
            if (!occupied) cells.add(cell);
        }
        return cells.get(Random.Int(cells.size()));
    }

    @Override protected void createItems() {
        for (int cell : keys) drop(new IronKey(), cell);
        drop(new Food().identify(), entrance);
        for (int cell : caches) {
            drop((Random.Int(2) == 0 ? new PotionOfHealing() : new PotionOfMana()).identify(), cell);
            drop(new Food().identify(), cell);
            drop(armoryItem().upgrade().identify(), cell);
            if (Random.Int(2) == 0) drop(new ScrollOfUpgrade().identify(), cell);
            else drop(new PotionOfRejuvenation().identify(), cell);
            heaps.get(cell).type = Heap.Type.CHEST;
        }
    }

    public int[] patrolCells() { return patrol.clone(); }

    public boolean isMinerDoor(int cell) {
        for (int door : minerDoors) if (cell == door) return true;
        return false;
    }

    public void interactRescue(final Hero hero, final int door) {
        int found = -1;
        for (int i = 0; i < minerDoors.length; i++) if (door == minerDoors[i]) found = i;
        if (found < 0 || !canFree(hero, found)) return;
        final int miner = found;
        GameScene.show(new WndOptions(Messages.get("daily.mine_cell"), Messages.get("daily.mine_free_prompt"),
                Messages.get("daily.mine_free"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !canFree(hero, miner)) return;
                minerState[miner] = 1;
                setDoor(door, Terrain.OPEN_DOOR);
                for (Mob mob : mobs) if (mob instanceof DailyAlly && ((DailyAlly)mob).rescueId() == miner)
                    ((DailyAlly)mob).free(hero);
                hero.spendAndNext(1f);
            }
        });
    }

    private boolean current(Hero hero) {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level == this
                && hero != null && hero == Dungeon.hero && hero.isAlive();
    }

    private boolean canFree(Hero hero, int miner) {
        return current(hero) && minerState[miner] == 0 && map[minerDoors[miner]] == Terrain.LOCKED_DOOR
                && Level.adjacent(hero.pos, minerDoors[miner]);
    }

    public boolean atLift(int cell) {
        return open(cell) && (cell == entrance || Level.adjacent(cell, entrance));
    }

    public int minersSafe() {
        int count = 0;
        for (int state : minerState) if (state == 2) count++;
        return count;
    }

    public void updateMiners() {
        if (!current(Dungeon.hero)) return;
        for (int id = 0; id < minerState.length; id++) {
            if (minerState[id] >= 2) continue;
            DailyAlly miner = null;
            for (Mob mob : mobs) if (mob instanceof DailyAlly && ((DailyAlly)mob).rescueId() == id) miner = (DailyAlly)mob;
            if (miner == null || !miner.isAlive()) {
                minerDied(id);
                return;
            }
            if (minerState[id] == 1 && atLift(miner.pos) && (miner.sprite == null || !miner.sprite.isMoving)) {
                minerState[id] = 2;
                miner.evacuate();
                GLog.p("daily.mine_safe");
            }
        }
    }

    public void minerDied(int id) {
        if (!current(Dungeon.hero) || id < 0 || id >= minerState.length || minerState[id] >= 2) return;
        minerState[id] = 3;
        DailyRun.fail("daily.miner_lost");
    }

    public void depart(final Hero hero) {
        if (!current(hero) || hero.pos != entrance) return;
        updateMiners();
        if (DailyRun.finished()) return;
        if (minersSafe() < 3) {
            int remaining = 3 - minersSafe();
            String message = remaining == 1 ? Messages.get("daily.mine_wait_one")
                    : Messages.get("daily.mine_wait", remaining);
            GameScene.show(new WndMessage(message));
            return;
        }
        GameScene.show(new WndOptions(Messages.get("daily.mine_lift"), Messages.get("daily.mine_depart_prompt"),
                Messages.get("daily.mine_depart"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !current(hero) || hero.pos != entrance || minersSafe() != 3) return;
                hero.spendAndNext(1f);
                DailyRun.finish(true);
            }
        });
    }

    @Override public String doorName(int cell) {
        if (cell == entrance) return Messages.get("daily.mine_lift");
        return map[cell] == Terrain.LOCKED_DOOR && isMinerDoor(cell) ? Messages.get("daily.mine_cell") : null;
    }

    @Override public String doorDescription(int cell) {
        if (cell == entrance) return Messages.get("daily.mine_lift_desc");
        return map[cell] == Terrain.LOCKED_DOOR && isMinerDoor(cell) ? Messages.get("daily.mine_cell_desc") : null;
    }

    @Override public String tileName(int tile) {
        return tile == Terrain.ENTRANCE ? Messages.get("daily.mine_lift") : super.tileName(tile);
    }

    @Override public String tileDesc(int tile) {
        return tile == Terrain.ENTRANCE ? Messages.get("daily.mine_lift_desc") : super.tileDesc(tile);
    }

    @Override public boolean insideBossRoom(int cell) { return false; }

    @Override public int randomRespawnCell() {
        int chosen = -1, count = 0;
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || Actor.findChar(cell) != null) continue;
            boolean captive = false;
            for (int i = 0; i < minerCells.length; i++) if (minerState[i] == 0 && cell == minerCells[i]) captive = true;
            if (!captive && Random.Int(++count) == 0) chosen = cell;
        }
        return chosen;
    }

    @Override public String tilesTex() { return Difficulties.is3d ? Assets.TILES_CAVES_3D : Assets.TILES_CAVES; }
    @Override public String waterTex() { return Assets.WATER_CAVES; }
}
