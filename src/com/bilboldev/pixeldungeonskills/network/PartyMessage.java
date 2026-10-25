package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.ui.QuickSlot;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

final class PartyMessage {
    private PartyMessage() { }

    static void validate(JSONObject message, boolean host, int sequence) throws IOException, JSONException {
        String kind = string(message, "kind", 16);
        if (host) {
            if (kind.equals("resync")) {
                fields(message, "kind", "sequence");
                integer(message, "sequence", 0, sequence);
                return;
            }
            if (!kind.equals("command") || message.toString().length() > 2048) throw invalid();
            fields(message, "kind", "action", "depth", "sequence", "payload");
            depth(message);
            integer(message, "sequence", 0, sequence);
            command(string(message, "action", 24), object(message, "payload"));
            return;
        }
        switch (kind) {
            case "boss_remains":
                fields(message, "kind", "depth", "cell");
                int bossFloor = integer(message, "depth", 5, 20);
                if (bossFloor % 5 != 0) throw invalid();
                integer(message, "cell", 0, Level.LENGTH - 1);
                break;
            case "effects":
                depth(message);
                PartyEffects.validate(message);
                break;
            case "state":
                fields(message, "kind", "sequence", "state");
                integer(message, "sequence", 1, Integer.MAX_VALUE);
                string(message, "state", PartyConnection.MAX_MESSAGE_BYTES);
                break;
            case "departed":
                fields(message, "kind", "sequence", "state");
                integer(message, "sequence", 1, Integer.MAX_VALUE);
                string(message, "state", PartyConnection.MAX_MESSAGE_BYTES);
                break;
            case "fallen":
                fields(message, "kind", "hero");
                integer(message, "hero", 0, 1);
                break;
            case "stopped":
                fields(message, "kind", "reason");
                String reason = string(message, "reason", 16);
                if (!reason.equals("ENEMY") && !reason.equals("INTERRUPTED") && !reason.equals("CANCELLED")) throw invalid();
                break;
            case "finished":
                fields(message, "kind", "surface", "?state", "?sequence");
                bool(message, "surface");
                if (message.has("state")) {
                    integer(message, "sequence", 1, Integer.MAX_VALUE);
                    string(message, "state", PartyConnection.MAX_MESSAGE_BYTES);
                }
                break;
            case "animation":
                String animation = string(message, "animation", 16);
                if (animation.equals("move")) {
                    int count = integer(message, "count", 1, 48);
                    String[] names = new String[count + 5];
                    names[0] = "kind";
                    names[1] = "animation";
                    names[2] = "depth";
                    names[3] = "sequence";
                    names[4] = "count";
                    for (int i = 0; i < count; i++) {
                        names[i + 5] = "move" + i;
                        JSONObject move = object(message, names[i + 5]);
                        fields(move, "actor", "from", "cell", "step");
                        integer(move, "actor", 0, Integer.MAX_VALUE - 1);
                        integer(move, "from", 0, Level.LENGTH - 1);
                        integer(move, "cell", 0, Level.LENGTH - 1);
                        integer(move, "step", 1, Integer.MAX_VALUE);
                    }
                    fields(message, names);
                    integer(message, "sequence", 0, Integer.MAX_VALUE);
                    depth(message);
                    break;
                } else {
                    fields(message, "kind", "actor", "animation", "cell", "depth");
                }
                integer(message, "actor", 0, Integer.MAX_VALUE - 1);
                depth(message);
                integer(message, "cell", 0, Level.LENGTH - 1);
                if (!animation.equals("attack") && !animation.equals("zap") && !animation.equals("operate")
                        && !animation.equals("read")) throw invalid();
                break;
            case "target":
                int token = integer(message, "token", 0, Integer.MAX_VALUE);
                fields(message, "kind", "token", token == 0 ? "?prompt" : "prompt");
                if (message.has("prompt")) PartyText.validate(object(message, "prompt"), 2048);
                break;
            case "log":
                fields(message, "kind", "tone", "text");
                integer(message, "tone", 0, 4);
                PartyText.validate(object(message, "text"), 1024);
                break;
            case "ping":
                fields(message, "kind", "hero", "ping");
                PartyConnection.hero(string(message, "hero", 24));
                PartySession.Ping.fromId(integer(message, "ping", 1, 255));
                break;
            case "ui":
                fields(message, "kind", "payload");
                window(object(message, "payload"));
                break;
            default: throw invalid();
        }
    }

