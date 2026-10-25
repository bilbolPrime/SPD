package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.utils.Bundle;

public abstract class Recovery extends Buff {
    public static final int DURATION = 10;
    private int remaining;
    private int turns;

    protected abstract boolean mana();

    public void start() {
        remaining = Math.max(0, maximum() - value());
        turns = DURATION;
        if (remaining == 0) detach();
    }

    public int remaining() { return remaining; }

    private int value() { return mana() ? target.MP : target.HP; }

    private int maximum() { return mana() ? target.MMP : target.HT; }

    @Override
    public boolean act() {
        if (!target.isAlive() || remaining <= 0 || turns <= 0 || value() >= maximum()) {
            detach();
            return true;
        }
        int amount = (remaining + turns - 1) / turns;
        if (mana()) target.MP = Math.min(target.MMP, target.MP + amount);
        else target.HP = Math.min(target.HT, target.HP + amount);
        remaining -= amount;
        turns--;
        if (remaining == 0 || turns == 0 || value() >= maximum()) detach();
        else spend(TICK);
        return true;
    }

    @Override
    public String toString() { return Messages.get(this, "name"); }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("recovery_left", remaining);
        bundle.put("recovery_turns", turns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        remaining = Math.max(0, bundle.getInt("recovery_left"));
        turns = Math.max(0, Math.min(DURATION, bundle.getInt("recovery_turns")));
    }
}
