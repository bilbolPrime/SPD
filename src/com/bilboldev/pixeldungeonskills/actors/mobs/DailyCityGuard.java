package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.levels.DailyHoardLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.DailyCitySprite;
import com.bilboldev.pixeldungeonskills.sprites.ItemSpriteSheet;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.utils.Random;

public class DailyCityGuard extends Mob {
    public final int kind;
    private final int post;
    private int patrolStep, patrolTarget = -1;
    private Object pendingAttack;

    public DailyCityGuard(int kind, int post) {
        if (kind < 0 || kind > 3) throw new IllegalArgumentException("Unknown city guard");
        this.kind = kind;
        this.post = post;
        spriteClass = DailyCitySprite.class;
        name = displayName();
        HP = HT = kind == 0 ? 75 : kind == 1 ? 44 : 34;
        defenseSkill = kind == 0 ? 18 : 14;
        EXP = maxLvl = 0;
        noLoot = true;
        WANDERING = new Patrol();
        state = WANDERING;
    }

    @Override public DailyCitySprite sprite() {
        return new DailyCitySprite(kind);
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyHoardLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null || buff(Sleep.class) != null) {
            spend(TICK);
            return true;
        }
        if (pendingAttack != null) return false;
        return super.act();
    }

    @Override protected boolean canAttack(Char target) {
        return target != null && !isCharmedBy(target) && (Level.adjacent(pos, target.pos)
                || kind >= 2 && Level.distance(pos, target.pos) <= 5
                && Ballistica.cast(pos, target.pos, false, true) == target.pos);
    }

    @Override protected boolean doAttack(Char target) {
        final int from = pos, to = target.pos;
        final Object attack = new Object();
        pendingAttack = attack;
        spend(prepareRebornAttack(target, attackDelay()));
        if (sprite != null && sprite.parent != null && (sprite.visible || Dungeon.visible[to])) {
            sprite.attack(to, () -> {
                if (pendingAttack != attack) return;
                if (!inCurrentRun() || !isAlive() || sprite == null || sprite.parent == null) {
                    finishAttack(attack, target, from, to);
                    return;
                }
                sprite.idle();
                if (kind == 2 && !Level.adjacent(from, to)) {
                    Sample.INSTANCE.play(Assets.SND_MISS);
                    ((MissileSprite)sprite.parent.recycle(MissileSprite.class)).reset(from, to,
                            ItemSpriteSheet.Arrow, null, () -> finishAttack(attack, target, from, to));
                } else if (kind == 3 && !Level.adjacent(from, to)) {
                    Sample.INSTANCE.play(Assets.SND_ZAP);
                    MagicMissile.blueLight(sprite.parent, from, to, () -> finishAttack(attack, target, from, to));
                } else finishAttack(attack, target, from, to);
            });
        } else finishAttack(attack, target, from, to);
        return pendingAttack == null;
    }

    private void finishAttack(Object attack, Char target, int from, int to) {
        if (pendingAttack != attack) return;
        pendingAttack = null;
        try {
            if (inCurrentRun() && isAlive() && pos == from && target.isAlive() && target.pos == to
                    && (target == Dungeon.hero || Dungeon.level.mobs.contains(target)) && canAttack(target)) attack(target);
        } finally {
            next();
        }
    }

    @Override public void onAttackComplete() {

    }

    @Override public void beckon(int cell) {
        super.beckon(cell);
        patrolTarget = cell;
    }

    @Override protected void onRemove() {
        pendingAttack = null;
        next();
        super.onRemove();
    }

    @Override public void damage(int damage, Object source) {
        if (inCurrentRun()) super.damage(damage, source);
    }

    @Override public void die(Object cause) {
        if (!inCurrentRun()) return;
        DailyHoardLevel level = (DailyHoardLevel)Dungeon.level;
        int cell = pos;
        super.die(cause);
        if (kind == 0) level.captainDied(cell);
    }

    @Override public int attackSkill(Char target) { return kind == 0 ? 24 : 21; }
    @Override public int damageRoll() { return Random.NormalIntRange(kind == 0 ? 9 : 6, kind == 0 ? 16 : 12); }
    @Override public int dr() { return kind == 0 ? 4 : kind == 1 ? 3 : 1; }
    @Override public Message nameMessage() { return Message.key("daily.city_guard_" + kind); }
    @Override public String displayName() { return nameMessage().toString(); }
    @Override public String description() { return Messages.get("daily.city_guard_desc_" + kind); }

    private class Patrol implements AiState {
        @Override public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;
            if (enemyInFOV) {
                state = HUNTING;
                target = enemy.pos;
                return HUNTING.act(true, justAlerted);
            }
            DailyHoardLevel level = (DailyHoardLevel)Dungeon.level;
            int home = level.alarmRaised() ? level.goal : post;
            if (patrolTarget < 0 || pos == patrolTarget) patrolTarget = level.patrolCell(home, patrolStep++);
            int previous = pos;
            if (patrolTarget >= 0 && getCloser(patrolTarget)) {
                spend(1 / speed());
                return moveSprite(previous, pos);
            }
            patrolTarget = -1;
            spend(TICK);
            return true;
        }

        @Override public String status() { return stateMessage("wandering"); }
    }
}