    private static void depth(JSONObject message) throws IOException, JSONException {
        int depth = integer(message, "depth", 1, 1000);
        if (depth > 26 && depth != 1000) throw invalid();
    }

    private static void command(String action, JSONObject payload) throws IOException, JSONException {
        switch (action) {
            case "move":
                fields(payload, "value");
                integer(payload, "value", 0, Level.LENGTH - 1);
                break;
            case "target":
                fields(payload, "value", "token");
                integer(payload, "value", -1, Level.LENGTH - 1);
                integer(payload, "token", 1, Integer.MAX_VALUE);
                break;
            case "rest":
                fields(payload, "value");
                integer(payload, "value", 0, 1);
                break;
            case "quick":
            case "quick_assign":
                fields(payload, "value");
                integer(payload, "value", 0, QuickSlot.MAX_SLOTS - 1);
                break;
            case "ping":
                fields(payload, "value");
                PartySession.Ping.fromId(integer(payload, "value", 1, 255));
                break;
            case "search":
            case "resume":
            case "cancel":
            case "inventory":
            case "skills":
            case "last_skill":
            case "go":
                fields(payload, "value");
                integer(payload, "value", 0, 0);
                break;
            case "ui_choose":
                fields(payload, "token", "index");
                token(payload);
                integer(payload, "index", 0, 255);
                break;
            case "ui_close":
                fields(payload, "token");
                token(payload);
                break;
            default: throw invalid();
        }
    }

    private static void window(JSONObject payload) throws IOException, JSONException {
        boolean closed = payload.has("closed") && bool(payload, "closed");
        if (closed) {
            fields(payload, "token", "closed", "choices");
            integer(payload, "choices", 0, 0);
        } else {
            int count = integer(payload, "choices", 0, 256);
            String[] fields = new String[count + 4];
            fields[0] = "token";
            fields[1] = "title";
            fields[2] = "text";
            fields[3] = "choices";
            for (int i = 0; i < count; i++) {
                fields[i + 4] = "choice" + i;
                PartyText.validate(object(payload, fields[i + 4]), 1024);
            }
            fields(payload, fields);
            PartyText.validate(object(payload, "title"), 512);
            PartyText.validate(object(payload, "text"), 16384);
        }
        token(payload);
    }

    private static void token(JSONObject object) throws IOException, JSONException {
        integer(object, "token", 1, Integer.MAX_VALUE);
    }

    static void fields(JSONObject object, String... names) throws IOException {
        Set<String> allowed = new HashSet<>();
        for (String name : names) {
            boolean optional = name.startsWith("?");
            if (optional) name = name.substring(1);
            if (!optional && !object.has(name)) throw invalid();
            allowed.add(name);
        }
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            if (!allowed.contains(keys.next())) throw invalid();
        }
    }

    static int integer(JSONObject object, String key, int minimum, int maximum) throws IOException, JSONException {
        Object value = object.get(key);
        if (!(value instanceof Integer) && !(value instanceof Long)) throw invalid();
        long number = ((Number)value).longValue();
        if (number < minimum || number > maximum) throw invalid();
        return (int)number;
    }

    static boolean bool(JSONObject object, String key) throws IOException, JSONException {
        Object value = object.get(key);
        if (!(value instanceof Boolean)) throw invalid();
        return (Boolean)value;
    }

    static String string(JSONObject object, String key, int maximum) throws IOException, JSONException {
        return text(object.get(key), maximum);
    }

    static String text(Object value, int maximum) throws IOException {
        if (!(value instanceof String) || ((String)value).length() > maximum) throw invalid();
        String text = (String)value;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isISOControl(ch) && ch != '\n' && ch != '\t' && ch != '\r') throw invalid();
        }
        return text;
    }

    static JSONObject object(JSONObject object, String key) throws IOException, JSONException {
        Object value = object.get(key);
        if (!(value instanceof JSONObject)) throw invalid();
        return (JSONObject)value;
    }

    static IOException invalid() {
        return new IOException("Invalid party message");
    }
}
