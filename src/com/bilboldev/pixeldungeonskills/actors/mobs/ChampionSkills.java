package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion.Skill;
import static com.bilboldev.pixeldungeonskills.actors.buffs.RebornChampion.Skill.*;

public final class ChampionSkills {
    private ChampionSkills() { }

    public static Skill[] pool(Class<? extends Mob> type) {
        if (RimeBat.class.isAssignableFrom(type)) return new Skill[]{FROST, EVASION, DOUBLE_STRIKE, HASTE, LEECH};
        if (Iceclaw.class.isAssignableFrom(type)) return new Skill[]{SHELL, FROST, SMASH, BRACE, KNOCKBACK};
        if (Frostback.class.isAssignableFrom(type)) return new Skill[]{SMASH, AGGRESSION, KNOCKBACK, SMITE, BRACE};
        if (Shardling.class.isAssignableFrom(type)) return new Skill[]{FROST, WARD, SLOW, ACCURACY, SHOCK};
        if (HollowMiner.class.isAssignableFrom(type)) return new Skill[]{SMASH, ACCURACY, SHELL, SMITE, BLEED};
        if (Rimehorn.class.isAssignableFrom(type)) return new Skill[]{KNOCKBACK, SHELL, BRACE, AGGRESSION, SMASH};
        if (Rat.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, EVASION, AGGRESSION};
        if (Gnoll.class.isAssignableFrom(type)) return new Skill[]{SMASH, KNOCKBACK, ACCURACY};
        if (Crab.class.isAssignableFrom(type)) return new Skill[]{SMASH, SHELL, BRACE};
        if (Skeleton.class.isAssignableFrom(type)) return new Skill[]{SMASH, SHELL, KNOCKBACK, SMITE};
        if (Thief.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, EVASION, VENOM, HASTE};
        if (Swarm.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, EVASION, VENOM, AGGRESSION};
        if (Shaman.class.isAssignableFrom(type)) return new Skill[]{SHOCK, CURSE, SLOW, WARD};
        if (Bat.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, EVASION, ACCURACY, HASTE, BLEED};
        if (Brute.class.isAssignableFrom(type)) return new Skill[]{SMASH, SMITE, KNOCKBACK, AGGRESSION, SHELL};
        if (Spinner.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, EVASION, ROOT, ACCURACY, HASTE};
        if (Elemental.class.isAssignableFrom(type)) return new Skill[]{FIRE, KNOCKBACK, WARD, HASTE, AGGRESSION, ACCURACY};
        if (Monk.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, EVASION, ACCURACY, SMITE, KNOCKBACK, HASTE};
        if (Warlock.class.isAssignableFrom(type)) return new Skill[]{CURSE, SLOW, LEECH, SHOCK, WARD, ACCURACY};
        if (Golem.class.isAssignableFrom(type)) return new Skill[]{SMASH, SMITE, KNOCKBACK, SHELL, BRACE, AGGRESSION};
        if (Succubus.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, LEECH, CURSE, FROST, EVASION, HASTE, ACCURACY};
        if (Eye.class.isAssignableFrom(type)) return new Skill[]{CURSE, SLOW, SHOCK, WARD, LEECH, ACCURACY, AGGRESSION};
        if (Scorpio.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_SHOT, KNEE_SHOT, VENOM, ACCURACY, BLEED, HASTE, SHELL};
        if (Piranha.class.isAssignableFrom(type)) return new Skill[]{DOUBLE_STRIKE, ACCURACY, EVASION, AGGRESSION, BLEED};
        if (Statue.class.isAssignableFrom(type)) return new Skill[]{SMASH, SHELL, SMITE, ACCURACY, BRACE, KNOCKBACK};
        return new Skill[0];
    }

    public static int minRank(int depth) {
        return depth <= 10 ? 1 : depth <= 15 ? 2 : 3;
    }

    public static int maxRank(int depth) {
        return depth <= 5 ? 2 : depth <= 10 ? 3 : depth <= 15 ? 4 : 5;
    }

    public static int poolSize(int depth) {
        return depth <= 5 ? 3 : depth <= 10 ? 4 : depth <= 15 ? 5 : depth <= 20 ? 6 : 7;
    }
}
