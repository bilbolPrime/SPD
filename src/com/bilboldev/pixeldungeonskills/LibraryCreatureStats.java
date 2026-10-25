package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;

public final class LibraryCreatureStats {
    public final String health, damage, armor, accuracy, evasion;
    private final String note;
    private Class<? extends Mob> champion;

    private LibraryCreatureStats(int health, int minimum, int maximum, int armor, int accuracy, int evasion) {
        this(String.valueOf(health), range(minimum, maximum), range(0, armor),
                String.valueOf(accuracy), String.valueOf(evasion), null);
    }

    private LibraryCreatureStats(String health, String damage, String armor, String accuracy, String evasion, String note) {
        this.health = health;
        this.damage = damage;
        this.armor = armor;
        this.accuracy = accuracy;
        this.evasion = evasion;
        this.note = note;
    }

    public String note() {
        return note == null ? "" : LibraryCatalog.text(note);
    }

    public RebornChampion.Skill[] championSkills() {
        return champion == null ? new RebornChampion.Skill[0] : RebornChampion.skillsFor(champion);
    }

    private LibraryCreatureStats champion(Class<? extends Mob> type) {
        champion = type;
        return this;
    }

    private static String range(int minimum, int maximum) {
        return minimum == maximum ? String.valueOf(minimum) : minimum + "-" + maximum;
    }

