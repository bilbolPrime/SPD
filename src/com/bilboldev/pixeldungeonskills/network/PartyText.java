package com.bilboldev.pixeldungeonskills.network;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.utils.Bundle;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PartyText {
    private PartyText() { }
    private static final int MAX_DEPTH = 8;
    private static final int MAX_PARTS = 64;
    private static final String[] SEPARATORS = {"", " ", "\n", "\n\n"};
    private static final Pattern FORMAT = Pattern.compile("%(?:(\\d+)\\$)?[-#+0,(]*\\d*(?:\\.\\d+)?([sdioxXeEgGfcbBhHn%])");

    public static Bundle encode(Message message) {
        return message.visit(ENCODER);
    }

    private static final Message.Visitor<Bundle> ENCODER = new Message.Visitor<Bundle>() {
        @Override public Bundle key(String key, Object[] args) {
            Bundle value = new Bundle();
            value.put("id", PartyCatalog.message(key));
            value.put("count", args.length);
            for (int i = 0; i < args.length; i++) value.put("arg" + i, argument(args[i]));
            return value;
        }
        @Override public Bundle empty() {
            Bundle value = new Bundle();
            value.put("empty", true);
            return value;
        }
        @Override public Bundle join(int separator, Message[] parts) {
            if (parts.length > MAX_PARTS) throw new IllegalArgumentException("Too many message parts");
            Bundle value = new Bundle();
            value.put("separator", separator);
            value.put("parts", parts.length);
            for (int i = 0; i < parts.length; i++) value.put("part" + i, encode(parts[i]));
            return value;
        }
        @Override public Bundle cased(int mode, Message text) {
            Bundle value = new Bundle();
            value.put("case", mode);
            value.put("value", encode(text));
            return value;
        }
        @Override public Bundle item(Item item, int field, String action) {
            Bundle holder = new Bundle();
            holder.put("object", item);
            try {
                JSONObject data = new JSONObject(holder.toString());
                if (item instanceof com.bilboldev.pixeldungeonskills.items.bags.Bag) {
                    JSONObject bag = data.getJSONObject("object");
                    bag.remove("inventory");
                    bag.remove("inventory2");
                }
                PartyNames.encode(data);
                PartySymbols.encode(data);
                Bundle text = Bundle.fromNetwork(data, PartyObjects::create);
                text.put("item_value", field);
                if (action != null) text.put("action", PartySymbols.id(action));
                return text;
            } catch (JSONException | IOException failure) {
                throw new IllegalArgumentException("Could not describe item", failure);
            }
        }
        @Override public Bundle skill(Skill skill, int field, boolean cooldown) {
            Bundle text = new Bundle();
            text.put("skill", PartySymbols.id(skill.getClass().getName()));
            text.put("field", field);
            text.put("level", skill.level);
            text.put("active", skill.active);
            text.put("cooldown", cooldown);
            return text;
        }
        @Override public Bundle local(String value) {
            throw new IllegalArgumentException("Local display text cannot be sent to a party");
        }
    };

    private static Bundle argument(Object argument) {
        if (argument instanceof Message) return encode((Message)argument);
        if (!(argument instanceof Number)) throw new IllegalArgumentException("Unsupported message argument");
        double number = ((Number)argument).doubleValue();
        if (Double.isNaN(number) || Double.isInfinite(number) || Math.abs(number) > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Message number is out of range");
        Bundle value = new Bundle();
        if (number == Math.rint(number)) value.put("number", (int)number);
        else value.put("decimal", (float)number);
        return value;
    }

    public static String render(Bundle text) {
        String result = decoded(text).toString();
        if (result.length() > 65536) throw new IllegalArgumentException("Message is too long");
        return result;
    }

    public static Message decoded(Bundle text) {
        if (text.contains("empty")) return Message.empty();
        if (text.contains("case")) return Message.cased(decoded(text.getBundle("value")), text.getInt("case"));
        if (text.contains("item_value")) return Message.item(readItem(text.getBundle("object")), text.getInt("item_value"),
                text.contains("action") ? PartySymbols.name(text.getInt("action")) : null);
        if (text.contains("skill")) {
            Skill skill = PartySkills.create(text.getInt("skill"));
            skill.level = text.getInt("level");
            skill.active = text.getBoolean("active");
            return Message.skill(skill, text.getInt("field"), text.getBoolean("cooldown"));
        }
        if (text.contains("parts")) {
            Message[] parts = new Message[text.getInt("parts")];
            for (int i = 0; i < parts.length; i++) parts[i] = decoded(text.getBundle("part" + i));
            return Message.join(text.getInt("separator"), parts);
        }
        Object[] args = new Object[text.getInt("count")];
        for (int i = 0; i < args.length; i++) {
            Bundle arg = text.getBundle("arg" + i);
            if (arg.contains("number")) args[i] = arg.getInt("number");
            else if (arg.contains("decimal")) args[i] = arg.getFloat("decimal");
            else args[i] = decoded(arg);
        }
        return Message.key(PartyCatalog.message(text.getInt("id")), args);
    }

    private static Item readItem(Bundle value) {
        try {
            JSONObject object = new JSONObject(value.toString());
            PartySymbols.decode(object);
            PartyNames.decode(object);
            return PartyState.readItem(object);
        } catch (JSONException | IOException failure) {
            throw new IllegalArgumentException("Invalid item text", failure);
        }
    }

    static void validate(JSONObject text, int maximum) throws IOException, JSONException {
        int[] nodes = {0};
        validate(text, 0, nodes);

        if (text.toString().length() > Math.max(4096, maximum * 8)) throw PartyMessage.invalid();
    }

    static void validateName(JSONObject saved) throws IOException, JSONException {
        JSONObject text = new JSONObject(saved.toString());
        nameKeys(text, 0);
        validate(text, 1024);
    }

    private static void nameKeys(JSONObject text, int depth) throws IOException, JSONException {
        if (depth > MAX_DEPTH || text.has("item_value") || text.has("skill") || text.has("id"))
            throw PartyMessage.invalid();
        if (text.has("message_key")) {
            text.put("id", PartyCatalog.message(PartyMessage.string(text, "message_key", 256)));
            text.remove("message_key");
        }
        java.util.Iterator<String> keys = text.keys();
        while (keys.hasNext()) {
            Object value = text.get(keys.next());
            if (value instanceof JSONObject) nameKeys((JSONObject)value, depth + 1);
        }
    }

    private static void validate(JSONObject text, int depth, int[] nodes) throws IOException, JSONException {
        if (depth > MAX_DEPTH || ++nodes[0] > 256) throw PartyMessage.invalid();
        if (text.has("case")) {
            PartyMessage.fields(text, "case", "value");
            PartyMessage.integer(text, "case", 1, 2);
            validate(PartyMessage.object(text, "value"), depth + 1, nodes);
        } else if (text.has("item_value")) {
            int field = PartyMessage.integer(text, "item_value", 0, 3);
            PartyMessage.fields(text, "item_value", "object", field == 3 ? "action" : "?action");
            if (text.has("action")) PartySymbols.name(PartyMessage.integer(text, "action", 1, 65535));
            JSONObject item = new JSONObject(PartyMessage.object(text, "object").toString());
            PartySymbols.decode(item);
            PartyNames.decode(item);
            PartyState.validateItem(item);
        } else if (text.has("skill")) {
            PartyMessage.fields(text, "skill", "field", "level", "active", "cooldown");
            PartySkills.require(PartyMessage.integer(text, "skill", 1, 65535));
            PartyMessage.integer(text, "field", 0, 1);
            PartyMessage.integer(text, "level", 0, 100);
            PartyMessage.bool(text, "active");
            PartyMessage.bool(text, "cooldown");
        } else if (text.has("empty")) {
            PartyMessage.fields(text, "empty");
            if (!PartyMessage.bool(text, "empty")) throw PartyMessage.invalid();
        } else if (text.has("number")) {
            PartyMessage.fields(text, "number");
            PartyMessage.integer(text, "number", -Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else if (text.has("decimal")) {
            PartyMessage.fields(text, "decimal");
            Object value = text.get("decimal");
            if (!(value instanceof Number)) throw PartyMessage.invalid();
            double number = ((Number)value).doubleValue();
            if (Double.isNaN(number) || Double.isInfinite(number) || Math.abs(number) > Integer.MAX_VALUE)
                throw PartyMessage.invalid();
        } else if (text.has("parts")) {
            int count = PartyMessage.integer(text, "parts", 0, MAX_PARTS);
            PartyMessage.integer(text, "separator", 0, SEPARATORS.length - 1);
            String[] fields = new String[count + 2];
            fields[0] = "parts";
            fields[1] = "separator";
            for (int i = 0; i < count; i++) {
                fields[i + 2] = "part" + i;
                validate(PartyMessage.object(text, fields[i + 2]), depth + 1, nodes);
            }
            PartyMessage.fields(text, fields);
        } else {
            String key;
            try {
                key = PartyCatalog.message(PartyMessage.integer(text, "id", 1, 65535));
            } catch (IllegalArgumentException invalid) {
                throw PartyMessage.invalid();
            }
            int count = PartyMessage.integer(text, "count", 0, 16);
            if (Messages.argumentCount(key) != count) throw PartyMessage.invalid();
            String[] fields = new String[count + 2];
            fields[0] = "id";
            fields[1] = "count";
            for (int i = 0; i < count; i++) {
                fields[i + 2] = "arg" + i;
                validate(PartyMessage.object(text, fields[i + 2]), depth + 1, nodes);
            }
            PartyMessage.fields(text, fields);
            Matcher formats = FORMAT.matcher(Messages.template(key, true));
            int next = 0;
            while (formats.find()) {
                char format = formats.group(2).charAt(0);
                if (format == '%' || format == 'n') continue;
                int index = formats.group(1) == null ? next++ : Integer.parseInt(formats.group(1)) - 1;
                JSONObject arg = PartyMessage.object(text, "arg" + index);
                if ("cChH".indexOf(format) >= 0) throw PartyMessage.invalid();
                if ("doxX".indexOf(format) >= 0 && !arg.has("number")) throw PartyMessage.invalid();
                if ("eEfgGaA".indexOf(format) >= 0 && !arg.has("decimal") && !arg.has("number")) throw PartyMessage.invalid();
            }
        }
    }
}
