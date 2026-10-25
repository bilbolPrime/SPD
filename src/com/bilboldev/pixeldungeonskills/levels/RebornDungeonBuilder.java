package com.bilboldev.pixeldungeonskills.levels;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryRecruitment;
import com.bilboldev.pixeldungeonskills.levels.painters.RebornRoomPainter;
import com.bilboldev.utils.Random;
import com.bilboldev.utils.Rect;
import java.util.*;

public final class RebornDungeonBuilder {
    private RebornDungeonBuilder() {}
    private static final int MIN_PLOT = 7;

    public static boolean supports(Level level) {
        Class<?> type = level.getClass();
        return type == SewerLevel.class || type == PrisonLevel.class || type == CavesLevel.class
                || type == CityLevel.class || type == HallsLevel.class;
    }

    static boolean build(RegularLevel level) {
        Plan plan = null;
        for (int attempt = 0; attempt < 96; attempt++) {
            Plan candidate = new Plan(level);
            if (candidate.prepare()) {
                plan = candidate;
                break;
            }
        }

        if (plan == null) return RebornLayout.build(level);
        level.themedLayout = true;
        level.rooms = new LinkedHashSet<>();

        if (plan.workshop != null) level.rooms.add(plan.workshop);
        level.rooms.addAll(plan.rooms);
        level.roomEntrance = plan.entrance;
        level.roomExit = plan.exit;
        level.assignRoomType();

        Arrays.fill(level.map, level.feeling == Level.Feeling.CHASM ? Terrain.CHASM : Terrain.WALL);
        int style = Random.Int(6);
        for (Room room : level.rooms) {
            if (room.type == Room.Type.STANDARD) RebornRoomPainter.paint(level, room, style++ % 6);
            else room.type.paint(level, room);
        }
        for (Link link : plan.links) {
            for (int cell : link.path) level.map[cell] = level.tunnelTile();
        }
        HashSet<Integer> paintedDoors = new HashSet<>();
        for (Room room : level.rooms) for (Room.Door door : room.connected.values())
            if (paintedDoors.add(door.x + door.y * Level.WIDTH)) paintDoor(level, door);

        if (level.feeling == Level.Feeling.WATER) level.paintWater();
        if (level.feeling == Level.Feeling.GRASS) level.paintGrass();
        return true;
    }

    private static void paintDoor(Level level, Room.Door door) {
        int tile;
        switch (door.type) {
            case HIDDEN: tile = Terrain.SECRET_DOOR;
            ((RegularLevel)level).secretDoors++;
            break;
            case LOCKED: tile = Terrain.LOCKED_DOOR;
            break;
            case BARRICADE: tile = Random.Int(3) == 0 ? Terrain.BOOKSHELF : Terrain.BARRICADE;
            break;
            case EMPTY: case TUNNEL: tile = level.tunnelTile();
            break;
            default: tile = Terrain.DOOR;
        }
        level.map[door.x + door.y * Level.WIDTH] = tile;
    }

