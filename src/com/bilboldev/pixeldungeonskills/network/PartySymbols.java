package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.noosa.Game;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

final class PartySymbols {
    private static final Map<String, Integer> ids = new HashMap<>();
    private static final Map<Integer, String> symbols = new HashMap<>();

    static {
        String path = "network/symbol-ids.properties";
        try (InputStream stream = Game.instance == null
                ? PartySymbols.class.getClassLoader().getResourceAsStream(path) : Game.instance.getAssets().open(path)) {
            if (stream == null) throw new IOException("Missing party symbols");
            Properties entries = new Properties();
            entries.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            for (String number : entries.stringPropertyNames()) {
                int id = Integer.parseInt(number);
                String symbol = entries.getProperty(number);
                if (id <= 0 || symbols.put(id, symbol) != null || ids.put(symbol, id) != null)
                    throw new IllegalStateException("Duplicate party symbol");
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Could not load party symbols", failure);
        }
    }

    private PartySymbols() { }

    static int id(String symbol) {
        Integer id = ids.get(symbol);
        if (id == null) throw new IllegalArgumentException("Unregistered party symbol: " + symbol);
        return id;
    }

    static String name(int id) {
        String symbol = symbols.get(id);
        if (symbol == null) throw new IllegalArgumentException("Unknown party symbol ID");
        return symbol;
    }

    static void encode(JSONObject state) throws JSONException, IOException {
        visit(state, false, 0);
    }

    static void decode(JSONObject state) throws JSONException, IOException {
        visit(state, true, 0);
    }

    private static Object visit(Object value, boolean reading, int depth) throws JSONException, IOException {
        if (depth > 64) throw PartyMessage.invalid();
        if (value instanceof String) {
            if (reading) throw PartyMessage.invalid();
            return new JSONObject().put("symbol", id((String)value));
        }
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject)value;
            if (reading && object.has("symbol")) {
                PartyMessage.fields(object, "symbol");
                return name(PartyMessage.integer(object, "symbol", 1, 65535));
            }
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals("message_key")) {
                    object.put(key, reading
                            ? PartyCatalog.message(PartyMessage.integer(object, key, 1, 65535))
                            : PartyCatalog.message(object.getString(key)));
                    continue;
                }
                object.put(key, visit(object.get(key), reading, depth + 1));
            }
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray)value;
            for (int i = 0; i < array.length(); i++) array.put(i, visit(array.get(i), reading, depth + 1));
        }
        return value;
    }
}
