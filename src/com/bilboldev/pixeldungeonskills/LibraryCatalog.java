package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.noosa.TextureFilm;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.IceWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.GlacierCleaver;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.HoarfrostStaff;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.RimePick;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.GravefrostScythe;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.FrostbiteRifle;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroSubClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MercenaryCamp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.BarbecueKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Bee;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Blacksmith;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.EchoReflection;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Ghost;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Imp;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.ImpShopkeeper;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.MirrorImage;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Prospector;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatKing;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.RatScout;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.ShadowClone;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Shopkeeper;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Wandmaker;
import com.bilboldev.pixeldungeonskills.actors.skills.Skill;
import com.bilboldev.pixeldungeonskills.actors.skills.skilltree.*;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.items.*;
import com.bilboldev.pixeldungeonskills.items.armor.*;
import com.bilboldev.pixeldungeonskills.items.bags.*;
import com.bilboldev.pixeldungeonskills.items.food.*;
import com.bilboldev.pixeldungeonskills.items.keys.GoldenKey;
import com.bilboldev.pixeldungeonskills.items.keys.IronKey;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.items.potions.*;
import com.bilboldev.pixeldungeonskills.items.quest.*;
import com.bilboldev.pixeldungeonskills.items.rings.*;
import com.bilboldev.pixeldungeonskills.items.scrolls.*;
import com.bilboldev.pixeldungeonskills.items.wands.*;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.*;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.*;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.*;
import com.bilboldev.pixeldungeonskills.plants.*;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.sprites.*;
import com.bilboldev.pixeldungeonskills.thetiles.DungeonTileSheet;
import com.bilboldev.pixeldungeonskills.ui.Icons;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class LibraryCatalog {
    public enum Section {
        DEPTHS, QUESTS, ITEMS, CREATURES, HEROES, SKILLS, RECIPES;

        public Image image() {
            switch (this) {
                case DEPTHS: return stairs(Assets.TILES_SEWERS_3D);
                case QUESTS: return new ItemSprite(ItemSpriteSheet.SCROLL_KAUNAN, null);
                case ITEMS: return new ItemSprite(ItemSpriteSheet.SHORT_SWORD, null);
                case CREATURES: return idleImage(new RatSprite());
                case HEROES: return heroImage(Assets.WARRIOR, 0);
                case SKILLS: return Icons.SKILLS.get();
                case RECIPES: return new ItemSprite(ItemSpriteSheet.RECIPE_HEALING, null);
                default: throw new AssertionError(this);
            }
        }
    }

    private enum Region { SEWERS, PRISON, CAVES, ICE_CAVES, CITY, HALLS }

    public enum Shelf {
        DEPTHS(Section.DEPTHS), QUESTS(Section.QUESTS),
        WEAPONS(Section.ITEMS), ARMOR(Section.ITEMS), RINGS(Section.ITEMS),
        WANDS(Section.ITEMS), POTIONS(Section.ITEMS), SCROLLS(Section.ITEMS),
        FOOD(Section.ITEMS), SEEDS(Section.ITEMS), MISC(Section.ITEMS),
        ENEMIES(Section.CREATURES), BOSSES(Section.CREATURES), RIVALS(Section.CREATURES), PEOPLE(Section.CREATURES),
        CLASSES(Section.HEROES), ABILITIES(Section.SKILLS), RECIPES(Section.RECIPES);

        public final Section section;

        Shelf(Section section) {
            this.section = section;
        }

        public String title() {
            return text(name().toLowerCase(Locale.ROOT));
        }
    }

    public static final class Entry {
        public final String key;
        public final Shelf shelf;
        private Class<?> type;
        private Region region;
        private int depth, tier;
        private String rewardKey;
        public final boolean countsForIdentification;
        public final HeroClass heroClass;
        private final Supplier<String> name, description;
        private final Supplier<Image> image;
        private final Supplier<? extends Item> equipment;
        private final Supplier<LibraryCreatureStats> stats;

        boolean knownAtStart() {
            if (shelf == Shelf.DEPTHS) return region == Region.SEWERS;

            return type == Bag.class || type == ClothArmor.class || type == Food.class
                    || type == Keyring.class || type == PotionHolder.class || type == Bow.class
                    || type == Arrow.class || type == ShortSword.class || type == Knuckles.class
                    || type == Dagger.class || type == Handgun.class || type == Boomerang.class
                    || type == RingOfShadows.class || type == WandOfMagicMissile.class
                    || type == PotionOfMana.class || type == PotionOfHealing.class
                    || type == PotionOfStrength.class || type == PotionOfExperience.class
                    || type == SoulCrystal.class || type == ScrollOfIdentify.class
                    || type == ScrollOfMagicMapping.class || type == ScrollOfHome.class
                    || type == ScrollOfSacrifice.class || type == ScrollOfBloodyRitual.class
                    || type == ScrollOfSkill.class || type == ScrollOfReadiness.class || type == ScrollOfFrostLevel.class;
        }

        private Entry(String key, Shelf shelf, Supplier<String> name, Supplier<String> description,
                      Supplier<Image> image, boolean countsForIdentification, Supplier<? extends Item> equipment,
                      HeroClass heroClass, Supplier<LibraryCreatureStats> stats) {
            this.key = key;
            this.shelf = shelf;
            this.name = name;
            this.description = description;
            this.image = image;
            this.countsForIdentification = countsForIdentification;
            this.equipment = equipment;
            this.heroClass = heroClass;
            this.stats = stats;
        }

        public LibraryCreatureStats creatureStats() {
            return stats == null ? null : stats.get();
        }

        public LibraryCreatureStats creatureStats(int depth) {
            return shelf == Shelf.RIVALS ? LibraryCreatureStats.rival(rivalClass(), depth) : creatureStats();
        }

        public HeroClass rivalClass() {
            return HeroClass.valueOf(key.substring("rival.".length()));
        }

        public String id() {
            return shelf.name() + ":" + key;
        }

        public String name() {
            return Messages.capitalize(name.get());
        }

        public String description() {
            return description.get();
        }

        public String reward() {
            return rewardKey == null ? "" : Messages.get(rewardKey);
        }

        public String info() {
            if (equipment == null) return description();
            Item item = newItem();
            if (item.weaponEffect != null && !Lore.knownModifier("weapon_prefix:" + item.weaponEffect.Name)) {
                item.weaponEffect = null;
            }
            if (item instanceof ClassArmor) return description() + "\n\n" + text("class_armor_stats");
            return info(item);
        }

        public boolean customizable() {
            return equipment != null;
        }

        public Item newItem() {
            if (equipment == null) throw new IllegalStateException("This entry has no equipment");
            Item item = equipment.get();
            if (item instanceof Ring && !item.isUpgradable()) item.level(1);
            item.levelKnown = item.cursedKnown = true;
            return item;
        }

        public String info(Item item) {
            if (item instanceof Ring) {
                String result = description() + "\n\n" + text("ring_level", item.level())
                        + "\n\n" + ringStats((Ring)item);
                if (item.cursed) result += "\n\n" + Messages.get("items.rings.ring.curse_known");
                return result;
            }
            String details = item.info(null);
            String result = item instanceof IceWeapon ? details : description() + details.substring(item.desc().length());
            if (item instanceof Bow) {
                result += "\n\n" + text("bow_stats", ((Bow)item).damageBonus());
            } else if (item instanceof Pickaxe) {
                Weapon weapon = (Weapon)item;
                result += "\n\n" + text("weapon_stats", weapon.min(), weapon.max(), weapon.STR());
            } else if (item instanceof MissileWeapon) {
                result += "\n\n" + text("strength", ((Weapon)item).STR);
            }
            if (item instanceof Gun) result += "\n\n" + text("gun_modifiers");
            return result;
        }

        public Image image() {
            return image.get();
        }

        public Image plantImage() {
            Plant plant;
            if (type == Dreamweed.Seed.class) plant = new Dreamweed();
            else if (type == Earthroot.Seed.class) plant = new Earthroot();
            else if (type == Fadeleaf.Seed.class) plant = new Fadeleaf();
            else if (type == Firebloom.Seed.class) plant = new Firebloom();
            else if (type == Icecap.Seed.class) plant = new Icecap();
            else if (type == Rotberry.Seed.class) plant = new Rotberry();
            else if (type == Sorrowmoss.Seed.class) plant = new Sorrowmoss();
            else if (type == Sungrass.Seed.class) plant = new Sungrass();
            else return null;
            PlantSprite sprite = new PlantSprite(plant.image);
            sprite.origin.set(0);
            return sprite;
        }

        public Image image(int depth) {
            if (shelf != Shelf.RIVALS) return image();
            Image portrait = heroImage(rivalClass().spritesheet(), depth / 5 + 1);
            portrait.tint(0xB52B32, .38f);
            return portrait;
        }
    }

    private static final List<Entry> entries = createEntries();

    private LibraryCatalog() { }

    public static List<Entry> entries() {
        return entries;
    }

    public static void sort(List<Entry> list) {
        list.sort(Comparator.comparingInt((Entry entry) -> entry.shelf == Shelf.QUESTS ? entry.depth
                        : entry.region == null ? Integer.MAX_VALUE : entry.region.ordinal())
                .thenComparingInt(entry -> entry.depth)
                .thenComparingInt(entry -> entry.tier)
                .thenComparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
    }

    public static Entry chapter(int depth) {
        if (depth < 1 || depth > 26) return null;
        Entry chapter = null;
        for (Entry entry : entries) {
            if (entry.shelf == Shelf.DEPTHS && entry.depth <= depth) chapter = entry;
        }
        return chapter;
    }

    public static SkillTree skillTree(HeroClass hero) {
        switch (hero) {
            case WARRIOR: return new WarriorSkillTree(0, 0).build();
            case MAGE: return new MageSkillTree(0, 0).build();
            case ROGUE: return new RogueSkillTree(0, 0).build();
            case HUNTRESS: return new HuntressSkillTree(0, 0).build();
            case NECROMANCER: return new NecromancerSkillTree(0, 0).build();
            case MERCENARY: return new MercenarySkillTree(0, 0).build();
            default: throw new IllegalArgumentException("Choose a playable hero class");
        }
    }

    public static Entry find(Item item) {
        Class<?> type = item.getClass();
        while (type.isAnonymousClass()) type = type.getSuperclass();
        return findType(type);
    }

    public static Entry find(Mob mob) {
        if (mob instanceof EchoDuel) return findType(EchoReflection.class);
        if (mob instanceof RivalBoss) return find(Shelf.RIVALS, "rival." + ((RivalBoss)mob).heroClass().name());
        if (mob instanceof HiredMerc) return find(Shelf.PEOPLE, "mercenary." + ((HiredMerc)mob).mercType.name());
        if (mob instanceof MercenaryCamp) return find(Shelf.PEOPLE, "mercenary." + ((MercenaryCamp)mob).offer().name());
        if (mob instanceof RebornKing.Retainer) {
            return find(Shelf.ENEMIES, "actors.mobs.rebornking$retainer.name_" + ((RebornKing.Retainer)mob).role);
        }
        if (mob instanceof RivalServant) {
            return find(Shelf.ENEMIES, "actors.mobs.rivalservant." + (mob.spriteClass == RivalSprite.class ? "clone" : "name"));
        }
        if (mob instanceof SummonedPet) {
            switch (((SummonedPet)mob).petType) {
                case RAT: return findType(com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Rat.class);
                case CRAB: return findType(com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Crab.class);
                case SKELETON: return findType(com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Skeleton.class);
                case SKELETON_ARCHER: return find(Shelf.PEOPLE, "actors.mobs.npcs.summonedpet.skeleton_archer");
                case FIRE_ELEMENTAL: return find(Shelf.PEOPLE, "actors.mobs.npcs.summonedpet.fire_elemental");
                default: return null;
            }
        }
        if (mob instanceof RebornGoo) return findType(Goo.class);
        if (mob instanceof RebornTengu) return findType(Tengu.class);
        if (mob instanceof RebornDM300) return findType(DM300.class);
        if (mob instanceof RebornKing) return findType(King.class);
        if (mob instanceof RebornYog) return findType(Yog.class);
        if (mob instanceof RebornYog.Rotting) return findType(Yog.RottingFist.class);
        if (mob instanceof RebornYog.BurningHand) return findType(Yog.BurningFist.class);
        if (mob instanceof RebornYog.Brood) return findType(Yog.Larva.class);
        return findType(mob.getClass());
    }

    static Entry findType(Class<?> type) {
        for (Entry entry : entries) {
            if (entry.type == type) return entry;
        }
        return null;
    }

    public static Entry find(Shelf shelf, String key) {
        for (Entry entry : entries) {
            if (entry.shelf == shelf && entry.key.equals(key)) return entry;
        }
        return null;
    }

    public static Entry item(String key) {
        for (Entry entry : entries) {
            if (entry.shelf.section == Section.ITEMS && entry.key.equals(key)) return entry;
        }
        throw new IllegalArgumentException("Unknown Lore item: " + key);
    }

    public static Entry recipe(AlchemyRecipe recipe) {
        return find(Shelf.RECIPES, recipe.name());
    }

    static Entry retiredSeedSymbol(String id) {
        String key = id.substring(id.indexOf(':') + 1);
        Supplier<Image> image;
        switch (key) {
            case "custom.generated.entry_bomb_trigger_beacon_d48a0d00eb":
                image = () -> new ItemSprite(ItemSpriteSheet.BEACON, null); break;
            case "custom.generated.entry_remote_bomb_13fd5dc52e":
                image = () -> new ItemSprite(ItemSpriteSheet.RemoteBomb, null); break;
            case "items.weapon.missiles.bullet.name":
                image = () -> new ItemSprite(ItemSpriteSheet.BULLET, null); break;
            case "items.weapon.missiles.darts.dart.name":
                image = () -> new ItemSprite(ItemSpriteSheet.DART, null); break;
            case "custom.generated.entry_throw_spear_ab9f12188c":
                image = () -> new ItemSprite(ItemSpriteSheet.JOE_SPEAR, null); break;
            case "custom.generated.entry_marsupial_ranged_rat_2a05e3384d":
                image = () -> idleImage(new RatArcherSprite()); break;
            case "custom.generated.entry_marsupial_brute_rat_bdfe6b9bc3":
                image = () -> idleImage(new RatBruteSprite()); break;
            default: throw new IllegalArgumentException("Unknown seed symbol: " + id);
        }
        return new Entry(key, Shelf.valueOf(id.substring(0, id.indexOf(':'))),
                () -> Messages.get(key), () -> "", image, false, null, null, null);
    }

    public static String text(String key, Object... args) {
        return Messages.get("library." + key, args);
    }

    private static String ringStats(Ring ring) {
        int level = ring.effectiveLevel();
        if (ring instanceof RingOfAccuracy) return text("ring_accuracy", Math.pow(1.4, level));
        if (ring instanceof RingOfEvasion) return text("ring_evasion", Math.pow(1.2, level));
        if (ring instanceof RingOfHaste) return text("ring_haste", Math.pow(1.1, -level));
        if (ring instanceof RingOfShadows) return text("ring_shadows", level);
        if (ring instanceof RingOfMending) return text("ring_mending", 10 / Math.pow(1.2, level));
        if (ring instanceof RingOfSatiety) return text("ring_satiety", 10 - level);
        if (ring instanceof RingOfPower) return text("ring_power", level);
        if (ring instanceof RingOfDetection) return text("ring_detection", Math.max(1, 1 + level));
        if (ring instanceof RingOfElements) {
            double chance = level > 0 ? 100.0 * level / (level + 3) : 0;
            return text("ring_elements", chance, ((RingOfElements)ring).new Resistance().durationFactor());
        }
        if (ring instanceof RingOfHerbalism) {
            double seeds = 0;
            double dew = 0;
            for (int roll = 0; roll <= level; roll++) {
                seeds += Math.min(18, roll + 1);
                dew += Math.min(6, roll + 1);
            }
            return text("ring_herbalism", level < 0 ? 0 : 100 * seeds / (18 * (level + 1)),
                    level < 0 ? 0 : 100 * dew / (6 * (level + 1)));
        }
        if (ring instanceof RingOfHaggler) return text("ring_haggler");
        if (ring instanceof RingOfThorns) return text("ring_thorns");
        throw new IllegalArgumentException("Unknown Library ring");
    }

    private static void item(List<Entry> list, Shelf shelf, Class<? extends Item> type,
                             int image, String name, String description) {
        item(list, shelf, type, image, name, description, null);
    }

    private static void item(List<Entry> list, Shelf shelf, Class<? extends Item> type, int image, String name, String description,
                             Supplier<? extends Item> equipment) {
        int frame = ItemSpriteSheet.identifiedImage(type, image);
        Entry entry = new Entry(name, shelf, () -> Messages.get(name), () -> Messages.get(description),
                () -> new ItemSprite(frame, null), image < 0, equipment, null, null);
        entry.type = type;
        if (equipment != null && (Armor.class.isAssignableFrom(type) || MeleeWeapon.class.isAssignableFrom(type))) {
            Item item = equipment.get();
            entry.tier = item instanceof Armor ? ((Armor)item).tier : ((MeleeWeapon)item).tier;
        }
        list.add(entry);
    }

    private static Image stairs(String texture) {
        Image image = new Image(texture);
        image.frame(new TextureFilm(image.texture, 16, 16).get(DungeonTileSheet.EXIT));
        return image;
    }

    private static void chapter(List<Entry> list, Region region, int depth, String name, String texture) {
        String key = "journal.document.intros." + name;
        Entry entry = new Entry(key, Shelf.DEPTHS, () -> Messages.get(key + ".title"),
                () -> Messages.get(key + ".body"), () -> stairs(texture), false, null, null, null);
        entry.region = region;
        entry.depth = depth;
        list.add(entry);
    }

    private static void quest(List<Entry> list, Region region, int depth, String name, String giver, Supplier<Image> image) {
        String key = "library.quest." + name;
        Entry entry = new Entry(key + ".name", Shelf.QUESTS, () -> Messages.get(key + ".name"),
                () -> Messages.get(key + ".desc"), image, false, null, null, null);
        entry.region = region;
        entry.depth = depth;
        entry.rewardKey = "library.quest." + giver + ".reward";
        list.add(entry);
    }

    private static MovieClip idleImage(CharSprite sprite) {
        MovieClip image = new MovieClip(sprite.texture);
        image.play(sprite.idleAnimation());
        image.rm = sprite.rm;
        image.gm = sprite.gm;
        image.bm = sprite.bm;
        image.am = sprite.am;
        image.ra = sprite.ra;
        image.ga = sprite.ga;
        image.ba = sprite.ba;
        image.aa = sprite.aa;
        sprite.destroy();
        return image;
    }

    private static MovieClip sheepImage() {
        MovieClip image = new MovieClip(Assets.SHEEP);
        image.play(SheepSprite.idleAnimation(new TextureFilm(image.texture, 16, 15)));
        return image;
    }

    public static MovieClip heroImage(String texture, int tier) {
        MovieClip image = new MovieClip(texture);
        TextureFilm tiers = new TextureFilm(image.texture, image.texture.width, 15);
        TextureFilm frames = new TextureFilm(tiers, tier, 12, 15);
        image.play(new MovieClip.Animation(1, true).frames(frames, 0, 0, 0, 1, 0, 0, 1, 1));
        return image;
    }

    private static void creature(List<Entry> list, Shelf shelf, Class<? extends Mob> type, String name, String description,
                                 Supplier<Image> image) {
        creature(list, shelf, null, 0, type, name, description, image);
    }

    private static void creature(List<Entry> list, Shelf shelf, Region region, int depth, Class<? extends Mob> type,
                                 String name, String description, Supplier<Image> image) {
        Entry entry = new Entry(name, shelf, () -> Messages.get(name), () -> Messages.get(description), image, false,
                null, null, () -> LibraryCreatureStats.creature(name, description));
        entry.type = type;
        entry.region = region;
        entry.depth = depth;
        list.add(entry);
    }

    private static void hero(List<Entry> list, HeroClass hero, HeroSubClass first, HeroSubClass second) {
        list.add(new Entry("hero." + hero.name(), Shelf.CLASSES, () -> Messages.maybeTranslate(hero.title()), () -> {
            StringBuilder description = new StringBuilder();
            for (String perk : hero.perks()) {
                if (description.length() > 0) description.append("\n\n");
                description.append(Messages.maybeTranslate(perk));
            }
            for (HeroSubClass sub : new HeroSubClass[]{first, second}) {
                description.append("\n\n_").append(Messages.capitalize(Messages.maybeTranslate(sub.title())))
                        .append("_\n").append(sub.desc());
            }
            return description.toString();
        }, () -> heroImage(hero.spritesheet(), 6), false, null, hero, null));
        list.add(new Entry("rival." + hero.name(), Shelf.RIVALS,
                () -> Messages.get("actors.mobs.rivalboss.name", (Object)Messages.maybeTranslate(hero.title())),
                () -> text("rival_desc"), () -> {
            Image image = heroImage(hero.spritesheet(), 2);
            image.tint(0xB52B32, .38f);
            return image;
        }, false, null, null, () -> LibraryCreatureStats.rival(hero, 5)));
    }

    private static void skills(List<Entry> list, HeroClass hero, SkillTree tree) {
        for (SkillContainer container : tree.getSkills()) {
            Skill skill = container.skill;
            list.add(new Entry("skill." + hero.name() + "." + skill.image, Shelf.ABILITIES,
                    () -> Messages.maybeTranslate(skill.name), () -> {
                StringBuilder info = new StringBuilder(text("skill_owner",
                        Messages.capitalize(Messages.maybeTranslate(hero.title()))));
                info.append('\n');
                for (String line : skill.info().split("\\n", -1)) {
                    info.append('\n').append(Messages.maybeTranslate(line));
                }
                return info.toString();
            }, () -> new SkillSprite(skill.image), false, null, null, null));
        }
    }

    private static List<Entry> createEntries() {
        List<Entry> list = new ArrayList<>();
        chapter(list, Region.SEWERS, 1, "sewers", Assets.TILES_SEWERS_3D);
        chapter(list, Region.PRISON, 6, "prison", Assets.TILES_PRISON_3D);
        chapter(list, Region.CAVES, 11, "caves", Assets.TILES_CAVES_3D);
        chapter(list, Region.CITY, 16, "city", Assets.TILES_CITY_3D);
        chapter(list, Region.HALLS, 22, "halls", Assets.TILES_HALLS_3D);
        quest(list, Region.SEWERS, 2, "ghost_rose", "ghost", () -> idleImage(new GhostSprite()));
        quest(list, Region.SEWERS, 2, "ghost_rat", "ghost", () -> idleImage(new GhostSprite()));
        quest(list, Region.SEWERS, 2, "ghost_curse", "ghost", () -> idleImage(new GhostSprite()));
        quest(list, Region.PRISON, 7, "wandmaker_berry", "wandmaker", () -> idleImage(new WandmakerSprite()));
        quest(list, Region.PRISON, 7, "wandmaker_dust", "wandmaker", () -> idleImage(new WandmakerSprite()));
        quest(list, Region.PRISON, 7, "wandmaker_fish", "wandmaker", () -> idleImage(new WandmakerSprite()));
        quest(list, Region.CAVES, 12, "blacksmith_gold", "blacksmith", () -> idleImage(new BlacksmithSprite()));
        quest(list, Region.CAVES, 12, "blacksmith_blood", "blacksmith", () -> idleImage(new BlacksmithSprite()));
        quest(list, Region.ICE_CAVES, 11, "prospector_torches", "prospector", () -> idleImage(new ProspectorSprite()));
        quest(list, Region.ICE_CAVES, 11, "prospector_iceclaws", "prospector", () -> idleImage(new ProspectorSprite()));
        quest(list, Region.CITY, 17, "imp_golems", "imp", () -> idleImage(new ImpSprite()));
        quest(list, Region.CITY, 17, "imp_monks", "imp", () -> idleImage(new ImpSprite()));

        item(list, Shelf.MISC, Amulet.class, ItemSpriteSheet.AMULET, "items.amulet.name", "items.amulet.desc");
        item(list, Shelf.MISC, Ankh.class, ItemSpriteSheet.ANKH, "items.ankh.name", "items.ankh.desc");
        item(list, Shelf.MISC, ArmorKit.class, ItemSpriteSheet.KIT, "custom.generated.entry_armor_kit_93e26fc37c", "items.armorkit.desc");
        item(list, Shelf.MISC, Bomb.class, ItemSpriteSheet.BOMB, "items.bombs.bomb.name", "items.bomb.desc");
        item(list, Shelf.MISC, Corpse.class, ItemSpriteSheet.CORPSE, "items.corpse.name", "items.corpse.desc");
        item(list, Shelf.MISC, DewVial.class, ItemSpriteSheet.VIAL, "items.dewvial.name", "items.dewvial.desc");
        item(list, Shelf.MISC, Dewdrop.class, ItemSpriteSheet.DEWDROP, "items.dewdrop.name", "items.dewdrop.desc");
        item(list, Shelf.MISC, Gold.class, ItemSpriteSheet.GOLD, "items.gold.name", "items.gold.desc");
        item(list, Shelf.MISC, Honeypot.class, ItemSpriteSheet.HONEYPOT, "items.honeypot.name", "items.honeypot.desc");
        item(list, Shelf.MISC, LloydsBeacon.class, ItemSpriteSheet.BEACON, "items.artifacts.lloydsbeacon.name", "custom.generated.entry_lloyd_s_beacon_is_an_intricate_magic_device_that_1e7d6a657d");
        item(list, Shelf.MISC, SoulCrystalFilled.class, ItemSpriteSheet.CRYSTAL_FULL, "library.items.soulcrystal.name", "library.items.soulcrystal.desc");
        item(list, Shelf.MISC, TomeOfMastery.class, ItemSpriteSheet.MASTERY, "custom.generated.entry_tome_of_mastery_edf1f9ccdf", "items.tomeofmastery.desc");
        item(list, Shelf.MISC, Torch.class, ItemSpriteSheet.TORCH, "items.torch.name", "items.torch.desc");
        item(list, Shelf.MISC, Weightstone.class, ItemSpriteSheet.WEIGHT, "custom.generated.entry_weightstone_e2276c93cf", "items.weightstone.desc");
        item(list, Shelf.ARMOR, ClothArmor.class, ItemSpriteSheet.ARMOR_CLOTH, "items.armor.clotharmor.name", "items.armor.clotharmor.desc", ClothArmor::new);
        item(list, Shelf.ARMOR, HuntressArmor.class, ItemSpriteSheet.ARMOR_HUNTRESS, "items.armor.huntressarmor.name", "library.items.armor.huntressarmor.desc", HuntressArmor::new);
        item(list, Shelf.ARMOR, LeatherArmor.class, ItemSpriteSheet.ARMOR_LEATHER, "items.armor.leatherarmor.name", "items.armor.leatherarmor.desc", LeatherArmor::new);
        item(list, Shelf.ARMOR, MageArmor.class, ItemSpriteSheet.ARMOR_MAGE, "items.armor.magearmor.name", "library.items.armor.magearmor.desc", MageArmor::new);
        item(list, Shelf.ARMOR, MailArmor.class, ItemSpriteSheet.ARMOR_MAIL, "items.armor.mailarmor.name", "items.armor.mailarmor.desc", MailArmor::new);
        item(list, Shelf.ARMOR, MercenaryArmor.class, ItemSpriteSheet.ARMOR_MERCENARY, "custom.generated.necro_merc.mercenary_coat", "custom.generated.necro_merc.mercenary_coat_desc", MercenaryArmor::new);
        item(list, Shelf.ARMOR, NecromancerArmor.class, ItemSpriteSheet.ARMOR_NECROMANCER, "custom.generated.necro_merc.necromancer_robe", "custom.generated.necro_merc.necromancer_robe_desc", NecromancerArmor::new);
        item(list, Shelf.ARMOR, PlateArmor.class, ItemSpriteSheet.ARMOR_PLATE, "items.armor.platearmor.name", "items.armor.platearmor.desc", PlateArmor::new);
        item(list, Shelf.ARMOR, RogueArmor.class, ItemSpriteSheet.ARMOR_ROGUE, "items.armor.roguearmor.name", "library.items.armor.roguearmor.desc", RogueArmor::new);
        item(list, Shelf.ARMOR, ScaleArmor.class, ItemSpriteSheet.ARMOR_SCALE, "items.armor.scalearmor.name", "items.armor.scalearmor.desc", ScaleArmor::new);
        item(list, Shelf.ARMOR, WarriorArmor.class, ItemSpriteSheet.ARMOR_WARRIOR, "items.armor.warriorarmor.name", "library.items.armor.warriorarmor.desc", WarriorArmor::new);
        item(list, Shelf.MISC, Bag.class, 11, "items.bags.bag.name", "library.items.bag.desc");
        item(list, Shelf.MISC, Keyring.class, ItemSpriteSheet.KEYRING, "items.bags.keyring.name", "items.bags.keyring.info");
        item(list, Shelf.MISC, PotionHolder.class, ItemSpriteSheet.POTION_HOLDER, "items.bags.potionbandolier.name", "items.bags.potionbandolier.desc");
        item(list, Shelf.MISC, ScrollHolder.class, ItemSpriteSheet.HOLDER, "items.bags.scrollholder.name", "items.bags.scrollholder.desc");
        item(list, Shelf.MISC, SeedPouch.class, ItemSpriteSheet.POUCH, "items.bags.seedpouch.name", "items.bags.seedpouch.desc");
        item(list, Shelf.MISC, WandHolster.class, ItemSpriteSheet.HOLSTER, "items.bags.magicalholster.name", "items.bags.magicalholster.desc");
        item(list, Shelf.FOOD, ChargrilledMeat.class, ItemSpriteSheet.STEAK, "items.food.chargrilledmeat.name", "items.food.chargrilledmeat.desc");
        item(list, Shelf.FOOD, Food.class, ItemSpriteSheet.RATION, "items.food.food.name", "items.food.food.desc");
        item(list, Shelf.FOOD, FrozenCarpaccio.class, ItemSpriteSheet.CARPACCIO, "items.food.frozencarpaccio.name", "items.food.frozencarpaccio.desc");
        item(list, Shelf.FOOD, MysteryMeat.class, ItemSpriteSheet.MEAT, "items.food.mysterymeat.name", "items.food.mysterymeat.desc");
        item(list, Shelf.FOOD, OverpricedRation.class, ItemSpriteSheet.OVERPRICED, "items.food.smallration.name", "items.food.smallration.desc");
        item(list, Shelf.FOOD, Pasty.class, ItemSpriteSheet.PASTY, "items.food.pasty.name", "items.food.pasty.desc");
        item(list, Shelf.MISC, GoldenKey.class, ItemSpriteSheet.GOLDEN_KEY, "items.keys.goldenkey.name", "items.keys.goldenkey.desc");
        item(list, Shelf.MISC, IronKey.class, ItemSpriteSheet.IRON_KEY, "items.keys.ironkey.name", "items.keys.ironkey.desc");
        item(list, Shelf.MISC, SkeletonKey.class, ItemSpriteSheet.SKELETON_KEY, "items.keys.skeletonkey.name", "items.keys.skeletonkey.desc");
        item(list, Shelf.POTIONS, PotionOfExperience.class, -1, "items.potions.potionofexperience.name", "items.potions.potionofexperience.desc");
        item(list, Shelf.POTIONS, PotionOfFrost.class, -1, "items.potions.potionoffrost.name", "items.potions.potionoffrost.desc");
        item(list, Shelf.POTIONS, PotionOfHealing.class, -1, "items.potions.potionofhealing.name", "items.potions.potionofhealing.desc_reborn");
        item(list, Shelf.POTIONS, PotionOfRejuvenation.class, ItemSpriteSheet.POTION_REJUVENATION, "items.potions.potionofrejuvenation.name", "items.potions.potionofrejuvenation.desc");
        item(list, Shelf.POTIONS, PotionOfInvisibility.class, -1, "items.potions.potionofinvisibility.name", "items.potions.potionofinvisibility.desc");
        item(list, Shelf.POTIONS, PotionOfLevitation.class, -1, "items.potions.potionoflevitation.name", "items.potions.potionoflevitation.desc");
        item(list, Shelf.POTIONS, PotionOfLiquidFlame.class, -1, "items.potions.potionofliquidflame.name", "items.potions.potionofliquidflame.desc");
        item(list, Shelf.POTIONS, PotionOfMana.class, ItemSpriteSheet.POTION_MANA, "custom.generated.entry_potion_of_mana_dfd416a689", "items.potions.potionofmana.desc_reborn");
        item(list, Shelf.POTIONS, PotionOfMight.class, -1, "items.potions.potionofmight.name", "items.potions.potionofmight.desc");
        item(list, Shelf.POTIONS, PotionOfMindVision.class, -1, "items.potions.potionofmindvision.name", "items.potions.potionofmindvision.desc");
        item(list, Shelf.POTIONS, PotionOfParalyticGas.class, -1, "items.potions.potionofparalyticgas.name", "items.potions.potionofparalyticgas.desc");
        item(list, Shelf.POTIONS, PotionOfPurity.class, -1, "items.potions.potionofpurity.name", "items.potions.potionofpurity.desc");
        item(list, Shelf.POTIONS, PotionOfStrength.class, -1, "items.potions.potionofstrength.name", "items.potions.potionofstrength.desc");
        item(list, Shelf.POTIONS, PotionOfToxicGas.class, -1, "items.potions.potionoftoxicgas.name", "items.potions.potionoftoxicgas.desc");
        item(list, Shelf.MISC, CorpseDust.class, ItemSpriteSheet.DUST, "items.quest.corpsedust.name", "items.quest.corpsedust.desc");
        item(list, Shelf.MISC, DarkGold.class, ItemSpriteSheet.ORE, "items.quest.darkgold.name", "items.quest.darkgold.desc");
        item(list, Shelf.MISC, DriedRose.class, ItemSpriteSheet.ROSE, "items.artifacts.driedrose.name", "items.quest.driedrose.desc");
        item(list, Shelf.MISC, DwarfToken.class, ItemSpriteSheet.TOKEN, "items.quest.dwarftoken.name", "items.quest.dwarftoken.desc");
        item(list, Shelf.MISC, PhantomFish.class, ItemSpriteSheet.PHANTOM, "custom.generated.entry_phantom_fish_f08302c02d", "items.quest.phantomfish.desc");
        item(list, Shelf.MISC, Pickaxe.class, ItemSpriteSheet.PICKAXE, "items.quest.pickaxe.name", "items.quest.pickaxe.desc", Pickaxe::new);
        item(list, Shelf.MISC, RatSkull.class, ItemSpriteSheet.SKULL, "items.quest.ratskull.name", "items.quest.ratskull.desc");
        item(list, Shelf.RINGS, RingOfAccuracy.class, -1, "items.rings.ringofaccuracy.name", "items.rings.ringofaccuracy.desc", RingOfAccuracy::new);
        item(list, Shelf.RINGS, RingOfDetection.class, -1, "items.rings.ringofdetection.name", "items.rings.ringofdetection.desc", RingOfDetection::new);
        item(list, Shelf.RINGS, RingOfElements.class, -1, "items.rings.ringofelements.name", "items.rings.ringofelements.desc", RingOfElements::new);
        item(list, Shelf.RINGS, RingOfEvasion.class, -1, "items.rings.ringofevasion.name", "items.rings.ringofevasion.desc", RingOfEvasion::new);
        item(list, Shelf.RINGS, RingOfHaggler.class, -1, "items.rings.ringofhaggler.name", "items.rings.ringofhaggler.desc", RingOfHaggler::new);
        item(list, Shelf.RINGS, RingOfHaste.class, -1, "items.rings.ringofhaste.name", "items.rings.ringofhaste.desc", RingOfHaste::new);
        item(list, Shelf.RINGS, RingOfHerbalism.class, -1, "items.rings.ringofherbalism.name", "items.rings.ringofherbalism.desc", RingOfHerbalism::new);
        item(list, Shelf.RINGS, RingOfMending.class, -1, "items.rings.ringofmending.name", "items.rings.ringofmending.desc", RingOfMending::new);
        item(list, Shelf.RINGS, RingOfPower.class, -1, "items.rings.ringofpower.name", "items.rings.ringofpower.desc", RingOfPower::new);
        item(list, Shelf.RINGS, RingOfSatiety.class, -1, "items.rings.ringofsatiety.name", "items.rings.ringofsatiety.desc", RingOfSatiety::new);
        item(list, Shelf.RINGS, RingOfShadows.class, -1, "items.rings.ringofshadows.name", "items.rings.ringofshadows.desc", RingOfShadows::new);
        item(list, Shelf.RINGS, RingOfThorns.class, -1, "items.rings.ringofthorns.name", "items.rings.ringofthorns.desc", RingOfThorns::new);
        item(list, Shelf.SCROLLS, ScrollOfBloodyRitual.class, -1, "custom.generated.entry_scroll_of_bloody_ritual_4018462837", "items.scrolls.scrollofbloodyritual.desc");
        item(list, Shelf.SCROLLS, ScrollOfChallenge.class, -1, "items.scrolls.scrollofchallenge.name", "items.scrolls.scrollofchallenge.desc");
        item(list, Shelf.SCROLLS, ScrollOfEnchantment.class, -1, "items.scrolls.scrollofenchantment.name", "items.scrolls.scrollofenchantment.desc");
        item(list, Shelf.SCROLLS, ScrollOfFrostLevel.class, -1, "items.scrolls.scrolloffrostlevel.name", "items.scrolls.scrolloffrostlevel.desc");
        item(list, Shelf.SCROLLS, ScrollOfHome.class, -1, "custom.generated.entry_scroll_of_refuge_6bf631f52f", "items.scrolls.scrollofhome.desc");
        item(list, Shelf.SCROLLS, ScrollOfIdentify.class, -1, "items.scrolls.scrollofidentify.name", "items.scrolls.scrollofidentify.desc");
        item(list, Shelf.SCROLLS, ScrollOfLullaby.class, -1, "items.scrolls.scrolloflullaby.name", "items.scrolls.scrolloflullaby.desc");
        item(list, Shelf.SCROLLS, ScrollOfMagicMapping.class, -1, "items.scrolls.scrollofmagicmapping.name", "items.scrolls.scrollofmagicmapping.desc");
        item(list, Shelf.SCROLLS, ScrollOfMirrorImage.class, -1, "items.scrolls.scrollofmirrorimage.name", "items.scrolls.scrollofmirrorimage.desc");
        item(list, Shelf.SCROLLS, ScrollOfPsionicBlast.class, -1, "items.scrolls.scrollofpsionicblast.name", "items.scrolls.scrollofpsionicblast.desc");
        item(list, Shelf.SCROLLS, ScrollOfReadiness.class, -1, "custom.generated.entry_scroll_of_readiness_0af44740d5", "items.scrolls.scrollofreadiness.desc");
        item(list, Shelf.SCROLLS, ScrollOfRecharging.class, -1, "items.scrolls.scrollofrecharging.name", "items.scrolls.scrollofrecharging.desc");
        item(list, Shelf.SCROLLS, ScrollOfRemoveCurse.class, -1, "items.scrolls.scrollofremovecurse.name", "items.scrolls.scrollofremovecurse.desc");
        item(list, Shelf.SCROLLS, ScrollOfSacrifice.class, -1, "custom.generated.entry_scroll_of_sacrifice_9f7f9eccc4", "items.scrolls.scrollofsacrifice.desc");
        item(list, Shelf.SCROLLS, ScrollOfSkill.class, -1, "custom.generated.entry_scroll_of_skill_eee69d0bc2", "items.scrolls.scrollofskill.desc");
        item(list, Shelf.SCROLLS, ScrollOfTeleportation.class, -1, "items.scrolls.scrollofteleportation.name", "items.scrolls.scrollofteleportation.desc");
        item(list, Shelf.SCROLLS, ScrollOfTerror.class, -1, "items.scrolls.scrollofterror.name", "items.scrolls.scrollofterror.desc");
        item(list, Shelf.SCROLLS, ScrollOfUpgrade.class, -1, "items.scrolls.scrollofupgrade.name", "items.scrolls.scrollofupgrade.desc");
        item(list, Shelf.SCROLLS, ScrollOfWipeOut.class, ItemSpriteSheet.SCROLL_WIPE_OUT, "items.scrolls.scrollofwipeout.name", "items.scrolls.scrollofwipeout.desc");
        item(list, Shelf.WANDS, WandOfAmok.class, -1, "custom.generated.entry_wand_of_amok_a1f8b95ec8", "items.wands.wandofamok.desc", WandOfAmok::new);
        item(list, Shelf.WANDS, WandOfAvalanche.class, -1, "custom.generated.entry_wand_of_avalanche_4c30cb6f79", "items.wands.wandofavalanche.desc", WandOfAvalanche::new);
        item(list, Shelf.WANDS, WandOfBlink.class, -1, "items.wands.wandofblink.name", "items.wands.wandofblink.desc", WandOfBlink::new);
        item(list, Shelf.WANDS, WandOfDisintegration.class, -1, "items.wands.wandofdisintegration.name", "library.items.wands.wandofdisintegration.desc", WandOfDisintegration::new);
        item(list, Shelf.WANDS, WandOfFirebolt.class, -1, "items.wands.wandoffirebolt.name", "items.wands.wandoffirebolt.desc", WandOfFirebolt::new);
        item(list, Shelf.WANDS, WandOfFlock.class, -1, "custom.generated.entry_wand_of_flock_dcf2e58d6f", "items.wands.wandofflock.desc", WandOfFlock::new);
        item(list, Shelf.WANDS, WandOfLightning.class, -1, "items.wands.wandoflightning.name", "library.items.wands.wandoflightning.desc", WandOfLightning::new);
        item(list, Shelf.WANDS, WandOfMagicMissile.class, ItemSpriteSheet.WAND_MAGIC_MISSILE, "items.wands.wandofmagicmissile.name", "library.items.wands.wandofmagicmissile.desc", WandOfMagicMissile::new);
        item(list, Shelf.WANDS, WandOfPoison.class, -1, "custom.generated.entry_wand_of_poison_9162292201", "items.wands.wandofpoison.desc", WandOfPoison::new);
        item(list, Shelf.WANDS, WandOfReach.class, -1, "custom.generated.entry_wand_of_reach_1ac9244cd0", "items.wands.wandofreach.desc", WandOfReach::new);
        item(list, Shelf.WANDS, WandOfRegrowth.class, -1, "items.wands.wandofregrowth.name", "library.items.wands.wandofregrowth.desc", WandOfRegrowth::new);
        item(list, Shelf.WANDS, WandOfSlowness.class, -1, "custom.generated.entry_wand_of_slowness_b6860e02d0", "items.wands.wandofslowness.desc", WandOfSlowness::new);
        item(list, Shelf.WANDS, WandOfTeleportation.class, -1, "custom.generated.entry_wand_of_teleportation_c4087ef55d", "items.wands.wandofteleportation.desc", WandOfTeleportation::new);
        item(list, Shelf.WEAPONS, GlacierCleaver.class, ItemSpriteSheet.GLACIER_CLEAVER, "items.weapon.ice.glaciercleaver.name", "items.weapon.ice.glaciercleaver.desc", GlacierCleaver::new);
        item(list, Shelf.WANDS, HoarfrostStaff.class, ItemSpriteSheet.HOARFROST_STAFF, "items.weapon.ice.hoarfroststaff.name", "items.weapon.ice.hoarfroststaff.desc", HoarfrostStaff::new);
        item(list, Shelf.WEAPONS, RimePick.class, ItemSpriteSheet.RIME_PICK, "items.weapon.ice.rimepick.name", "items.weapon.ice.rimepick.desc", RimePick::new);
        item(list, Shelf.WEAPONS, com.bilboldev.pixeldungeonskills.items.weapon.ice.FrostBow.class, ItemSpriteSheet.FROST_BOW, "items.weapon.ice.frostbow.name", "items.weapon.ice.frostbow.desc", com.bilboldev.pixeldungeonskills.items.weapon.ice.FrostBow::new);
        item(list, Shelf.WEAPONS, GravefrostScythe.class, ItemSpriteSheet.GRAVEFROST_SCYTHE, "items.weapon.ice.gravefrostscythe.name", "items.weapon.ice.gravefrostscythe.desc", GravefrostScythe::new);
        item(list, Shelf.WEAPONS, FrostbiteRifle.class, ItemSpriteSheet.FROSTBITE_RIFLE, "items.weapon.ice.frostbiterifle.name", "items.weapon.ice.frostbiterifle.desc", FrostbiteRifle::new);
        item(list, Shelf.WEAPONS, Blunderbuss.class, ItemSpriteSheet.BLUNDERBUSS, "custom.generated.necro_merc.blunderbuss", "custom.generated.necro_merc.blunderbuss_desc", Blunderbuss::new);
        item(list, Shelf.WEAPONS, Handgun.class, ItemSpriteSheet.HANDGUN, "custom.generated.necro_merc.handgun", "custom.generated.necro_merc.handgun_desc", Handgun::new);
        item(list, Shelf.WEAPONS, Mortar.class, ItemSpriteSheet.MORTAR, "custom.generated.necro_merc.mortar", "custom.generated.necro_merc.mortar_desc", Mortar::new);
        item(list, Shelf.WEAPONS, Pistol.class, ItemSpriteSheet.PISTOL, "custom.generated.necro_merc.pistol", "custom.generated.necro_merc.pistol_desc", Pistol::new);
        item(list, Shelf.WEAPONS, Rifle.class, ItemSpriteSheet.RIFLE, "custom.generated.necro_merc.rifle", "custom.generated.necro_merc.rifle_desc", Rifle::new);
        item(list, Shelf.WEAPONS, BattleAxe.class, ItemSpriteSheet.BATTLE_AXE, "items.weapon.melee.battleaxe.name", "library.items.weapon.melee.battleaxe.desc", BattleAxe::new);
        item(list, Shelf.WEAPONS, Dagger.class, ItemSpriteSheet.DAGGER, "items.weapon.melee.dagger.name", "library.items.weapon.melee.dagger.desc", Dagger::new);
        item(list, Shelf.WEAPONS, DualSwords.class, ItemSpriteSheet.DualSwords, "custom.generated.entry_dualswords_322f5278e5", "items.weapon.melee.dualswords.desc", DualSwords::new);
        item(list, Shelf.WEAPONS, Glaive.class, ItemSpriteSheet.GLAIVE, "items.weapon.melee.glaive.name", "library.items.weapon.melee.glaive.desc", Glaive::new);
        item(list, Shelf.WEAPONS, Knuckles.class, ItemSpriteSheet.KNUCKLEDUSTER, "items.weapon.melee.knuckles.name", "items.weapon.melee.knuckles.desc", Knuckles::new);
        item(list, Shelf.WEAPONS, Longsword.class, ItemSpriteSheet.LONG_SWORD, "items.weapon.melee.longsword.name", "library.items.weapon.melee.longsword.desc", Longsword::new);
        item(list, Shelf.WEAPONS, Mace.class, ItemSpriteSheet.MACE, "items.weapon.melee.mace.name", "library.items.weapon.melee.mace.desc", Mace::new);
        item(list, Shelf.WEAPONS, NecroBlade.class, ItemSpriteSheet.NecroBlade0, "custom.generated.entry_necroblade_9d330b652a", "library.items.necroblade.desc", NecroBlade::new);
        item(list, Shelf.WEAPONS, Quarterstaff.class, ItemSpriteSheet.QUARTERSTAFF, "items.weapon.melee.quarterstaff.name", "items.weapon.melee.quarterstaff.desc", Quarterstaff::new);
        item(list, Shelf.WEAPONS, ShortSword.class, ItemSpriteSheet.SHORT_SWORD, "items.weapon.melee.shortsword.name", "library.items.weapon.melee.shortsword.desc", ShortSword::new);
        item(list, Shelf.WEAPONS, SoulBlade.class, ItemSpriteSheet.SoulBlade_1, "custom.generated.entry_soul_blade_b10657965a", "library.items.soulblade.desc", SoulBlade::new);
        item(list, Shelf.WEAPONS, Spear.class, ItemSpriteSheet.SPEAR, "items.weapon.melee.spear.name", "items.weapon.melee.spear.desc", Spear::new);
        item(list, Shelf.WEAPONS, Sword.class, ItemSpriteSheet.SWORD, "items.weapon.melee.sword.name", "library.items.weapon.melee.sword.desc", Sword::new);
        item(list, Shelf.WEAPONS, WarHammer.class, ItemSpriteSheet.WAR_HAMMER, "items.weapon.melee.warhammer.name", "library.items.weapon.melee.warhammer.desc", WarHammer::new);
        item(list, Shelf.WEAPONS, Arrow.class, ItemSpriteSheet.Arrow, "custom.generated.entry_arrow_39a531d92b", "custom.generated.entry_arrows_are_more_powerful_and_accurate_than_darts_690136fdc1", Arrow::new);
        item(list, Shelf.WEAPONS, BombArrow.class, ItemSpriteSheet.BombArrow, "items.weapon.missiles.bombarrow.name", "items.weapon.missiles.bombarrow.desc", BombArrow::new);
        item(list, Shelf.WEAPONS, Boomerang.class, ItemSpriteSheet.BOOMERANG, "items.weapon.missiles.boomerang.name", "items.weapon.missiles.boomerang.desc", Boomerang::new);
        item(list, Shelf.WEAPONS, Bow.class, ItemSpriteSheet.Bow, "items.weapon.missiles.bow.name", "items.weapon.missiles.bow.desc", Bow::new);
        item(list, Shelf.WEAPONS, CupidArrow.class, ItemSpriteSheet.CupidArrow, "custom.generated.entry_cupid_arrow_ed1abafc12", "items.weapon.missiles.cupidarrow.desc", CupidArrow::new);
        item(list, Shelf.WEAPONS, CurareDart.class, ItemSpriteSheet.CURARE_DART, "custom.generated.entry_curare_dart_f1f9a829f6", "items.weapon.missiles.curaredart.desc", CurareDart::new);
        item(list, Shelf.WEAPONS, FlameBow.class, ItemSpriteSheet.FlameBow, "custom.generated.entry_flame_bow_2e0dfb9f5f", "library.items.flamebow.desc", FlameBow::new);
        item(list, Shelf.WEAPONS, FrostBow.class, ItemSpriteSheet.ForstBow, "library.items.frostbow.name", "library.items.frostbow.desc", FrostBow::new);
        item(list, Shelf.WEAPONS, IncendiaryDart.class, ItemSpriteSheet.INCENDIARY_DART, "items.weapon.missiles.darts.incendiarydart.name", "items.weapon.missiles.incendiarydart.desc", IncendiaryDart::new);
        item(list, Shelf.WEAPONS, Javelin.class, ItemSpriteSheet.JAVELIN, "items.weapon.missiles.javelin.name", "library.items.weapon.missiles.javelin.desc", Javelin::new);
        item(list, Shelf.WEAPONS, Shuriken.class, ItemSpriteSheet.SHURIKEN, "items.weapon.missiles.shuriken.name", "items.weapon.missiles.shuriken.desc", Shuriken::new);
        item(list, Shelf.WEAPONS, SoulCrystal.class, ItemSpriteSheet.CRYSTAL_EMPTY, "custom.generated.entry_soul_crystal_2a5340bfb7", "items.weapon.missiles.soulcrystal.desc");
        item(list, Shelf.WEAPONS, Tamahawk.class, ItemSpriteSheet.TOMAHAWK, "items.weapon.missiles.tomahawk.name", "items.weapon.missiles.tamahawk.desc", Tamahawk::new);
        item(list, Shelf.SEEDS, Dreamweed.Seed.class, ItemSpriteSheet.SEED_DREAMWEED, "library.items.dreamweed.name", "library.items.dreamweed.desc");
        item(list, Shelf.SEEDS, Earthroot.Seed.class, ItemSpriteSheet.SEED_EARTHROOT, "plants.earthroot$seed.name", "custom.generated.entry_when_a_creature_touches_an_earthroot_its_roots_c_9788c228cf");
        item(list, Shelf.SEEDS, Fadeleaf.Seed.class, ItemSpriteSheet.SEED_FADELEAF, "plants.fadeleaf$seed.name", "plants.fadeleaf.desc");
        item(list, Shelf.SEEDS, Firebloom.Seed.class, ItemSpriteSheet.SEED_FIREBLOOM, "plants.firebloom$seed.name", "plants.firebloom.desc");
        item(list, Shelf.SEEDS, Icecap.Seed.class, ItemSpriteSheet.SEED_ICECAP, "plants.icecap$seed.name", "custom.generated.entry_upon_touching_an_icecap_excretes_a_pollen_which_500f943bc3");
        item(list, Shelf.SEEDS, Rotberry.Seed.class, ItemSpriteSheet.SEED_ROTBERRY, "plants.rotberry$seed.name", "custom.generated.entry_berries_of_this_shrub_taste_like_sweet_sweet_dea_42413fe945");
        item(list, Shelf.SEEDS, Sorrowmoss.Seed.class, ItemSpriteSheet.SEED_SORROWMOSS, "plants.sorrowmoss$seed.name", "plants.sorrowmoss.desc");
        item(list, Shelf.SEEDS, Sungrass.Seed.class, ItemSpriteSheet.SEED_SUNGRASS, "plants.sungrass$seed.name", "custom.generated.entry_sungrass_is_renowned_for_its_sap_s_healing_prope_66b708bb75");
        creature(list, Shelf.ENEMIES, Region.HALLS, 23, Acidic.class, "actors.mobs.acidic.name", "actors.mobs.acidic.desc", () -> idleImage(new AcidicSprite()));
        creature(list, Shelf.ENEMIES, Region.SEWERS, 1, Albino.class, "actors.mobs.albino.name", "actors.mobs.albino.desc", () -> idleImage(new AlbinoSprite()));
        creature(list, Shelf.ENEMIES, Region.PRISON, 6, Bandit.class, "actors.mobs.bandit.name", "actors.mobs.bandit.desc", () -> idleImage(new BanditSprite()));
        creature(list, Shelf.ENEMIES, Region.CAVES, 11, Bat.class, "actors.mobs.bat.name", "library.actors.mobs.bat.desc", () -> idleImage(new BatSprite()));
        creature(list, Shelf.ENEMIES, Region.CAVES, 11, Brute.class, "actors.mobs.brute.name", "actors.mobs.brute.desc", () -> idleImage(new BruteSprite()));
        creature(list, Shelf.ENEMIES, Region.SEWERS, 3, Crab.class, "actors.mobs.crab.name", "actors.mobs.crab.desc", () -> idleImage(new CrabSprite()));
        creature(list, Shelf.ENEMIES, Region.SEWERS, 2, CursePersonification.class, "actors.mobs.cursepersonification.name", "library.creatures.cursepersonification.desc", () -> idleImage(new WraithSprite()));
        creature(list, Shelf.BOSSES, Region.CAVES, 15, DM300.class, "actors.mobs.dm300.name", "actors.mobs.reborndm300.desc", () -> idleImage(new DM300Sprite()));
        creature(list, Shelf.ENEMIES, EchoReflection.class, "actors.mobs.npcs.echoreflection.name", "actors.mobs.npcs.echoreflection.desc", () -> heroImage(Assets.WARRIOR, 0));
        creature(list, Shelf.ENEMIES, Region.CITY, 16, Elemental.class, "actors.mobs.elemental$fireelemental.name", "actors.mobs.elemental.desc", () -> idleImage(new ElementalSprite()));
        creature(list, Shelf.ENEMIES, Region.HALLS, 22, Eye.class, "actors.mobs.eye.name", "actors.mobs.eye.desc", () -> idleImage(new EyeSprite()));
        creature(list, Shelf.ENEMIES, Region.SEWERS, 2, FetidRat.class, "actors.mobs.fetidrat.name", "library.actors.mobs.fetidrat.desc", () -> idleImage(new FetidRatSprite()));
        creature(list, Shelf.ENEMIES, Region.SEWERS, 2, Gnoll.class, "actors.mobs.gnoll.name", "actors.mobs.gnoll.desc", () -> idleImage(new GnollSprite()));
        creature(list, Shelf.ENEMIES, Region.CITY, 18, Golem.class, "actors.mobs.golem.name", "actors.mobs.golem.desc", () -> idleImage(new GolemSprite()));
        creature(list, Shelf.BOSSES, Region.SEWERS, 5, Goo.class, "actors.mobs.goo.name", "actors.mobs.reborngoo.desc", () -> idleImage(new GooSprite()));
        creature(list, Shelf.BOSSES, Region.CITY, 20, King.class, "actors.mobs.dwarfking.name", "actors.mobs.rebornking.desc", () -> idleImage(new KingSprite()));
        creature(list, Shelf.ENEMIES, Mimic.class, "actors.mobs.mimic.name", "actors.mobs.mimic.desc", () -> idleImage(new MimicSprite()));
        creature(list, Shelf.ENEMIES, Region.CITY, 16, Monk.class, "actors.mobs.monk.name", "actors.mobs.monk.desc", () -> idleImage(new MonkSprite()));
        creature(list, Shelf.ENEMIES, Piranha.class, "actors.mobs.piranha.name", "library.actors.mobs.piranha.desc", () -> idleImage(new PiranhaSprite()));
        creature(list, Shelf.ENEMIES, Region.SEWERS, 1, Rat.class, "actors.mobs.rat.name", "actors.mobs.rat.desc", () -> idleImage(new RatSprite()));
        creature(list, Shelf.ENEMIES, Region.HALLS, 23, Scorpio.class, "actors.mobs.scorpio.name", "actors.mobs.scorpio.desc", () -> idleImage(new ScorpioSprite()));
        creature(list, Shelf.ENEMIES, Region.CITY, 16, Senior.class, "actors.mobs.senior.name", "actors.mobs.senior.desc", () -> idleImage(new SeniorSprite()));
        creature(list, Shelf.ENEMIES, Region.PRISON, 6, Shaman.class, "actors.mobs.shaman.name", "actors.mobs.shaman.desc", () -> idleImage(new ShamanSprite()));
        creature(list, Shelf.ENEMIES, Region.CAVES, 11, Shielded.class, "actors.mobs.shielded.name", "actors.mobs.shielded.desc", () -> idleImage(new ShieldedSprite()));
        creature(list, Shelf.ENEMIES, Region.PRISON, 6, Skeleton.class, "actors.mobs.skeleton.name", "actors.mobs.skeleton.desc", () -> idleImage(new SkeletonSprite()));
        creature(list, Shelf.ENEMIES, Region.CAVES, 12, Spinner.class, "actors.mobs.spinner.name", "actors.mobs.spinner.desc", () -> idleImage(new SpinnerSprite()));
        creature(list, Shelf.ENEMIES, Statue.class, "actors.mobs.statue.name", "actors.mobs.statue.desc", () -> idleImage(new StatueSprite()));
        creature(list, Shelf.ENEMIES, Region.HALLS, 22, Succubus.class, "actors.mobs.succubus.name", "actors.mobs.succubus.desc", () -> idleImage(new SuccubusSprite()));
        creature(list, Shelf.ENEMIES, Region.PRISON, 6, Swarm.class, "actors.mobs.swarm.name", "actors.mobs.swarm.desc", () -> idleImage(new SwarmSprite()));
        creature(list, Shelf.BOSSES, Region.PRISON, 10, Tengu.class, "actors.mobs.tengu.name", "actors.mobs.reborntengu.desc", () -> idleImage(new TenguSprite()));
        creature(list, Shelf.ENEMIES, Region.PRISON, 10, TenguShadow.class, "actors.mobs.tengushadow.name", "actors.mobs.tengushadow.desc", () -> idleImage(new TenguShadowSprite()));
        creature(list, Shelf.ENEMIES, Region.PRISON, 6, Thief.class, "actors.mobs.thief.name", "actors.mobs.thief.desc", () -> idleImage(new ThiefSprite()));
        creature(list, Shelf.ENEMIES, Region.CITY, 16, Warlock.class, "actors.mobs.warlock.name", "actors.mobs.warlock.desc", () -> idleImage(new WarlockSprite()));
        creature(list, Shelf.ENEMIES, Wraith.class, "actors.mobs.wraith.name", "actors.mobs.wraith.desc", () -> idleImage(new WraithSprite()));
        creature(list, Shelf.BOSSES, Region.HALLS, 25, Yog.class, "actors.mobs.yogdzewa.name", "actors.mobs.rebornyog.desc", () -> idleImage(new YogSprite()));
        creature(list, Shelf.BOSSES, Region.HALLS, 25, Yog.RottingFist.class, "actors.mobs.yogfist$rottingfist.name", "actors.mobs.rebornyog$rotting.desc", () -> idleImage(new RottingFistSprite()));
        creature(list, Shelf.BOSSES, Region.HALLS, 25, Yog.BurningFist.class, "actors.mobs.yogfist$burningfist.name", "actors.mobs.rebornyog$burninghand.desc", () -> idleImage(new BurningFistSprite()));
        creature(list, Shelf.ENEMIES, Region.HALLS, 25, Yog.Larva.class, "actors.mobs.yogdzewa$larva.name", "actors.mobs.yogdzewa$larva.desc", () -> idleImage(new LarvaSprite()));
        creature(list, Shelf.ENEMIES, Region.ICE_CAVES, 11, RimeBat.class, "actors.mobs.rimebat.name", "actors.mobs.rimebat.desc", () -> idleImage(new IceSprite.Bat()));
        creature(list, Shelf.ENEMIES, Region.ICE_CAVES, 11, Iceclaw.class, "actors.mobs.iceclaw.name", "actors.mobs.iceclaw.desc", () -> idleImage(new IceSprite.Claw()));
        creature(list, Shelf.ENEMIES, Region.ICE_CAVES, 11, Frostback.class, "actors.mobs.frostback.name", "actors.mobs.frostback.desc", () -> idleImage(new IceSprite.Gnoll()));
        creature(list, Shelf.ENEMIES, Region.ICE_CAVES, 11, Shardling.class, "actors.mobs.shardling.name", "actors.mobs.shardling.desc", () -> idleImage(new IceSprite.Shard()));
        creature(list, Shelf.ENEMIES, Region.ICE_CAVES, 11, HollowMiner.class, "actors.mobs.hollowminer.name", "actors.mobs.hollowminer.desc", () -> idleImage(new IceSprite.Miner()));
        creature(list, Shelf.ENEMIES, Region.ICE_CAVES, 11, Rimehorn.class, "actors.mobs.rimehorn.name", "actors.mobs.rimehorn.desc", () -> idleImage(new IceSprite.Horn()));
        creature(list, Shelf.BOSSES, Region.ICE_CAVES, 14, OldRime.class, "actors.mobs.oldrime.name", "actors.mobs.oldrime.desc", () -> idleImage(new IceSprite.Rime()));
        creature(list, Shelf.PEOPLE, Region.ICE_CAVES, 11, Prospector.class, "actors.mobs.npcs.prospector.name", "actors.mobs.npcs.prospector.desc", () -> idleImage(new ProspectorSprite()));
        creature(list, Shelf.ENEMIES, RivalServant.class, "actors.mobs.rivalservant.name", "actors.mobs.rivalservant.desc", () -> idleImage(new SkeletonSprite()));
        creature(list, Shelf.ENEMIES, RivalServant.class, "actors.mobs.rivalservant.clone", "actors.mobs.rivalservant.desc", () -> heroImage(Assets.ROGUE, 2));
        for (int role = 0; role < 4; role++) {
            final int tint = new int[]{0xFFFFFF, 0xA6CCFF, 0xFFBF73, 0xA6FFA6}[role];
            final String key = "actors.mobs.rebornking$retainer.";
            final int kind = role;
            list.add(new Entry(key + "name_" + kind, Shelf.ENEMIES, () -> Messages.get(key + "name_" + kind),
                    () -> Messages.get(key + "desc_" + kind), () -> {
                Image image = idleImage(new UndeadSprite());
                image.hardlight(tint);
                return image;
            }, false, null, null, () -> LibraryCreatureStats.retainer(kind)));
            Entry retainer = list.get(list.size() - 1);
            retainer.region = Region.CITY;
            retainer.depth = 20;
        }
        creature(list, Shelf.PEOPLE, BarbecueKing.class, "actors.mobs.npcs.barbecueking.name", "actors.mobs.npcs.barbecueking.desc", () -> idleImage(new RatKingSprite()));
        creature(list, Shelf.PEOPLE, Bee.class, "actors.mobs.bee.name", "actors.mobs.bee.desc", () -> idleImage(new BeeSprite()));
        creature(list, Shelf.PEOPLE, Region.CAVES, 12, Blacksmith.class, "actors.mobs.npcs.blacksmith.name", "library.actors.mobs.npcs.blacksmith.desc", () -> idleImage(new BlacksmithSprite()));
        creature(list, Shelf.PEOPLE, com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Crab.class, "actors.mobs.npcs.crab.name", "custom.generated.entry_summoned_crabs_will_protect_their_master_mage_f0480efbe2", () -> idleImage(new CrabSprite()));
        creature(list, Shelf.PEOPLE, Region.SEWERS, 2, Ghost.class, "actors.mobs.npcs.ghost.name", "actors.mobs.npcs.ghost.desc", () -> idleImage(new GhostSprite()));
        creature(list, Shelf.PEOPLE, Region.CITY, 17, Imp.class, "actors.mobs.npcs.imp.name", "actors.mobs.npcs.imp.desc", () -> idleImage(new ImpSprite()));
        creature(list, Shelf.PEOPLE, Region.CITY, 21, ImpShopkeeper.class, "actors.mobs.npcs.impshopkeeper.name", "library.actors.mobs.npcs.impshopkeeper.desc", () -> idleImage(new ImpSprite()));
        creature(list, Shelf.PEOPLE, com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Rat.class, "actors.mobs.npcs.rat.name", "custom.generated.entry_summoned_rats_will_protect_their_master_mage_2e1307be37", () -> idleImage(new RatSprite()));
        creature(list, Shelf.PEOPLE, Region.SEWERS, 5, RatKing.class, "actors.mobs.npcs.ratking.name", "actors.mobs.npcs.ratking.desc", () -> idleImage(new RatKingSprite()));
        creature(list, Shelf.PEOPLE, RatScout.class, "actors.mobs.npcs.ratscout.name", "library.creatures.ratscout.desc", () -> idleImage(new RatScoutSprite()));
        creature(list, Shelf.PEOPLE, Shopkeeper.class, "actors.mobs.npcs.shopkeeper.name", "actors.mobs.npcs.shopkeeper.desc", () -> idleImage(new ShopkeeperSprite()));
        creature(list, Shelf.PEOPLE, com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Skeleton.class, "actors.mobs.npcs.skeleton.name", "custom.generated.entry_summoned_skeletons_will_protect_their_master_mag_23733b5d57", () -> idleImage(new SkeletonSprite()));
        creature(list, Shelf.PEOPLE, Region.PRISON, 7, Wandmaker.class, "actors.mobs.npcs.wandmaker.name", "actors.mobs.npcs.wandmaker.desc", () -> idleImage(new WandmakerSprite()));
        creature(list, Shelf.PEOPLE, WandOfFlock.Sheep.class, "actors.mobs.npcs.sheep.name", "actors.mobs.npcs.sheep.desc", LibraryCatalog::sheepImage);
        creature(list, Shelf.PEOPLE, MirrorImage.class, "actors.mobs.npcs.mirrorimage.name", "actors.mobs.npcs.mirrorimage.desc", () -> heroImage(Assets.WARRIOR, 1));
        creature(list, Shelf.PEOPLE, ShadowClone.class, "actors.mobs.rivalservant.clone", "library.creatures.shadowclone.desc", () -> heroImage(Assets.ROGUE, 1));
        creature(list, Shelf.PEOPLE, SummonedPet.class, "actors.mobs.npcs.summonedpet.skeleton_archer", "library.creatures.skeletonarcher.desc", () -> idleImage(new SkeletonSprite()));
        creature(list, Shelf.PEOPLE, SummonedPet.class, "actors.mobs.npcs.summonedpet.fire_elemental", "library.creatures.fireelemental.desc", () -> idleImage(new ElementalSprite()));
        hero(list, HeroClass.WARRIOR, HeroSubClass.GLADIATOR, HeroSubClass.BERSERKER);
        hero(list, HeroClass.MAGE, HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK);
        hero(list, HeroClass.ROGUE, HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER);
        hero(list, HeroClass.HUNTRESS, HeroSubClass.SNIPER, HeroSubClass.WARDEN);
        hero(list, HeroClass.NECROMANCER, HeroSubClass.SPIRIT_BINDER, HeroSubClass.LICH);
        hero(list, HeroClass.MERCENARY, HeroSubClass.MARSHAL, HeroSubClass.EXECUTIONER);
        for (HiredMerc.MERC_TYPES merc : HiredMerc.MERC_TYPES.values()) {
            final String sheet = MercSprite.spritesheet(merc);
            list.add(new Entry("mercenary." + merc.name(), Shelf.PEOPLE,
                    () -> Messages.maybeTranslate(merc.getName()),
                    () -> Messages.maybeTranslate(merc.getDescription()),
                    () -> heroImage(sheet, merc == HiredMerc.MERC_TYPES.ArcherMaiden ? 0 : 5),
                    false, null, null, LibraryCreatureStats::mercenary));
        }
        for (HeroClass hero : new HeroClass[]{HeroClass.WARRIOR, HeroClass.MAGE, HeroClass.ROGUE,
                HeroClass.HUNTRESS, HeroClass.NECROMANCER, HeroClass.MERCENARY}) {
            skills(list, hero, skillTree(hero));
        }
        for (AlchemyRecipe recipe : AlchemyRecipe.values()) {
            list.add(new Entry(recipe.name(), Shelf.RECIPES,
                    () -> findType(recipe.lorePotion()).name(),
                    () -> text("recipe_desc", findType(recipe.seed).name(), findType(recipe.lorePotion()).name()),
                    () -> new ItemSprite(ItemSpriteSheet.RECIPE_HEALING + recipe.ordinal(), null),
                    false, null, null, null));
        }
        return Collections.unmodifiableList(list);
    }
}
