package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyCityGuard;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Gold;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.keys.GoldenKey;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfInvisibility;
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

public class DailyHoardLevel extends DailyLevel {
    public int vaultDoor;
    private int captainCell;
    private int[] cacheCells, shortcutDoors;
    private final int[] approaches = new int[3];
    private final int[] plannedKinds = new int[24], plannedGates = new int[24], spawnOffsets = new int[24];
    private final int[] arrivals = new int[24], pending = new int[24];
    private final float[] due = new float[24], retryAfter = new float[24];
    private final ArrayList<Block> blocks = new ArrayList<>();
    private Block vault, square;
    private Gold tribute;
    private boolean alarm, taken, keyDropped;
    private float alarmTime, lastUpdate = -1;
    private static final int[] NO_WARNINGS = new int[0];

    private static class Block {
        int x, y, w, h, front, back;

        Block(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        int center() { return (y + h / 2) * WIDTH + x + w / 2; }

        int door(int side) {
            if (side == 0) return (y + 1) * WIDTH + x + Random.IntRange(3, w - 4);
            if (side == 1) return (y + Random.IntRange(3, h - 4)) * WIDTH + x + w - 2;
            if (side == 2) return (y + h - 2) * WIDTH + x + Random.IntRange(3, w - 4);
            return (y + Random.IntRange(3, h - 4)) * WIDTH + x + 1;
        }
    }

    {
        color1 = 0x665c60;
        color2 = 0x927d74;
    }

    @Override protected boolean build() {
        scenario = 6;
        feeling = Feeling.NONE;
        Arrays.fill(map, Terrain.WALL);
        Arrays.fill(pending, -1);
        blocks.clear();
        blocks.add(new Block(1, 1, WIDTH - 2, HEIGHT - 2));
        int count = Random.IntRange(7, 9);
        while (blocks.size() < count) {
            Block largest = null;
            for (Block block : blocks) {
                if (block.w < 14 && block.h < 14) continue;
                if (largest == null || block.w * block.h > largest.w * largest.h) largest = block;
            }
            if (largest == null) break;
            boolean vertical = largest.w >= 14 && (largest.h < 14 || largest.w > largest.h * 1.3f
                    || largest.h <= largest.w * 1.3f && Random.Int(2) == 0);
            int length = vertical ? largest.w : largest.h;
            int cut = Random.IntRange(Math.max(7, length / 2 - 3), Math.min(length - 7, length / 2 + 3));
            if (vertical) {
                blocks.add(new Block(largest.x + cut, largest.y, largest.w - cut, largest.h));
                largest.w = cut;
            } else {
                blocks.add(new Block(largest.x, largest.y + cut, largest.w, largest.h - cut));
                largest.h = cut;
            }
        }
        for (int y = 1; y < HEIGHT - 1; y++) for (int x = 1; x < WIDTH - 1; x++)
            map[y * WIDTH + x] = Terrain.EMPTY;
        int side = Random.Int(4);
        entrance = edge(side, Random.IntRange(4, WIDTH - 5));
        vault = blocks.get(0);
        for (Block block : blocks)
            if (Level.distance(block.center(), entrance) > Level.distance(vault.center(), entrance)) vault = block;
        ArrayList<Block> other = new ArrayList<>(blocks);
        other.remove(vault);
        square = other.remove(Random.Int(other.size()));
        Block barracks = other.get(Random.Int(other.size()));
        captainCell = barracks.center();
        ArrayList<Integer> shortcuts = new ArrayList<>();
        for (Block block : blocks) {
            if (block == square) continue;
            for (int y = block.y + 1; y < block.y + block.h - 1; y++)
                for (int x = block.x + 1; x < block.x + block.w - 1; x++)
                    map[y * WIDTH + x] = x == block.x + 1 || x == block.x + block.w - 2
                            || y == block.y + 1 || y == block.y + block.h - 2 ? Terrain.WALL : Terrain.EMPTY_SP;
            int facing = Random.Int(4);
            block.front = block.door(facing);
            map[block.front] = block == vault ? Terrain.LOCKED_DOOR : Terrain.DOOR;
            if (block != vault) {
                block.back = block.door((facing + 2) % 4);
                map[block.back] = shortcuts.size() < 3 ? Terrain.LOCKED_DOOR : Terrain.DOOR;
                if (map[block.back] == Terrain.LOCKED_DOOR) shortcuts.add(block.back);
            }
        }
        vaultDoor = vault.front;
        goal = vault.center();
        shortcutDoors = new int[shortcuts.size()];
        for (int i = 0; i < shortcuts.size(); i++) shortcutDoors[i] = shortcuts.get(i);
        cacheCells = new int[Math.min(4, other.size())];
        for (int i = 0; i < cacheCells.length; i++)
            cacheCells[i] = other.remove(Random.Int(other.size())).center();
        supplyCell = cacheCells[0];
        map[entrance] = Terrain.ENTRANCE;
        map[vaultDoor] = Terrain.OPEN_DOOR;
        int[] fromVault = distances(goal), fromEntrance = distances(entrance);
        ArrayList<Integer> gates = new ArrayList<>();
        int farthest = -1;
        for (int i = 1; i <= 3; i++) for (int along = 4; along < WIDTH - 4; along++) {
            int cell = edge((side + i) % 4, along);
            if (fromEntrance[cell] < 14) continue;
            if (farthest < 0 || fromVault[cell] > fromVault[farthest]) farthest = cell;
            if (fromVault[cell] >= 30) gates.add(cell);
        }
        exit = gates.isEmpty() ? farthest : gates.get(Random.Int(gates.size()));
        map[vaultDoor] = Terrain.LOCKED_DOOR;
        for (int i = 0; i < approaches.length; i++) {
            ArrayList<Integer> entryCells = new ArrayList<>();
            for (int along = 3; along < WIDTH - 3; along++) {
                int cell = edge((side + i + 1) % 4, along);
                if (Level.distance(cell, exit) > 3) entryCells.add(cell);
            }
            approaches[i] = entryCells.get(Random.Int(entryCells.size()));
            map[approaches[i]] = Terrain.EMPTY_SP;
        }
        map[exit] = Terrain.EXIT;

        mapped[exit] = true;
        for (int i = 0; i < plannedKinds.length; i++) {
            plannedKinds[i] = Random.Int(4) == 0 ? 3 : Random.IntRange(1, 2);
            plannedGates[i] = Random.Int(approaches.length);
            spawnOffsets[i] = Random.Int(9);
        }
        int[] reachable = distances(entrance);
        if (reachable[captainCell] < 0 || reachable[exit] < 0 || reachable[goal] >= 0)
            throw new IllegalStateException("Invalid treasury route");
        for (int cell : cacheCells) if (reachable[cell] < 0) throw new IllegalStateException("Unreachable city cache");
        map[vaultDoor] = Terrain.OPEN_DOOR;
        int route = distances(entrance)[captainCell] + distances(captainCell)[goal] + distances(goal)[exit];
        for (int cell : cacheCells) route += distances(cell)[vaultDoor];
        if (distances(goal)[exit] < 0) throw new IllegalStateException("Treasury has no escape route");
        turnLimit = Math.max(800, route * 3 + 300);
        map[vaultDoor] = Terrain.LOCKED_DOOR;
        return true;
    }