    static void finish(RegularLevel level) {
        for (Room room : level.rooms) if (room.type == Room.Type.STANDARD)
            RebornRoomPainter.reconnect(level, room);
        int[] distance = distances(level, level.entrance);
        if (level instanceof CavesLevel) ensureMiningOre(level, distance);
        boolean[] routes = new boolean[Level.LENGTH];
        protectRoute(level, distance, level.exit, routes);
        for (Room room : level.rooms) {
            for (int door : room.rebornDoors) {
                if (distance[door] >= 0) protectRoute(level, distance, door, routes);

                for (int n : Level.NEIGHBOURS4) if (validStep(door, door+n) && distance[door+n] >= 0)
                    protectRoute(level, distance, door+n, routes);
            }
        }
        ArrayList<Integer> choices = new ArrayList<>();
        for (Room room : level.rooms) if (room.type == Room.Type.STANDARD) {
            for (int y = room.top+1; y < room.bottom; y++) for (int x = room.left+1; x < room.right; x++) {
                int cell = x + y * Level.WIDTH;
                if (level.map[cell] != Terrain.EMPTY || distance[cell] < 10 || routes[cell]) continue;
                boolean nearDoor = false;
                for (int door : room.rebornDoors) if (Level.distance(cell, door) < 3) nearDoor = true;
                if (!nearDoor) choices.add(cell);
            }
        }
        int traps = Dungeon.depth == 1 ? 0 : 2 + (Dungeon.depth-1)/5;
        int region = (Dungeon.depth-1)/5;
        int[][] types = {
                {
                    Terrain.SECRET_POISON_TRAP, Terrain.SECRET_GRIPPING_TRAP
                },
                {
                    Terrain.SECRET_ALARM_TRAP, Terrain.SECRET_POISON_TRAP
                },
                {
                    Terrain.SECRET_GRIPPING_TRAP, Terrain.SECRET_LIGHTNING_TRAP
                },
                {
                    Terrain.SECRET_LIGHTNING_TRAP, Terrain.SECRET_PARALYTIC_TRAP
                },
                {
                    Terrain.SECRET_FIRE_TRAP, Terrain.SECRET_TOXIC_TRAP
                }
        };
        while (traps-- > 0 && !choices.isEmpty()) {
            int cell = choices.remove(Random.Int(choices.size()));
            level.map[cell] = types[Math.min(4, region)][Random.Int(2)];

            for (Iterator<Integer> remaining = choices.iterator(); remaining.hasNext();) {
                if (Level.distance(cell, remaining.next()) < 3) remaining.remove();
            }
        }
    }

    private static void ensureMiningOre(RegularLevel level, int[] distance) {
        int available = 0;
        ArrayList<Integer> choices = new ArrayList<>();
        for (int cell = 0; cell < Level.LENGTH; cell++) {
            if (level.map[cell] != Terrain.WALL && level.map[cell] != Terrain.WALL_DECO) continue;
            boolean accessible = false;
            for (int offset : Level.NEIGHBOURS8) {
                int next = cell + offset;
                if (next >= 0 && next < Level.LENGTH && Level.adjacent(cell, next) && distance[next] >= 0) {
                    accessible = true;
                    break;
                }
            }
            if (!accessible) continue;
            if (level.map[cell] == Terrain.WALL_DECO) {
                available++;
                continue;
            }
            int x = cell % Level.WIDTH, y = cell / Level.WIDTH;
            for (Room room : level.rooms) if (room.type == Room.Type.STANDARD
                    && x >= room.left && x <= room.right && y >= room.top && y <= room.bottom) {
                choices.add(cell);
                break;
            }
        }

        while (available < 18 && !choices.isEmpty()) {
            level.map[choices.remove(Random.Int(choices.size()))] = Terrain.WALL_DECO;
            available++;
        }
    }

    public static boolean walkable(Level level, int cell) {
        int flags = Terrain.flags[level.map[cell]];
        return (flags & Terrain.PASSABLE) != 0 && (flags & (Terrain.PIT | Terrain.AVOID)) == 0;
    }

    public static boolean validStep(int from, int to) {
        return to >= 0 && to < Level.LENGTH
                && Math.abs(from % Level.WIDTH - to % Level.WIDTH) + Math.abs(from / Level.WIDTH - to / Level.WIDTH) == 1;
    }

    public static int[] distances(Level level, int start) {
        int[] result = new int[Level.LENGTH], queue = new int[Level.LENGTH];
        Arrays.fill(result, -1);
        result[start] = 0;
        int tail = 1;
        queue[0] = start;
        for (int head = 0; head < tail; head++) for (int n : Level.NEIGHBOURS4) {
            int next = queue[head] + n;
            if (validStep(queue[head], next) && result[next] < 0 && walkable(level, next)) {
                result[next] = result[queue[head]] + 1;
                queue[tail++] = next;
            }
        }
        return result;
    }

    private static void protectRoute(Level level, int[] distance, int cell, boolean[] routes) {
        while (distance[cell] > 0 && !routes[cell]) {
            routes[cell] = true;
            for (int n : Level.NEIGHBOURS4) {
                int next = cell+n;
                if (validStep(cell, next) && distance[next] == distance[cell]-1) {
                    cell = next;
                    break;
                }
            }
        }
    }

    private static int center(Room room) {
        return (room.left+room.right)/2 + ((room.top+room.bottom)/2)*Level.WIDTH;
    }

