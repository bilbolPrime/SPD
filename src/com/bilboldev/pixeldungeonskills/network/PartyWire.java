package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.utils.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Iterator;

final class PartyWire {
    private PartyWire() { }

    static String encode(Bundle message) {
        try {
            JSONObject data = new JSONObject(message.toString());
            String kind = data.getString("kind");
            for (String field : fields(kind)) {
                if (data.has(field)) data.put(field, PartySymbols.id(data.getString(field)));
            }
            noText(data, true, 0);
            return data.toString();
        } catch (IOException | JSONException failure) {
            throw new IllegalArgumentException("Invalid outgoing party message", failure);
        }
    }

    static void decode(JSONObject data) throws IOException, JSONException {
        noText(data, true, 0);
        String kind = PartySymbols.name(PartyMessage.integer(data, "kind", 1, 65535));
        for (String field : fields(kind)) {
            if (data.has(field)) data.put(field, PartySymbols.name(PartyMessage.integer(data, field, 1, 65535)));
        }
    }

    private static String[] fields(String kind) {
        switch (kind) {
            case "command": return new String[]{"kind", "action"};
            case "animation": return new String[]{"kind", "animation"};
            case "stopped": return new String[]{"kind", "reason"};
            case "ping": return new String[]{"kind", "hero"};
            default: return new String[]{"kind"};
        }
    }

    private static void noText(Object value, boolean root, int depth) throws IOException, JSONException {
        if (depth > 64) throw PartyMessage.invalid();
        if (value instanceof String) throw PartyMessage.invalid();
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject)value;
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object child = object.get(key);
                if (root && key.equals("state") && child instanceof String) {
                    String bytes = (String)child;
                    if (bytes.length() > PartyConnection.MAX_MESSAGE_BYTES || (bytes.length() & 1) != 0)
                        throw PartyMessage.invalid();
                    for (int i = 0; i < bytes.length(); i++) {
                        char digit = bytes.charAt(i);
                        if (!(digit >= '0' && digit <= '9') && !(digit >= 'a' && digit <= 'f'))
                            throw PartyMessage.invalid();
                    }
                } else noText(child, false, depth + 1);
            }
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray)value;
            for (int i = 0; i < array.length(); i++) noText(array.get(i), false, depth + 1);
        }
    }
}
