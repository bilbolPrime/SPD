package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.RebornSkillTree;
import com.bilboldev.pixeldungeonskills.actors.skills.skill4.Talent;
import com.bilboldev.utils.Bundle;

public class SkillRecovery extends Buff {
    private int healthTurns;
    private int manaTurns;

    public SkillRecovery() {
        spend(TICK);
    }

    @Override
    public boolean attachTo(Char target) {
        return target instanceof Hero && super.attachTo(target);
    }

    @Override
    public boolean act() {
        Hero hero = (Hero)target;
        RebornSkillTree tree = hero.talents();
        if (!hero.isAlive() || tree == null) {
            detach();
            return true;
        }
        int health = tree.bonus(Talent.Effect.REGENERATION);
        if (health == 0 || hero.isStarving()) healthTurns = 0;
        else if (++healthTurns >= Talent.healthRecoveryInterval(health)) {
            hero.HP = Math.min(hero.HT, hero.HP + 1);
            healthTurns = 0;
        }
        int mana = tree.bonus(Talent.Effect.MANA_REGENERATION);
        if (mana == 0) manaTurns = 0;
        else if (++manaTurns >= Talent.manaRecoveryInterval(mana)) {
            hero.MP = Math.min(hero.MMP, hero.MP + 1);
            manaTurns = 0;
        }
        spend(TICK);
        return true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("skill4_hp_turns", healthTurns);
        bundle.put("skill4_mp_turns", manaTurns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        healthTurns = Math.max(0, Math.min(99, bundle.getInt("skill4_hp_turns")));
        manaTurns = Math.max(0, Math.min(9, bundle.getInt("skill4_mp_turns")));
    }
}
