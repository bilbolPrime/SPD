package com.bilboldev.pixeldungeonskills.levels.gauntlet;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.armor.*;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfEnchantment;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfUpgrade;
import com.bilboldev.pixeldungeonskills.items.wands.*;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.*;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.*;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.*;
import com.bilboldev.utils.Random;
import java.util.ArrayList;

public enum GauntletProfile {
    WARRIOR(4, new int[]{6, 4, 9, 6, 10, 7, 12, 9, 11, 4}, new Class<?>[][]{
        {
            Rat.class, Rat.class, Gnoll.class
        },
        {
            Gnoll.class, Crab.class, Gnoll.class, Crab.class
        },
        {
            Skeleton.class, Thief.class, Skeleton.class
        },
        {
            Skeleton.class, Shaman.class, Thief.class, Skeleton.class, Shaman.class
        },
        {
            Bat.class, Brute.class, Bat.class, Bat.class
        },
        {
            Brute.class, Spinner.class, Brute.class, Shaman.class, Spinner.class, Brute.class
        },
        {
            Monk.class, Elemental.class, Warlock.class, Monk.class, Elemental.class, Warlock.class
        },
        {
            Golem.class, Monk.class, Warlock.class, Monk.class, Golem.class, Warlock.class, Monk.class
        },
        {
            Succubus.class, Scorpio.class, Succubus.class, Eye.class, Succubus.class, Scorpio.class, Eye.class, Succubus.class
        }
    }),

    MAGE(5, new int[]{4, 5, 6, 4, 7, 5, 8, 6, 7, 3}, new Class<?>[][]{
        {
            Rat.class, Rat.class
        },
        {
            Gnoll.class, Gnoll.class, Crab.class
        },
        {
            Skeleton.class, Skeleton.class, Thief.class
        },
        {
            Skeleton.class, Skeleton.class, Thief.class, Shaman.class
        },
        {
            Brute.class, Brute.class, Bat.class
        },
        {
            Brute.class, Brute.class, Spinner.class, Bat.class, Shaman.class
        },
        {
            Golem.class, Monk.class, Monk.class, Warlock.class
        },
        {
            Golem.class, Golem.class, Monk.class, Monk.class, Warlock.class
        },
        {
            Succubus.class, Succubus.class, Scorpio.class, Eye.class, Succubus.class, Scorpio.class
        }
    }),

    ROGUE(4, new int[]{9, 7, 11, 8, 12, 9, 13, 10, 12, 5}, new Class<?>[][]{
        {
            Rat.class, Gnoll.class
        },
        {
            Gnoll.class, Crab.class, Gnoll.class
        },
        {
            Thief.class, Skeleton.class
        },
        {
            Thief.class, Shaman.class, Thief.class, Skeleton.class
        },
        {
            Bat.class, Brute.class, Bat.class
        },
        {
            Spinner.class, Brute.class, Bat.class, Spinner.class, Bat.class
        },
        {
            Warlock.class, Monk.class, Elemental.class, Warlock.class, Monk.class
        },
        {
            Monk.class, Warlock.class, Golem.class, Warlock.class, Monk.class, Elemental.class
        },
        {
            Scorpio.class, Succubus.class, Eye.class, Succubus.class, Scorpio.class, Succubus.class
        }
    }),

    HUNTRESS(6, new int[]{3, 4, 5, 3, 6, 4, 6, 5, 6, 3}, new Class<?>[][]{
        {
            Rat.class, Rat.class
        },
        {
            Crab.class, Gnoll.class, Rat.class
        },
        {
            Thief.class, Skeleton.class
        },
        {
            Skeleton.class, Thief.class, Shaman.class, Skeleton.class
        },
        {
            Bat.class, Bat.class, Brute.class, Bat.class
        },
        {
            Bat.class, Spinner.class, Brute.class, Spinner.class, Bat.class
        },
        {
            Monk.class, Elemental.class, Monk.class, Warlock.class, Elemental.class
        },
        {
            Monk.class, Golem.class, Warlock.class, Monk.class, Elemental.class, Warlock.class
        },
        {
            Succubus.class, Scorpio.class, Succubus.class, Eye.class, Scorpio.class, Succubus.class, Succubus.class
        }
    }),

    NECROMANCER(5, new int[]{5, 6, 7, 5, 8, 6, 9, 7, 8, 4}, new Class<?>[][]{
        {
            Rat.class, Rat.class, Rat.class
        },
        {
            Rat.class, Gnoll.class, Gnoll.class, Crab.class
        },
        {
            Skeleton.class, Skeleton.class, Skeleton.class, Thief.class
        },
        {
            Skeleton.class, Shaman.class, Skeleton.class, Thief.class, Skeleton.class
        },
        {
            Bat.class, Bat.class, Brute.class, Bat.class
        },
        {
            Brute.class, Spinner.class, Bat.class, Brute.class, Bat.class, Spinner.class
        },
        {
            Monk.class, Warlock.class, Monk.class, Elemental.class, Warlock.class, Monk.class
        },
        {
            Warlock.class, Golem.class, Monk.class, Warlock.class, Elemental.class, Monk.class, Monk.class
        },
        {
            Succubus.class, Eye.class, Scorpio.class, Succubus.class, Succubus.class, Scorpio.class, Eye.class
        }
    }),

