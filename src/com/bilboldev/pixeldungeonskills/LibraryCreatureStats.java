package com.bilboldev.pixeldungeonskills;

import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.buffs.Charm;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frostbite;
import com.bilboldev.pixeldungeonskills.actors.buffs.Paralysis;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion;
import com.bilboldev.pixeldungeonskills.actors.buffs.Roots;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.buffs.Vertigo;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfPsionicBlast;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfFirebolt;
import com.bilboldev.pixeldungeonskills.items.wands.WandOfDisintegration;
import com.bilboldev.pixeldungeonskills.levels.traps.LightningTrap;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Death;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Leech;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import java.util.EnumSet;
import java.util.Set;

public final class LibraryCreatureStats {
    private enum Effect {
        ICE("ice", Frostbite.class),
        FREEZING("freezing", Frost.class),
        FIRE("fire", WandOfFirebolt.class),
        BURNING("burning", Burning.class),
        POISON("poison", Poison.class),
        TOXIC_GAS("toxic_gas", ToxicGas.class),
        PARALYSIS("paralysis", Paralysis.class),
        ROOTS("roots", Roots.class),
        FEAR("fear", Terror.class),
        AMOK("amok", Amok.class),
        SLEEP("sleep", Sleep.class),
        CHARM("charm", Charm.class),
        VERTIGO("vertigo", Vertigo.class),
        INSTANT_DEATH("grim", Death.class),
        LIFE_STEAL("life_steal", Leech.class),
        PSIONIC("psionic", ScrollOfPsionicBlast.class),
        LIGHTNING_TRAPS("lightning_traps", LightningTrap.Electricity.class),
        DISINTEGRATION("disintegration", WandOfDisintegration.class);

        final String key;
        final Class<?> effect;

        Effect(String key, Class<?> effect) {
            this.key = key;
            this.effect = effect;
        }
    }

    private static EnumSet<Effect> effects(Set<Class<?>> types) {
        EnumSet<Effect> effects = EnumSet.noneOf(Effect.class);
        for (Effect effect : Effect.values()) {
            if (types.contains(effect.effect)) effects.add(effect);
        }
        return effects;
    }

    private static EnumSet<Effect> immunityEffects(Class<?> type) {
        EnumSet<Effect> effects = EnumSet.noneOf(Effect.class);
        if (type == null) return effects;
        if (IceMob.class.isAssignableFrom(type) || type == OldRime.class) effects.add(Effect.ICE);
        else if (Brute.class.isAssignableFrom(type) || type == Eye.class) effects.add(Effect.FEAR);
        else if (type == CursePersonification.class)
            effects.addAll(EnumSet.of(Effect.INSTANT_DEATH, Effect.FEAR, Effect.PARALYSIS, Effect.ROOTS));
        else if (type == DM300.class || type == RebornDM300.class) effects.add(Effect.TOXIC_GAS);
        else if (type == Elemental.class) effects.addAll(EnumSet.of(Effect.FIRE, Effect.PSIONIC));
        else if (type == FetidRat.class) effects.add(Effect.PARALYSIS);
        else if (type == Golem.class) effects.addAll(EnumSet.of(Effect.AMOK, Effect.FEAR, Effect.SLEEP));
        else if (Monk.class.isAssignableFrom(type)) effects.addAll(EnumSet.of(Effect.AMOK, Effect.FEAR));
        else if (type == Piranha.class)
            effects.addAll(EnumSet.of(Effect.FREEZING, Effect.BURNING, Effect.TOXIC_GAS,
                    Effect.PARALYSIS, Effect.ROOTS));
        else if (type == Skeleton.class) effects.add(Effect.INSTANT_DEATH);
        else if (type == Spinner.class) effects.add(Effect.ROOTS);
        else if (type == Statue.class) effects.add(Effect.LIFE_STEAL);
        else if (type == Succubus.class) effects.add(Effect.SLEEP);
        else if (type == Wraith.class) effects.addAll(EnumSet.of(Effect.INSTANT_DEATH, Effect.FEAR));
        else if (type == Mimic.class) effects.add(Effect.PSIONIC);
        else if (type == Yog.class || type == RebornYog.class) effects.add(Effect.BURNING);
        else if (type == Yog.BurningFist.class || type == RebornYog.BurningHand.class)
            effects.addAll(EnumSet.of(Effect.BURNING, Effect.TOXIC_GAS));
        return effects;
    }

