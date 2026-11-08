package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Difficulties;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyAlly;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyFoe;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyRatKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.DailyTengu;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.EquipmentAttribute;
import com.bilboldev.pixeldungeonskills.items.food.Food;
import com.bilboldev.pixeldungeonskills.items.keys.GoldenKey;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfMana;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfRejuvenation;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfFirebolt;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfLightning;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Rifle;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Mace;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.Sword;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.FlameBow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.FrostBow;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.DailyScene;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.windows.WndOptions;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class DailyLevel extends Level {
    public int scenario, goal, turnLimit;
    public int bossEntrance = -1;
    public int rescueCell = -1, rescueDoor = -1, supplyCell = -1;
    private boolean bossRoomSealed, rescued;
    private int firstCaptain = -1, secondCaptain = -1;
    private int[] lockedDoors = new int[0], ironKeys = new int[0];
    private int[] chestCells = new int[0], goldenKeys = new int[0];
    private ArrayList<Room> rooms;

    private static class Room {
        int x, y, width, height, center;

        Room(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    @Override protected boolean build() {
        scenario = DailyRun.scenario(DailyRun.day());
        feeling = Feeling.NONE;
        Arrays.fill(map, Terrain.WALL);
        bossRoomSealed = rescued = false;
        int split = Random.IntRange(14, 16);
        int leftSplit = Random.IntRange(12, 18), rightSplit = Random.IntRange(12, 18);
        rooms = new ArrayList<>();
        rooms.add(new Room(1, 1, split, leftSplit));
        rooms.add(new Room(1, leftSplit + 1, split, 30 - leftSplit));
        rooms.add(new Room(split + 1, 1, 30 - split, rightSplit));
        rooms.add(new Room(split + 1, rightSplit + 1, 30 - split, 30 - rightSplit));
        Room bossRoom = rooms.get(Random.Int(rooms.size()));
        Room rescueRoom;
        int rescueIndex = Random.Int(rooms.size() - 1);
        if (rescueIndex >= rooms.indexOf(bossRoom)) rescueIndex++;
        rescueRoom = rooms.get(rescueIndex);
        int roomCount = Random.IntRange(8, 9);
        while (rooms.size() < roomCount) {
            Room largest = null;
            for (Room room : rooms) {
                if (room == bossRoom || room == rescueRoom || (room.width < 12 && room.height < 12)) continue;
                if (largest == null || room.width * room.height > largest.width * largest.height) largest = room;
            }
            if (largest == null) break;
            boolean vertical = largest.width >= 12 && (largest.height < 12 || Random.Int(2) == 0);
            int cut = Random.IntRange(6, (vertical ? largest.width : largest.height) - 6);
            if (vertical) {
                rooms.add(new Room(largest.x + cut, largest.y, largest.width - cut, largest.height));
                largest.width = cut;
            } else {
                rooms.add(new Room(largest.x, largest.y + cut, largest.width, largest.height - cut));
                largest.height = cut;
            }
        }
        boolean transpose = Random.Int(2) == 0;
        for (Room room : rooms) {
            if (transpose) {
                int x = room.x, width = room.width;
                room.x = room.y;
                room.width = room.height;
                room.y = x;
                room.height = width;
            }
            int width = room == bossRoom || room == rescueRoom ? 7 : Random.IntRange(3, Math.min(7, room.width - 2));
            int height = room == bossRoom || room == rescueRoom ? 7 : Random.IntRange(3, Math.min(7, room.height - 2));
            int x = room.x + Random.IntRange(1, room.width - width - 1);
            int y = room.y + Random.IntRange(1, room.height - height - 1);
            room.center = (y + height / 2) * WIDTH + x + width / 2;
            for (int dy = 0; dy < height; dy++) for (int dx = 0; dx < width; dx++)
                map[(y + dy) * WIDTH + x + dx] = Terrain.EMPTY;
        }
        goal = bossRoom.center;
        rescueCell = rescueRoom.center;
        rescueDoor = rescueCell + 2 * WIDTH;
        Room start = null;
        for (Room room : rooms) {
            if (room == bossRoom || room == rescueRoom) continue;
            if (start == null || distanceBetween(room.center, goal) > distanceBetween(start.center, goal)) start = room;
        }
        entrance = start.center;
        exit = -1;
        boolean[] joined = new boolean[rooms.size()];
        boolean[][] connected = new boolean[rooms.size()][rooms.size()];
        joined[rooms.indexOf(start)] = true;
        for (int count = 1; count < rooms.size() - 1; count++) {
            int from = -1, to = -1, shortest = Integer.MAX_VALUE;
            for (int a = 0; a < rooms.size(); a++) if (joined[a]) {
                for (int b = 0; b < rooms.size(); b++) if (!joined[b] && rooms.get(b) != bossRoom) {
                    int cost = distanceBetween(rooms.get(a).center, rooms.get(b).center) + Random.Int(12);
                    if (cost < shortest) {
                        from = a;
                        to = b;
                        shortest = cost;
                    }
                }
            }
            corridor(rooms.get(from).center, rooms.get(to).center);
            connected[from][to] = connected[to][from] = true;
            joined[to] = true;
        }
        for (int a = 0; a < rooms.size(); a++) for (int b = a + 1; b < rooms.size(); b++) {
            if (rooms.get(a) != bossRoom && rooms.get(b) != bossRoom && !connected[a][b]
                    && distanceBetween(rooms.get(a).center, rooms.get(b).center) < 18 && Random.Int(4) == 0)
                corridor(rooms.get(a).center, rooms.get(b).center);
        }
        Room neighbor = start;
        for (Room room : rooms) if (room != bossRoom
                && distanceBetween(room.center, goal) < distanceBetween(neighbor.center, goal)) neighbor = room;
        buildBossRoom(neighbor.center);
        buildCell();
        int[] distance = distances(entrance);
        for (Room room : rooms) {
            if (room == rescueRoom || (scenario == 1 && room == bossRoom)) continue;
            if (distance[room.center] < 0) throw new IllegalStateException("Daily room cannot be reached");
        }
        if (distance[rescueDoor + WIDTH] < 0 || distance[rescueCell] >= 0
                || (scenario == 1 && distance[goal] >= 0))
            throw new IllegalStateException("Invalid daily cell or hideout");
        firstCaptain = secondCaptain = -1;
        if (scenario == 1) {
            firstCaptain = farthestRoom(distance, -1);
            secondCaptain = farthestRoom(distances(firstCaptain), firstCaptain);
        }
        ArrayList<Integer> unused = new ArrayList<>();
        for (Room room : rooms) if (room.center != entrance && room.center != goal && room.center != rescueCell
                && room.center != firstCaptain && room.center != secondCaptain) unused.add(room.center);
        supplyCell = unused.remove(Random.Int(unused.size()));
        if (scenario == 1) {
            chestCells = new int[]{supplyCell, unused.remove(Random.Int(unused.size())), unused.remove(Random.Int(unused.size()))};
        } else {
            chestCells = new int[]{supplyCell};
        }
        if (scenario == 1) {
            placeLocks();
        } else {
            lockedDoors = ironKeys = goldenKeys = new int[0];
        }
        calculateTurnLimit();
        return true;
    }

    private int farthestRoom(int[] distance, int excluded) {
        int farthest = -1;
        for (Room room : rooms) {
            int cell = room.center;
            if (cell == entrance || cell == goal || cell == rescueCell || cell == excluded || distance[cell] < 0) continue;
            if (farthest < 0 || distance[cell] > distance[farthest]) farthest = cell;
        }
        return farthest;
    }

    private static int distanceBetween(int a, int b) {
        return Math.abs(a % WIDTH - b % WIDTH) + Math.abs(a / WIDTH - b / WIDTH);
    }

    private void corridor(int start, int end) {
        int[] previous = new int[LENGTH], queue = new int[LENGTH];
        Arrays.fill(previous, -1);
        previous[start] = start;
        queue[0] = start;
        int read = 0, write = 1, direction = Random.Int(4);
        while (read < write && previous[end] < 0) {
            int cell = queue[read++];
            for (int i = 0; i < 4; i++) {
                int next = cell + NEIGHBOURS4[(i + direction) % 4];
                if (next < WIDTH || next >= LENGTH - WIDTH || next % WIDTH == 0 || next % WIDTH == WIDTH - 1
                        || previous[next] >= 0 || Math.abs(next % WIDTH - cell % WIDTH) > 1
                        || (Math.abs(next % WIDTH - goal % WIDTH) <= 4 && Math.abs(next / WIDTH - goal / WIDTH) <= 4)) continue;
                previous[next] = cell;
                queue[write++] = next;
            }
        }
        if (previous[end] < 0) throw new IllegalStateException("Daily rooms cannot connect");
        for (int cell = end; cell != start; cell = previous[cell]) map[cell] = Terrain.EMPTY;
        map[start] = Terrain.EMPTY;
    }

    private void buildBossRoom(int neighbor) {
        for (int y = -4; y <= 4; y++) for (int x = -4; x <= 4; x++)
            map[goal + y * WIDTH + x] = Math.abs(x) == 4 || Math.abs(y) == 4 ? Terrain.WALL : Terrain.EMPTY;
        int dx = neighbor % WIDTH - goal % WIDTH, dy = neighbor / WIDTH - goal / WIDTH;
        int direction = Math.abs(dx) >= Math.abs(dy) ? Integer.signum(dx) : Integer.signum(dy) * WIDTH;
        bossEntrance = goal + 4 * direction;
        corridor(goal + 5 * direction, neighbor);
        map[bossEntrance] = scenario == 1 ? Terrain.LOCKED_DOOR : Terrain.DOOR;
    }

    private void buildCell() {
        for (int y = -3; y <= 3; y++) for (int x = -3; x <= 3; x++) {
            boolean wall = (Math.abs(x) == 2 && Math.abs(y) <= 2) || (Math.abs(y) == 2 && Math.abs(x) <= 2);
            map[rescueCell + y * WIDTH + x] = wall ? Terrain.WALL : Terrain.EMPTY;
        }
        map[rescueDoor] = Terrain.LOCKED_DOOR;
    }

    private void placeLocks() {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (!walkable(cell) || reserved(cell) || (Math.abs(cell % WIDTH - rescueCell % WIDTH) <= 3
                    && Math.abs(cell / WIDTH - rescueCell / WIDTH) <= 3)) continue;
            boolean vertical = walkable(cell - WIDTH) && walkable(cell + WIDTH) && !walkable(cell - 1) && !walkable(cell + 1);
            boolean horizontal = walkable(cell - 1) && walkable(cell + 1) && !walkable(cell - WIDTH) && !walkable(cell + WIDTH);
            if (vertical || horizontal) candidates.add(cell);
        }
        ArrayList<Integer> doors = new ArrayList<>();
        while (!candidates.isEmpty() && doors.size() < 3) {
            int cell = candidates.remove(Random.Int(candidates.size()));
            boolean close = false;
            for (int door : doors) if (distanceBetween(cell, door) < 3) close = true;
            if (!close) doors.add(cell);
        }
        lockedDoors = new int[doors.size()];
        for (int i = 0; i < doors.size(); i++) map[lockedDoors[i] = doors.get(i)] = Terrain.LOCKED_DOOR;
        ironKeys = new int[lockedDoors.length];
        ArrayList<Integer> occupied = new ArrayList<>();
        for (int chest : chestCells) occupied.add(chest);
        int[] reachable = distances(entrance);

        for (int i = 0; i < ironKeys.length; i++) {
            ironKeys[i] = looseItemCell(reachable, occupied);
            occupied.add(ironKeys[i]);
        }
        for (int door : lockedDoors) map[door] = Terrain.EMPTY;
        reachable = distances(entrance);
        goldenKeys = new int[chestCells.length];
        for (int i = 0; i < goldenKeys.length; i++) {
            goldenKeys[i] = looseItemCell(reachable, occupied);
            occupied.add(goldenKeys[i]);
        }
        for (int door : lockedDoors) map[door] = Terrain.LOCKED_DOOR;
    }

    private int looseItemCell(int[] reachable, ArrayList<Integer> occupied) {
        ArrayList<Integer> candidates = new ArrayList<>();
        for (int cell = 0; cell < LENGTH; cell++)
            if (reachable[cell] > 0 && walkable(cell) && !reserved(cell) && !occupied.contains(cell)) candidates.add(cell);
        if (candidates.isEmpty()) throw new IllegalStateException("No space for daily key");
        return candidates.get(Random.Int(candidates.size()));
    }

    private boolean reserved(int cell) {
        return cell == entrance || cell == bossEntrance || cell == rescueDoor || insideBossRoom(cell) || insideCell(cell);
    }

    private boolean walkable(int cell) {
        return cell >= 0 && cell < LENGTH && (Terrain.flags[map[cell]] & Terrain.PASSABLE) != 0;
    }

    public int[] distances(int start) {
        int[] distance = new int[LENGTH], queue = new int[LENGTH];
        Arrays.fill(distance, -1);
        distance[start] = 0;
        int read = 0, write = 1;
        queue[0] = start;
        while (read < write) {
            int cell = queue[read++];
            for (int offset : NEIGHBOURS4) {
                int next = cell + offset;
                if (!walkable(next) || distance[next] >= 0 || Math.abs(next % WIDTH - cell % WIDTH) > 1) continue;
                distance[next] = distance[cell] + 1;
                queue[write++] = next;
            }
        }
        return distance;
    }

    private void calculateTurnLimit() {
        int bossTile = map[bossEntrance];
        map[bossEntrance] = Terrain.EMPTY;
        for (int door : lockedDoors) map[door] = Terrain.EMPTY;
        ArrayList<Integer> stops = new ArrayList<>();
        if (scenario == 1) {
            stops.add(firstCaptain);
            stops.add(secondCaptain);
        }
        stops.add(rescueDoor + WIDTH);
        for (int chest : chestCells) stops.add(chest);
        for (int key : ironKeys) stops.add(key);
        for (int key : goldenKeys) stops.add(key);
        int route = 0, cell = entrance;
        while (!stops.isEmpty()) {
            int[] distance = distances(cell);
            int closest = 0;
            for (int i = 1; i < stops.size(); i++) if (distance[stops.get(i)] < distance[stops.get(closest)]) closest = i;
            int next = stops.remove(closest);
            if (distance[next] < 0) throw new IllegalStateException("Unreachable daily objective");
            route += distance[next];
            cell = next;
        }
        int finish = distances(cell)[goal];
        if (finish < 0) throw new IllegalStateException("Daily boss cannot be reached");
        route += finish;
        turnLimit = Math.max(600, route * 3 + 300);
        map[bossEntrance] = bossTile;
        for (int door : lockedDoors) map[door] = Terrain.LOCKED_DOOR;
    }

    @Override protected void decorate() {
        for (int cell = WIDTH + 1; cell < LENGTH - WIDTH - 1; cell++) {
            if (map[cell] == Terrain.WALL && map[cell + WIDTH] == Terrain.EMPTY && Random.Int(5) == 0)
                map[cell] = Terrain.WALL_DECO;
            else if (map[cell] == Terrain.EMPTY && Random.Int(18) == 0) map[cell] = Terrain.EMPTY_DECO;
        }
    }

    @Override protected void createMobs() {
        for (Room room : rooms) {
            if (room.center == entrance || room.center == goal) continue;
            int count = room.center == supplyCell ? 2 : Random.IntRange(1, 2);
            for (int i = 0; i < count; i++) {
                int kind = scenario == 0 ? Random.Int(2) : 2 + Random.Int(2);
                if (i == 0 && (room.center == firstCaptain || room.center == secondCaptain)) kind = 6;
                DailyFoe guard = new DailyFoe(kind);
                guard.pos = freeRoomCell(room);
                mobs.add(guard);
            }
        }
        for (int i = 0; i < (scenario == 1 ? 2 : 1); i++) {
            DailyAlly ally = new DailyAlly(scenario);
            ally.pos = rescueCell + i;
            mobs.add(ally);
        }
        Mob boss = scenario == 0 ? new DailyRatKing() : new DailyTengu();
        boss.pos = goal;
        mobs.add(boss);
        if (scenario == 0) for (int offset : new int[]{-1, 1}) {
            DailyFoe guard = new DailyFoe(7);
            guard.pos = goal + offset;
            mobs.add(guard);
        }
    }

    private int freeRoomCell(Room room) {
        ArrayList<Integer> cells = new ArrayList<>();
        for (int y = room.y; y < room.y + room.height; y++) for (int x = room.x; x < room.x + room.width; x++) {
            int cell = y * WIDTH + x;
            if (!walkable(cell) || reserved(cell)) continue;
            boolean occupied = false;
            for (Mob mob : mobs) if (mob.pos == cell) occupied = true;
            if (!occupied) cells.add(cell);
        }
        if (cells.isEmpty()) throw new IllegalStateException("No space for daily guard");
        return cells.get(Random.Int(cells.size()));
    }

    @Override protected void createItems() {
        drop(new Food().identify(), entrance);
        for (int cell : ironKeys) drop(new IronKey(), cell);
        for (int cell : goldenKeys) drop(new GoldenKey(), cell);
        for (int i = 0; i < chestCells.length; i++) {
            int cell = chestCells[i];
            drop((i == 0 ? new PotionOfRejuvenation() : Random.Int(2) == 0 ? new PotionOfHealing() : new PotionOfMana()).identify(), cell);
            if (i == 0) {
                drop(new ScrollOfUpgrade().identify(), cell);
                drop(new Food().identify(), cell);
            }
            if (i == chestCells.length - 1) drop(armoryItem(), cell);
            heaps.get(cell).type = scenario == 1 ? Heap.Type.LOCKED_CHEST : Heap.Type.CHEST;
        }
    }

    @Override
    public Heap drop(Item item, int cell) {
        if (item != null) item.identify();
        return super.drop(item, cell);
    }

    protected Item armoryItem() {
        Item item;
        switch (Dungeon.hero.heroClass) {
            case MAGE: case NECROMANCER:
                item = Random.Int(2) == 0 ? new WandOfFirebolt() : new WandOfLightning();
                break;
            case HUNTRESS:
                item = Random.Int(2) == 0 ? new FlameBow() : new FrostBow();
                break;
            case MERCENARY:
                item = new Rifle();
                break;
            default:
                item = Random.Int(2) == 0 ? new Sword() : new Mace();
                break;
        }
        int upgrades = Random.IntRange(2, 3);
        for (int i = 0; i < upgrades; i++) item.upgrade();
        EquipmentAttribute.roll(item);
        return item.identify();
    }

    public int captainsRemaining() {
        int count = 0;
        for (Mob mob : mobs) if (mob instanceof DailyFoe && ((DailyFoe)mob).escapeCaptain() && mob.isAlive()) count++;
        return count;
    }

    public boolean prisonSecured() { return scenario == 1 && captainsRemaining() == 0; }
    public boolean rescued() { return rescued; }

    public boolean insideBossRoom(int cell) {
        return cell >= 0 && cell < LENGTH && Math.abs(cell % WIDTH - goal % WIDTH) <= 3
                && Math.abs(cell / WIDTH - goal / WIDTH) <= 3;
    }

    private boolean insideCell(int cell) {
        return cell >= 0 && cell < LENGTH && Math.abs(cell % WIDTH - rescueCell % WIDTH) <= 1
                && Math.abs(cell / WIDTH - rescueCell / WIDTH) <= 1;
    }

    public void interactRescue(final Hero hero) {
        if (!canFree(hero)) return;
        GameScene.show(new WndOptions(Messages.get("daily.rescue_title_" + scenario),
                Messages.get("daily.rescue_prompt_" + scenario), Messages.get("daily.rescue_free_" + scenario),
                Messages.get("daily.rescue_leave")) {
            @Override protected void onSelect(int index) {
                if (index != 0 || !canFree(hero)) return;
                rescued = true;
                setDoor(rescueDoor, Terrain.OPEN_DOOR);
                for (Mob mob : mobs) if (mob instanceof DailyAlly) ((DailyAlly)mob).free(hero);
                hero.spendAndNext(1f);
            }
        });
    }

    private boolean canFree(Hero hero) {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level == this && hero == Dungeon.hero
                && hero.isAlive() && !rescued && map[rescueDoor] == Terrain.LOCKED_DOOR
                && Level.adjacent(hero.pos, rescueDoor);
    }

    public void updatePrisonGate() {
        if (scenario != 1 || bossRoomSealed || map[bossEntrance] != Terrain.LOCKED_DOOR || !prisonSecured()) return;
        setDoor(bossEntrance, Terrain.EMPTY);
    }

    public void sealBossRoom() {
        if (bossRoomSealed) return;
        int[] toDoor = distances(bossEntrance);
        for (Mob mob : mobs) {
            if (!(mob instanceof DailyAlly) || ((DailyAlly)mob).imprisoned() || !mob.isAlive()
                    || insideBossRoom(mob.pos) || toDoor[mob.pos] < 0 || toDoor[mob.pos] > 4) continue;
            int closest = -1;
            for (int cell = 0; cell < LENGTH; cell++) {
                if (!insideBossRoom(cell) || !walkable(cell) || Actor.findChar(cell) != null) continue;
                if (closest < 0 || distanceBetween(cell, bossEntrance) < distanceBetween(closest, bossEntrance)) closest = cell;
            }
            if (closest >= 0) {
                Actor.freeCell(mob.pos);
                mob.move(closest);
                Actor.occupyCell(mob);
                if (mob.sprite != null) {
                    mob.sprite.interruptMotion();
                    mob.sprite.place(closest);
                }
            }
        }
        bossRoomSealed = true;
        setDoor(bossEntrance, Terrain.LOCKED_DOOR);
    }

    protected void setDoor(int cell, int terrain) {
        map[cell] = terrain;
        if (Dungeon.level == this) {
            Level.set(cell, terrain);
            if (Game.scene() instanceof DailyScene) {
                GameScene.updateMap(cell);
                Dungeon.observe();
            }
        }
    }

    public String doorName(int cell) {
        if (map[cell] != Terrain.LOCKED_DOOR) return null;
        if (cell == rescueDoor) return Messages.get("daily.rescue_title_" + scenario);
        if (cell == bossEntrance && scenario == 1) return Messages.get("daily.boss_door");
        return null;
    }

    public String doorDescription(int cell) {
        if (map[cell] != Terrain.LOCKED_DOOR) return null;
        if (cell == rescueDoor) return Messages.get("daily.rescue_door_desc");
        if (cell == bossEntrance && scenario == 1)
            return Messages.get(bossRoomSealed ? "daily.boss_door_sealed" : "daily.boss_door_locked");
        return null;
    }

    @Override public Actor respawner() { return null; }
    @Override public Actor mercRespawner() { return null; }
    @Override public int randomRespawnCell() {
        int chosen = -1, count = 0;
        boolean hideoutLocked = scenario == 1 && !prisonSecured();
        for (int cell = 0; cell < LENGTH; cell++) {
            if (!rescued && insideCell(cell)) continue;
            if (scenario == 1 && (bossRoomSealed ? !insideBossRoom(cell) : hideoutLocked && insideBossRoom(cell))) continue;
            if (walkable(cell) && Actor.findChar(cell) == null && Random.Int(++count) == 0) chosen = cell;
        }
        return chosen;
    }

    @Override public String tilesTex() {
        if (scenario == 0) return Difficulties.is3d ? Assets.TILES_SEWERS_3D : Assets.TILES_SEWERS;
        return Difficulties.is3d ? Assets.TILES_PRISON_3D : Assets.TILES_PRISON;
    }
    @Override public String waterTex() { return scenario == 0 ? Assets.WATER_SEWERS : Assets.WATER_PRISON; }

    @Override public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("daily_scenario", scenario);
        bundle.put("daily_goal", goal);
        bundle.put("daily_limit", turnLimit);
        bundle.put("daily_boss_entrance", bossEntrance);
        bundle.put("daily_boss_sealed", bossRoomSealed);
        bundle.put("daily_rescue_cell", rescueCell);
        bundle.put("daily_rescue_door", rescueDoor);
        bundle.put("daily_rescued", rescued);
        bundle.put("daily_supply", supplyCell);
        bundle.put("daily_locked_doors", lockedDoors);
        bundle.put("daily_chests", chestCells);
    }

    @Override public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        scenario = bundle.getInt("daily_scenario");
        goal = bundle.getInt("daily_goal");
        turnLimit = bundle.getInt("daily_limit");
        bossEntrance = bundle.getInt("daily_boss_entrance");
        bossRoomSealed = bundle.getBoolean("daily_boss_sealed");
        rescueCell = bundle.getInt("daily_rescue_cell");
        rescueDoor = bundle.getInt("daily_rescue_door");
        rescued = bundle.getBoolean("daily_rescued");
        supplyCell = bundle.getInt("daily_supply");
        lockedDoors = bundle.getIntArray("daily_locked_doors");
        chestCells = bundle.getIntArray("daily_chests");
        if (scenario < 0 || scenario > 1 || goal < 0 || goal >= LENGTH || turnLimit < 1
                || bossEntrance < 0 || bossEntrance >= LENGTH || rescueCell < 0 || rescueCell >= LENGTH
                || rescueDoor < 0 || rescueDoor >= LENGTH || supplyCell < 0 || supplyCell >= LENGTH)
            throw new IllegalArgumentException("Invalid daily floor");
    }
}
