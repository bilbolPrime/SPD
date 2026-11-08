package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyDemon;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Callback;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class DailySealLevel extends DailyLevel {
    private static final float[] CADENCE = {30, 40, 55};
    private static final int[] NO_WARNINGS = new int[0];
    public final int[] siteCells = new int[3];
    public int[] cacheCells;
    private final boolean[] closed = new boolean[3];
    private final int[] pending = {-1, -1, -1};
    private final float[] warningDue = new float[3], nextSpawn = CADENCE.clone();
    private float lastUpdate = -1, debuffReady;
    private int operating = -1;
    private ArrayList<Chamber> chambers;

    private static class Chamber {
        int x, y, width, height, center;

        Chamber(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    {
        color1 = 0x801500;
        color2 = 0xA68521;
        viewDistance = 6;
    }

    @Override protected boolean build() {
        scenario = 4;
        feeling = Feeling.NONE;
        Arrays.fill(map, Terrain.WALL);
        chambers = new ArrayList<>();
        chambers.add(new Chamber(1, 1, 30, 30));
        int count = Random.IntRange(7, 9);
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
                chambers.add(new Chamber(largest.x + cut, largest.y, largest.width - cut, largest.height));
                largest.width = cut;
            } else {
                chambers.add(new Chamber(largest.x, largest.y + cut, largest.width, largest.height - cut));
                largest.height = cut;
            }
        }
        for (Chamber room : chambers) {
            int width = Random.IntRange(5, Math.min(10, room.width - 2));
            int height = Random.IntRange(5, Math.min(10, room.height - 2));
            room.x += Random.IntRange(1, room.width - width - 1);
            room.y += Random.IntRange(1, room.height - height - 1);
            room.width = width;
            room.height = height;
            room.center = (room.y + height / 2) * WIDTH + room.x + width / 2;
            for (int y = room.y; y < room.y + height; y++) for (int x = room.x; x < room.x + width; x++)
                map[y * WIDTH + x] = Terrain.EMPTY;
        }
        int start = Random.Int(chambers.size());
        entrance = chambers.get(start).center;
        boolean[] joined = new boolean[chambers.size()];
        boolean[][] linked = new boolean[chambers.size()][chambers.size()];
        joined[start] = true;
        for (int i = 1; i < chambers.size(); i++) {
            int from = -1, to = -1, shortest = Integer.MAX_VALUE;
            for (int a = 0; a < chambers.size(); a++) if (joined[a]) {
                for (int b = 0; b < chambers.size(); b++) if (!joined[b]) {
                    int cost = distanceBetween(chambers.get(a).center, chambers.get(b).center) + Random.Int(8);
                    if (cost < shortest) {
                        shortest = cost;
                        from = a;
                        to = b;
                    }
                }
            }
            passage(chambers.get(from).center, chambers.get(to).center);
            linked[from][to] = linked[to][from] = true;
            joined[to] = true;
        }
        int loops = Random.IntRange(3, 5);
        for (int i = 0; i < loops; i++) {
            int from = -1, to = -1, shortest = Integer.MAX_VALUE;
            for (int a = 0; a < chambers.size(); a++) for (int b = a + 1; b < chambers.size(); b++) {
                if (linked[a][b]) continue;
                int cost = distanceBetween(chambers.get(a).center, chambers.get(b).center) + Random.Int(16);
                if (cost < shortest) {
                    shortest = cost;
                    from = a;
                    to = b;
                }
            }
            passage(chambers.get(from).center, chambers.get(to).center);
            linked[from][to] = linked[to][from] = true;
        }
        ArrayList<Integer> unused = new ArrayList<>();
        for (Chamber room : chambers) if (room.center != entrance) unused.add(room.center);
        ArrayList<Integer> sites = new ArrayList<>();
        for (int i = 0; i < siteCells.length; i++) {
            int furthest = -1, widest = -1;
            for (int cell : unused) {
                int nearest = Level.distance(cell, entrance);
                for (int site : sites) nearest = Math.min(nearest, Level.distance(cell, site));
                if (nearest > widest) {
                    widest = nearest;
                    furthest = cell;
                }
            }
            sites.add(furthest);
            unused.remove(Integer.valueOf(furthest));
        }
        for (int i = 0; i < siteCells.length; i++) siteCells[i] = sites.remove(Random.Int(sites.size()));
        cacheCells = new int[Random.IntRange(2, Math.min(3, unused.size()))];
        for (int i = 0; i < cacheCells.length; i++) cacheCells[i] = unused.remove(Random.Int(unused.size()));
        for (int site : siteCells) for (int dy : new int[]{-2, 2}) for (int dx : new int[]{-2, 2}) {
            int cell = site + dy * WIDTH + dx;
            if (!open(cell)) continue;
            map[cell] = Terrain.STATUE;
            if (!routesOpen()) map[cell] = Terrain.EMPTY;
        }
        map[entrance] = Terrain.ENTRANCE;
        for (int site : siteCells) map[site] = Terrain.PEDESTAL;
        goal = siteCells[0];
        supplyCell = cacheCells[0];
        exit = -1;
        calculateBudget();
        return true;
    }

    private static int distanceBetween(int a, int b) {
        return Math.abs(a % WIDTH - b % WIDTH) + Math.abs(a / WIDTH - b / WIDTH);
    }

    private void passage(int from, int to) {
        int x = from % WIDTH, y = from / WIDTH;
        boolean horizontal = Random.Int(2) == 0;
        while (x != to % WIDTH || y != to / WIDTH) {
            if (x != to % WIDTH && (horizontal || y == to / WIDTH)) x += Integer.signum(to % WIDTH - x);
            else y += Integer.signum(to / WIDTH - y);
            map[y * WIDTH + x] = Terrain.EMPTY;
        }
    }

    private boolean open(int cell) {
        return cell >= 0 && cell < LENGTH && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
    }

    private boolean routesOpen() {
        int[] reachable = distances(entrance);
        for (int cell = 0; cell < LENGTH; cell++) if (open(cell) && reachable[cell] < 0) return false;
        return true;
    }

    private void calculateBudget() {
        int[] start = distances(entrance);
        int[][] route = new int[3][];
        for (int i = 0; i < siteCells.length; i++) route[i] = distances(siteCells[i]);
        int longest = 0;
        for (int first = 0; first < 3; first++) for (int second = 0; second < 3; second++) {
            if (first == second) continue;
            int third = 3 - first - second;
            longest = Math.max(longest, start[siteCells[first]] + route[first][siteCells[second]]
                    + route[second][siteCells[third]]);
        }
        if (!routesOpen()) throw new IllegalStateException("Unreachable ritual site");
        turnLimit = Math.max(900, (longest + cacheCells.length * 15) * 4 + 400);
    }

    @Override protected void decorate() {
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] == Terrain.EMPTY && Random.Int(14) == 0) map[cell] = Terrain.EMPTY_DECO;
            else if (map[cell] == Terrain.WALL && open(cell + WIDTH) && Random.Int(4) == 0) map[cell] = Terrain.WALL_DECO;
        }
    }

    @Override protected void createMobs() {
        for (int site = 0; site < siteCells.length; site++) {
            int cell = spawnCell(site, null);
            if (cell < 0) continue;
            DailyDemon demon = new DailyDemon(site, site);
            demon.pos = cell;
            mobs.add(demon);
        }
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || Level.distance(cell, entrance) <= 7) continue;
            boolean nearSite = false;
            for (int site : siteCells) if (Level.distance(cell, site) <= 3) nearSite = true;
            if (!nearSite) candidates.add(cell);
        }
        int roaming = Random.IntRange(2, 3);
        for (int i = 0; i < roaming && !candidates.isEmpty(); i++) {
            int cell = candidates.remove(Random.Int(candidates.size()));
            DailyDemon demon = new DailyDemon(Random.Int(3), -1);
            demon.pos = cell;
            mobs.add(demon);
            for (int j = candidates.size() - 1; j >= 0; j--)
                if (Level.distance(cell, candidates.get(j)) < 3) candidates.remove(j);
        }
    }

    @Override protected void createItems() {
        for (int i = 0; i < cacheCells.length; i++) {
            int cell = cacheCells[i];
            drop(new Food().identify(), cell);
            drop((Random.Int(2) == 0 ? new PotionOfHealing() : new PotionOfMana()).identify(), cell);
            if (i == 0) drop(armoryItem().upgrade().identify(), cell);
            else if (Random.Int(2) == 0) drop(new ScrollOfUpgrade().identify(), cell);
            else drop(new PotionOfRejuvenation().identify(), cell);
            heaps.get(cell).type = Heap.Type.CHEST;
        }
    }

    private boolean current(Hero hero) {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level == this
                && hero != null && hero == Dungeon.hero && hero.isAlive();
    }

    public void updateRitual() {
        if (!current(Dungeon.hero)) return;
        float now = DailyRun.elapsedTurns();
        if (now <= lastUpdate) return;
        lastUpdate = now;
        for (int site = 0; site < siteCells.length; site++) {
            if (closed[site]) continue;
            if (pending[site] >= 0) {
                if (now < warningDue[site]) continue;
                int cell = pending[site];
                pending[site] = -1;
                if (now >= nextSpawn[site]) nextSpawn[site] = now + CADENCE[site];
                if (atCapacity(site) || !canSpawn(cell, Dungeon.hero)) continue;
                DailyDemon demon = new DailyDemon(site, site);
                demon.pos = cell;
                GameScene.add(demon, 1f);
                if (Dungeon.visible[cell]) CellEmitter.center(cell).burst(Speck.factory(Speck.LIGHT), 6);
                continue;
            }
            if (now < nextSpawn[site]) continue;
            nextSpawn[site] = now + CADENCE[site];
            if (atCapacity(site)) continue;
            int cell = spawnCell(site, Dungeon.hero);
            if (cell < 0) continue;
            pending[site] = cell;
            warningDue[site] = now + 2;
        }
    }

    private boolean atCapacity(int site) {
        int local = 0, total = 0;
        for (Mob mob : mobs) if (mob instanceof DailyDemon && mob.isAlive()) {
            total++;
            if (((DailyDemon)mob).sourceSite() == site) local++;
        }
        return local >= 3 || total >= 9;
    }

    private int spawnCell(int site, Hero avoidHero) {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            int distance = Level.distance(cell, siteCells[site]);
            if (distance >= 1 && distance <= 3 && canSpawn(cell, avoidHero)) candidates.add(cell);
        }
        return candidates.isEmpty() ? -1 : candidates.get(Random.Int(candidates.size()));
    }

    private boolean canSpawn(int cell, Hero avoidHero) {
        if (!open(cell) || Level.distance(cell, entrance) <= 7 || map[cell] == Terrain.PEDESTAL
                || Actor.findChar(cell) != null || heaps.get(cell) != null) return false;
        if (avoidHero != null && Level.distance(cell, avoidHero.pos) <= 1) return false;
        for (int mark : pending) if (cell == mark) return false;
        for (Mob mob : mobs) if (mob.isAlive() && mob.pos == cell) return false;
        return true;
    }

    public int[] warningCells() {
        return current(Dungeon.hero) ? pending : NO_WARNINGS;
    }

    public boolean tryDebuff() {
        if (!current(Dungeon.hero) || DailyRun.elapsedTurns() < debuffReady) return false;
        debuffReady = DailyRun.elapsedTurns() + 8;
        return true;
    }

    public int sitesClosed() {
        int count = 0;
        for (boolean sealed : closed) if (sealed) count++;
        return count;
    }

    public boolean siteClosed(int site) { return site >= 0 && site < closed.length && closed[site]; }

    public String siteName(int site) { return Messages.get("daily.seal_site_" + site); }

    public void interactSite(final Hero hero) {
        if (!current(hero) || !hero.ready || operating >= 0) return;
        int found = -1;
        for (int i = 0; i < siteCells.length; i++) if (hero.pos == siteCells[i] && !closed[i]) found = i;
        if (found < 0) return;
        final int site = found;
        GameScene.show(new WndOptions(siteName(site), Messages.get("daily.seal_close_prompt"),
                Messages.get("daily.seal_close"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !current(hero) || !hero.ready || operating >= 0 || closed[site]
                        || hero.pos != siteCells[site]) return;
                operating = site;
                hero.busy();
                hero.spend(2f);
                Callback complete = () -> finishClosing(hero, site);
                if (hero.sprite != null) hero.sprite.operate(hero.pos, complete);
                else complete.call();
            }
        });
    }

    private void finishClosing(Hero hero, int site) {
        if (operating != site) return;
        operating = -1;
        if (!current(hero)) return;
        if (!closed[site] && hero.pos == siteCells[site]) {
            closed[site] = true;
            pending[site] = -1;
            if (Dungeon.visible[hero.pos]) CellEmitter.center(hero.pos).burst(Speck.factory(Speck.LIGHT), 8);
        }
        if (hero.sprite != null) hero.sprite.idle();
        if (sitesClosed() == siteCells.length) DailyRun.finish(true);
        hero.next();
    }

    @Override public String doorName(int cell) {
        if (cell == entrance) return Messages.get("daily.seal_entrance");
        for (int i = 0; i < siteCells.length; i++) if (cell == siteCells[i])
            return closed[i] ? Messages.get("daily.seal_closed") : siteName(i);
        return null;
    }

    @Override public String doorDescription(int cell) {
        if (cell == entrance) return Messages.get("daily.seal_entrance_desc");
        for (int i = 0; i < siteCells.length; i++) if (cell == siteCells[i])
            return Messages.get(closed[i] ? "daily.seal_closed_desc" : "daily.seal_site_desc_" + i);
        return null;
    }

    @Override public String tileName(int tile) {
        return tile == Terrain.ENTRANCE ? Messages.get("daily.seal_entrance") : super.tileName(tile);
    }

    @Override public String tileDesc(int tile) {
        return tile == Terrain.ENTRANCE ? Messages.get("daily.seal_entrance_desc") : super.tileDesc(tile);
    }

    @Override public boolean insideBossRoom(int cell) { return false; }

    @Override public int randomRespawnCell() {
        int chosen = -1, count = 0;
        for (int cell = 0; cell < LENGTH; cell++)
            if (open(cell) && Actor.findChar(cell) == null && Random.Int(++count) == 0) chosen = cell;
        return chosen;
    }

    @Override public String tilesTex() { return Difficulties.is3d ? Assets.TILES_HALLS_3D : Assets.TILES_HALLS; }
    @Override public String waterTex() { return Assets.WATER_HALLS; }
}