    public static String traits(Mob mob) {
        return traits(mob.getClass(), effects(mob.immunities()), effects(mob.resistances()));
    }

    public static String traits(Class<?> type) {
        if (type == Goo.class) type = RebornGoo.class;
        else if (type == Tengu.class) type = RebornTengu.class;
        else if (type == DM300.class) type = RebornDM300.class;
        else if (type == King.class) type = RebornKing.class;
        else if (type == Yog.class) type = RebornYog.class;
        else if (type == Yog.RottingFist.class) type = RebornYog.Rotting.class;
        else if (type == Yog.BurningFist.class) type = RebornYog.BurningHand.class;
        return traits(type, immunityEffects(type), resistanceEffects(type));
    }

    private static EnumSet<Effect> resistanceEffects(Class<?> type) {
        EnumSet<Effect> effects = EnumSet.noneOf(Effect.class);
        if (type == null) return effects;
        if (RebornBoss.class.isAssignableFrom(type))
            effects.addAll(EnumSet.of(Effect.INSTANT_DEATH, Effect.PSIONIC));
        else if (Tengu.class.isAssignableFrom(type) || type == Statue.class)
            effects.addAll(EnumSet.of(Effect.POISON, Effect.TOXIC_GAS, Effect.INSTANT_DEATH, Effect.PSIONIC));
        else if (type == Eye.class)
            effects.addAll(EnumSet.of(Effect.DISINTEGRATION, Effect.INSTANT_DEATH, Effect.LIFE_STEAL));
        else if (Scorpio.class.isAssignableFrom(type)) effects.addAll(EnumSet.of(Effect.LIFE_STEAL, Effect.POISON));
        else if (type == Bat.class || type == Succubus.class) effects.add(Effect.LIFE_STEAL);
        else if (type == Spinner.class) effects.add(Effect.POISON);
        else if (type == Golem.class) effects.add(Effect.PSIONIC);
        else if (type == Shaman.class) effects.add(Effect.LIGHTNING_TRAPS);
        else if (type == Warlock.class) effects.add(Effect.INSTANT_DEATH);
        return effects;
    }

    private static String traits(Class<?> type, EnumSet<Effect> immune, EnumSet<Effect> resistant) {
        resistant.removeAll(immune);
        String text = effectText(immune, "immune_");
        String resistances = effectText(resistant, "resist_");
        if (!resistances.isEmpty()) text += (text.isEmpty() ? "" : "\n") + resistances;
        if (type == Elemental.class) {
            text += "\n_" + Messages.get("ui.itemdropcards.hurt_by_freezing") + "_";
            text += "\n_" + Messages.get("ui.itemdropcards.burning_heals") + "_";
        } else if (type == Iceclaw.class) {
            text += "\n_" + Messages.get("ui.itemdropcards.fire_breaks_guard") + "_";
        }
        return text;
    }

    private static String effectText(EnumSet<Effect> effects, String prefix) {
        if (effects.contains(Effect.ICE)) effects.remove(Effect.FREEZING);
        if (effects.contains(Effect.FIRE)) effects.remove(Effect.BURNING);
        StringBuilder text = new StringBuilder();
        for (Effect effect : effects) {
            if (text.length() > 0) text.append('\n');
            text.append('_').append(Messages.get("ui.itemdropcards." + prefix + effect.key)).append('_');
        }
        return text.toString();
    }

