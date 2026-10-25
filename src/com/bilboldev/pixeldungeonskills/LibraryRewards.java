package com.bilboldev.pixeldungeonskills;

import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Badges.Badge;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Entry;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Section;
import com.bilboldev.pixeldungeonskills.LibraryCatalog.Shelf;
import com.bilboldev.pixeldungeonskills.actors.mobs.Mob;
import com.bilboldev.pixeldungeonskills.actors.mobs.Thief;
import com.bilboldev.pixeldungeonskills.items.Corpse;
import com.bilboldev.pixeldungeonskills.items.Generator;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfHealing;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.sprites.ItemSprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LibraryRewards {
    private LibraryRewards() { }

    public static List<Badge> badges(Entry entry) {
        Set<Badge> badges = new LinkedHashSet<>();
        String key = entry.key;
        if (entry.shelf.section == Section.ITEMS) {
            if (entry.countsForIdentification) {
                switch (entry.shelf) {
                    case POTIONS: badges.add(Badge.ALL_POTIONS_IDENTIFIED); break;
                    case SCROLLS: badges.add(Badge.ALL_SCROLLS_IDENTIFIED); break;
                    case RINGS: badges.add(Badge.ALL_RINGS_IDENTIFIED); break;
                    case WANDS: badges.add(Badge.ALL_WANDS_IDENTIFIED); break;
                    default: break;
                }
                badges.add(Badge.ALL_ITEMS_IDENTIFIED);
            }
            if (entry.shelf == Shelf.FOOD) {
                Collections.addAll(badges, Badge.FOOD_EATEN_1, Badge.FOOD_EATEN_2,
                        Badge.FOOD_EATEN_3, Badge.FOOD_EATEN_4);
            }
            if (entry.shelf == Shelf.SEEDS) {
                Collections.addAll(badges, Badge.POTIONS_COOKED_1, Badge.POTIONS_COOKED_2,
                        Badge.POTIONS_COOKED_3, Badge.POTIONS_COOKED_4);
            }
        } else if (entry.shelf.section == Section.CREATURES && countsAsKill(entry)) {
            Collections.addAll(badges, Badge.MONSTERS_SLAIN_1, Badge.MONSTERS_SLAIN_2,
                    Badge.MONSTERS_SLAIN_3, Badge.MONSTERS_SLAIN_4, Badge.NIGHT_HUNTER);
        }
        switch (key) {
            case "items.amulet.name":
                Collections.addAll(badges, Badge.VICTORY, Badge.VICTORY_ALL_CLASSES, Badge.HAPPY_END, Badge.CHAMPION);
                break;
            case "items.gold.name":
                Collections.addAll(badges, Badge.GOLD_COLLECTED_1, Badge.GOLD_COLLECTED_2,
                        Badge.GOLD_COLLECTED_3, Badge.GOLD_COLLECTED_4);
                break;
            case "items.potions.potionofstrength.name": case "items.potions.potionofmight.name":
                Collections.addAll(badges, Badge.STRENGTH_ATTAINED_1, Badge.STRENGTH_ATTAINED_2,
                        Badge.STRENGTH_ATTAINED_3, Badge.STRENGTH_ATTAINED_4);
                break;
            case "items.potions.potionofexperience.name":
                Collections.addAll(badges, Badge.LEVEL_REACHED_1, Badge.LEVEL_REACHED_2,
                        Badge.LEVEL_REACHED_3, Badge.LEVEL_REACHED_4);
                break;
            case "items.scrolls.scrollofidentify.name":
                Collections.addAll(badges, Badge.ALL_POTIONS_IDENTIFIED, Badge.ALL_RINGS_IDENTIFIED, Badge.ALL_WANDS_IDENTIFIED);
                break;
            case "items.scrolls.scrollofupgrade.name": itemBadges(badges); break;
            case "items.scrolls.scrollofenchantment.name": badges.add(Badge.GRIM_WEAPON); break;
            case "items.rings.ringofhaggler.name": badges.add(Badge.RING_OF_HAGGLER); break;
            case "items.rings.ringofthorns.name": badges.add(Badge.RING_OF_THORNS); break;
            case "items.bags.seedpouch.name": case "items.bags.scrollholder.name": case "items.bags.magicalholster.name":
                badges.add(Badge.ALL_BAGS_BOUGHT);
                break;
            case "actors.mobs.goo.name":
                Collections.addAll(badges, Badge.BOSS_SLAIN_1, Badge.BOSS_SLAIN_1_ALL_CLASSES);
                break;
            case "actors.mobs.tengu.name": badges.add(Badge.BOSS_SLAIN_2); break;
            case "actors.mobs.dm300.name":
                Collections.addAll(badges, Badge.BOSS_SLAIN_3, Badge.BOSS_SLAIN_3_ALL_SUBCLASSES);
                break;
            case "actors.mobs.dwarfking.name": badges.add(Badge.BOSS_SLAIN_4); break;
            case "actors.mobs.albino.name": case "actors.mobs.bandit.name": case "actors.mobs.shielded.name":
            case "actors.mobs.senior.name": case "actors.mobs.acidic.name":
                badges.add(Badge.RARE);
                break;
            case "actors.mobs.piranha.name": badges.add(Badge.PIRANHAS); break;
            default: break;
        }
        if (entry.shelf == Shelf.RIVALS) {
            Collections.addAll(badges, Badge.BOSS_SLAIN_1, Badge.BOSS_SLAIN_2, Badge.BOSS_SLAIN_3,
                    Badge.BOSS_SLAIN_4, Badge.BOSS_SLAIN_1_ALL_CLASSES, Badge.BOSS_SLAIN_3_ALL_SUBCLASSES);
        }
        return new ArrayList<>(badges);
    }

    private static void itemBadges(Set<Badge> badges) {
        Collections.addAll(badges, Badge.ITEM_LEVEL_1, Badge.ITEM_LEVEL_2, Badge.ITEM_LEVEL_3, Badge.ITEM_LEVEL_4);
    }

    private static boolean countsAsKill(Entry entry) {
        if (entry.shelf != Shelf.ENEMIES && entry.shelf != Shelf.BOSSES && entry.shelf != Shelf.RIVALS) return false;
        switch (entry.key) {
            case "actors.mobs.cursepersonification.name": case "actors.mobs.npcs.echoreflection.name":
            case "actors.mobs.tengushadow.name": case "actors.mobs.yogdzewa$larva.name":
            case "actors.mobs.rivalservant.name": case "actors.mobs.rivalservant.clone":
            case "actors.mobs.npcs.ratking.name":
                return false;
            default: return !entry.key.startsWith("actors.mobs.rebornking$retainer.");
        }
    }

    public static List<Drop> drops(Entry entry) {
        return drops(entry, 0);
    }

    public static List<Drop> drops(Entry entry, int depth) {
        List<Drop> drops = new ArrayList<>();
        if (entry.shelf.section != Section.CREATURES) return drops;
        switch (entry.key) {
            case "actors.mobs.bat.name": case "actors.mobs.swarm.name":
                drops.add(new Drop("items.potions.potionofhealing.name"));
                drops.add(new Drop("items.potions.potionofrejuvenation.name"));
                break;
            case "actors.mobs.gnoll.name": case "actors.mobs.brute.name": case "actors.mobs.shielded.name":
                drops.add(new Drop("items.gold.name"));
                break;
            case "actors.mobs.crab.name": case "actors.mobs.spinner.name": case "actors.mobs.piranha.name":
                drops.add(new Drop("items.food.mysterymeat.name"));
                break;
            case "actors.mobs.scorpio.name": case "actors.mobs.acidic.name":
                drops.add(new Drop("items.potions.potionofhealing.name"));
                drops.add(new Drop("items.potions.potionofrejuvenation.name"));
                drops.add(new Drop("items.food.mysterymeat.name"));
                break;
            case "actors.mobs.elemental$fireelemental.name":
                drops.add(new Drop("items.potions.potionofliquidflame.name"));
                break;
            case "actors.mobs.eye.name": drops.add(new Drop("items.dewdrop.name")); break;
            case "actors.mobs.monk.name": case "actors.mobs.senior.name":
                drops.add(new Drop("items.food.food.name"));
                drops.add(new Drop("items.quest.dwarftoken.name", "imp_drop"));
                break;
            case "actors.mobs.golem.name": drops.add(new Drop("items.quest.dwarftoken.name", "imp_drop")); break;
            case "actors.mobs.shaman.name": addPool(drops, Generator.Category.SCROLL, false); break;
            case "actors.mobs.warlock.name": addPool(drops, Generator.Category.POTION, false); break;
            case "actors.mobs.skeleton.name": addPool(drops, Generator.Category.WEAPON, false); break;
            case "actors.mobs.statue.name": addPool(drops, Generator.Category.WEAPON, true); break;
            case "actors.mobs.mimic.name": drops.add(new Drop(null, "chest_drop")); break;
            case "actors.mobs.thief.name": case "actors.mobs.bandit.name":
                drops.add(new Drop("items.rings.ringofhaggler.name"));
                drops.add(new Drop("items.gold.name", "thief_gold"));
                drops.add(new Drop(null, "stolen_drop"));
                break;
            case "actors.mobs.succubus.name": drops.add(new Drop("items.scrolls.scrolloflullaby.name")); break;
            case "actors.mobs.rimebat.name": case "actors.mobs.iceclaw.name": case "actors.mobs.frostback.name":
            case "actors.mobs.shardling.name": case "actors.mobs.hollowminer.name": case "actors.mobs.rimehorn.name":
                drops.add(new Drop("items.potions.potionoffrost.name"));
                break;
            case "actors.mobs.fetidrat.name": drops.add(new Drop("items.quest.ratskull.name")); break;
            case "actors.mobs.npcs.echoreflection.name":
                drops.add(new Drop("custom.generated.entry_scroll_of_skill_eee69d0bc2"));
                break;
            case "actors.mobs.goo.name":
                drops.add(new Drop("items.keys.skeletonkey.name"));
                drops.add(new Drop("items.artifacts.lloydsbeacon.name"));
                break;
            case "actors.mobs.tengu.name":
                drops.add(new Drop("items.keys.skeletonkey.name"));
                drops.add(new Drop("custom.generated.entry_tome_of_mastery_edf1f9ccdf"));
                drops.add(new Drop("items.scrolls.scrolloffrostlevel.name"));
                break;
            case "actors.mobs.oldrime.name":
                drops.add(new Drop("items.weapon.ice.glaciercleaver.name", "ice_weapon_drop"));
                drops.add(new Drop("items.weapon.ice.hoarfroststaff.name", "ice_weapon_drop"));
                drops.add(new Drop("items.weapon.ice.rimepick.name", "ice_weapon_drop"));
                drops.add(new Drop("items.weapon.ice.frostbow.name", "ice_weapon_drop"));
                drops.add(new Drop("items.weapon.ice.gravefrostscythe.name", "ice_weapon_drop"));
                drops.add(new Drop("items.weapon.ice.frostbiterifle.name", "ice_weapon_drop"));
                break;
            case "actors.mobs.dm300.name":
                drops.add(new Drop("items.keys.skeletonkey.name"));
                drops.add(new Drop("items.rings.ringofthorns.name"));
                break;
            case "actors.mobs.dwarfking.name":
                drops.add(new Drop("items.keys.skeletonkey.name"));
                drops.add(new Drop("custom.generated.entry_armor_kit_93e26fc37c"));
                break;
            case "actors.mobs.yogdzewa.name": drops.add(new Drop("items.keys.skeletonkey.name")); break;
            default: break;
        }
        if (entry.key.equals("actors.mobs.rat.name") || entry.key.equals("actors.mobs.albino.name")
                || entry.key.equals("actors.mobs.gnoll.name") || entry.key.equals("actors.mobs.crab.name")) {
            drops.add(new Drop("items.artifacts.driedrose.name", "ghost_drop"));
        }
        if (entry.shelf == Shelf.RIVALS) {
            drops.add(new Drop("items.keys.skeletonkey.name"));
            if (depth == 0 || depth == 5) drops.add(new Drop("items.artifacts.lloydsbeacon.name"));
            if (depth == 0 || depth == 10) drops.add(new Drop("custom.generated.entry_tome_of_mastery_edf1f9ccdf"));
            if (depth == 0 || depth == 10) drops.add(new Drop("items.scrolls.scrolloffrostlevel.name"));
            if (depth == 0 || depth == 15) drops.add(new Drop("items.rings.ringofthorns.name"));
            if (depth == 0 || depth == 20) drops.add(new Drop("custom.generated.entry_armor_kit_93e26fc37c"));
        }
        if (entry.shelf == Shelf.ENEMIES && countsAsKill(entry)) {
            drops.add(new Drop("items.corpse.name", "corpse_drop"));
        }
        if (entry.creatureStats().championSkills().length > 0) drops.add(new Drop(null, "champion_drop"));
        return drops;
    }

    private static void addPool(List<Drop> drops, Generator.Category category, boolean statue) {
        for (Class<?> type : Generator.lootClasses(category)) {
            if (statue && !MeleeWeapon.class.isAssignableFrom(type)) continue;
            Entry item = LibraryCatalog.findType(type);
            if (item != null) drops.add(new Drop(item.key, statue ? "statue_drop" : null));
            if (type == PotionOfHealing.class) {
                drops.add(new Drop("items.potions.potionofrejuvenation.name"));
            }
        }
    }

    public static final class Drop {
        public final String itemKey;
        private final String note;

        private Drop(String itemKey) {
            this(itemKey, null);
        }

        private Drop(String itemKey, String note) {
            this.itemKey = itemKey;
            this.note = note;
        }

        public String key() {
            return itemKey == null ? "random:" + note : itemKey;
        }

        boolean matches(Item item, Mob source, boolean championReward) {
            if (championReward) return "champion_drop".equals(note);
            if (itemKey != null) {
                Entry entry = LibraryCatalog.find(item);
                return entry != null && itemKey.equals(entry.key);
            }
            switch (note) {
                case "chest_drop": return !(item instanceof Corpse);
                case "stolen_drop": return source instanceof Thief && ((Thief)source).item == item;
                default: return false;
            }
        }

        public Image image() {
            if (itemKey != null) return LibraryCatalog.item(itemKey).image();
            ItemSprite image = new ItemSprite(ItemSpriteSheet.SMTH, null);
            image.tint(0xDDD4C5, 1f);
            return image;
        }

        public String name() {
            return itemKey == null ? LibraryCatalog.text("random_item") : LibraryCatalog.item(itemKey).name();
        }

        public String info() {
            if (itemKey == null) return LibraryCatalog.text(note == null ? "random_drop" : note);
            Entry item = LibraryCatalog.item(itemKey);
            String info = item.info();
            if (note != null) info += "\n\n" + LibraryCatalog.text(note);
            return info;
        }
    }
}
