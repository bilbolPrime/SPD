package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.utils.Bundle;

public class SkillEffect extends FlavourBuff {
    public Talent talent = Talent.PARRY;
    public int power;
    public int enemy = -1;

    public static SkillEffect find(Hero hero, Talent.Effect effect) {
        for (SkillEffect buff : hero.buffs(SkillEffect.class)) {
            if (buff.talent.effect == effect) return buff;
        }
        return null;
    }

    public static int power(Hero hero, Talent.Effect effect) {
        SkillEffect buff = find(hero, effect);
        return buff == null ? 0 : buff.power;
    }

    public static SkillEffect grant(Hero hero, Talent talent, int power, int turns) {
        SkillEffect old = find(hero, talent.effect);
        if (old != null) old.detach();
        SkillEffect buff = new SkillEffect();
        buff.talent = talent;
        buff.power = power;
        buff.attachTo(hero);
        buff.spend(turns);
        return buff;
    }

    @Override public int icon() {
        switch (talent.effect) {
            case GUARD: return BuffIndicator.ARMOR;
            case DODGE: case CLEANSE: return BuffIndicator.SHADOWS;
            case HASTE: return BuffIndicator.COMBO;
            case MARK: return BuffIndicator.MARK;
            default: return BuffIndicator.FOCUS;
        }
    }
    @Override public String displayName() { return talent.title(); }
    @Override public String toString() { return talent.title(); }

    @Override public void detach() {
        Hero hero = target instanceof Hero ? (Hero)target : null;
        super.detach();
        if (talent.effect == Talent.Effect.LAW && hero != null) {
            if (hero.isAlive()) hero.HP = Math.max(1, hero.HP - power);
            hero.refreshBonuses();
        }
    }

    @Override public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("talent", talent.ordinal());
        bundle.put("talent_power", power);
        bundle.put("talent_enemy", enemy);
    }

    @Override public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        int id = bundle.getInt("talent");
        if (id < 0 || id >= Talent.values().length) throw new IllegalArgumentException("Unknown talent");
        talent = Talent.values()[id];
        power = Math.max(0, bundle.getInt("talent_power"));
        enemy = bundle.getInt("talent_enemy");
    }
}
