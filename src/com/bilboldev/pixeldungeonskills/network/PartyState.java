package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.GameplayMode;
import com.bilboldev.pixeldungeonskills.Journal;
import com.bilboldev.pixeldungeonskills.RunRules;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Blob;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Hunger;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalBoss;
import com.bilboldev.pixeldungeonskills.actors.mobs.RivalServant;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Room;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.plants.Plant;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import com.bilboldev.utils.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import static com.bilboldev.pixeldungeonskills.network.PartyMessage.*;

final class PartyState {
    private int objectCount;
    private int totalUpgrades;

    static void validateItem(JSONObject item) throws IOException, JSONException {
        type(item, Item.class);
        new PartyState().walk(item, "", 0);
    }

    static Item readItem(JSONObject item) throws IOException, JSONException {
        validateItem(item);
        JSONObject wrapper = new JSONObject().put("item", item);
        return (Item)Bundle.fromNetwork(wrapper, PartyObjects::create).get("item");
    }

    static Bundle read(JSONObject state) throws IOException, JSONException {
        PartySymbols.decode(state);
        PartyNames.decode(state);
        PartyState validator = new PartyState();
        validator.walk(state, "", 0);
        PartyQuests.validate(object(state, "quests"));
        Bundle bundle = Bundle.fromNetwork(state, PartyObjects::create);
        RunRules rules = RunRules.restore(bundle);
        if (rules.mode != GameplayMode.REBORN || rules.version != RunRules.newRun(GameplayMode.REBORN).version)
            throw invalid();
        int current = depth(state, "depth");
        integer(state, "reborn_encounter_seed", 0, 999999);
        integer(state, "rival_checked", 0, 15);
        int rivalFloor = integer(state, "rival_floor", 0, 20);
        int rivalClass = integer(state, "rival_class", 0, 6);
        if (rivalFloor % 5 != 0 || (rivalFloor == 0) != (rivalClass == 0)) throw invalid();
        JSONArray floors = array(state, "floors", 40);
        Set<Integer> depths = new HashSet<>();
        for (int i = 0; i < floors.length(); i++) {
            int floor = number(floors.get(i), 1, 1000);
            if (floor > 26 && floor != 1000 || !depths.add(floor)) throw invalid();
            JSONObject wrapper = object(state, "floor_" + floor);
            JSONObject level = object(wrapper, "level");
            type(level, Level.class);
            Level.Feeling.valueOf(string(wrapper, "feeling", 24));
            integer(wrapper, "view_distance", 1, 64);
            JSONArray mobs = array(level, "mobs", 256);
            Set<Integer> ids = new HashSet<>();
            Set<Integer> cells = new HashSet<>();
            for (int m = 0; m < mobs.length(); m++) {
                JSONObject mob = mobs.getJSONObject(m);
                int id = integer(mob, "id", 0, Integer.MAX_VALUE - 1);
                if (!ids.add(id)) throw invalid();
                if (integer(mob, "HP", -1000000, 1000000) > 0 && !cells.add(integer(mob, "pos", 0, Level.LENGTH - 1)))
                    throw invalid();
                runtime(object(wrapper, "mob_" + id));
            }
        }
        if (!depths.contains(current)) throw invalid();
        bools(state, "visible", Level.LENGTH);
        PartyConnection.hero(string(state, "second_class", 24));
        int firstId = -1;
        for (int i = 0; i < 2; i++) {
            JSONObject wrapper = object(state, "hero_" + i);
            JSONObject hero = object(wrapper, "hero");
            type(hero, Hero.class);
            int id = integer(hero, "id", 0, Integer.MAX_VALUE - 1);
            if (i == 1 && firstId == id) throw invalid();
            firstId = id;
            runtime(object(wrapper, "runtime"));
            action(object(wrapper, "action"));
            action(object(wrapper, "last_action"));
            ints(state, "enemies_" + i, 256, 0, Integer.MAX_VALUE - 1);
        }
        integer(state, "clock_owner", 0, 1);
        if (state.has("turn_hero")) integer(state, "turn_hero", -1, 1);
        if (state.has("dead_mobs") || state.has("dead_cells")) {
            ints(state, "dead_mobs", PartySession.MAX_DEATHS, 0, Integer.MAX_VALUE - 1);
            ints(state, "dead_cells", PartySession.MAX_DEATHS, 0, Level.LENGTH - 1);
            JSONArray deaths = state.getJSONArray("dead_mobs");
            if (deaths.length() != state.getJSONArray("dead_cells").length()) throw invalid();
            Set<Integer> ids = new HashSet<>();
            for (int i = 0; i < deaths.length(); i++) if (!ids.add(deaths.getInt(i))) throw invalid();
        }
        ints(state, "dropped_floors", 40, 1, 1001);
        ints(state, "chapters", 32, 0, 1000);
        return bundle;
    }

