package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.utils.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.bilboldev.pixeldungeonskills.network.PartyMessage.*;

public final class PartyQuests {
    enum Quest {
        GHOST(1, "sadGhost"), WANDMAKER(2, "wandmaker"), BLACKSMITH(3, "blacksmith"), IMP(4, "demon");

        final int id;
        final String key;

        Quest(int id, String key) {
            this.id = id;
            this.key = key;
        }
    }

    enum Variant {
        ROSE(11, Quest.GHOST, "ROSE"), RAT(12, Quest.GHOST, "RAT"), CURSE(13, Quest.GHOST, "CURSE"),
        BERRY(21, Quest.WANDMAKER, "BERRY"), DUST(22, Quest.WANDMAKER, "DUST"), FISH(23, Quest.WANDMAKER, "FISH"),
        GOLD(31, Quest.BLACKSMITH, "false"), BLOOD(32, Quest.BLACKSMITH, "true"),
        GOLEMS(41, Quest.IMP, "false"), MONKS(42, Quest.IMP, "true");

        final int id;
        final Quest quest;
        final String value;

        Variant(int id, Quest quest, String value) {
            this.id = id;
            this.quest = quest;
            this.value = value;
        }

        static Variant find(Quest quest, int id) throws IOException {
            for (Variant variant : values()) if (variant.quest == quest && variant.id == id) return variant;
            throw invalid();
        }
    }

    private PartyQuests() { }

    public static Bundle encode(Bundle quests) {
        try {
            JSONObject source = new JSONObject(quests.toString());
            JSONObject result = new JSONObject();
            for (Quest quest : Quest.values()) {
                JSONObject node = source.getJSONObject(quest.key);
                if (node.getBoolean("spawned")) {
                    String value = node.has("type") ? node.getString("type") : String.valueOf(node.getBoolean("alternative"));
                    Variant selected = null;
                    for (Variant variant : Variant.values()) {
                        if (variant.quest == quest && variant.value.equals(value)) selected = variant;
                    }
                    if (selected == null) throw new IllegalArgumentException("Unknown quest variant");
                    node.remove("type");
                    node.remove("alternative");
                    node.put("variant", selected.id);
                }
                result.put(Integer.toString(quest.id), node);
            }
            return Bundle.fromNetwork(result, PartyObjects::create);
        } catch (JSONException failure) {
            throw new IllegalArgumentException("Could not describe party quests", failure);
        }
    }

    public static Bundle decode(Bundle quests) throws IOException {
        try {
            JSONObject source = new JSONObject(quests.toString());
            validate(source);
            JSONObject result = new JSONObject();
            for (Quest quest : Quest.values()) {
                JSONObject node = source.getJSONObject(Integer.toString(quest.id));
                if (node.getBoolean("spawned")) {
                    Variant variant = Variant.find(quest, node.getInt("variant"));
                    node.remove("variant");
                    if (quest == Quest.GHOST || quest == Quest.WANDMAKER) node.put("type", variant.value);
                    else node.put("alternative", Boolean.parseBoolean(variant.value));
                }
                result.put(quest.key, node);
            }
            return Bundle.fromNetwork(result, PartyObjects::create);
        } catch (JSONException failure) {
            throw invalid();
        }
    }

    static void validate(JSONObject quests) throws IOException, JSONException {
        fields(quests, "1", "2", "3", "4");
        for (Quest quest : Quest.values()) {
            JSONObject node = object(quests, Integer.toString(quest.id));
            if (!bool(node, "spawned")) {
                fields(node, "spawned");
                continue;
            }
            Variant variant = Variant.find(quest, integer(node, "variant", 1, 99));
            boolean given = bool(node, "given");
            switch (quest) {
                case GHOST:
                    fields(node, "spawned", "variant", "given", "processed", "depth", "?weapon", "?armor",
                            variant == Variant.ROSE ? "left2kill" : "?left2kill");
                    if (bool(node, "processed") && !given) throw invalid();
                    integer(node, "depth", 1, 26);
                    if (node.has("left2kill")) integer(node, "left2kill", 0, 1000);
                    reward(node, "weapon", Weapon.class);
                    reward(node, "armor", Armor.class);
                    if (hasReward(node, "weapon") != hasReward(node, "armor")
                            || !given && !hasReward(node, "weapon")) throw invalid();
                    break;
                case WANDMAKER:
                    fields(node, "spawned", "variant", "given", "?wand1", "?wand2");
                    reward(node, "wand1", Wand.class);
                    reward(node, "wand2", Wand.class);
                    if (hasReward(node, "wand1") != hasReward(node, "wand2")
                            || !given && !hasReward(node, "wand1")) throw invalid();
                    break;
                case BLACKSMITH:
                    fields(node, "spawned", "variant", "given", "completed", "reforged");
                    boolean completed = bool(node, "completed");
                    if (completed && !given || bool(node, "reforged") && !completed) throw invalid();
                    break;
                case IMP:
                    fields(node, "spawned", "variant", "given", "completed", "?reward");
                    if (bool(node, "completed") && !given) throw invalid();
                    reward(node, "reward", Ring.class);
                    if (!node.getBoolean("completed") && !hasReward(node, "reward")) throw invalid();
                    break;
            }
        }
    }

    private static boolean hasReward(JSONObject node, String key) throws IOException, JSONException {
        return node.has(key) && object(node, key).length() != 0;
    }

    private static void reward(JSONObject node, String key, Class<?> expected) throws IOException, JSONException {
        if (!node.has(key)) return;
        JSONObject item = object(node, key);
        if (item.length() == 0) return;
        if (!expected.isAssignableFrom(PartyObjects.type(string(item, "__className", 160)))) throw invalid();
    }
}