    public static LibraryCreatureStats creature(String name, String description) {
        String varies = LibraryCatalog.text("varies");
        switch (name) {
            case "actors.mobs.rimebat.name": return new LibraryCreatureStats(40, 7, 13, 3, 20, 20).champion(RimeBat.class);
            case "actors.mobs.iceclaw.name": return new LibraryCreatureStats(58, 9, 17, 8, 22, 9).champion(Iceclaw.class);
            case "actors.mobs.frostback.name": return new LibraryCreatureStats(76, 12, 23, 7, 24, 14).champion(Frostback.class);
            case "actors.mobs.shardling.name": return new LibraryCreatureStats(36, 10, 16, 3, 24, 12).champion(Shardling.class);
            case "actors.mobs.hollowminer.name": return new LibraryCreatureStats(50, 10, 18, 5, 23, 12).champion(HollowMiner.class);
            case "actors.mobs.rimehorn.name": return new LibraryCreatureStats(90, 14, 25, 6, 25, 10).champion(Rimehorn.class);
            case "actors.mobs.oldrime.name": return new LibraryCreatureStats(450, 18, 26, 9, 30, 18);

            case "actors.mobs.acidic.name": return new LibraryCreatureStats(95, 20, 32, 16, 36, 24);
            case "actors.mobs.scorpio.name": return new LibraryCreatureStats(95, 20, 32, 16, 36, 24).champion(Scorpio.class);
            case "actors.mobs.albino.name": return new LibraryCreatureStats(15, 1, 5, 1, 8, 3);
            case "actors.mobs.bandit.name": return new LibraryCreatureStats(20, 1, 7, 3, 12, 12);
            case "actors.mobs.thief.name": return new LibraryCreatureStats(20, 1, 7, 3, 12, 12).champion(Thief.class);
            case "actors.mobs.bat.name": return new LibraryCreatureStats(30, 6, 12, 4, 16, 15).champion(Bat.class);
            case "actors.mobs.brute.name": return new LibraryCreatureStats("40", "8-18", "0-8", "20", "15", "brute_stats").champion(Brute.class);
            case "actors.mobs.shielded.name": return new LibraryCreatureStats("40", "8-18", "0-10", "20", "20", "brute_stats");
            case "actors.mobs.crab.name": return new LibraryCreatureStats(15, 3, 6, 4, 12, 5).champion(Crab.class);
            case "actors.mobs.cursepersonification.name":
                return new LibraryCreatureStats(varies, "3-5", "0-1", varies, varies, "depth_stats");
            case "actors.mobs.dm300.name": return new LibraryCreatureStats("352", "17-23", "0-10", "28", "18", "boss_stats");
            case "actors.mobs.elemental$fireelemental.name": return new LibraryCreatureStats(65, 16, 20, 5, 25, 20).champion(Elemental.class);
            case "actors.mobs.enslavedsouls.name": return new LibraryCreatureStats(1, 10, 20, 8, 5, 1);
            case "actors.mobs.eye.name": return new LibraryCreatureStats("100", "14-20", "0-10", "30", "20", "eye_stats").champion(Eye.class);
            case "actors.mobs.fetidrat.name": return new LibraryCreatureStats(15, 2, 6, 2, 12, 5);
            case "actors.mobs.gnoll.name": return new LibraryCreatureStats(12, 2, 5, 2, 11, 4).champion(Gnoll.class);
            case "actors.mobs.golem.name": return new LibraryCreatureStats(85, 20, 40, 12, 28, 18).champion(Golem.class);
            case "actors.mobs.goo.name": return new LibraryCreatureStats("100", "4-11", "0-3", "12", "12", "boss_stats");
            case "actors.mobs.dwarfking.name": return new LibraryCreatureStats("525", "20-32", "0-12", "32", "25", "boss_stats");
            case "actors.mobs.mimic.name": return new LibraryCreatureStats(varies, varies, "0", varies, varies, "depth_stats");
            case "actors.mobs.monk.name": return new LibraryCreatureStats(70, 12, 16, 2, 30, 30).champion(Monk.class);
            case "actors.mobs.piranha.name": return new LibraryCreatureStats(varies, varies, varies, varies, varies, "depth_stats");
            case "actors.mobs.rat.name": return new LibraryCreatureStats(8, 1, 5, 1, 8, 3).champion(Rat.class);
            case "custom.generated.entry_marsupial_ranged_rat_2a05e3384d": return new LibraryCreatureStats(8, 1, 5, 1, 8, 2);
            case "custom.generated.entry_marsupial_brute_rat_bdfe6b9bc3":
                return new LibraryCreatureStats("20+", "1-5", "0-1", "8", "7", "ratbrute_stats");
            case "actors.mobs.senior.name": return new LibraryCreatureStats(70, 12, 20, 2, 30, 30);
            case "actors.mobs.shaman.name": return new LibraryCreatureStats(18, 2, 6, 4, 11, 8).champion(Shaman.class);
            case "actors.mobs.skeleton.name": return new LibraryCreatureStats(25, 3, 8, 5, 12, 9).champion(Skeleton.class);
            case "actors.mobs.spinner.name": return new LibraryCreatureStats(50, 12, 16, 6, 20, 14).champion(Spinner.class);
            case "actors.mobs.statue.name": return new LibraryCreatureStats(varies, varies, varies, varies, varies, "statue_stats");
            case "actors.mobs.succubus.name": return new LibraryCreatureStats(80, 15, 25, 10, 40, 25).champion(Succubus.class);
            case "actors.mobs.swarm.name": return new LibraryCreatureStats("80", "1-4", "0", "12", "5", "swarm_stats").champion(Swarm.class);
            case "actors.mobs.tengu.name": return new LibraryCreatureStats(180, 8, 15, 5, 20, 20);
            case "actors.mobs.tengushadow.name": return new LibraryCreatureStats(12, 3, 6, 0, 18, 8);
            case "actors.mobs.warlock.name": return new LibraryCreatureStats(70, 12, 20, 8, 25, 18).champion(Warlock.class);
            case "actors.mobs.wraith.name": return new LibraryCreatureStats("1", varies, "0", varies, varies, "depth_stats");
            case "actors.mobs.yogdzewa.name": return new LibraryCreatureStats("960", "-", "0-8", "-", "20", "yog_stats");
            case "actors.mobs.yogfist$rottingfist.name": return new LibraryCreatureStats("440", "18-26", "0-10", "34", "24", "boss_stats");
            case "actors.mobs.yogfist$burningfist.name": return new LibraryCreatureStats("360", "18-26", "0-10", "34", "24", "boss_stats");
            case "actors.mobs.yogdzewa$larva.name": return new LibraryCreatureStats(25, 15, 20, 8, 30, 20);
            case "actors.mobs.rivalservant.name": return new LibraryCreatureStats("8-32", "1-9", "0", "12-24", "4-16", "rival_stats");
            case "actors.mobs.rivalservant.clone":
                if ("library.creatures.shadowclone.desc".equals(description)) {
                    return new LibraryCreatureStats("1", varies, "0", varies, "0", "mirror_stats");
                }
                return new LibraryCreatureStats("1", "1-9", "0", "12-24", "4-16", "rival_stats");
            case "actors.mobs.npcs.crab.name":
            case "actors.mobs.npcs.rat.name":
            case "actors.mobs.npcs.skeleton.name":
            case "actors.mobs.bee.name":
            case "actors.mobs.npcs.summonedpet.skeleton_archer":
            case "actors.mobs.npcs.summonedpet.fire_elemental":
                return summoned();
            case "actors.mobs.npcs.mirrorimage.name": return new LibraryCreatureStats("1", varies, "0", varies, "0", "mirror_stats");
            case "actors.mobs.npcs.ratking.name":
                if ("gauntlet.king_desc".equals(description)) return new LibraryCreatureStats("1", "-", "0", "-", "0", "noncombatant_stats");
                return noncombatant();
            case "actors.mobs.npcs.echoreflection.name":
                return new LibraryCreatureStats(varies, varies, varies, varies, varies, "reflection_stats");
            case "actors.mobs.npcs.barbecueking.name":
            case "actors.mobs.npcs.blacksmith.name":
            case "actors.mobs.npcs.prospector.name":
            case "actors.mobs.npcs.ghost.name":
            case "actors.mobs.npcs.imp.name":
            case "actors.mobs.npcs.impshopkeeper.name":
            case "actors.mobs.npcs.ratscout.name":
            case "actors.mobs.npcs.shopkeeper.name":
            case "actors.mobs.npcs.wandmaker.name":
            case "actors.mobs.npcs.sheep.name": return noncombatant();
            default: throw new IllegalArgumentException("Missing creature stats: " + name);
        }
    }

