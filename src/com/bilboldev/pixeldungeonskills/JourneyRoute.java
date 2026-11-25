package com.bilboldev.pixeldungeonskills;

import com.bilboldev.utils.Bundle;
import java.io.IOException;
import java.util.ArrayList;

public final class JourneyRoute {
    public enum Biome { SEWERS, PRISON, CAVES, CITY, HALLS }
    public enum Objective { NONE, CHECKPOINT, SUPPLIES, RESCUE, OUTPOST }

    public static final class Node {
        public final int id, stage, layer, column, depth, floors;
        public final Biome biome;
        public final Objective objective;
        public final int[] next;
        public final boolean gate, finish, restock;
        public boolean completed;

        private Node(int id, Layer layer, int row, int column, int[] next, Objective objective) {
            this.id = id;
            stage = layer.stage;
            this.layer = row;
            this.column = column;
            floors = id == 0 ? 0 : objective == Objective.SUPPLIES ? 3 : 2;
            biome = layer.biome;
            gate = layer.gate;
            finish = layer.finish;
            depth = gate || finish || id == 0 ? layer.depth : Math.min(layer.depth, stage * 5 - floors);
            this.objective = objective;
            restock = objective == Objective.SUPPLIES;
            this.next = next;
        }

        public int depth(int floor) {
            if (floor < 0 || floor >= floors) throw new IllegalArgumentException("Invalid Journey floor");
            return depth + floor;
        }
    }

    private static final class Layer {
        final int stage, depth;
        final int[] columns;
        final Biome biome;
        final boolean gate, finish;

        Layer(int stage, int depth, int[] columns, Biome biome, boolean gate, boolean finish) {
            this.stage = stage;
            this.depth = depth;
            this.columns = columns;
            this.biome = biome;
            this.gate = gate;
            this.finish = finish;
        }
    }

    public final long seed;
    public final ArrayList<Node> nodes = new ArrayList<>();
    public JourneyRoute(long seed) {
        this.seed = seed;
        java.util.Random random = new java.util.Random(seed);
        ArrayList<Layer> layers = new ArrayList<>();
        layers.add(new Layer(0, 1, new int[]{3}, Biome.SEWERS, false, false));
        for (int stage = 0; stage < 5; stage++) {
            int previousWidth = 0;
            for (int branch = 0; branch < 3; branch++) {
                int three = previousWidth == 3 ? 0 : 50;
                int two = previousWidth == 2 ? 0 : 33;
                int one = previousWidth == 1 ? 0 : 17;
                int roll = random.nextInt(three + two + one);
                int width = roll < three ? 3 : roll < three + two ? 2 : 1;
                int[] columns;
                if (width == 1) columns = new int[]{3};
                else if (width == 3) columns = new int[]{0, 3, 6};
                else {
                    int absent = random.nextInt(3), index = 0;
                    columns = new int[2];
                    for (int lane = 0; lane < 3; lane++) if (lane != absent) columns[index++] = lane * 3;
                }
                previousWidth = width;
                layers.add(new Layer(stage + 1, stage * 5 + 1 + branch, columns,
                        Biome.values()[stage], false, false));
            }
            layers.add(new Layer(stage + 1, stage * 5 + 4, new int[]{3}, Biome.values()[stage], stage < 4, stage == 4));
        }
        java.util.Random objectives = new java.util.Random(seed ^ 0x719B3E5C28D4A6F0L);
        Objective[] special = {Objective.CHECKPOINT, Objective.SUPPLIES, Objective.RESCUE, Objective.OUTPOST};
        ArrayList<Objective> assigned = new ArrayList<>();
        ArrayList<Integer> free = new ArrayList<>();
        for (Layer layer : layers) {
            boolean ordinary = layer.stage > 0 && !layer.gate && !layer.finish;
            for (int column = 0; column < layer.columns.length; column++) {
                Objective objective = ordinary && objectives.nextInt(4) == 0
                        ? special[objectives.nextInt(special.length)] : Objective.NONE;
                if (ordinary && objective == Objective.NONE) free.add(assigned.size());
                assigned.add(objective);
            }
        }
        ensureObjectives(assigned, free, objectives);
        java.util.Random paths = new java.util.Random(seed ^ 0x4D82C670A9E135BFL);
        for (int row = 0; row < layers.size(); row++) {
            Layer layer = layers.get(row);
            Layer following = row + 1 < layers.size() ? layers.get(row + 1) : null;
            int nextStart = nodes.size() + layer.columns.length;
            int[][] next = following == null ? new int[layer.columns.length][0]
                    : connect(layer.columns, following.columns, nextStart, paths);
            for (int column = 0; column < layer.columns.length; column++) {
                nodes.add(new Node(nodes.size(), layer, row, layer.columns[column], next[column], assigned.get(nodes.size())));
            }
        }
        nodes.get(0).completed = true;
    }

