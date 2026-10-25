package com.bilboldev.pixeldungeonskills.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Iterator;

final class PartyNames {
    private PartyNames() { }

    static void encode(JSONObject state) throws JSONException, IOException {
        visit(state, false, 0);
    }

    static void decode(JSONObject state) throws JSONException, IOException {
        visit(state, true, 0);
    }

    private static void visit(Object value, boolean reading, int depth) throws JSONException, IOException {
        if (depth > 48) throw PartyMessage.invalid();
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray)value;
            for (int i = 0; i < array.length(); i++) visit(array.get(i), reading, depth + 1);
        } else if (value instanceof JSONObject) {
            JSONObject object = (JSONObject)value;
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith("class ") && key.endsWith("_label")) {
                    String itemType = key.substring(6, key.length() - 6);
                    PartyObjects.item(itemType);
                    if (reading) object.put(key, PartyCatalog.appearance(PartyMessage.integer(object, key, 1, 255)));
                    else object.put(key, PartyCatalog.appearance(object.getString(key)));
                } else visit(object.get(key), reading, depth + 1);
            }
        }
    }
}