    private static LibraryCreatureStats noncombatant() {
        return new LibraryCreatureStats("-", "-", "-", "-", "-", "noncombatant_stats");
    }

    public static LibraryCreatureStats summoned() {
        String varies = LibraryCatalog.text("varies");
        return new LibraryCreatureStats(varies, varies, "0", varies, varies, "summon_stats");
    }

    public static LibraryCreatureStats mercenary() {
        String varies = LibraryCatalog.text("varies");
        return new LibraryCreatureStats(varies, varies, "0", varies, varies, "mercenary_stats");
    }

    public static LibraryCreatureStats retainer(int role) {
        return new LibraryCreatureStats(role == 0 ? "44" : "30", "10-17", role == 0 ? "0-5" : "0-2",
                "28", "20", "retainer_stats");
    }

    public static LibraryCreatureStats rival(HeroClass hero, int depth) {
        if (depth < 5 || depth > 20 || depth % 5 != 0) throw new IllegalArgumentException("Invalid rival depth");
        int tier = depth / 5 - 1;
        int[] health;
        switch (hero) {
            case WARRIOR: health = new int[]{135, 240, 465, 695}; break;
            case MAGE: health = new int[]{95, 175, 330, 490}; break;
            case ROGUE: case HUNTRESS: health = new int[]{100, 180, 350, 525}; break;
            case NECROMANCER: health = new int[]{90, 160, 310, 465}; break;
            case MERCENARY: health = new int[]{110, 200, 385, 580}; break;
            default: throw new IllegalArgumentException("Unknown rival hero");
        }
        int armor = new int[]{2, 4, 7, 10}[tier];
        if (hero == HeroClass.WARRIOR) armor += 2;
        if (hero == HeroClass.MAGE || hero == HeroClass.NECROMANCER) armor /= 2;
        int evasion = new int[]{8, 14, 18, 24}[tier] + (hero == HeroClass.ROGUE ? 2 : 0);
        return new LibraryCreatureStats(health[tier], new int[]{4, 8, 13, 18}[tier],
                new int[]{8, 13, 20, 26}[tier], armor, new int[]{12, 20, 28, 32}[tier], evasion);
    }
}