    private static int[][] connect(int[] from, int[] to, int nextStart, java.util.Random random) {
        boolean[][] connected = new boolean[from.length][to.length];
        for (int i = 0; i < from.length; i++) {
            boolean outgoing = false;
            for (int j = 0; j < to.length; j++) {
                connected[i][j] = random.nextInt(100) < connectionWeight(from[i], to[j]);
                outgoing |= connected[i][j];
            }
            if (!outgoing) connected[i][chooseConnection(from[i], to, random)] = true;
        }

        for (int j = 0; j < to.length; j++) {
            boolean incoming = false;
            for (int i = 0; i < from.length; i++) incoming |= connected[i][j];
            if (!incoming) connected[chooseConnection(to[j], from, random)][j] = true;
        }
        int[][] next = new int[from.length][];
        for (int i = 0; i < from.length; i++) {
            int count = 0;
            for (boolean link : connected[i]) if (link) count++;
            next[i] = new int[count];
            int index = 0;
            for (int j = 0; j < to.length; j++) if (connected[i][j]) next[i][index++] = nextStart + j;
        }
        return next;
    }

    private static int chooseConnection(int column, int[] choices, java.util.Random random) {
        int total = 0;
        for (int choice : choices) total += connectionWeight(column, choice);
        int roll = random.nextInt(total), index = 0;
        while (roll >= connectionWeight(column, choices[index])) {
            roll -= connectionWeight(column, choices[index++]);
        }
        return index;
    }

    private static int connectionWeight(int from, int to) {
        int distance = Math.abs(from - to);
        return distance == 0 ? 50 : distance == 3 ? 35 : 15;
    }

    private static void ensureObjectives(ArrayList<Objective> assigned, ArrayList<Integer> free, java.util.Random random) {
        int[] counts = new int[Objective.values().length];
        for (Objective objective : assigned) counts[objective.ordinal()]++;
        for (Objective objective : Objective.values()) if (objective != Objective.NONE && counts[objective.ordinal()] == 0) {
            int node;
            if (!free.isEmpty()) node = free.remove(random.nextInt(free.size()));
            else {
                ArrayList<Integer> duplicates = new ArrayList<>();
                for (int i = 0; i < assigned.size(); i++) {
                    Objective existing = assigned.get(i);
                    if (existing != Objective.NONE && counts[existing.ordinal()] > 1) duplicates.add(i);
                }
                node = duplicates.get(random.nextInt(duplicates.size()));
                counts[assigned.get(node).ordinal()]--;
            }
            assigned.set(node, objective);
            counts[objective.ordinal()]++;
        }
    }

    public Node node(int id) {
        if (id < 0 || id >= nodes.size()) throw new IllegalArgumentException("Invalid Journey node");
        return nodes.get(id);
    }

    void store(Bundle bundle) {
        bundle.put("route_seed", Long.toString(seed));
        ArrayList<Integer> completed = new ArrayList<>();
        for (Node node : nodes) if (node.completed) completed.add(node.id);
        int[] ids = new int[completed.size()];
        for (int i = 0; i < ids.length; i++) ids[i] = completed.get(i);
        bundle.put("completed", ids);
    }

    static JourneyRoute restore(Bundle bundle) throws IOException {
        JourneyRoute route;
        try {
            route = new JourneyRoute(Long.parseLong(bundle.getString("route_seed")));
        } catch (NumberFormatException invalid) {
            throw new IOException("Invalid Journey route seed", invalid);
        }
        int[] completed = bundle.getIntArray("completed");
        if (completed == null) throw new IOException("Missing Journey route progress");
        for (int id : completed) {
            if (id < 0 || id >= route.nodes.size()) throw new IOException("Invalid Journey route progress");
            route.node(id).completed = true;
        }
        return route;
    }
}
