package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.utils.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import static com.bilboldev.pixeldungeonskills.network.PartyMessage.*;

final class PartyUpdates {
    private static final String[] GRIDS = {"map", "visited", "mapped"};
    private JSONObject previous;
    private int revision;

    int revision() {
        return revision;
    }

    String encode(Bundle snapshot) throws IOException {
        return encode(snapshot, false);
    }

    String encode(Bundle snapshot, boolean full) throws IOException {
        try {
            if (revision == Integer.MAX_VALUE) throw invalid();
            JSONObject current = PartyConnection.parse(snapshot.toString(), PartySnapshot.MAX_BYTES);
            PartyNames.encode(current);
            PartySymbols.encode(current);
            JSONObject world = new JSONObject();
            Iterator<String> keys = current.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!key.startsWith("floor_")) world.put(key, current.get(key));
            }
            JSONArray floors = new JSONArray();
            for (int depth : floorIds(current)) {
                String key = "floor_" + depth;
                JSONObject floor = object(current, key);
                JSONObject before = full || previous == null ? null : previous.optJSONObject(key);
                if (before != null && same(before, floor)) continue;
                JSONObject update = new JSONObject().put("depth", depth);
                if (before == null) update.put("full", floor);
                else {
                    JSONObject oldLevel = object(before, "level");
                    JSONObject level = object(floor, "level");
                    if (!same(oldLevel.get("__className"), level.get("__className"))) throw invalid();
                    update.put("fields", changes(before, floor, "level"));
                    update.put("level", changes(oldLevel, level, "__className", "map", "visited", "mapped"));
                    JSONObject tiles = new JSONObject();
                    for (String grid : GRIDS) {
                        JSONArray oldCells = oldLevel.getJSONArray(grid);
                        JSONArray cells = level.getJSONArray(grid);
                        if (oldCells.length() != Level.LENGTH || cells.length() != Level.LENGTH) throw invalid();
                        JSONArray edits = new JSONArray();
                        for (int cell = 0; cell < Level.LENGTH; cell++) {
                            if (!same(oldCells.get(cell), cells.get(cell)))
                                edits.put(new JSONArray().put(cell).put(cells.get(cell)));
                        }
                        if (edits.length() != 0) tiles.put(grid, edits);
                    }
                    update.put("tiles", tiles);
                }
                floors.put(update);
            }
            JSONObject packet = new JSONObject().put("base", full ? 0 : revision).put("revision", revision + 1)
                    .put("world", world).put("floors", floors);
            String encoded = PartySnapshot.compress(packet.toString());
            previous = current;
            revision++;
            return encoded;
        } catch (JSONException | IllegalArgumentException failure) {
            throw new IOException("Could not encode party changes", failure);
        }
    }

    Bundle decode(String encoded, int expectedRevision) throws IOException {
        try {
            if (expectedRevision <= revision) return null;
            JSONObject packet = PartySnapshot.unpack(encoded);
            fields(packet, "base", "revision", "world", "floors");
            int base = integer(packet, "base", 0, Integer.MAX_VALUE - 1);
            integer(packet, "revision", expectedRevision, expectedRevision);
            if (base != 0 && (base != revision || expectedRevision != revision + 1)) throw new MissingBase();
            JSONObject world = object(packet, "world");
            Iterator<String> keys = world.keys();
            while (keys.hasNext()) {
                if (keys.next().startsWith("floor_")) throw invalid();
            }
            int seed = integer(world, "reborn_encounter_seed", 0, 999999);
            if (previous != null && seed != previous.getInt("reborn_encounter_seed")) throw invalid();
            Set<Integer> depths = floorIds(world);
            if (previous != null && !depths.containsAll(floorIds(previous))) throw invalid();
            JSONObject complete = copy(world);
            if (base != 0 && previous != null) {
                for (int depth : depths) {
                    String key = "floor_" + depth;
                    if (previous.has(key)) complete.put(key, previous.get(key));
                }
            }
            JSONArray floors = packet.getJSONArray("floors");
            if (floors.length() > depths.size()) throw invalid();
            Set<Integer> changed = new HashSet<>();
            for (int index = 0; index < floors.length(); index++) {
                JSONObject update = floors.getJSONObject(index);
                int depth = integer(update, "depth", 1, 1000);
                if (!depths.contains(depth) || !changed.add(depth)) throw invalid();
                String key = "floor_" + depth;
                JSONObject before = base == 0 || previous == null ? null : previous.optJSONObject(key);
                JSONObject floor;
                if (before == null) {
                    fields(update, "depth", "full");
                    floor = object(update, "full");
                } else {
                    fields(update, "depth", "fields", "level", "tiles");
                    floor = apply(before, object(update, "fields"), "level");
                    JSONObject level = apply(object(before, "level"), object(update, "level"),
                            "__className", "map", "visited", "mapped");
                    JSONObject tiles = object(update, "tiles");
                    fields(tiles, "?map", "?visited", "?mapped");
                    for (String grid : GRIDS) {
                        if (tiles.has(grid)) {
                            JSONArray cells = level.getJSONArray(grid);
                            JSONArray edits = tiles.getJSONArray(grid);
                            level.put(grid, applyTiles(cells, edits, grid));
                        }
                    }
                    floor.put("level", level);
                }
                complete.put(key, floor);
            }
            for (int depth : depths) {
                if (!complete.has("floor_" + depth)) throw invalid();
            }

            Bundle result = PartyState.read(PartyJson.copy(complete));
            previous = complete;
            revision = expectedRevision;
            return result;
        } catch (JSONException | IllegalArgumentException failure) {
            throw new IOException("Invalid party changes", failure);
        }
    }

    static final class MissingBase extends IOException {
        MissingBase() { super("Party update needs a full state"); }
    }

    private static Set<Integer> floorIds(JSONObject world) throws IOException, JSONException {
        JSONArray floors = world.getJSONArray("floors");
        if (floors.length() == 0 || floors.length() > 40) throw invalid();
        Set<Integer> result = new LinkedHashSet<>();
        for (int index = 0; index < floors.length(); index++) {
            Object value = floors.get(index);
            if (!(value instanceof Integer) && !(value instanceof Long)) throw invalid();
            long depth = ((Number)value).longValue();
            if (depth < 1 || depth > 26 && depth != 1000 || !result.add((int)depth)) throw invalid();
        }
        return result;
    }

    private static JSONObject changes(JSONObject before, JSONObject after, String... excluded) throws JSONException {
        Set<String> skip = new HashSet<>(Arrays.asList(excluded));
        JSONObject values = new JSONObject();
        JSONArray removed = new JSONArray();
        Iterator<String> keys = after.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!skip.contains(key) && (!before.has(key) || !same(before.get(key), after.get(key))))
                values.put(key, after.get(key));
        }
        keys = before.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!skip.contains(key) && !after.has(key)) removed.put(key);
        }
        return new JSONObject().put("set", values).put("remove", removed);
    }

    private static JSONObject apply(JSONObject before, JSONObject patch, String... excluded) throws IOException, JSONException {
        fields(patch, "set", "remove");
        JSONObject values = object(patch, "set");
        JSONArray removed = patch.getJSONArray("remove");
        if (values.length() > 512 || removed.length() > 512) throw invalid();
        Set<String> used = new HashSet<>(Arrays.asList(excluded));
        JSONObject result = copy(before);
        Iterator<String> keys = values.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!used.add(key)) throw invalid();
            result.put(key, values.get(key));
        }
        for (int index = 0; index < removed.length(); index++) {
            String key = text(removed.get(index), 160);
            if (!used.add(key) || !before.has(key)) throw invalid();
            result.remove(key);
        }
        return result;
    }

    private static JSONArray applyTiles(JSONArray before, JSONArray edits, String grid) throws IOException, JSONException {
        if (before.length() != Level.LENGTH || edits.length() > Level.LENGTH) throw invalid();
        JSONArray result = new JSONArray();
        for (int cell = 0; cell < Level.LENGTH; cell++) result.put(before.get(cell));
        int previousCell = -1;
        for (int index = 0; index < edits.length(); index++) {
            JSONArray edit = edits.getJSONArray(index);
            if (edit.length() != 2 || !(edit.get(0) instanceof Integer) && !(edit.get(0) instanceof Long)) throw invalid();
            long cell = ((Number)edit.get(0)).longValue();
            if (cell <= previousCell || cell >= Level.LENGTH) throw invalid();
            Object value = edit.get(1);
            if (grid.equals("map")) {
                if (!(value instanceof Integer) && !(value instanceof Long)) throw invalid();
                long tile = ((Number)value).longValue();
                if (tile < 0 || tile > Terrain.STORAGE || tile == 47) throw invalid();
            } else if (!(value instanceof Boolean)) throw invalid();
            result.put((int)cell, value);
            previousCell = (int)cell;
        }
        return result;
    }

    private static JSONObject copy(JSONObject source) throws JSONException {
        JSONObject result = new JSONObject();
        Iterator<String> keys = source.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            result.put(key, source.get(key));
        }
        return result;
    }

    private static boolean same(Object first, Object second) throws JSONException {
        if (first instanceof JSONObject && second instanceof JSONObject) {
            JSONObject a = (JSONObject)first;
            JSONObject b = (JSONObject)second;
            if (a.length() != b.length()) return false;
            Iterator<String> keys = a.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!b.has(key) || !same(a.get(key), b.get(key))) return false;
            }
            return true;
        }
        if (first instanceof JSONArray && second instanceof JSONArray) {
            JSONArray a = (JSONArray)first;
            JSONArray b = (JSONArray)second;
            if (a.length() != b.length()) return false;
            for (int i = 0; i < a.length(); i++) {
                if (!same(a.get(i), b.get(i))) return false;
            }
            return true;
        }
        return first.equals(second);
    }
}