    private static int manhattan(int a, int b) {
        return Math.abs(a%Level.WIDTH-b%Level.WIDTH) + Math.abs(a/Level.WIDTH-b/Level.WIDTH);
    }

    private static final class Link {
        final Room a, b;
        int[] path;
        Link(Room a, Room b) {
            this.a = a;
            this.b = b;
            a.connect(b);
        }
    }

    private static final class Plan {
        final RegularLevel level;
        final RebornRoomDimensions dimensions;
        final ArrayList<Room> rooms = new ArrayList<>();
        final IdentityHashMap<Room, Rect> plots = new IdentityHashMap<>();
        final LinkedHashSet<Room> core = new LinkedHashSet<>();
        final ArrayList<Room> branches = new ArrayList<>();
        final ArrayList<Link> links = new ArrayList<>();
        Room entrance, exit, workshop;
        int reserveAttempts;

        Plan(RegularLevel level) {
            this.level = level;
            dimensions = RebornRoomDimensions.forLevel(level);
        }

        boolean prepare() {
            if (!partition()) return false;
            for (Room a : rooms) for (Room b : rooms) if (a != b) a.addNeigbour(b);
            entrance = rooms.get(Random.Int(rooms.size()));
            exit = entrance;
            for (Room room : rooms) if (manhattan(center(entrance), center(room)) > manhattan(center(entrance), center(exit))) exit = room;
            entrance.type = Room.Type.ENTRANCE;
            exit.type = Room.Type.EXIT;
            core.addAll(rooms);
            ArrayList<Room.Type> roles = new ArrayList<>();
            if (Dungeon.shopOnLevel()) roles.add(Room.Type.SHOP);
            if (level instanceof CavesLevel) roles.add(Room.Type.STANDARD);
            if (!LocalCoop.active() && MercenaryRecruitment.campFloor(Dungeon.depth)) roles.add(Room.Type.MERCENARY);
            for (int i = 0; i < (Level.pitRoomNeeded ? 3 : 2); i++) roles.add(Room.Type.NULL);
            if (!reserve(roles, 0)) return false;
            for (Room room : branches) if (room.type == Room.Type.STANDARD) workshop = room;
            LinkedHashSet<Room> reached = new LinkedHashSet<>();
            reached.add(entrance);
            while (reached.size() < core.size()) {
                ArrayList<Room[]> candidates = new ArrayList<>();
                for (Room room : reached) for (Room next : rooms)
                    if (core.contains(next) && !reached.contains(next) && room.neigbours.contains(next)) candidates.add(new Room[]{room, next});
                if (candidates.isEmpty()) return false;
                Room[] edge = Random.element(candidates);
                links.add(new Link(edge[0], edge[1]));
                reached.add(edge[1]);
            }

            int loops = Random.IntRange(1, 2);
            while (loops-- > 0) {
                Room bestA = null, bestB = null;
                int best = 1;
                for (Room a : core) for (Room b : core) {
                    if (!a.neigbours.contains(b) || a.connected.containsKey(b)) continue;
                    int score = graphDistance(a, b);
                    if (score > best) {
                        best = score;
                        bestA = a;
                        bestB = b;
                    }
                }
                if (bestA == null) break;
                links.add(new Link(bestA, bestB));
            }
            for (Room branch : branches) {
                ArrayList<Room> neighbours = new ArrayList<>();
                for (Room room : core) if (branch.neigbours.contains(room)) neighbours.add(room);
                Room parent = branch.type == Room.Type.SHOP ? entrance : Random.element(neighbours);
                links.add(new Link(branch, parent));
            }
            for (Room room : rooms) {
                plots.put(room, new Rect(room));
                fit(room);
            }
            for (Link link : links) if (!route(link)) return false;
            for (Room room : rooms) {
                room.rebornDoors = new int[room.connected.size()];
                int i = 0;
                for (Room.Door door : room.connected.values()) room.rebornDoors[i++] = door.x+door.y*Level.WIDTH;
            }

            return routeCost(entrance, exit) <= 78 && routeCost(entrance, exit) >= 18;
        }

