package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.items.Item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

final class PartyCatalog {
    private static final Map<Integer, String> messages = load("network/message-ids.properties");
    private static final Map<Integer, String> types = load("network/type-ids.properties");
    private static final Map<String, Integer> messageIds = reverse(messages);
    private static final Map<Integer, String> appearances = load("network/appearance-ids.properties");
    private static final Map<String, Integer> appearanceIds = reverse(appearances);
    private static final Map<String, Integer> itemNames = new HashMap<>();

    static {
        for (Map.Entry<Integer, String> entry : types.entrySet()) {
            try {
                if (Item.class.isAssignableFrom(PartyObjects.type(entry.getValue()))) {
                    String key = entry.getValue().substring("com.bilboldev.pixeldungeonskills.".length())
                            .toLowerCase(Locale.ROOT);
                    itemNames.put(key, entry.getKey());
                }
            } catch (IllegalArgumentException invalid) {
                throw new IllegalStateException("Unknown party item type", invalid);
            }
        }
    }

    private PartyCatalog() { }

    static int message(String key) {
        Integer id = messageIds.get(key);
        if (id == null) throw new IllegalArgumentException("Unregistered party message: " + key);
        return id;
    }

    static String message(int id) {
        String key = messages.get(id);
        if (key == null) throw new IllegalArgumentException("Unknown party message ID");
        return key;
    }

    static int item(String key) {
        Integer id = itemNames.get(key);
        return id == null ? 0 : id;
    }

    static String itemKey(int id) {
        String type = types.get(id);
        if (type == null) throw new IllegalArgumentException("Unknown party item ID");
        String key = type.substring("com.bilboldev.pixeldungeonskills.".length()).toLowerCase(Locale.ROOT);
        if (!itemNames.containsKey(key)) throw new IllegalArgumentException("Party type is not an item");
        return key;
    }

    static int appearance(String label) {
        Integer id = appearanceIds.get(label);
        if (id == null) throw new IllegalArgumentException("Unknown item appearance");
        return id;
    }

    static String appearance(int id) {
        String label = appearances.get(id);
        if (label == null) throw new IllegalArgumentException("Unknown item appearance ID");
        return label;
    }

    private static Map<String, Integer> reverse(Map<Integer, String> entries) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<Integer, String> entry : entries.entrySet()) {
            if (result.put(entry.getValue(), entry.getKey()) != null)
                throw new IllegalStateException("Duplicate party catalog entry");
        }
        return result;
    }

    private static Map<Integer, String> load(String path) {
        try (InputStream input = Game.instance != null ? Game.instance.getAssets().open(path)
                : PartyCatalog.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new IOException("Missing party catalog");
            Properties entries = new Properties();
            entries.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            Map<Integer, String> result = new HashMap<>();
            for (String id : entries.stringPropertyNames()) result.put(Integer.parseInt(id), entries.getProperty(id));
            return result;
        } catch (IOException failure) {
            throw new IllegalStateException("Could not load " + path, failure);
        }
    }
}
