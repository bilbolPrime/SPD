package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.Heap;
import com.bilboldev.pixeldungeonskills.items.KindOfWeapon;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class OffBalance extends Buff {
    public static final int WARNING_STACKS = 7;
    public static final int DISARM_STACKS = 10;

    private int stacks;

    public OffBalance() {
        spend(TICK);
    }

    @Override
    public boolean attachTo(Char target) {
        return target instanceof Hero && super.attachTo(target);
    }

    public static void hit(Hero hero) {
        if (LocalCoop.contains(hero) && hero != Dungeon.hero) {
            LocalCoop.withHero(hero, () -> hit(hero));
            return;
        }
        KindOfWeapon weapon = hero.belongings.weapon;
        if (weapon == null || !weapon.canDisarm(hero)) {
            Buff.detach(hero, OffBalance.class);
            return;
        }
        if (Random.Int(2) != 0) return;

        OffBalance balance = Buff.affect(hero, OffBalance.class);
        int previous = balance.stacks;
        balance.stacks += Random.IntRange(1, 3);
        if (balance.stacks >= DISARM_STACKS) {
            balance.detach();
            if (weapon.disarm(hero)) {
                Heap heap = Dungeon.level.drop(weapon, hero.pos);
                if (heap.sprite != null) heap.sprite.drop();
                GLog.w(OffBalance.class, "disarmed");
            }
        } else if (previous < WARNING_STACKS && balance.stacks >= WARNING_STACKS) {
            GLog.w(OffBalance.class, "warning");
        }
    }

    @Override
    public boolean act() {
        Hero hero = (Hero)target;
        KindOfWeapon weapon = hero.belongings.weapon;
        if (!hero.isAlive() || weapon == null || !weapon.canDisarm(hero) || stacks <= 0) {
            detach();
            return true;
        }
        if (--stacks == 0) detach();
        else spend(TICK);
        return true;
    }

    @Override
    public int icon() {
        return BuffIndicator.OFF_BALANCE;
    }

    @Override
    public int remainingTurns() {
        return stacks;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("stacks", stacks);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stacks = Math.max(0, Math.min(DISARM_STACKS - 1, bundle.getInt("stacks")));
    }
}