    MERCENARY(6, new int[]{4, 3, 6, 4, 7, 5, 7, 6, 8, 3}, new Class<?>[][]{
        {
            Rat.class, Gnoll.class, Rat.class
        },
        {
            Gnoll.class, Crab.class, Gnoll.class, Gnoll.class
        },
        {
            Skeleton.class, Thief.class, Skeleton.class
        },
        {
            Shaman.class, Skeleton.class, Thief.class, Shaman.class, Skeleton.class
        },
        {
            Brute.class, Bat.class, Brute.class, Bat.class
        },
        {
            Brute.class, Spinner.class, Brute.class, Bat.class, Spinner.class, Brute.class
        },
        {
            Warlock.class, Monk.class, Elemental.class, Golem.class, Monk.class
        },
        {
            Golem.class, Warlock.class, Monk.class, Warlock.class, Elemental.class, Golem.class
        },
        {
            Scorpio.class, Succubus.class, Eye.class, Scorpio.class, Succubus.class, Scorpio.class, Eye.class
        }
    });

    public final int spawnDistance;
    private final int[] cover;
    private final Class<?>[][] waves;

    GauntletProfile(int spawnDistance, int[] cover, Class<?>[][] waves) {
        this.spawnDistance = spawnDistance;
        this.cover = cover;
        this.waves = waves;
    }

    public static GauntletProfile forHero(HeroClass heroClass) {
        return valueOf(heroClass.name());
    }

    public int coverBudget(int wave) {
        return cover[wave - 1];
    }

    public int enemyCount(int wave) {
        return wave == 10 ? 1 : waves[wave - 1].length;
    }

    public Class<?> enemyType(int wave, int index) {
        return wave == 10 ? GauntletRatKing.class : waves[wave - 1][index];
    }

    public ArrayList<Item> rewards(int wave) {
        ArrayList<Item> result = new ArrayList<>();
        result.add(weaponReward(wave));
        result.add(armorReward(wave));
        result.add(new ScrollOfUpgrade().identify());
        result.add(new ScrollOfEnchantment().identify());
        if (this == HUNTRESS) result.add(new Arrow(12 + 2 * wave).identify());
        return result;
    }

    private Item weaponReward(int wave) {

        int stage = Math.min(3, wave / 2);
        Item[] options;
        switch (this) {
            case MAGE:
                options = stage == 0 ? new Item[]{new WandOfMagicMissile(), new WandOfFirebolt()}
                        : stage == 1 ? new Item[]{new WandOfFirebolt(), new WandOfLightning()}
                        : new Item[]{new WandOfLightning(), new WandOfDisintegration(), new WandOfAvalanche()};
                break;
            case ROGUE:
                options = stage == 0 ? new Item[]{new Dagger(), new Knuckles()}
                        : stage == 1 ? new Item[]{new Quarterstaff(), new Sword()}
                        : stage == 2 ? new Item[]{new Sword(), new Longsword()}
                        : new Item[]{new Longsword(), new BattleAxe()};
                break;
            case HUNTRESS:
                options = stage == 0 ? new Item[]{new Bow(), new Boomerang()}
                        : stage == 1 ? new Item[]{new FrostBow(), new Boomerang()}
                        : new Item[]{new FrostBow(), new FlameBow(), new Boomerang()};
                break;
            case NECROMANCER:
                options = stage == 0 ? new Item[]{new WandOfPoison(), new WandOfMagicMissile()}
                        : stage == 1 ? new Item[]{new WandOfSlowness(), new WandOfPoison(), new SoulBlade()}
                        : new Item[]{new WandOfDisintegration(), new SoulBlade(), new NecroBlade()};
                break;
            case MERCENARY:
                options = stage == 0 ? new Item[]{new Pistol()}
                        : stage == 1 ? new Item[]{new Pistol(), new Blunderbuss()}
                        : stage == 2 ? new Item[]{new Blunderbuss(), new Rifle()}
                        : new Item[]{new Rifle(), new Mortar()};
                break;
            default:
                options = stage == 0 ? new Item[]{new Quarterstaff(), new Spear()}
                        : stage == 1 ? new Item[]{new Mace(), new Sword()}
                        : stage == 2 ? new Item[]{new Longsword(), new BattleAxe()}
                        : new Item[]{new WarHammer(), new Glaive()};
        }
        Item item = Random.element(options);
        int upgrades = equipmentUpgrades(wave);
        if (this == HUNTRESS || this == ROGUE) upgrades++;
        item.upgrade(upgrades);
        if (item instanceof MeleeWeapon) {

            item.upgrade(Math.max(0, ((MeleeWeapon)item).STR() - Dungeon.hero.STR()));
        }
        return item.identify();
    }

    private Item armorReward(int wave) {
        int tier;
        switch (this) {
            case MAGE: case NECROMANCER: tier = wave < 4 ? 1 : wave < 7 ? 2 : 3;
            break;
            case ROGUE: case HUNTRESS: tier = wave < 6 ? 2 : 3;
            break;
            case MERCENARY: tier = Math.min(4, 2 + wave / 3);
            break;
            default: tier = Math.min(5, 1 + (wave + 1) / 2);
        }
        Armor[] tiers = {new ClothArmor(), new LeatherArmor(), new MailArmor(), new ScaleArmor(), new PlateArmor()};
        return tiers[tier - 1].upgrade(equipmentUpgrades(wave)).identify();
    }

    private static int equipmentUpgrades(int wave) {

        return Random.IntRange(Math.max(1, wave / 3), (wave + 1) / 2);
    }
}