    private void walk(Object value, String key, int depth) throws IOException, JSONException {
        if (depth > 48) throw invalid();
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject)value;
            if (object.length() > 512) throw invalid();
            Class<?> type = null;
            if (object.has("__className")) {
                if (++objectCount > 8192) throw invalid();
                type = PartyObjects.type(string(object, "__className", 160));
                if (Item.class.isAssignableFrom(type)) {
                    int level = integer(object, "level", -100, 100);
                    totalUpgrades += Math.abs(level);
                    if (totalUpgrades > 20000) throw invalid();
                    integer(object, "quantity", 0, 1000000);
                }
                if (type == com.bilboldev.pixeldungeonskills.items.SoulCrystalFilled.class) {
                    if (object.has("capturedname")) throw invalid();
                    PartyText.validateName(object(object, "captured_text"));
                }
                if (type == com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet.class) {
                    if (object.has("name")) throw invalid();
                    PartyText.validateName(object(object, "name_text"));
                }
                if (Char.class.isAssignableFrom(type)) {
                    integer(object, "pos", 0, Level.LENGTH - 1);
                    integer(object, "HP", -1000000, 1000000);
                    integer(object, "HT", 1, 1000000);
                    collection(object, "buffs", Buff.class, 256);
                }
                if (type == Hero.class) {
                    PartyConnection.hero(string(object, "class", 24));
                    integer(object, "lvl", 1, 1000);
                    if (object.has("merctype") || object.has("mercweapon") || object.has("mercarmor") || object.has("mercitem"))
                        throw invalid();
                }
                if (Mob.class.isAssignableFrom(type)) integer(object, "target", -1, Level.LENGTH - 1);
                if (RivalBoss.class.isAssignableFrom(type)) {
                    int floor = integer(object, "rival_floor", 5, 20);
                    if (floor % 5 != 0) throw invalid();
                    integer(object, "rival_ability", 0, type == com.bilboldev.pixeldungeonskills.actors.mobs.RivalNecromancer.class ? 4 : 3);
                    integer(object, "phase", 0, 2);
                    for (String field : new String[]{"rival_turns", "rival_attacks", "rival_retreats", "rival_waits"})
                        integer(object, field, 0, Integer.MAX_VALUE - 1);
                    bool(object, "rival_entered");
                    JSONArray ids = ints(object, "rival_responses", 2, 0, Integer.MAX_VALUE - 1);
                    JSONArray actions = ints(object, "rival_actions", 2, 0, Integer.MAX_VALUE - 1);
                    if (ids.length() != actions.length()) throw invalid();
                }
                if (type == RivalServant.class) {
                    integer(object, "rival_rank", 1, 4);
                    integer(object, "rival_role", 0, 2);
                    integer(object, "rival_owner", 1, Integer.MAX_VALUE - 1);
                    integer(object, "rival_lifetime", 0, 12);
                }
                if (Blob.class.isAssignableFrom(type) && object.has("cur")) {
                    JSONArray cells = ints(object, "cur", Level.LENGTH, 0, 1000000);
                    integer(object, "start", 0, Level.LENGTH - cells.length());
                }
                if (Level.class.isAssignableFrom(type)) level(object);
                if (type == Heap.class) {
                    integer(object, "pos", 0, Level.LENGTH - 1);
                    Heap.Type.valueOf(string(object, "type", 32));
                }
                if (Plant.class.isAssignableFrom(type)) integer(object, "pos", 0, Level.LENGTH - 1);
                if (type == Room.class) {
                    int left = integer(object, "left", 0, Level.WIDTH - 1);
                    int top = integer(object, "top", 0, Level.HEIGHT - 1);
                    integer(object, "right", left, Level.WIDTH - 1);
                    integer(object, "bottom", top, Level.HEIGHT - 1);
                    Room.Type.valueOf(string(object, "type", 48));
                }
                expected(key, type);
            }
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String child = keys.next();
                Object entry = object.get(child);
                if (child.equals("name_text") || child.equals("captured_text"))
                    PartyText.validateName(object(object, child));
                if (child.equals("quick_slots")) {
                    if (type != null && Item.class.isAssignableFrom(type)) {
                        ints(object, child, QuickSlot.MAX_SLOTS, 0, QuickSlot.MAX_SLOTS - 1);
                    } else {
                        JSONArray slots = array(object, child, QuickSlot.MAX_SLOTS);
                        for (int i = 0; i < slots.length(); i++) {
                            String item = text(slots.get(i), 160);
                            if (!item.isEmpty()) PartyObjects.item(item);
                        }
                    }
                }
                if (child.equals("sprite")) PartyObjects.sprite(text(entry, 160));
                if (child.equals("items") || child.equals("stored") || child.startsWith("dropped") && !child.equals("dropped_floors"))
                    collection(object, child, Item.class, 1024);
                if (child.equals("journal")) collection(object, child, Journal.Record.class, 256);
                if (child.equals("level") && entry instanceof Number) {

                    if (type == Hunger.class) {
                        double hunger = ((Number)entry).doubleValue();
                        if (hunger < -1000000 || hunger > 1000000) throw invalid();
                    } else if (type != null && Buff.class.isAssignableFrom(type)) {
                        integer(object, child, 0, 1000000);
                    } else if (type != null && Mob.class.isAssignableFrom(type)) {
                        integer(object, child, 0, 1000);
                    } else {
                        integer(object, child, -100, 100);
                    }
                }
                if (child.startsWith("SKILL_3.0_ ") || child.startsWith("skill_level ")) integer(object, child, 0, 100);
                if (child.equals("buff_count")) integer(object, child, 0, 256);
                if (child.equals("rival_shield") || child.equals("rival_shield_spent") || child.equals("rival_healed"))
                    integer(object, child, 0, 1000000);
                if (child.equals("rival_summoned")) integer(object, child, 0, 12);
                if (child.equals("rival_bullets")) integer(object, child, 0, 3);
                if (child.equals("rival_smoke_turns")) integer(object, child, 0, 2);
                if (child.equals("rival_fallen_cell")) integer(object, child, -1, Level.LENGTH - 1);
                if (child.equals("warning") || child.equals("commitment") || child.equals("residue")
                        || child.equals("reborn_buttresses") || child.equals("reborn_trap_pads") || child.equals("reborn_doors"))
                    ints(object, child, Level.LENGTH, 0, Level.LENGTH - 1);
                if (child.equals("shot_origin") || child.equals("attack_origin") || child.equals("door")
                        || child.equals("reborn_gate") || child.equals("stairs")) integer(object, child, -1, Level.LENGTH - 1);
                walk(entry, child, depth + 1);
            }
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray)value;
            if (array.length() > Level.LENGTH) throw invalid();
            for (int i = 0; i < array.length(); i++) walk(array.get(i), key, depth + 1);
        } else if (value instanceof String) {
            text(value, 4096);
        } else if (value instanceof Number) {
            double number = ((Number)value).doubleValue();
            if (Double.isNaN(number) || Double.isInfinite(number) || Float.isInfinite(((Number)value).floatValue())) throw invalid();
        } else if (!(value instanceof Boolean)) throw invalid();
    }

    private static void level(JSONObject level) throws IOException, JSONException {
        JSONArray map = ints(level, "map", Level.LENGTH, 0, Terrain.STORAGE);
        if (map.length() != Level.LENGTH) throw invalid();
        for (int i = 0; i < map.length(); i++) {
            if (map.getInt(i) == 47) throw invalid();
        }
        bools(level, "visited", Level.LENGTH);
        bools(level, "mapped", Level.LENGTH);
        integer(level, "entrance", 0, Level.LENGTH - 1);
        integer(level, "exit", 0, Level.LENGTH - 1);
        integer(level, "storage", 0, Level.LENGTH - 1);
        collection(level, "mobs", Mob.class, 256);
        collection(level, "heaps", Heap.class, Level.LENGTH);
        collection(level, "plants", Plant.class, Level.LENGTH);
        collection(level, "blobs", Blob.class, 64);
        if (level.has("rooms")) collection(level, "rooms", Room.class, 256);
    }

    private static void runtime(JSONObject runtime) throws IOException, JSONException {
        integer(runtime, "hp", -1000000, 1000000);
        integer(runtime, "ht", 1, 1000000);
        integer(runtime, "invisible", 0, 256);
        integer(runtime, "view_distance", 0, 64);
        Object speed = runtime.get("speed");
        if (!(speed instanceof Number) || ((Number)speed).doubleValue() < 0.01 || ((Number)speed).doubleValue() > 100)
            throw invalid();
        bool(runtime, "paralysed");
        bool(runtime, "rooted");
        bool(runtime, "flying");
        int count = integer(runtime, "buff_count", 0, 256);
        for (int i = 0; i < count; i++) {
            JSONObject buff = object(runtime, "buff_" + i);
            if (!Buff.class.isAssignableFrom(PartyObjects.type(string(buff, "type", 160)))) throw invalid();
        }
    }

    private static void action(JSONObject action) throws IOException, JSONException {
        if (action.length() == 0) return;
        fields(action, "type", "cell", "target");
        switch (string(action, "type", 16)) {
            case "move":
            case "pickup":
            case "chest":
            case "buy":
            case "unlock":
            case "descend":
            case "ascend":
            case "cook":
            case "storage":
            case "attack":
            case "interact":
            case "discuss":
                break;
            default:
                throw invalid();
        }
        integer(action, "cell", 0, Level.LENGTH - 1);
        integer(action, "target", -1, Integer.MAX_VALUE - 1);
    }

    private static void expected(String key, Class<?> type) throws IOException {
        Class<?> expected;
        switch (key) {
            case "hero":
                expected = Hero.class;
                break;
            case "level":
                expected = Level.class;
                break;
            case "weapon":
                expected = KindOfWeapon.class;
                break;
            case "armor":
                expected = Armor.class;
                break;
            case "ring1":
            case "ring2":
                expected = Ring.class;
                break;
            case "bow":
                expected = Bow.class;
                break;
            case "glyph":
                expected = Armor.Glyph.class;
                break;
            case "enchantment":
                expected = Weapon.Enchantment.class;
                break;
            case "item":
                expected = Item.class;
                break;
            case "arena":
            case "chamber":
                expected = Room.class;
                break;
            default:
                return;
        }
        if (!expected.isAssignableFrom(type)) throw invalid();
    }

    private static void type(JSONObject object, Class<?> expected) throws IOException, JSONException {
        if (!expected.isAssignableFrom(PartyObjects.type(string(object, "__className", 160)))) throw invalid();
    }

    private static void collection(JSONObject object, String key, Class<?> type, int maximum) throws IOException, JSONException {
        JSONArray entries = array(object, key, maximum);
        for (int i = 0; i < entries.length(); i++) {
            Object value = entries.get(i);
            if (!(value instanceof JSONObject)) throw invalid();
            type((JSONObject)value, type);
        }
    }

    private static int depth(JSONObject object, String key) throws IOException, JSONException {
        int depth = integer(object, key, 1, 1000);
        if (depth > 26 && depth != 1000) throw invalid();
        return depth;
    }

    private static JSONArray array(JSONObject object, String key, int maximum) throws IOException, JSONException {
        Object value = object.get(key);
        if (!(value instanceof JSONArray) || ((JSONArray)value).length() > maximum) throw invalid();
        return (JSONArray)value;
    }

    private static JSONArray ints(JSONObject object, String key, int maximum, int minimum, int largest) throws IOException, JSONException {
        JSONArray array = array(object, key, maximum);
        for (int i = 0; i < array.length(); i++) number(array.get(i), minimum, largest);
        return array;
    }

    private static int number(Object value, int minimum, int maximum) throws IOException {
        if (!(value instanceof Integer) && !(value instanceof Long)) throw invalid();
        long number = ((Number)value).longValue();
        if (number < minimum || number > maximum) throw invalid();
        return (int)number;
    }

    private static void bools(JSONObject object, String key, int length) throws IOException, JSONException {
        JSONArray array = array(object, key, length);
        if (array.length() != length) throw invalid();
        for (int i = 0; i < array.length(); i++) {
            if (!(array.get(i) instanceof Boolean)) throw invalid();
        }
    }
}
