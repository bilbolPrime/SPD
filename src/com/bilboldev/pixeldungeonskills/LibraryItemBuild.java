package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.*;
import com.bilboldev.pixeldungeonskills.items.armor.glyphs.*;
import com.bilboldev.pixeldungeonskills.items.rings.*;
import com.bilboldev.pixeldungeonskills.items.wands.*;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.WeaponEffects.WeaponEffect;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.*;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Boomerang;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class LibraryItemBuild {
    public enum Property {
        BASE_ARMOR, PREFIX, ENCHANTMENT, GLYPH, BALANCE;

        public String title() {
            return text(name().toLowerCase(Locale.ROOT));
        }
    }

    private enum EnchantmentChoice {
        FIRE(Fire::new), POISON(Poison::new), DEATH(Death::new), PARALYSIS(Paralysis::new),
        LEECH(Leech::new), SLOW(Slow::new), SHOCK(Shock::new), INSTABILITY(Instability::new),
        HORROR(Horror::new), LUCK(Luck::new), TEMPERING(Tempering::new);

        final Supplier<Weapon.Enchantment> create;
        final Class<? extends Weapon.Enchantment> type;

        EnchantmentChoice(Supplier<Weapon.Enchantment> create) {
            this.create = create;
            type = create.get().getClass();
        }

        String key() {
            return "enchantment_" + name().toLowerCase(Locale.ROOT);
        }
    }

    private enum GlyphChoice {
        BOUNCE(Bounce::new), AFFECTION(Affection::new), ANTI_ENTROPY(AntiEntropy::new),
        MULTIPLICITY(Multiplicity::new), POTENTIAL(Potential::new), METABOLISM(Metabolism::new),
        STENCH(Stench::new), VISCOSITY(Viscosity::new), DISPLACEMENT(Displacement::new),
        ENTANGLEMENT(Entanglement::new), AUTO_REPAIR(AutoRepair::new);

        final Supplier<Armor.Glyph> create;
        final Class<? extends Armor.Glyph> type;

        GlyphChoice(Supplier<Armor.Glyph> create) {
            this.create = create;
            type = create.get().getClass();
        }

        String key() {
            return "glyph_" + name().toLowerCase(Locale.ROOT);
        }
    }

    public final Entry entry;
    private final Item original;
    private final List<Property> properties;
    private final EnumMap<Property, Integer> choices = new EnumMap<>(Property.class);
    private final String[] weaponPrefixes;
    private int level;
    private boolean cursed;

    public LibraryItemBuild(Entry entry) {
        if (entry == null || !entry.customizable()) throw new IllegalArgumentException("Choose equipment");
        this.entry = entry;
        original = entry.newItem();
        level = original instanceof Ring && !original.isUpgradable() ? 1 : original.level();
        cursed = original.cursed;
        ArrayList<Property> available = new ArrayList<>();
        if (original instanceof ClassArmor) available.add(Property.BASE_ARMOR);
        if (original instanceof Armor || WeaponEffect.supports(original)) available.add(Property.PREFIX);
        if (original instanceof MeleeWeapon || original instanceof Boomerang) {
            if (!(original instanceof IceWeapon)) available.add(Property.ENCHANTMENT);
            available.add(Property.BALANCE);
        }
        if (original instanceof Armor) available.add(Property.GLYPH);
        properties = Collections.unmodifiableList(available);
        for (Property property : available) choices.put(property, 0);
        if (original instanceof ClassArmor) {
            int base = 4;
            while (base > 0 && !Lore.known(LibraryCatalog.find(baseArmor(base)))) base--;
            choices.put(Property.BASE_ARMOR, base);
        }
        if (!WeaponEffect.supports(original)) weaponPrefixes = new String[0];
        else if (original instanceof MeleeWeapon) weaponPrefixes = new String[]{"Smite", "KnockBack", "Venom"};
        else if (original instanceof Bow) weaponPrefixes = new String[]{"Venom", "KneeShot"};
        else weaponPrefixes = new String[]{"Spark", "Curse", "DrainLife"};
        if (original.weaponEffect != null && Lore.knownModifier("weapon_prefix:" + original.weaponEffect.Name)) {
            for (int i = 0; i < weaponPrefixes.length; i++) {
                if (original.weaponEffect.Name.equals(weaponPrefixes[i])) choices.put(Property.PREFIX, i + 1);
            }
        }
    }

    public boolean canUpgrade() {
        return original.isUpgradable() || original instanceof ClassArmor || original instanceof Ring;
    }

    public int minimumLevel() {
        if (!canUpgrade()) return level;
        int minimum = original instanceof Wand ? -Math.min(2, ((Wand)original).maxCharges) : -10;
        if (original instanceof WandOfDisintegration) minimum = -1;
        if (original instanceof Bow) minimum = -5;
        while (minimum < 0) {
            Item candidate = item(minimum);
            if (candidate instanceof Weapon) {
                Weapon weapon = (Weapon)candidate;
                if (weapon.min() >= 0 && weapon.max() >= weapon.min()) break;
            } else if (!(candidate instanceof Armor) || ((Armor)candidate).DR() >= 0) {
                break;
            }
            minimum++;
        }
        return minimum;
    }

    public int maximumLevel() {
        return canUpgrade() ? 30 : level;
    }

    public int level() {
        return level;
    }

    public void level(int value) {
        if (value < minimumLevel() || value > maximumLevel()) throw new IllegalArgumentException("Invalid equipment level");
        level = value;
    }

    public boolean canCurse() {
        return true;
    }

    public boolean cursed() {
        return cursed;
    }

    public void cursed(boolean value) {
        cursed = value;
    }

    public List<Property> properties() {
        return properties;
    }

    public int choice(Property property) {
        Integer value = choices.get(property);
        if (value == null) throw new IllegalArgumentException("This item does not support " + property);
        return value;
    }

    public void choose(Property property, int index) {
        choice(property);
        if (index < 0 || index >= options(property).length) throw new IllegalArgumentException("Invalid equipment option");
        choices.put(property, index);
        level = Math.max(level, minimumLevel());
    }

    public String[] options(Property property) {
        choice(property);
        ArrayList<String> options = new ArrayList<>();
        switch (property) {
            case BASE_ARMOR:
                for (int tier = 0; tier < 5; tier++) options.add(Messages.capitalize(baseArmor(tier).trueName()));
                break;
            case PREFIX:
                options.add(text("none"));
                if (original instanceof Armor) {
                    for (ArmorPrefix prefix : ArmorPrefix.values()) options.add(prefix.activationName());
                } else {
                    for (String prefix : weaponPrefixes) options.add(WeaponEffect.restore(prefix).prefix());
                }
                break;
            case ENCHANTMENT:
                options.add(text("none"));
                for (EnchantmentChoice enchantment : EnchantmentChoice.values()) options.add(text(enchantment.key()));
                break;
            case GLYPH:
                options.add(text("none"));
                for (GlyphChoice glyph : GlyphChoice.values()) options.add(text(glyph.key()));
                break;
            case BALANCE:
                options.add(text("balance_none"));
                options.add(text("balance_speed"));
                options.add(text("balance_accuracy"));
                break;
        }
        return options.toArray(new String[0]);
    }

    public List<Integer> knownOptions(Property property) {
        ArrayList<Integer> result = new ArrayList<>();
        int count = options(property).length;
        for (int i = 0; i < count; i++) {
            boolean known = property == Property.BASE_ARMOR ? Lore.known(LibraryCatalog.find(baseArmor(i)))
                    : Lore.knownModifier(modifierKey(property, i));
            if (known) result.add(i);
        }
        return result;
    }

    private String modifierKey(Property property, int index) {
        if (index == 0) return null;
        switch (property) {
            case PREFIX:
                return original instanceof Armor ? "armor_prefix:" + ArmorPrefix.values()[index - 1].id
                        : "weapon_prefix:" + weaponPrefixes[index - 1];
            case ENCHANTMENT: return EnchantmentChoice.values()[index - 1].key();
            case GLYPH: return GlyphChoice.values()[index - 1].key();
            default: return null;
        }
    }

    public static List<String> modifierKeys(Item item) {
        ArrayList<String> result = new ArrayList<>();
        if (item.weaponEffect != null) result.add("weapon_prefix:" + item.weaponEffect.Name);
        if (item instanceof Weapon && ((Weapon)item).isEnchanted()) {
            Class<?> type = ((Weapon)item).enchantment().getClass();
            for (EnchantmentChoice choice : EnchantmentChoice.values()) {
                if (choice.type == type) result.add(choice.key());
            }
        }
        if (item instanceof Armor) {
            Armor armor = (Armor)item;
            if (armor.prefix != null) result.add("armor_prefix:" + armor.prefix.id);
            if (armor.glyph != null) {
                for (GlyphChoice choice : GlyphChoice.values()) {
                    if (choice.type == armor.glyph.getClass()) result.add(choice.key());
                }
            }
        }
        return result;
    }

    static List<String> modifiers() {
        ArrayList<String> keys = new ArrayList<>();
        for (EnchantmentChoice choice : EnchantmentChoice.values()) keys.add(choice.key());
        for (GlyphChoice choice : GlyphChoice.values()) keys.add(choice.key());
        for (ArmorPrefix prefix : ArmorPrefix.values()) keys.add("armor_prefix:" + prefix.id);
        for (String prefix : new String[]{"Smite", "KnockBack", "Venom", "KneeShot", "Spark", "Curse", "DrainLife"}) {
            keys.add("weapon_prefix:" + prefix);
        }
        return keys;
    }

    static String modifierName(String key) {
        if (key.startsWith("weapon_prefix:")) return WeaponEffect.restore(key.substring(14)).prefix();
        if (key.startsWith("armor_prefix:")) return ArmorPrefix.restore(key.substring(13)).activationName();
        return text(key);
    }

    public ItemSprite image() {
        Item item = item();
        return new ItemSprite(ItemSpriteSheet.identifiedImage(item.getClass(), item.image()), item.glowing());
    }

    public Item item() {
        return item(level);
    }

    public Item optionItem(Property property, int index) {
        LibraryItemBuild option = new LibraryItemBuild(entry);
        option.level = level;
        option.cursed = cursed;
        option.choices.putAll(choices);
        option.choose(property, index);
        if (property == Property.BASE_ARMOR) return baseArmor(index);
        return option.item();
    }

    private Item item(int level) {
        Item item = original instanceof ClassArmor ? baseArmor(choice(Property.BASE_ARMOR)) : entry.newItem();
        item.weaponEffect = null;
        if (item instanceof Weapon) ((Weapon)item).enchant(null);
        if (item instanceof Armor) ((Armor)item).inscribe(null);

        if (item instanceof MeleeWeapon || item instanceof Armor) {
            while (item.level() < level) item.upgrade();
            while (item.level() > level) item.degrade();
        } else {
            item.level(level);
        }
        item.fix();
        if (item instanceof Wand) {
            Wand wand = (Wand)item;
            wand.updateLevel();
            wand.curCharges = wand.maxCharges;
        }
        if (choices.containsKey(Property.PREFIX) && choice(Property.PREFIX) > 0) {
            int index = choice(Property.PREFIX) - 1;
            if (item instanceof Armor) ((Armor)item).prefix = ArmorPrefix.values()[index];
            else item.weaponEffect = WeaponEffect.restore(weaponPrefixes[index]);
        }
        if (choices.containsKey(Property.ENCHANTMENT) && choice(Property.ENCHANTMENT) > 0) {
            ((Weapon)item).enchant(EnchantmentChoice.values()[choice(Property.ENCHANTMENT) - 1].create.get());
        }
        if (choices.containsKey(Property.BALANCE)) ((Weapon)item).imbue = Weapon.Imbue.values()[choice(Property.BALANCE)];
        if (choices.containsKey(Property.GLYPH) && choice(Property.GLYPH) > 0) {
            ((Armor)item).inscribe(GlyphChoice.values()[choice(Property.GLYPH) - 1].create.get());
        }
        if (original instanceof ClassArmor) item = ClassArmor.upgrade(armorClass((ClassArmor)original), (Armor)item);
        item.levelKnown = item.cursedKnown = true;
        item.cursed = cursed;
        return item;
    }

    public String name() {
        Item item = item();
        String name = item.prefixName(entry.name());
        if (level != 0) name = text("level_name", name, level);
        if (choices.containsKey(Property.ENCHANTMENT) && choice(Property.ENCHANTMENT) > 0) {
            name = text("modifier_name", name, options(Property.ENCHANTMENT)[choice(Property.ENCHANTMENT)]);
        }
        if (choices.containsKey(Property.GLYPH) && choice(Property.GLYPH) > 0) {
            name = text("modifier_name", name, options(Property.GLYPH)[choice(Property.GLYPH)]);
        }
        return Messages.capitalize(name);
    }

    public String info() {
        Item item = item();
        String info = entry.info(item);
        if (original instanceof ClassArmor) {
            info += "\n\n" + text("class_armor_source", options(Property.BASE_ARMOR)[choice(Property.BASE_ARMOR)], level);
        }
        if (item instanceof Wand) info += "\n\n" + wandStats((Wand)item);
        if (choices.containsKey(Property.ENCHANTMENT) && choice(Property.ENCHANTMENT) > 0) {
            EnchantmentChoice enchantment = EnchantmentChoice.values()[choice(Property.ENCHANTMENT) - 1];
            info += "\n\n_" + text(enchantment.key()) + "_\n" + text(enchantment.key() + "_desc");
        }
        if (choices.containsKey(Property.GLYPH) && choice(Property.GLYPH) > 0) {
            GlyphChoice glyph = GlyphChoice.values()[choice(Property.GLYPH) - 1];
            info += "\n\n_" + text(glyph.key()) + "_\n" + text(glyph.key() + "_desc");
        }
        return info;
    }

    private static String wandStats(Wand wand) {
        String result = text("wand_charges", wand.maxCharges);
        int level = wand.effectiveLevel();
        int minimum;
        int maximum;
        String condition;
        if (wand instanceof WandOfMagicMissile) {
            minimum = 1;
            maximum = 5 + level * 2;
            condition = "wand_zap_base";
        } else if (wand instanceof WandOfFirebolt) {
            minimum = 1;
            maximum = 7 + level * level;
            condition = "wand_zap_fire";
        } else if (wand instanceof WandOfLightning) {
            minimum = 5 + level / 2;
            maximum = 9 + level;
            condition = "wand_zap_lightning";
        } else if (wand instanceof WandOfDisintegration) {
            int power = level + 1;
            minimum = power;
            maximum = 8 + power * power / 3;
            condition = "wand_zap_disintegration";
        } else if (wand instanceof WandOfAvalanche) {
            minimum = 2;
            maximum = 5 + (1 + level / 3) * 2;
            condition = "wand_zap_avalanche";
        } else {
            return result;
        }
        return result + "\n\n" + text("wand_zap_damage", minimum, maximum) + "\n" + text(condition);
    }

    private static Armor baseArmor(int choice) {
        switch (choice) {
            case 0: return new ClothArmor();
            case 1: return new LeatherArmor();
            case 2: return new MailArmor();
            case 3: return new ScaleArmor();
            case 4: return new PlateArmor();
            default: throw new IllegalArgumentException("Unknown base armor");
        }
    }

    private static HeroClass armorClass(ClassArmor armor) {
        if (armor instanceof WarriorArmor) return HeroClass.WARRIOR;
        if (armor instanceof MageArmor) return HeroClass.MAGE;
        if (armor instanceof RogueArmor) return HeroClass.ROGUE;
        if (armor instanceof HuntressArmor) return HeroClass.HUNTRESS;
        if (armor instanceof NecromancerArmor) return HeroClass.NECROMANCER;
        if (armor instanceof MercenaryArmor) return HeroClass.MERCENARY;
        throw new IllegalArgumentException("Unknown class armor");
    }

    private static String text(String key, Object... args) {
        return LibraryCatalog.text(key, args);
    }
}
