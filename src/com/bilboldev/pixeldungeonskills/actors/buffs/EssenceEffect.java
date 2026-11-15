package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.items.essences.Essence;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.ui.BuffIndicator;
import com.bilboldev.utils.Bundle;
import java.util.Locale;

public class EssenceEffect extends Buff {
    public enum Kind { FIRE, COLD, POISON, IRON, WARD, WIND }

    private Kind kind = Kind.FIRE;
    private int amount;
    private int turns;

    public EssenceEffect() {
        spend(TICK);
    }

    public static void apply(Char target, Kind kind, int amount, int turns) {
        if (!Dungeon.rules().hasRebornEncounters() || !target.isAlive()) return;
        EssenceEffect effect = find(target, kind);
        if (effect == null) {
            effect = new EssenceEffect();
            effect.kind = kind;
            effect.attachTo(target);
        }
        effect.amount = Math.max(effect.amount, amount);
        effect.turns = Math.max(effect.turns, turns);
        BuffIndicator.refreshHero();
    }

    private static EssenceEffect find(Char target, Kind kind) {
        for (EssenceEffect effect : target.buffs(EssenceEffect.class)) if (effect.kind == kind) return effect;
        return null;
    }

    public static boolean protectedFrom(Char target, Essence.Type element) {
        if (!Dungeon.rules().hasRebornEncounters()) return false;
        Kind kind = element == Essence.Type.EMBER ? Kind.FIRE
                : element == Essence.Type.RIME ? Kind.COLD : Kind.POISON;
        return find(target, kind) != null;
    }

    public static boolean blocks(Char target, Class<? extends Buff> status) {
        return status == Burning.class && protectedFrom(target, Essence.Type.EMBER)
                || (status == Frost.class || status == Frostbite.class) && protectedFrom(target, Essence.Type.RIME)
                || status == Poison.class && protectedFrom(target, Essence.Type.VENOM);
    }

    public static float movement(Char target) {
        return Dungeon.rules().hasRebornEncounters() && find(target, Kind.WIND) != null ? 2 : 1;
    }

    public static int attackBonus(Char target) {
        EssenceEffect effect = Dungeon.rules().hasRebornEncounters() ? find(target, Kind.IRON) : null;
        return effect == null ? 0 : effect.amount;
    }

    public static int absorb(Char target, int damage) {
        EssenceEffect effect = find(target, Kind.WARD);
        if (effect == null || damage <= 0) return damage;
        int absorbed = Math.min(damage, effect.amount);
        effect.amount -= absorbed;
        if (absorbed > 0 && target.sprite != null) target.sprite.showAbsorbed(absorbed);
        if (effect.amount <= 0) effect.detach();
        return damage - absorbed;
    }

    @Override
    public boolean act() {
        if (!target.isAlive() || !Dungeon.rules().hasRebornEncounters() || --turns <= 0) detach();
        else spend(TICK);
        return true;
    }

    @Override public int remainingTurns() { return turns; }
    @Override public int icon() { return kind == Kind.WIND ? BuffIndicator.COMBO : BuffIndicator.ESSENCE_FIRE + kind.ordinal(); }
    @Override public String toString() { return Messages.get(this, kind.name().toLowerCase(Locale.ROOT) + ".name"); }
    @Override public String displayName() { return Messages.get(this, "remaining", toString(), turns); }

    public Message nameMessage() {
        return Message.key(EssenceEffect.class, kind.name().toLowerCase(Locale.ROOT) + ".name");
    }

    public Message descriptionMessage() {
        String field = kind.name().toLowerCase(Locale.ROOT) + ".desc";
        return kind == Kind.IRON || kind == Kind.WARD ? Message.key(EssenceEffect.class, field, amount)
                : Message.key(EssenceEffect.class, field);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("essence_effect", kind.ordinal());
        bundle.put("essence_amount", amount);
        bundle.put("essence_turns", turns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        kind = Kind.values()[Math.max(0, Math.min(Kind.values().length - 1, bundle.getInt("essence_effect")))];
        amount = Math.max(0, Math.min(kind == Kind.IRON ? 20 : kind == Kind.WARD ? 250 : 0, bundle.getInt("essence_amount")));
        turns = Math.max(1, Math.min(kind == Kind.IRON ? 10 : kind == Kind.WARD ? 15 : kind == Kind.WIND ? 40 : 100,
                bundle.getInt("essence_turns")));
    }
}