    private static int edge(int side, int along) {
        if (side == 0) return WIDTH + along;
        if (side == 1) return along * WIDTH + WIDTH - 2;
        if (side == 2) return (HEIGHT - 2) * WIDTH + along;
        return along * WIDTH + 1;
    }

    private boolean open(int cell) {
        return cell >= 0 && cell < LENGTH && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
    }

    @Override protected void decorate() {
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] == Terrain.EMPTY && Random.Int(24) == 0) map[cell] = Terrain.EMPTY_DECO;
            else if (map[cell] == Terrain.WALL && open(cell + WIDTH) && Random.Int(4) == 0) map[cell] = Terrain.WALL_DECO;
        }
        int cx = square.center() % WIDTH, cy = square.center() / WIDTH;
        for (int dx : new int[]{-2, 2}) for (int dy : new int[]{-2, 2})
            map[(cy + dy) * WIDTH + cx + dx] = Terrain.STATUE;
    }

    @Override protected void createMobs() {
        DailyCityGuard captain = new DailyCityGuard(0, captainCell);
        captain.pos = captainCell;
        mobs.add(captain);
        int[] reachable = distances(entrance);
        ArrayList<Integer> places = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || reachable[cell] < 9 || cell == exit || cell == captainCell
                    || map[cell] == Terrain.DOOR || map[cell] == Terrain.LOCKED_DOOR) continue;
            places.add(cell);
        }
        int count = Random.IntRange(8, 11);
        for (int i = 0; i < count && !places.isEmpty(); i++) {
            int cell = places.remove(Random.Int(places.size()));
            DailyCityGuard guard = new DailyCityGuard(i % 4 == 3 ? 3 : Random.IntRange(1, 2), cell);
            guard.pos = cell;
            mobs.add(guard);
            for (int j = places.size() - 1; j >= 0; j--)
                if (Level.distance(cell, places.get(j)) < 3) places.remove(j);
        }
    }

    @Override protected void createItems() {
        tribute = new Gold(Random.IntRange(600, 900));
        drop(tribute, goal);
        ArrayList<Integer> places = new ArrayList<>();
        int[] reachable = distances(entrance);
        for (int cell = 0; cell < LENGTH; cell++)
            if (open(cell) && reachable[cell] >= 4 && cell != exit && cell != captainCell
                    && map[cell] != Terrain.DOOR && !cacheAt(cell)) places.add(cell);
        for (int i = 0; i < shortcutDoors.length; i++) drop(new IronKey(), places.remove(Random.Int(places.size())));
        drop(new GoldenKey(), places.remove(Random.Int(places.size())));
        for (int i = 0; i < cacheCells.length; i++) {
            int cell = cacheCells[i];
            drop(new Food().identify(), cell);
            drop((Random.Int(2) == 0 ? new PotionOfHealing() : new PotionOfMana()).identify(), cell);
            if (i == 0) drop(armoryItem().upgrade().identify(), cell);
            else if (i == 1) drop(new PotionOfInvisibility().identify(), cell);
            else if (i == 2) drop(new ScrollOfUpgrade().identify(), cell);
            else drop(new PotionOfRejuvenation().identify(), cell);
            heaps.get(cell).type = i == 0 ? Heap.Type.LOCKED_CHEST : Heap.Type.CHEST;
        }
    }

    private boolean cacheAt(int cell) {
        for (int cache : cacheCells) if (cache == cell) return true;
        return false;
    }

    private boolean current(Hero hero) {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level == this
                && hero != null && hero == Dungeon.hero && hero.isAlive();
    }

    public boolean alarmRaised() { return alarm; }
    public boolean goldTaken() { return taken; }
    public boolean captainDefeated() { return keyDropped; }

    public void captainDied(int cell) {
        if (!current(Dungeon.hero) || keyDropped) return;
        keyDropped = true;
        Heap heap = drop(new SkeletonKey(), cell);
        if (heap.sprite != null) heap.sprite.drop();
        GLog.p("daily.hoard_key_dropped");
    }

    public void interactVault(final Hero hero) {
        if (!current(hero) || !Level.adjacent(hero.pos, vaultDoor) || map[vaultDoor] != Terrain.LOCKED_DOOR) return;
        if (hero.belongings.getKey(SkeletonKey.class, Dungeon.levelKey()) == null) {
            GameScene.show(new WndMessage(Messages.get("daily.hoard_need_key")));
            return;
        }
        GameScene.show(new WndOptions(Messages.get("daily.hoard_vault"), Messages.get("daily.hoard_open_prompt"),
                Messages.get("daily.hoard_open"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !current(hero) || !Level.adjacent(hero.pos, vaultDoor)
                        || map[vaultDoor] != Terrain.LOCKED_DOOR) return;
                SkeletonKey key = hero.belongings.getKey(SkeletonKey.class, Dungeon.levelKey());
                if (key == null) return;
                key.detach(hero.belongings.backpack);
                setDoor(vaultDoor, Terrain.OPEN_DOOR);
                raiseAlarm();
                Sample.INSTANCE.play(Assets.SND_UNLOCK);
                hero.spendAndNext(1f);
            }
        });
    }

    private void raiseAlarm() {
        if (alarm) return;
        alarm = true;
        alarmTime = DailyRun.elapsedTurns();
        GLog.w("daily.hoard_alarm");
        Sample.INSTANCE.play(Assets.SND_ALERT);
        Dungeon.hero.interrupt();
        Dungeon.hero.restoreHealth = false;
        for (Mob mob : mobs) if (mob instanceof DailyCityGuard && mob.isAlive()) mob.beckon(goal);
    }

    public void collectGold(Gold gold, Hero hero) {
        if (!current(hero) || taken || gold != tribute) return;
        taken = true;

        if (map[vaultDoor] == Terrain.LOCKED_DOOR) setDoor(vaultDoor, Terrain.OPEN_DOOR);
        raiseAlarm();
        GLog.p("daily.hoard_take_gold");
    }

    public void depart(final Hero hero) {
        if (!current(hero) || hero.pos != exit) return;
        if (!taken) {
            GameScene.show(new WndMessage(Messages.get("daily.hoard_empty_handed")));
            return;
        }
        GameScene.show(new WndOptions(Messages.get("daily.hoard_exit"), Messages.get("daily.hoard_depart_prompt"),
                Messages.get("daily.hoard_depart"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !current(hero) || hero.pos != exit || !taken) return;
                hero.spendAndNext(1f);
                DailyRun.finish(true);
            }
        });
    }

    public void updateAlarm() {
        if (!current(Dungeon.hero) || !alarm) return;
        float now = DailyRun.elapsedTurns();
        if (now <= lastUpdate) return;
        lastUpdate = now;
        int living = 0;
        for (Mob mob : mobs) if (mob instanceof DailyCityGuard && mob.isAlive()) living++;
        for (int i = 0; i < arrivals.length; i++) {
            if (arrivals[i] != 1 || now < due[i]) continue;
            int cell = pending[i];
            pending[i] = -1;
            if (living >= 12 || !canSpawn(cell)) {
                arrivals[i] = 0;
                retryAfter[i] = now + 3;
                continue;
            }
            DailyCityGuard guard = new DailyCityGuard(plannedKinds[i], goal);
            guard.challengeExtra = true;
            guard.pos = cell;
            arrivals[i] = 2;
            living++;
            GameScene.add(guard, 1f);
            guard.beckon(goal);
        }
        int reserved = 0;
        for (int cell : pending) if (cell >= 0) reserved++;
        for (int i = 0; i < arrivals.length && living + reserved < 12; i++) {
            if (arrivals[i] != 0 || now < alarmTime + 12 + (i / 3) * 30 || now < retryAfter[i]) continue;
            int center = approaches[plannedGates[i]], cell = -1;
            for (int j = 0; j < 9; j++) {
                int offset = (spawnOffsets[i] + j) % 9;
                int candidate = center + (offset / 3 - 1) * WIDTH + offset % 3 - 1;
                if (canSpawn(candidate)) { cell = candidate; break; }
            }
            if (cell < 0) { retryAfter[i] = now + 3; continue; }
            arrivals[i] = 1;
            pending[i] = cell;
            due[i] = now + 3;
            reserved++;
        }
    }

    private boolean canSpawn(int cell) {
        if (!open(cell) || cell == entrance || cell == exit || heaps.get(cell) != null
                || Actor.findChar(cell) != null || Level.distance(cell, Dungeon.hero.pos) <= 2) return false;
        for (int warning : pending) if (warning == cell) return false;
        for (Mob mob : mobs) if (mob.isAlive() && mob.pos == cell) return false;
        return true;
    }

    public int[] warningCells() { return current(Dungeon.hero) ? pending : NO_WARNINGS; }

    public int patrolCell(int home, int step) {
        if (!open(home)) return -1;
        int[] reachable = distances(home);
        int choice = -1, best = -1;
        int direction = step % 4;
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || reachable[cell] < 1 || reachable[cell] > 5 || cell == exit || cell == entrance) continue;
            int dx = cell % WIDTH - home % WIDTH, dy = cell / WIDTH - home / WIDTH;
            int score = direction == 0 ? dx : direction == 1 ? dy : direction == 2 ? -dx : -dy;
            if (score > best) { best = score; choice = cell; }
        }
        return choice;
    }

    @Override public String doorName(int cell) {
        if (cell == vaultDoor) return Messages.get("daily.hoard_vault");
        if (cell == entrance) return Messages.get("daily.hoard_entrance");
        if (cell == exit) return Messages.get("daily.hoard_exit");
        for (int approach : approaches) if (cell == approach) return Messages.get("daily.hoard_approach");
        for (int door : shortcutDoors) if (cell == door) return Messages.get("daily.hoard_shortcut");
        return null;
    }

    @Override public String doorDescription(int cell) {
        if (cell == vaultDoor) return Messages.get(alarm ? "daily.hoard_vault_open" : "daily.hoard_need_key");
        if (cell == entrance) return Messages.get("daily.hoard_entrance_desc");
        if (cell == exit) return Messages.get("daily.hoard_exit_desc");
        for (int approach : approaches) if (cell == approach) return Messages.get("daily.hoard_approach_desc");
        for (int door : shortcutDoors) if (cell == door) return Messages.get(map[cell] == Terrain.LOCKED_DOOR
                ? "daily.hoard_shortcut_locked" : "daily.hoard_shortcut_open");
        return null;
    }

    @Override public String tilesTex() { return Difficulties.is3d ? Assets.TILES_CITY_3D : Assets.TILES_CITY; }
    @Override public String waterTex() { return Assets.WATER_CITY; }
}