    public final String health, damage, armor, accuracy, evasion;
    private final String note;
    private Class<? extends Mob> champion;
    private int firstChampionDepth, lastChampionDepth;

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

    public int championMinRank(RebornChampion.Skill skill) {
        RebornChampion.Skill[] pool = championSkills();
        int index = 0;
        while (index < pool.length && pool[index] != skill) index++;
        int depth = firstChampionDepth;
        while (depth < lastChampionDepth && ChampionSkills.poolSize(depth) <= index) depth++;
        return ChampionSkills.minRank(depth);
    }

    public int championMaxRank() {
        return ChampionSkills.maxRank(lastChampionDepth);
    }

    private LibraryCreatureStats champion(Class<? extends Mob> type, int firstDepth, int lastDepth) {
        champion = type;
        firstChampionDepth = firstDepth;
        lastChampionDepth = lastDepth;
        return this;
    }

    private static String range(int minimum, int maximum) {
        return minimum == maximum ? String.valueOf(minimum) : minimum + "-" + maximum;
    }

    public static LibraryCreatureStats creature(String name, String description) {
        String varies = LibraryCatalog.text("varies");
        switch (name) {
            case "actors.mobs.rimebat.name": return new LibraryCreatureStats(40, 7, 13, 3, 20, 20).champion(RimeBat.class, 11, 13);
            case "actors.mobs.iceclaw.name": return new LibraryCreatureStats(58, 9, 17, 8, 22, 9).champion(Iceclaw.class, 11, 13);
            case "actors.mobs.frostback.name": return new LibraryCreatureStats(76, 12, 23, 7, 24, 14).champion(Frostback.class, 11, 13);
            case "actors.mobs.shardling.name": return new LibraryCreatureStats(36, 10, 16, 3, 24, 12).champion(Shardling.class, 11, 13);
            case "actors.mobs.hollowminer.name": return new LibraryCreatureStats(50, 10, 18, 5, 23, 12).champion(HollowMiner.class, 11, 13);
            case "actors.mobs.rimehorn.name": return new LibraryCreatureStats(90, 14, 25, 6, 25, 10).champion(Rimehorn.class, 11, 13);
            case "actors.mobs.oldrime.name": return new LibraryCreatureStats(450, 18, 26, 9, 30, 18);

            case "actors.mobs.acidic.name": return new LibraryCreatureStats(95, 20, 32, 16, 36, 24).champion(Acidic.class, 23, 24);
            case "actors.mobs.scorpio.name": return new LibraryCreatureStats(95, 20, 32, 16, 36, 24).champion(Scorpio.class, 23, 24);
            case "actors.mobs.albino.name": return new LibraryCreatureStats(15, 1, 5, 1, 8, 3).champion(Albino.class, 1, 4);
            case "actors.mobs.bandit.name": return new LibraryCreatureStats(20, 1, 7, 3, 12, 12).champion(Bandit.class, 4, 9);
            case "actors.mobs.thief.name": return new LibraryCreatureStats(20, 1, 7, 3, 12, 12).champion(Thief.class, 4, 9);
            case "actors.mobs.bat.name": return new LibraryCreatureStats(30, 6, 12, 4, 16, 15).champion(Bat.class, 8, 14);
            case "actors.mobs.brute.name": return new LibraryCreatureStats("40", "8-18", "0-8", "20", "15", "brute_stats").champion(Brute.class, 9, 14);
            case "actors.mobs.shielded.name": return new LibraryCreatureStats("40", "8-18", "0-10", "20", "20", "brute_stats").champion(Shielded.class, 9, 14);
            case "actors.mobs.crab.name": return new LibraryCreatureStats(15, 3, 6, 4, 12, 5).champion(Crab.class, 3, 4);
            case "actors.mobs.cursepersonification.name":
                return new LibraryCreatureStats(varies, "3-5", "0-1", varies, varies, "depth_stats");
            case "actors.mobs.dm300.name": return new LibraryCreatureStats("352", "17-23", "0-10", "28", "18", "boss_stats");
            case "actors.mobs.elemental$fireelemental.name": return new LibraryCreatureStats(65, 16, 20, 5, 25, 20).champion(Elemental.class, 13, 19);
            case "actors.mobs.enslavedsouls.name": return new LibraryCreatureStats(1, 10, 20, 8, 5, 1);
            case "actors.mobs.eye.name": return new LibraryCreatureStats("100", "14-20", "0-10", "30", "20", "eye_stats").champion(Eye.class, 22, 24);
            case "actors.mobs.fetidrat.name": return new LibraryCreatureStats(15, 2, 6, 2, 12, 5);
            case "actors.mobs.gnoll.name": return new LibraryCreatureStats(12, 2, 5, 2, 11, 4).champion(Gnoll.class, 2, 8);
            case "actors.mobs.golem.name": return new LibraryCreatureStats(85, 20, 40, 12, 28, 18).champion(Golem.class, 18, 19);
            case "actors.mobs.goo.name": return new LibraryCreatureStats("100", "4-11", "0-3", "12", "12", "boss_stats");
            case "actors.mobs.dwarfking.name": return new LibraryCreatureStats("525", "20-32", "0-12", "32", "25", "boss_stats");
            case "actors.mobs.mimic.name": return new LibraryCreatureStats(varies, varies, "0", varies, varies, "depth_stats");
            case "actors.mobs.monk.name": return new LibraryCreatureStats(70, 12, 16, 2, 30, 30).champion(Monk.class, 14, 19);
            case "actors.mobs.piranha.name": return new LibraryCreatureStats(varies, varies, varies, varies, varies, "depth_stats").champion(Piranha.class, 2, 24);
            case "actors.mobs.rat.name": return new LibraryCreatureStats(8, 1, 5, 1, 8, 3).champion(Rat.class, 2, 4);
            case "custom.generated.entry_marsupial_ranged_rat_2a05e3384d": return new LibraryCreatureStats(8, 1, 5, 1, 8, 2);
            case "custom.generated.entry_marsupial_brute_rat_bdfe6b9bc3":
                return new LibraryCreatureStats("20+", "1-5", "0-1", "8", "7", "ratbrute_stats");
            case "actors.mobs.senior.name": return new LibraryCreatureStats(70, 12, 20, 2, 30, 30).champion(Senior.class, 14, 19);
            case "actors.mobs.shaman.name": return new LibraryCreatureStats(18, 2, 6, 4, 11, 8).champion(Shaman.class, 6, 14);
            case "actors.mobs.skeleton.name": return new LibraryCreatureStats(25, 3, 8, 5, 12, 9).champion(Skeleton.class, 4, 9);
            case "actors.mobs.spinner.name": return new LibraryCreatureStats(50, 12, 16, 6, 20, 14).champion(Spinner.class, 12, 14);
            case "actors.mobs.statue.name": return new LibraryCreatureStats(varies, varies, varies, varies, varies, "statue_stats").champion(Statue.class, 2, 24);
            case "actors.mobs.succubus.name": return new LibraryCreatureStats(80, 15, 25, 10, 40, 25).champion(Succubus.class, 19, 24);
            case "actors.mobs.swarm.name": return new LibraryCreatureStats("80", "1-4", "0", "12", "5", "swarm_stats").champion(Swarm.class, 3, 9);
            case "actors.mobs.tengu.name": return new LibraryCreatureStats(180, 8, 15, 5, 20, 20);
            case "actors.mobs.tengushadow.name": return new LibraryCreatureStats(12, 3, 6, 0, 18, 8);
            case "actors.mobs.warlock.name": return new LibraryCreatureStats(70, 12, 20, 8, 25, 18).champion(Warlock.class, 16, 19);
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
