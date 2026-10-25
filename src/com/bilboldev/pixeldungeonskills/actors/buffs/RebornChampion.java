package com.bilboldev.pixeldungeonskills.actors.buffs;

import com.bilboldev.pixeldungeonskills.messages.Message;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.mobs.*;
import com.bilboldev.pixeldungeonskills.effects.Pushing;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class RebornChampion extends Buff {
    public static final int CHAMP_REBORN = 5;
    private boolean applied;
    private boolean pair;

    private Char attackTarget;
    private Skill skill = Skill.NONE;

    private enum Skill {
        NONE(null), SMASH(Message.key("custom.generated.entry_smash_f9f0ebb581")), SMITE(Message.key("custom.generated.entry_smite_db8b402b39")),
        KNOCKBACK(Message.key("custom.generated.entry_knockback_3a9a3af4ee")), VENOM(Message.key("custom.generated.entry_poison_is_my_specialty_f2769a6000")),
        DOUBLE_STRIKE(Message.key("custom.generated.entry_too_slow_8ace904bbf")), DOUBLE_SHOT(Message.key("custom.generated.entry_two_for_one_8162f0b095")),
        KNEE_SHOT(Message.key("custom.generated.entry_easy_target_016d464998")), CURSE(Message.key("custom.generated.necro_merc.curse_cast")), SLOW(Message.key("custom.generated.entry_basic_training_6ca0ee1d5f"));

        final Message callout;
        Skill(Message callout) {
            this.callout = callout;
        }
    }

    private static final Skill[] MELEE = {Skill.SMASH, Skill.SMITE, Skill.KNOCKBACK};
    private static final Skill[] THIEF = {Skill.VENOM, Skill.DOUBLE_STRIKE};
    private static final Skill[] RANGED = {Skill.DOUBLE_SHOT, Skill.KNEE_SHOT};
    private static final Skill[] CASTER = {Skill.CURSE, Skill.SLOW};

    public static void grant(Mob mob, boolean pair) {
        if (mob.buff(RebornChampion.class) != null) return;
        RebornChampion buff = new RebornChampion();
        buff.pair = pair;
        buff.attachTo(mob);
    }

    public boolean isPair() {
        return pair;
    }

    @Override
    public boolean attachTo(Char target) {
        if (!(target instanceof Mob)) return false;
        if (target.buff(RebornChampion.class) != null) return target.buff(RebornChampion.class) == this;
        if (!super.attachTo(target)) return false;
        target.champ = CHAMP_REBORN;
        target.name = "Reborn " + target.name;
        if (!applied) {
            target.HT *= pair ? 2 : 3;
            target.HP = target.HT;
            applied = true;
        }
        updateSpriteState();
        return true;
    }

    @Override
    public boolean act() {
        diactivate();
        return true;
    }

    private Skill[] pool() {
        if (target instanceof Thief) return THIEF;
        if (target instanceof Scorpio) return RANGED;
        if (target instanceof Shaman || target instanceof Warlock || target instanceof Eye) return CASTER;
        return MELEE;
    }

    public void beginAttack(Char enemy) {
        attackTarget = enemy;
        skill = Skill.NONE;
        if (enemy == null || !enemy.isAlive() || !target.isAlive() || Random.Int(2) != 0) return;
        int eligible = 0;
        for (Skill candidate : pool()) if (eligible(candidate, enemy)) eligible++;
        if (eligible == 0) return;
        int choice = Random.Int(eligible);
        for (Skill candidate : pool()) {
            if (eligible(candidate, enemy) && choice-- == 0) {
                skill = candidate;
                break;
            }
        }
        if (target.sprite != null && target.sprite.visible && Dungeon.visible[target.pos]) {
            target.sprite.showStatus(CharSprite.NEUTRAL, skill.callout);
        }
    }

    public void ensureAttack(Char enemy) {
        if (attackTarget != enemy) beginAttack(enemy);
    }

    public void finishAttack() {
        attackTarget = null;
        skill = Skill.NONE;
    }

    public float delayMultiplier() {
        return skill == Skill.SMITE ? 2f : 1f;
    }

    public boolean doubleAttack() {
        return skill == Skill.DOUBLE_STRIKE || skill == Skill.DOUBLE_SHOT;
    }

    public int damage(int damage) {
        float multiplier = skill == Skill.SMASH ? 1.2f : skill == Skill.SMITE ? 1.4f
                : doubleAttack() ? .8f : 1f;
        return Math.max(0, Math.round(damage * multiplier));
    }

    private boolean eligible(Skill candidate, Char enemy) {
        switch (candidate) {
            case KNOCKBACK: return Pushing.knockbackCell(target, enemy) != -1;
            case VENOM: return canDebuff(enemy, Poison.class);
            case KNEE_SHOT: return canDebuff(enemy, Cripple.class);
            case CURSE: return canDebuff(enemy, NecroWeakened.class)
                    && canDebuff(enemy, Weakness.class) && canDebuff(enemy, RebornCurse.class);
            case SLOW: return canDebuff(enemy, Slow.class);
            default: return true;
        }
    }

    private static boolean canDebuff(Char enemy, Class<? extends Buff> type) {
        return enemy.buff(type) == null && !enemy.immunities().contains(type);
    }

    public void onHit(Char enemy) {
        if (enemy != attackTarget || !target.isAlive() || !enemy.isAlive() || !eligible(skill, enemy)) return;
        switch (skill) {
            case KNOCKBACK:
                Pushing.knockBack(target, enemy);
                break;
            case VENOM:
                Poison poison = new Poison();
                poison.setChampionDuration(3f * Poison.durationFactor(enemy));
                poison.attachTo(enemy);
                break;
            case KNEE_SHOT:
                Buff.affect(enemy, Cripple.class, 2f);
                break;
            case CURSE:
                Buff.affect(enemy, RebornCurse.class, 3f);
                break;
            case SLOW:
                Buff.affect(enemy, Slow.class, 2f);
                break;
            default:
                break;
        }
    }

    public void updateSpriteState() {
        if (target == null || !target.isAlive()) return;
        CharSprite sprite = target.sprite;
        if (sprite == null) return;
        if (sprite.rebornAura == null || !sprite.rebornAura.exists) sprite.add(CharSprite.State.CHAMPREBORN);
    }

    public String displayName(String name) {
        String prefix = "Reborn ";
        if (name.startsWith(prefix)) name = name.substring(prefix.length());
        return Messages.get(RebornChampion.class, "name", Messages.maybeTranslate(name));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("reborn_applied", applied);
        bundle.put("reborn_pair", pair);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        applied = bundle.getBoolean("reborn_applied");
        pair = bundle.getBoolean("reborn_pair");
    }
}
