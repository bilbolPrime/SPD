package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.items.Item;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.items.armor.Armor;
import com.bilboldev.pixeldungeonskills.items.quest.Pickaxe;
import com.bilboldev.pixeldungeonskills.items.rings.Ring;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfAccuracy;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfEvasion;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfHaste;
import com.bilboldev.pixeldungeonskills.items.rings.RingOfShadows;
import com.bilboldev.pixeldungeonskills.items.wands.Wand;
import com.bilboldev.pixeldungeonskills.items.weapon.Weapon;
import com.bilboldev.pixeldungeonskills.items.weapon.gun.Gun;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.MeleeWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.melee.NecroBlade;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Bow;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.MissileWeapon;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.NinjaBomb;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.SoulCrystal;

public final class LibraryHeroBuild {
    public enum Slot { WEAPON, ARMOR, RING1, RING2 }

    public static final int MAX_LEVEL = 30;
    public static final int MAX_STRENGTH = 50;

    public HeroClass heroClass = HeroClass.WARRIOR;
    public int level = 1;
    public int strength = 11;
    public LibraryItemBuild weapon, armor, ring1, ring2;

    public void chooseClass(HeroClass heroClass) {
        requireClass(heroClass);
        this.heroClass = heroClass;
        strength = heroClass == HeroClass.WARRIOR || heroClass == HeroClass.MERCENARY ? 11 : 10;
        if (weapon != null && !accepts(Slot.WEAPON, weapon.item())) weapon = null;
    }

    public LibraryItemBuild get(Slot slot) {
        switch (slot) {
            case WEAPON: return weapon;
            case ARMOR: return armor;
            case RING1: return ring1;
            case RING2: return ring2;
            default: throw new IllegalArgumentException("Unknown equipment slot");
        }
    }

    public void set(Slot slot, LibraryItemBuild build) {
        if (build != null && !accepts(slot, build.item())) {
            throw new IllegalArgumentException("This hero cannot equip that item in this slot");
        }
        switch (slot) {
            case WEAPON: weapon = build; break;
            case ARMOR: armor = build; break;
            case RING1: ring1 = build; break;
            case RING2: ring2 = build; break;
        }
    }

    public boolean accepts(Slot slot, Item item) {
        if (item == null) return true;
        switch (slot) {
            case WEAPON:
                if (item instanceof Wand) return heroClass == HeroClass.MAGE || heroClass == HeroClass.NECROMANCER;
                if (item instanceof Bow || item instanceof Arrow || item instanceof SoulCrystal || item instanceof NinjaBomb) return false;
                if (item instanceof MissileWeapon) return heroClass == HeroClass.ROGUE || heroClass == HeroClass.HUNTRESS;
                return item instanceof MeleeWeapon || item instanceof Pickaxe;
            case ARMOR:
                return item instanceof Armor;
            case RING1:
            case RING2:
                return item instanceof Ring;
            default:
                return false;
        }
    }

    public Stats calculate() {
        requireClass(heroClass);
        if (level < 1 || level > MAX_LEVEL || strength < 1 || strength > MAX_STRENGTH) {
            throw new IllegalArgumentException("Hero level or strength is outside the calculator range");
        }
        PreviewHero hero = new PreviewHero(heroClass, level, strength);
        hero.belongings.weapon = (KindOfWeapon)equipment(Slot.WEAPON);
        hero.belongings.armor = (Armor)equipment(Slot.ARMOR);
        hero.belongings.ring1 = (Ring)equipment(Slot.RING1);
        hero.belongings.ring2 = (Ring)equipment(Slot.RING2);
        addRingStats(hero, hero.belongings.ring1);
        addRingStats(hero, hero.belongings.ring2);
        return new Stats(hero);
    }

    private Item equipment(Slot slot) {
        LibraryItemBuild build = get(slot);
        Item item = build == null ? null : build.item();
        if (!accepts(slot, item)) throw new IllegalArgumentException("Invalid equipment for this hero");
        return item;
    }

    public int armorTier() {
        return armor == null ? 0 : ((Armor)armor.item()).tier;
    }

    private static void addRingStats(Hero hero, Ring ring) {

        if (ring instanceof RingOfAccuracy) hero.buffs().add(((RingOfAccuracy)ring).new Accuracy());
        if (ring instanceof RingOfEvasion) hero.buffs().add(((RingOfEvasion)ring).new Evasion());
        if (ring instanceof RingOfHaste) hero.buffs().add(((RingOfHaste)ring).new Haste());
        if (ring instanceof RingOfShadows) hero.buffs().add(((RingOfShadows)ring).new Shadows());
    }

    private static void requireClass(HeroClass heroClass) {
        if (heroClass == null) throw new IllegalArgumentException("Choose a hero class");
        switch (heroClass) {
            case WARRIOR: case MAGE: case ROGUE: case HUNTRESS: case NECROMANCER: case MERCENARY: return;
            default: throw new IllegalArgumentException("Choose a playable hero class");
        }
    }

    private static final class PreviewHero extends Hero {
        PreviewHero(HeroClass heroClass, int level, int strength) {
            this.heroClass = heroClass;
            lvl = level;
            STR = strength;
            attackSkill += level - 1;
            defenseSkill += level - 1;
            HT += (level - 1) * 5 - (heroClass == HeroClass.HUNTRESS ? 5 : 0);
            HP = HT;
            int mana;
            switch (heroClass) {
                case MAGE: case NECROMANCER: mana = 40; break;
                case ROGUE: mana = 30; break;
                case HUNTRESS: mana = 35; break;
                default: mana = 20;
            }
            MP = MMP = mana + (level - 1) * 5;
        }
    }

    public static final class Stats {
        public final int maxHealth, maxMana, damageMin, damageMax, armorMax, accuracy, evasion, stealth;
        public final float attackTurns, moveTurns;
        public final boolean firearm;
        public final int projectiles;

        private Stats(Hero hero) {
            maxHealth = hero.HT;
            maxMana = hero.MMP;
            armorMax = hero.dr();
            accuracy = hero.attackSkill(null, 0);
            evasion = hero.defenseSkill(null);
            stealth = hero.stealth();
            moveTurns = hero.hasteTime(1f / hero.armorSpeed(1f));

            KindOfWeapon weapon = hero.belongings.weapon;
            firearm = weapon instanceof Gun;
            if (firearm) {
                Gun gun = (Gun)weapon;
                projectiles = gun.projectilesPerShot();
                float share = 1f / projectiles;
                damageMin = Math.max(1, (int)(gun.min() * share));
                damageMax = Math.max(1, (int)(gun.max() * share));
                attackTurns = hero.hasteTime(gun.firingDelay(hero));
            } else {
                projectiles = 1;
                int minimum = weapon == null ? 1 : weapon.min();
                int maximum = weapon == null ? Math.max(1, hero.STR() - 9) : weapon.max();
                if (weapon instanceof Weapon) maximum += ((Weapon)weapon).excessStrengthDamage(hero);
                if (weapon instanceof NecroBlade) maximum += Math.max(0, ((NecroBlade)weapon).charge / 10 - 1);
                damageMin = Math.max(0, minimum);
                damageMax = Math.max(0, maximum);
                attackTurns = hero.hasteTime(hero.attackDelay());
            }
        }
    }
}