        boolean partition() {
            int target = Random.IntRange(dimensions.minRooms, dimensions.maxRooms);
            Room initial = new Room();
            initial.set(0, 0, Level.WIDTH-1, Level.HEIGHT-1);
            rooms.add(initial);
            while (rooms.size() < target) {
                ArrayList<int[]> splits = new ArrayList<>();
                int capacity = 0;
                for (Room room : rooms) capacity += (room.width()/MIN_PLOT)*(room.height()/MIN_PLOT);
                for (int i = 0; i < rooms.size(); i++) {
                    Room room = rooms.get(i);
                    int old = (room.width()/MIN_PLOT)*(room.height()/MIN_PLOT);
                    for (int axis = 0; axis < 2; axis++) {
                        int length = axis == 0 ? room.width() : room.height();
                        int other = axis == 0 ? room.height() : room.width();
                        for (int at = MIN_PLOT; at <= length-MIN_PLOT; at++) {
                            int replacement = (at/MIN_PLOT + (length-at)/MIN_PLOT)*(other/MIN_PLOT);
                            if (capacity-old+replacement >= target) splits.add(new int[]{i, axis, at});
                        }
                    }
                }
                if (splits.isEmpty()) return false;
                float[] weights = new float[splits.size()];
                float total = 0;
                for (int i = 0; i < splits.size(); i++) {
                    int[] option = splits.get(i);
                    Room plot = rooms.get(option[0]);
                    weights[i] = dimensions.cutWeight(option[1] == 0 ? plot.width() : plot.height(),
                            option[1] == 0 ? plot.height() : plot.width(), option[2]);
                    total += weights[i];
                }
                float choice = Random.Float(total);
                int selected = 0;
                while (selected < weights.length-1 && choice >= weights[selected]) choice -= weights[selected++];
                int[] split = splits.get(selected);
                Room a = rooms.remove(split[0]), b = new Room();
                b.set(a);
                if (split[1] == 0) {
                    a.right = a.left+split[2];
                    b.left = a.right;
                }
                else {
                    a.bottom = a.top+split[2];
                    b.top = a.bottom;
                }
                rooms.add(a);
                rooms.add(b);
            }
            for (Room room : rooms) room.type = Room.Type.STANDARD;
            return true;
        }

        boolean reserve(ArrayList<Room.Type> roles, int index) {
            if (++reserveAttempts > 3000) return false;
            if (index == roles.size()) return true;
            Room.Type role = roles.get(index);
            ArrayList<Room> choices = new ArrayList<>();
            for (Room room : core) {
                if (room == entrance || room == exit || (role == Room.Type.SHOP && !room.neigbours.contains(entrance))) continue;
                LinkedHashSet<Room> remaining = new LinkedHashSet<>(core);
                remaining.remove(room);
                if (!connectedLoop(remaining)) continue;
                boolean accessible = true;
                for (Room branch : branches) {
                    boolean adjacent = false;
                    for (Room other : remaining) if (branch.neigbours.contains(other)) adjacent = true;
                    if (!adjacent) accessible = false;
                }
                if (accessible) choices.add(room);
            }
            while (!choices.isEmpty()) {
                Room room = choices.remove(Random.Int(choices.size()));
                core.remove(room);
                branches.add(room);
                room.type = role;
                if (reserve(roles, index+1)) return true;
                core.add(room);
                branches.remove(room);
                room.type = Room.Type.STANDARD;
            }
            return false;
        }

        boolean connectedLoop(Set<Room> set) {
            HashSet<Room> seen = new HashSet<>();
            ArrayList<Room> queue = new ArrayList<>();
            queue.add(set.iterator().next());
            seen.add(queue.get(0));
            int edges = 0;
            for (int i = 0; i < queue.size(); i++) for (Room next : queue.get(i).neigbours) if (set.contains(next)) {
                edges++;
                if (seen.add(next)) queue.add(next);
            }
            return seen.size() == set.size() && edges/2 >= set.size();
        }

        int graphDistance(Room from, Room to) {
            HashMap<Room, Integer> distance = new HashMap<>();
            ArrayList<Room> queue = new ArrayList<>();
            queue.add(from);
            distance.put(from, 0);
            for (int i = 0; i < queue.size(); i++) for (Room next : queue.get(i).connected.keySet()) if (!distance.containsKey(next)) {
                int d = distance.get(queue.get(i))+1;
                if (next == to) return d;
                distance.put(next, d);
                queue.add(next);
            }
            return 0;
        }

