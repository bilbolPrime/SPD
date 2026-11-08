package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyFoe;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyQuarry;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfLiquidFlame;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.windows.WndMessage;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class DailyHuntLevel extends DailyLevel {
    public final int[] quarryCells = new int[3];
    public int[] cacheCells;
    private final boolean[] slain = new boolean[3];
    private final int[][] patrols = new int[3][];

    {
        color1 = 0x739CAD;
        color2 = 0xCAE4EB;
        viewDistance = 6;
    }

    @Override protected boolean build() {
        scenario = 3;
        feeling = Feeling.NONE;
        Arrays.fill(map, Terrain.WALL);
        Arrays.fill(quarryCells, -1);
        for (int y = 2; y < HEIGHT - 2; y++) for (int x = 2; x < WIDTH - 2; x++)
            map[y * WIDTH + x] = Random.Int(100) < 54 ? Terrain.EMPTY : Terrain.WALL;
        for (int pass = 0; pass < 3; pass++) {
            int[] smooth = map.clone();
            for (int y = 2; y < HEIGHT - 2; y++) for (int x = 2; x < WIDTH - 2; x++) {
                int cell = y * WIDTH + x, walls = 0;
                for (int offset : NEIGHBOURS8) if (map[cell + offset] == Terrain.WALL) walls++;
                if (walls > 4) smooth[cell] = Terrain.WALL;
                else if (walls < 4) smooth[cell] = Terrain.EMPTY;
            }
            map = smooth;
        }
        int[] hollows = new int[6];
        for (int i = 0; i < hollows.length; i++) {
            int chosen = -1, widest = -1;
            for (int sample = 0; sample < 64; sample++) {
                int cell = Random.IntRange(4, 27) * WIDTH + Random.IntRange(4, 27);
                int nearest = 40;
                for (int j = 0; j < i; j++) nearest = Math.min(nearest, Level.distance(cell, hollows[j]));
                if (nearest > widest) {
                    widest = nearest;
                    chosen = cell;
                }
            }
            hollows[i] = chosen;
            clearing(chosen, Random.IntRange(2, 3));
        }
        joinCaverns();
        chooseEntrance();
        int[] fromEntrance = distances(entrance);
        exit = entrance;
        for (int cell = 0; cell < LENGTH; cell++)
            if (roomForFight(cell) && fromEntrance[cell] > fromEntrance[exit]) exit = cell;
        clearing(entrance, 2);
        clearing(exit, 2);
        for (int i = 0; i < quarryCells.length; i++) {
            quarryCells[i] = chooseQuarry(i);
            clearing(quarryCells[i], 2);
        }

        fromEntrance = distances(entrance);
        if (Level.distance(entrance, exit) < 12) {
            int farthest = -1;
            for (int cell = 0; cell < LENGTH; cell++) {
                if (!roomForFight(cell) || fromEntrance[cell] < 0 || Level.distance(entrance, cell) < 12) continue;
                boolean nearQuarry = false;
                for (int quarry : quarryCells) if (Level.distance(cell, quarry) < 3) nearQuarry = true;
                if (!nearQuarry && (farthest < 0 || fromEntrance[cell] > fromEntrance[farthest])) farthest = cell;
            }
            if (farthest < 0) throw new IllegalStateException("No distant exit in the hunting caves");
            exit = farthest;
        }
        for (int i = 0; i < quarryCells.length; i++) patrols[i] = makePatrol(quarryCells[i]);
        cacheCells = new int[Random.IntRange(2, 3)];
        Arrays.fill(cacheCells, -1);
        for (int i = 0; i < cacheCells.length; i++) cacheCells[i] = chooseCache(i);
        supplyCell = cacheCells[0];
        goal = exit;
        map[entrance] = Terrain.ENTRANCE;
        map[exit] = Terrain.EXIT;
        calculateBudget();
        return true;
    }

    private void clearing(int center, int radius) {
        for (int y = -radius; y <= radius; y++) for (int x = -radius; x <= radius; x++) {
            if (Math.abs(x) == radius && Math.abs(y) == radius && Random.Int(2) == 0) continue;
            dig(center % WIDTH + x, center / WIDTH + y);
        }
    }

    private void dig(int x, int y) {
        if (x > 0 && x < WIDTH - 1 && y > 0 && y < HEIGHT - 1) map[y * WIDTH + x] = Terrain.EMPTY;
    }

    private boolean open(int cell) {
        return cell >= 0 && cell < LENGTH && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
    }

    private void joinCaverns() {
        ArrayList<ArrayList<Integer>> regions = new ArrayList<>();
        boolean[] seen = new boolean[LENGTH];
        for (int start = 0; start < LENGTH; start++) {
            if (!open(start) || seen[start]) continue;
            ArrayList<Integer> region = new ArrayList<>();
            region.add(start);
            seen[start] = true;
            for (int read = 0; read < region.size(); read++) {
                int cell = region.get(read);
                for (int offset : NEIGHBOURS4) {
                    int next = cell + offset;
                    if (!open(next) || seen[next]) continue;
                    seen[next] = true;
                    region.add(next);
                }
            }
            if (region.size() < 8) {
                for (int cell : region) map[cell] = Terrain.WALL;
            } else regions.add(region);
        }
        regions.sort((a, b) -> Integer.compare(b.size(), a.size()));
        ArrayList<Integer> joined = regions.get(0);
        for (int i = 1; i < regions.size(); i++) {
            int from = -1, to = -1, shortest = Integer.MAX_VALUE;
            for (int a : regions.get(i)) for (int b : joined) {
                int distance = Math.abs(a % WIDTH - b % WIDTH) + Math.abs(a / WIDTH - b / WIDTH);
                if (distance < shortest) {
                    shortest = distance;
                    from = a;
                    to = b;
                }
            }
            while (from != to) {
                int x = from % WIDTH, y = from / WIDTH;
                if (x != to % WIDTH && (y == to / WIDTH || Random.Int(2) == 0)) x += Integer.signum(to % WIDTH - x);
                else y += Integer.signum(to / WIDTH - y);
                dig(x, y);
                from = y * WIDTH + x;
                joined.add(from);
            }
            joined.addAll(regions.get(i));
        }
    }

    private boolean roomForFight(int cell) {
        int x = cell % WIDTH, y = cell / WIDTH;
        return open(cell) && x >= 3 && y >= 3 && x < WIDTH - 3 && y < HEIGHT - 3;
    }

    private void chooseEntrance() {
        int side = Random.Int(4), closest = Integer.MAX_VALUE;
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!roomForFight(cell)) continue;
            int x = cell % WIDTH, y = cell / WIDTH;
            int distance = side == 0 ? y : side == 1 ? HEIGHT - 1 - y : side == 2 ? x : WIDTH - 1 - x;
            if (distance < closest) {
                closest = distance;
                candidates.clear();
            }
            if (distance == closest) candidates.add(cell);
        }
        entrance = candidates.get(Random.Int(candidates.size()));
    }

    private int chooseQuarry(int count) {
        int[] fromEntrance = distances(entrance);
        int widest = -1;
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!roomForFight(cell) || fromEntrance[cell] < 12 || Level.distance(cell, entrance) <= 7) continue;
            int nearest = Math.min(Level.distance(cell, entrance), Level.distance(cell, exit));
            for (int i = 0; i < count; i++) nearest = Math.min(nearest, Level.distance(cell, quarryCells[i]));
            if (nearest > widest) {
                widest = nearest;
                candidates.clear();
            }
            if (nearest == widest) candidates.add(cell);
        }
        return candidates.get(Random.Int(candidates.size()));
    }

    private int[] makePatrol(int center) {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++)
            if (open(cell) && Level.distance(cell, center) == 2 && Level.distance(cell, entrance) > 7) candidates.add(cell);
        int first = candidates.remove(Random.Int(candidates.size()));
        int second = candidates.get(0);
        for (int cell : candidates) if (Level.distance(cell, first) > Level.distance(second, first)) second = cell;
        return new int[]{center, first, second};
    }

    private int chooseCache(int count) {
        int[] fromEntrance = distances(entrance), toExit = distances(exit);
        int[][] toQuarry = new int[quarryCells.length][];
        for (int i = 0; i < quarryCells.length; i++) toQuarry[i] = distances(quarryCells[i]);
        int best = Integer.MIN_VALUE;
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!roomForFight(cell) || fromEntrance[cell] < 8) continue;
            int spacing = Level.distance(cell, exit);
            for (int quarry : quarryCells) spacing = Math.min(spacing, Level.distance(cell, quarry));
            for (int i = 0; i < count; i++) spacing = Math.min(spacing, Level.distance(cell, cacheCells[i]));
            if (spacing < 4) continue;
            int detour = fromEntrance[cell] + toExit[cell] - fromEntrance[exit];
            for (int i = 0; i < quarryCells.length; i++)
                detour = Math.min(detour, fromEntrance[cell] + toQuarry[i][cell] - fromEntrance[quarryCells[i]]);
            int walls = 0;
            for (int offset : NEIGHBOURS4) if (!open(cell + offset)) walls++;
            int score = detour * 2 + walls * 3 + Random.Int(7);
            if (score > best) {
                best = score;
                candidates.clear();
            }
            if (score == best) candidates.add(cell);
        }
        return candidates.get(Random.Int(candidates.size()));
    }

    private void calculateBudget() {
        int[][] routes = new int[3][];
        for (int i = 0; i < routes.length; i++) routes[i] = distances(quarryCells[i]);
        int[] start = distances(entrance);
        int longest = 0;
        for (int first = 0; first < 3; first++) for (int second = 0; second < 3; second++) {
            if (first == second) continue;
            int third = 3 - first - second;
            int route = start[quarryCells[first]] + routes[first][quarryCells[second]]
                    + routes[second][quarryCells[third]] + routes[third][exit];
            longest = Math.max(longest, route);
        }
        for (int cell : cacheCells) {
            if (start[cell] < 0) throw new IllegalStateException("Unreachable hunting supplies");
            longest += 15;
        }
        if (start[exit] < 0) throw new IllegalStateException("Unreachable hunting exit");
        turnLimit = Math.max(900, longest * 4 + 400);
    }

    @Override protected void decorate() {
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] == Terrain.EMPTY && Random.Int(12) == 0) map[cell] = Terrain.EMPTY_DECO;
            else if (map[cell] == Terrain.WALL && open(cell + WIDTH) && Random.Int(3) == 0) map[cell] = Terrain.WALL_DECO;
        }
    }

    @Override protected void createMobs() {
        for (int i = 0; i < quarryCells.length; i++) {
            DailyQuarry quarry = new DailyQuarry(i, quarryPatrol(i));
            quarry.pos = quarryCells[i];
            mobs.add(quarry);
        }
        int[] fromEntrance = distances(entrance);
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!open(cell) || fromEntrance[cell] < 10 || Level.distance(cell, entrance) <= 7 || cell == exit) continue;
            boolean nearQuarry = false;
            for (int quarry : quarryCells) if (Level.distance(cell, quarry) <= 3) nearQuarry = true;
            if (!nearQuarry) candidates.add(cell);
        }
        int count = Random.IntRange(7, 10);
        for (int i = 0; i < count && !candidates.isEmpty(); i++) {
            int cell = candidates.remove(Random.Int(candidates.size()));
            DailyFoe bat = new DailyFoe(11);
            bat.pos = cell;
            mobs.add(bat);
            for (int j = candidates.size() - 1; j >= 0; j--)
                if (Level.distance(cell, candidates.get(j)) < 3) candidates.remove(j);
        }
    }

    @Override protected void createItems() {
        int flameCache = Random.Int(cacheCells.length);
        for (int i = 0; i < cacheCells.length; i++) {
            int cell = cacheCells[i];
            drop(new Food().identify(), cell);
            drop((Random.Int(2) == 0 ? new PotionOfHealing() : new PotionOfMana()).identify(), cell);
            if (i == flameCache) drop(new PotionOfLiquidFlame().identify(), cell);
            if (i == 0) drop(armoryItem().upgrade().identify(), cell);
            else if (Random.Int(2) == 0) drop(new ScrollOfUpgrade().identify(), cell);
            else drop(new PotionOfRejuvenation().identify(), cell);
            heaps.get(cell).type = Heap.Type.CHEST;
        }
    }

    public int[] quarryPatrol(int kind) { return patrols[kind].clone(); }

    public boolean isQuarrySlain(int kind) { return kind >= 0 && kind < slain.length && slain[kind]; }

    public int quarryRemaining() {
        int remaining = 0;
        for (boolean dead : slain) if (!dead) remaining++;
        return remaining;
    }

    public void quarrySlain(int kind) {
        if (current(Dungeon.hero) && kind >= 0 && kind < slain.length) slain[kind] = true;
    }

    private boolean current(Hero hero) {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level == this
                && hero != null && hero == Dungeon.hero && hero.isAlive();
    }

    public void depart(final Hero hero) {
        if (!current(hero) || hero.pos != exit) return;
        int remaining = quarryRemaining();
        if (remaining != 0) {
            GameScene.show(new WndMessage(remaining == 1 ? Messages.get("daily.hunt_left_one")
                    : Messages.get("daily.hunt_left", remaining)));
            return;
        }
        GameScene.show(new WndOptions(Messages.get("daily.hunt_exit"), Messages.get("daily.hunt_depart_prompt"),
                Messages.get("daily.hunt_depart"), Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !current(hero) || hero.pos != exit || quarryRemaining() != 0) return;
                hero.spendAndNext(1f);
                DailyRun.finish(true);
            }
        });
    }

    @Override public String doorName(int cell) {
        if (cell == entrance) return Messages.get("daily.hunt_entrance");
        return cell == exit ? Messages.get("daily.hunt_exit") : null;
    }

    @Override public String doorDescription(int cell) {
        if (cell == entrance) return Messages.get("daily.hunt_entrance_desc");
        return cell == exit ? Messages.get("daily.hunt_exit_desc") : null;
    }

    @Override public String tileName(int tile) {
        if (tile == Terrain.ENTRANCE) return Messages.get("daily.hunt_entrance");
        if (tile == Terrain.EXIT) return Messages.get("daily.hunt_exit");
        if (tile == Terrain.WATER) return Messages.get(IceCavesLevel.class, "water_name");
        return super.tileName(tile);
    }

    @Override public String tileDesc(int tile) {
        if (tile == Terrain.ENTRANCE) return Messages.get("daily.hunt_entrance_desc");
        if (tile == Terrain.EXIT) return Messages.get("daily.hunt_exit_desc");
        if (tile == Terrain.WATER) return Messages.get(IceCavesLevel.class, "water_desc");
        if (tile == Terrain.WALL_DECO) return Messages.get(IceCavesLevel.class, "wall_desc");
        return super.tileDesc(tile);
    }

    @Override public boolean insideBossRoom(int cell) { return false; }

    @Override public int randomRespawnCell() {
        int chosen = -1, count = 0;
        for (int cell = 0; cell < LENGTH; cell++)
            if (open(cell) && Actor.findChar(cell) == null && Random.Int(++count) == 0) chosen = cell;
        return chosen;
    }

    @Override public String tilesTex() { return Difficulties.is3d ? Assets.TILES_ICE_CAVES_3D : Assets.TILES_ICE_CAVES; }
    @Override public String waterTex() { return Assets.WATER_ICE_CAVES; }
}
