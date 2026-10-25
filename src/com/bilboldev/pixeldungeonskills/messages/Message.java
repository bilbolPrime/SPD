package com.bilboldev.pixeldungeonskills.messages;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.utils.Bundle;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Message {
    public interface Visitor<T> {
        T key(String key, Object[] arguments);
        T empty();
        T join(int separator, Message[] parts);
        T cased(int mode, Message value);
        T item(Item item, int field, String action);
        T skill(Skill skill, int field, boolean cooldown);
        T local(String value);
    }

    private Message() { }
    public abstract <T> T visit(Visitor<T> visitor);

    public static Message local(String value) {
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.local(value);
            }
            @Override
            public String localText() {
                return value;
            }
        };
    }

    public String localText() {
        return null;
    }

    public static Message key(String key, Object... arguments) {
        return key(() -> key, arguments);
    }

    public static Message key(Class<?> type, String field, Object... arguments) {
        return key(() -> Messages.key(type, field), arguments);
    }

    public static Message of(Object object, String field, Object... arguments) {
        return key(object.getClass(), field, arguments);
    }

    private static Message key(Supplier<String> key, Object[] arguments) {
        final Object[] saved = arguments.clone();
        for (Object arg : saved) {
            if (!(arg instanceof Message) && !(arg instanceof Number))
                throw new IllegalArgumentException("Message arguments must be numbers or message references");
        }
        return new Message() {
            @Override public <T> T visit(Visitor<T> visitor) {
                String resolved = key.get();
                if (Messages.argumentCount(resolved) != saved.length)
                    throw new IllegalArgumentException("Wrong message argument count: " + resolved);
                return visitor.key(resolved, saved.clone());
            }
        };
    }

    public static Message empty() {
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.empty();
            }
        };
    }

    public static Message paragraphs(Message... parts) {
        return join(3, parts);
    }
    public static Message line(Message... parts) {
        return join(1, parts);
    }

    public static Message join(int separator, Message... parts) {
        if (separator < 0 || separator >= SEPARATORS.length) throw new IllegalArgumentException("Unknown separator");
        final Message[] saved = parts.clone();
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.join(separator, saved.clone());
            }
        };
    }

    public static Message capitalize(Message value) {
        return cased(value, 1);
    }
    public static Message indefinite(Message value) {
        return cased(value, 2);
    }

    public static Message cased(Message value, int mode) {
        if (mode < 1 || mode > 2) throw new IllegalArgumentException("Unknown message case");
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.cased(mode, value);
            }
        };
    }

    public static Message itemName(Item item) {
        return item instanceof com.bilboldev.pixeldungeonskills.items.bags.Bag
                ? key(item.getClass(), "name") : item(item, 0, null);
    }
    public static Message item(Item item) {
        return item instanceof com.bilboldev.pixeldungeonskills.items.bags.Bag
                ? itemName(item) : item(item, 1, null);
    }
    public static Message description(Item item) {
        return item(item, 2, null);
    }
    public static Message action(Item item, String command) {
        return item(item, 3, command);
    }

    public static Message item(Item item, int field, String action) {
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.item(item, field, action);
            }
        };
    }

    public static Message skillName(Skill skill) {
        return skill(skill, 0);
    }
    public static Message skillDescription(Skill skill) {
        return skill(skill, 1);
    }

    private static Message skill(Skill skill, int field) {
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.skill(skill, field, skill.coolDown());
            }
        };
    }

    public static Message skill(Skill skill, int field, boolean cooldown) {
        return new Message() {
            @Override
            public <T> T visit(Visitor<T> visitor) {
                return visitor.skill(skill, field, cooldown);
            }
        };
    }

    private static final String[] SEPARATORS = {"", " ", "\n", "\n\n"};
    private static final Pattern FORMAT = Pattern.compile("%(?:(\\d+)\\$)?[-#+0,(]*\\d*(?:\\.\\d+)?([sdioxXeEgGfcbBhHn%])");

    @Override
    public final String toString() {
        return visit(LOCAL);
    }

    private static final Visitor<String> LOCAL = new Visitor<String>() {
        @Override public String key(String key, Object[] args) {
            for (int i = 0; i < args.length; i++) if (args[i] instanceof Message) args[i] = args[i].toString();
            return formatted(key, args);
        }
        @Override
        public String empty() {
            return "";
        }
        @Override public String join(int separator, Message[] parts) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) result.append(SEPARATORS[separator]);
                result.append(parts[i]);
            }
            return result.toString();
        }
        @Override public String cased(int mode, Message value) {
            return mode == 1 ? Messages.capitalize(value.toString())
                    : com.bilboldev.pixeldungeonskills.utils.Utils.indefinite(value.toString());
        }
        @Override
        public String item(Item item, int field, String action) {
            return itemValue(item, field, action);
        }
        @Override
        public String skill(Skill skill, int field, boolean cooldown) {
            return skillValue(skill, field, cooldown);
        }
        @Override
        public String local(String value) {
            return Messages.maybeTranslate(value);
        }
    };

    public final Bundle store() {
        return visit(SAVED);
    }

    private static Bundle saveArgument(Object argument) {
        if (argument instanceof Message) return ((Message)argument).store();
        Bundle value = new Bundle();
        if (argument instanceof Float || argument instanceof Double) value.put("decimal", ((Number)argument).floatValue());
        else value.put("number", ((Number)argument).intValue());
        return value;
    }

    private static final Visitor<Bundle> SAVED = new Visitor<Bundle>() {
        @Override public Bundle key(String key, Object[] args) {
            Bundle value = new Bundle();
            value.put("message_key", key);
            value.put("count", args.length);
            for (int i = 0; i < args.length; i++) value.put("arg" + i, saveArgument(args[i]));
            return value;
        }
        @Override public Bundle empty() {
            Bundle value = new Bundle();
            value.put("empty", true);
            return value;
        }
        @Override public Bundle join(int separator, Message[] parts) {
            Bundle value = new Bundle();
            value.put("separator", separator);
            value.put("parts", parts.length);
            for (int i = 0; i < parts.length; i++) value.put("part" + i, parts[i].store());
            return value;
        }
        @Override public Bundle cased(int mode, Message text) {
            Bundle value = new Bundle();
            value.put("case", mode);
            value.put("value", text.store());
            return value;
        }
        @Override public Bundle item(Item item, int field, String action) {
            throw new IllegalArgumentException("A persistent name cannot retain a live item");
        }
        @Override public Bundle skill(Skill skill, int field, boolean cooldown) {
            throw new IllegalArgumentException("A persistent name cannot retain a live skill");
        }
        @Override public Bundle local(String value) {
            throw new IllegalArgumentException("A legacy display name has no message identity");
        }
    };

    public static Message restore(Bundle value) {
        return restore(value, 0, new int[1]);
    }

    private static Message restore(Bundle value, int depth, int[] nodes) {
        if (depth > 8 || ++nodes[0] > 256) throw new IllegalArgumentException("Message is too complex");
        if (value.contains("empty")) return empty();
        if (value.contains("case")) return cased(restore(value.getBundle("value"), depth + 1, nodes), value.getInt("case"));
        if (value.contains("parts")) {
            int count = value.getInt("parts");
            if (count < 0 || count > 64) throw new IllegalArgumentException("Too many message parts");
            Message[] parts = new Message[count];
            for (int i = 0; i < parts.length; i++) parts[i] = restore(value.getBundle("part" + i), depth + 1, nodes);
            return join(value.getInt("separator"), parts);
        }
        int count = value.getInt("count");
        if (count < 0 || count > 16) throw new IllegalArgumentException("Too many message arguments");
        Object[] args = new Object[count];
        for (int i = 0; i < args.length; i++) {
            Bundle arg = value.getBundle("arg" + i);
            if (arg.contains("number")) args[i] = arg.getInt("number");
            else if (arg.contains("decimal")) args[i] = arg.getFloat("decimal");
            else args[i] = restore(arg, depth + 1, nodes);
        }
        return key(value.getString("message_key"), args);
    }

    private static String itemValue(Item item, int field, String action) {
        switch (field) {
            case 0: return Messages.maybeTranslate(item.name());
            case 1: return Messages.maybeTranslate(item.toString());
            case 2: return item.displayInfo();
            case 3: return item.actionName(action, Dungeon.hero);
            default: throw new IllegalArgumentException("Unknown item text field");
        }
    }

    private static String skillValue(Skill skill, int field, boolean cooldown) {
        if (field == 0) return Messages.maybeTranslate(skill.name);
        StringBuilder description = new StringBuilder();
        for (String line : skill.info().split("\\n", -1)) {
            if (description.length() > 0) description.append('\n');
            description.append(Messages.maybeTranslate(line));
        }
        if (cooldown) description.append('\n').append(Messages.get("custom.generated.entry_this_skill_is_on_cool_down_9d24e9e5e0"));
        return description.toString();
    }

    public static Message actor(Char actor) {
        return actor.nameMessage();
    }

    public static Message heroClass(com.bilboldev.pixeldungeonskills.actors.hero.HeroClass hero) {
        switch (hero) {
            case HATSUNE: return Message.key("custom.generated.entry_hatsune_c6857ab481");
            case DATENSHI: return Message.key("custom.generated.entry_datenshi_9d276e25b9");
            case ONLINE: return Message.key("custom.generated.entry_online_2dbc2fd235");
            case GAUNTLET: return Message.key("custom.generated.entry_gauntlet_7403dcaf26");
            case NECROMANCER: return Message.key("actors.mobs.necromancer.name");
            case MERCENARY: return Message.key("actors.hero.heroclass.mercenary");
            default: return key(hero.getClass(), hero.name().toLowerCase(Locale.ROOT));
        }
    }

    public static Message subclass(com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass sub, boolean description) {
        switch (sub) {
            case LICH: case SPIRIT_BINDER:
                return key("custom.generated.necro_merc." + sub.name().toLowerCase(Locale.ROOT) + (description ? "_desc" : ""));
            case MARSHAL: case EXECUTIONER:
                if (!description) return key("custom.generated.necro_merc." + sub.name().toLowerCase(Locale.ROOT));
            default:
                return key(sub.getClass(), sub.name().toLowerCase(Locale.ROOT) + (description ? "_legacy_desc" : ""));
        }
    }

    public static Message hero(Hero hero) {
        return hero.subClass == com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass.NONE
                ? heroClass(hero.heroClass) : subclass(hero.subClass, false);
    }

    private static String formatted(String key, Object[] args) {
        String template = Messages.template(key, false);
        if (template == null) throw new IllegalArgumentException("Unknown message key: " + key);
        if (args.length == 0) return template;
        Matcher formats = FORMAT.matcher(template);
        int next = 0;
        while (formats.find()) {
            char format = formats.group(2).charAt(0);
            if (format == '%' || format == 'n') continue;
            int index = formats.group(1) == null ? next++ : Integer.parseInt(formats.group(1)) - 1;
            if ("eEfgGaA".indexOf(format) >= 0 && args[index] instanceof Number)
                args[index] = ((Number)args[index]).doubleValue();
        }
        return String.format(Messages.locale(), format(template), args);
    }

    private static String format(String template) {
        Matcher tokens = FORMAT.matcher(template);
        StringBuilder result = new StringBuilder();
        int end = 0;
        while (tokens.find()) {
            result.append(template.substring(end, tokens.start()).replace("%", "%%"));
            result.append(tokens.group());
            end = tokens.end();
        }
        return result.append(template.substring(end).replace("%", "%%")).toString();
    }

}