        int routeCost(Room from, Room to) {
            HashMap<Room, Integer> distance = new HashMap<>();
            for (Room room : rooms) distance.put(room, 10000);
            distance.put(from, 0);
            for (int i = 0; i<rooms.size(); i++) for (Link link : links) {
                int cost = manhattan(center(link.a), center(link.b)) + 2;
                distance.put(link.b, Math.min(distance.get(link.b), distance.get(link.a)+cost));
                distance.put(link.a, Math.min(distance.get(link.a), distance.get(link.b)+cost));
            }
            return distance.get(to);
        }

        void fit(Room room) {
            if (room.type == Room.Type.STANDARD && room != workshop) {
                dimensions.fit(room);
                return;
            }
            int minimum = room.type == Room.Type.SHOP ? 7 : room.type != Room.Type.STANDARD || room == workshop ? 6 : 5;

            if (room.width() >= minimum+2) {
                room.left++;
                room.right--;
            }
            if (room.height() >= minimum+2) {
                room.top++;
                room.bottom--;
            }
            if (room.width() >= minimum+2 && Random.Int(3)==0) room.left += Random.Int(2);
            if (room.height() >= minimum+2 && Random.Int(3)==0) room.bottom -= Random.Int(2);
        }

        boolean route(Link link) {
            Room a = link.a, b = link.b;
            Rect pa = plots.get(a), pb = plots.get(b);
            Rect seam = pa.intersect(pb);
            Room.Door da, db;
            if (seam.width() == 0) {
                int y = Random.IntRange(seam.top+1, seam.bottom-1);
                da = new Room.Door(pa.left < pb.left ? a.right : a.left, clamp(y, a.top+2, a.bottom-2));
                db = new Room.Door(pa.left < pb.left ? b.left : b.right, clamp(y, b.top+2, b.bottom-2));
            } else {
                int x = Random.IntRange(seam.left+1, seam.right-1);
                da = new Room.Door(clamp(x, a.left+2, a.right-2), pa.top < pb.top ? a.bottom : a.top);
                db = new Room.Door(clamp(x, b.left+2, b.right-2), pa.top < pb.top ? b.top : b.bottom);
            }
            int start = da.x+da.y*Level.WIDTH, goal = db.x+db.y*Level.WIDTH;

            for (Room room : rooms) {
                if (contains(room, da.x, da.y) && room != a && !(start == goal && room == b)) return false;
                if (contains(room, db.x, db.y) && room != b && !(start == goal && room == a)) return false;
            }

            if (start == goal) db = da;
            a.connected.put(b, da);
            b.connected.put(a, db);
            int[] previous = new int[Level.LENGTH], queue = new int[Level.LENGTH];
            Arrays.fill(previous, -1);
            previous[start] = start;
            queue[0] = start;
            int tail = 1;
            for (int head = 0; head<tail && previous[goal]<0; head++) for (int n : Level.NEIGHBOURS4) {
                int next = queue[head]+n;
                if (!validStep(queue[head], next) || previous[next]>=0) continue;
                int x = next%Level.WIDTH, y = next/Level.WIDTH;
                if (x==0 || y==0 || x==Level.WIDTH-1 || y==Level.HEIGHT-1) continue;
                if (!contains(pa, x, y) && !contains(pb, x, y)) continue;
                boolean blocked = false;
                if (next != goal) for (Room room : rooms) if (contains(room, x, y)) {
                    blocked = true;
                    break;
                }
                if (blocked) continue;
                previous[next] = queue[head];
                queue[tail++] = next;
            }
            if (previous[goal]<0) return false;
            ArrayList<Integer> path = new ArrayList<>();
            int cell = goal;
            while (cell!=start) {
                path.add(cell);
                cell = previous[cell];
            }
            path.add(start);
            link.path = new int[path.size()];
            for (int i = 0;i<path.size();i++) link.path[i] = path.get(i);
            return true;
        }

        int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
        boolean contains(Rect r, int x, int y) {
            return x>=r.left && x<=r.right && y>=r.top && y<=r.bottom;
        }
    }
}
