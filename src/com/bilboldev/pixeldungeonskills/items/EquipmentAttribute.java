package com.bilboldev.pixeldungeonskills.items;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.quest.Pickaxe;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfMagicMissile;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.NecroBlade;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.SoulBlade;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.MissileWeapon;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class EquipmentAttribute {
    public enum Type {
        HEALTH(3, 12, 48, ItemSpriteSheet.POTION_HEALING),
        MANA(2, 10, 40, ItemSpriteSheet.POTION_MANA),
        ACCURACY(1, 4, 12, ItemSpriteSheet.SHORT_SWORD),
        EVASION(1, 4, 12, ItemSpriteSheet.ARMOR_CLOTH),
        ARMOR(1, 2, 6, ItemSpriteSheet.ARMOR_PLATE),
        MELEE(4, 10, 35, ItemSpriteSheet.SHORT_SWORD),
        RANGED(4, 10, 35, ItemSpriteSheet.Bow),
        WANDS(4, 10, 35, ItemSpriteSheet.WAND_MAGIC_MISSILE),
        CLASS_SKILLS(1, 2, 3, ItemSpriteSheet.SCROLL_SKILLPOINT),
        SKILL(1, 2, 3, ItemSpriteSheet.SCROLL_SKILLPOINT),
        TREE_SKILLS(1, 2, 3, ItemSpriteSheet.SCROLL_SKILLPOINT);

        public final int min, max, cap, image;

        Type(int min, int max, int cap, int image) {
            this.min = min;
            this.max = max;
            this.cap = cap;
            this.image = image;
        }

        public String key() { return "attributes." + name().toLowerCase(Locale.ROOT); }
        public String title() { return Messages.get(key() + ".name"); }
        public String description() { return Messages.get(key() + ".desc", min, max, cap); }
    }

    public static final EquipmentAttribute[] NONE = new EquipmentAttribute[0];
    private static final Type[] TYPES = Type.values();
    private static final Talent[] TALENTS = Talent.values();
    private static final HashMap<Class<?>, List<EquipmentAttribute>> choices = new HashMap<>();
    public final Type type;
    public final int amount;
    public final int target;

    public EquipmentAttribute(Type type, int amount, int target) {
        this.type = type;
        this.amount = amount;
        this.target = target;
    }

    public String description() {
        if (type == Type.CLASS_SKILLS) return Messages.get("attributes.class_bonus", amount,
                Message.heroClass(HeroClass.values()[target]).toString());
        if (type == Type.TREE_SKILLS) return Messages.get("attributes.tree_bonus", amount,
                Message.subclass(HeroSubClass.values()[target], false).toString());
        if (type == Type.SKILL) return Messages.get("attributes.skill_bonus", amount, TALENTS[target].title());
        return Messages.get(type.key() + ".bonus", amount);
    }

    public String rangeDescription() {
        String range = type.min + "\u2013" + type.max;
        if (type == Type.CLASS_SKILLS) return Messages.get("attributes.class_range", (Object)range,
                Message.heroClass(HeroClass.values()[target]).toString());
        if (type == Type.TREE_SKILLS) return Messages.get("attributes.tree_range", (Object)range,
                Message.subclass(HeroSubClass.values()[target], false).toString());
        if (type == Type.SKILL) return Messages.get("attributes.skill_range", (Object)range, TALENTS[target].title());
        return Messages.get(type.key() + ".range", (Object)range);
    }

    public String key() {
        return type.name() + ":" + target;
    }

    public static boolean supported(Item item) {

        return !(item instanceof Pickaxe) && !(item instanceof WandOfMagicMissile)
                && (item instanceof Armor || item instanceof Ring || item instanceof Wand
                || item instanceof Weapon && (!(item instanceof MissileWeapon) || item instanceof Bow));
    }

    public static void roll(Item item) {
        if (!Dungeon.rules().hasRebornEncounters() || !supported(item)
                || item.attributes.length != 0 || Random.Int(100) >= 80) return;
        int depth = Math.max(1, Dungeon.depth);
        int count = Random.IntRange(1, 3);
        boolean skills = Dungeon.hero != null && Dungeon.hero.skillSystem == 4;
        ArrayList<EquipmentAttribute> eligible = new ArrayList<>();
        for (EquipmentAttribute option : options(item)) {
            if (option.type == Type.CLASS_SKILLS && (!skills || depth < 11)) continue;
            if (option.type == Type.TREE_SKILLS && (!skills || depth < 6)) continue;
            if (option.type == Type.SKILL && (!skills || TALENTS[option.target].requiredLevel() > depth)) continue;
            eligible.add(option);
        }
        float[] weights = new float[TYPES.length];
        for (EquipmentAttribute option : eligible) weights[option.type.ordinal()] = weight(option.type);
        item.attributes = new EquipmentAttribute[count];
        for (int i = 0; i < count; i++) {
            Type type = TYPES[Random.chances(weights)];
            ArrayList<EquipmentAttribute> targets = new ArrayList<>();
            for (EquipmentAttribute option : eligible) if (option.type == type) targets.add(option);
            EquipmentAttribute chosen = targets.get(Random.Int(targets.size()));
            int progress = Math.min(24, depth - 1);
            int upper = type.min + ((type.max - type.min) * progress + 23) / 24;
            item.attributes[i] = new EquipmentAttribute(type, Random.IntRange(type.min, upper), chosen.target);
        }
    }

    private static int weight(Type type) {
        switch (type) {
            case CLASS_SKILLS: return 2;
            case TREE_SKILLS: return 4;
            case SKILL: return 8;
            default: return 12;
        }
    }

    public static synchronized List<EquipmentAttribute> options(Item item) {
        if (!supported(item)) return Collections.emptyList();
        List<EquipmentAttribute> cached = choices.get(item.getClass());
        if (cached != null) return cached;
        ArrayList<Type> pool = new ArrayList<>();
        pool.add(Type.HEALTH);
        pool.add(Type.MANA);
        pool.add(Type.ACCURACY);
        pool.add(Type.EVASION);
        if (item instanceof Armor || item instanceof Ring) pool.add(Type.ARMOR);
        if (item instanceof Weapon && !(item instanceof Bow) && !(item instanceof Gun) || item instanceof Ring) pool.add(Type.MELEE);
        if (item instanceof Bow || item instanceof Gun || item instanceof Ring) pool.add(Type.RANGED);
        if (item instanceof Wand || item instanceof Ring) pool.add(Type.WANDS);
        ArrayList<EquipmentAttribute> result = new ArrayList<>();
        for (Type type : pool) result.add(new EquipmentAttribute(type, type.min, 0));
        EnumSet<HeroClass> classes = EnumSet.noneOf(HeroClass.class);
        EnumSet<HeroSubClass> branches = EnumSet.noneOf(HeroSubClass.class);
        for (Talent talent : TALENTS) {
            if (!talent.available() || talent.maxRank() == 1 || !suits(item, talent)) continue;
            result.add(new EquipmentAttribute(Type.SKILL, Type.SKILL.min, talent.ordinal()));
            classes.add(talent.heroClass);
            if (talent.subclass != HeroSubClass.NONE) branches.add(talent.subclass);
        }
        if (!(item instanceof Armor)) {
            for (HeroSubClass branch : branches) result.add(new EquipmentAttribute(Type.TREE_SKILLS, Type.TREE_SKILLS.min, branch.ordinal()));
            for (HeroClass hero : classes) result.add(new EquipmentAttribute(Type.CLASS_SKILLS, Type.CLASS_SKILLS.min, hero.ordinal()));
        }
        cached = Collections.unmodifiableList(result);
        choices.put(item.getClass(), cached);
        return cached;
    }

    private static boolean suits(Item item, Talent talent) {
        if (item instanceof Armor || item instanceof Ring) {
            switch (talent.effect) {
                case HEALTH: case MANA: case ARMOR: case EVASION: case TENACITY: case STEALTH:
                case RESISTANCE: case POISON_RESISTANCE: case REGENERATION: case MANA_REGENERATION:
                    return true;
                default: return false;
            }
        }
        if (item instanceof IceWeapon) return talent.heroClass == ((IceWeapon)item).heroClass();
        if (item instanceof Bow) return talent.heroClass == HeroClass.HUNTRESS;
        if (item instanceof Gun) return talent.heroClass == HeroClass.MERCENARY;
        if (item instanceof NecroBlade) return talent.heroClass == HeroClass.NECROMANCER;
        if (item instanceof SoulBlade) return talent.heroClass == HeroClass.MAGE || talent.heroClass == HeroClass.NECROMANCER;
        if (item instanceof Wand) return talent.heroClass == HeroClass.MAGE || talent.heroClass == HeroClass.NECROMANCER;
        return talent.heroClass == HeroClass.WARRIOR || talent.heroClass == HeroClass.ROGUE;
    }

    public static int bonus(Hero hero, Type type) { return bonus(hero, type, -1); }

    public static int bonus(Hero hero, Type type, int target) {
        if (hero == null || !Dungeon.rules().hasRebornEncounters()) return 0;
        return total(hero, type, target, false);
    }

    public static int previewBonus(Hero hero, Type type) {
        return total(hero, type, -1, true);
    }

    private static int total(Hero hero, Type type, int target, boolean preview) {
        int total = value(hero.belongings.weapon, type, target, preview) + value(hero.belongings.armor, type, target, preview)
                + value(hero.belongings.bow, type, target, preview) + value(hero.belongings.ring1, type, target, preview)
                + value(hero.belongings.ring2, type, target, preview);
        return Math.min(type.cap, total);
    }

    private static int value(Item item, Type type, int target, boolean preview) {
        if (item == null || item.attributes.length == 0 || !preview && !item.isIdentified()) return 0;
        int value = 0;
        for (EquipmentAttribute attribute : item.attributes) {
            if (attribute.type == type && (target < 0 || target == attribute.target)) value += attribute.amount;
        }
        return value;
    }

    public static String describe(Item item) {
        return describe(item, item.isIdentified());
    }

    public static String describe(Item item, boolean identified) {
        if (item.attributes.length == 0) return "";
        StringBuilder text = new StringBuilder();
        for (EquipmentAttribute attribute : item.attributes) {
            text.append('\n');
            if (identified) text.append('_').append(attribute.description()).append('_');
            else text.append(Messages.get("attributes.unidentified"));
        }
        return text.toString();
    }

    public static int[] pack(EquipmentAttribute[] attributes) {
        int[] data = new int[attributes.length * 3];
        for (int i = 0; i < attributes.length; i++) {
            data[i * 3] = attributes[i].type.ordinal();
            data[i * 3 + 1] = attributes[i].amount;
            data[i * 3 + 2] = attributes[i].target;
        }
        return data;
    }

    public static boolean same(Item first, Item second) {
        if (first.attributes.length != second.attributes.length) return false;
        for (int i = 0; i < first.attributes.length; i++) {
            EquipmentAttribute a = first.attributes[i], b = second.attributes[i];
            if (a.type != b.type || a.amount != b.amount || a.target != b.target) return false;
        }
        return true;
    }

    public static boolean valid(int[] data) {
        if (data == null || data.length > 9 || data.length % 3 != 0) return false;
        for (int i = 0; i < data.length; i += 3) {
            if (data[i] < 0 || data[i] >= TYPES.length) return false;
            Type type = TYPES[data[i]];
            if (data[i + 1] < type.min || data[i + 1] > type.max) return false;
            int target = data[i + 2];
            if (type == Type.SKILL) {
                if (target < 0 || target >= TALENTS.length) return false;
            } else if (type == Type.TREE_SKILLS) {
                if (target <= 0 || target >= HeroSubClass.values().length) return false;
            } else if (type == Type.CLASS_SKILLS) {
                if (target < 0 || target >= HeroClass.values().length) return false;
                HeroClass hero = HeroClass.values()[target];
                if (hero != HeroClass.WARRIOR && hero != HeroClass.MAGE && hero != HeroClass.ROGUE
                        && hero != HeroClass.HUNTRESS && hero != HeroClass.NECROMANCER && hero != HeroClass.MERCENARY) return false;
            } else if (target != 0) return false;
        }
        return true;
    }

    public static EquipmentAttribute[] unpack(int[] data) {
        if (!valid(data)) return NONE;
        EquipmentAttribute[] attributes = new EquipmentAttribute[data.length / 3];
        for (int i = 0; i < attributes.length; i++)
            attributes[i] = new EquipmentAttribute(TYPES[data[i * 3]], data[i * 3 + 1], data[i * 3 + 2]);
        return attributes;
    }
}
