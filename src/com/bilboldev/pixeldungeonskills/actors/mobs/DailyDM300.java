package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.noosa.Camera;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.DailyMineLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.DM300Sprite;
import com.bilboldev.utils.Random;
import java.util.HashSet;

public class DailyDM300 extends RebornBoss {
    private int patrol;

    {
        name = "DM-300";
        spriteClass = DM300Sprite.class;
        HP = HT = 220;
        defenseSkill = 18;
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
        WANDERING = new Patrol();
        state = WANDERING;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyMineLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected Class<? extends Mob> original() {
        return DM300.class;
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null) {
            spend(TICK);
            return true;
        }
        return super.act();
    }

    @Override
    protected Char chooseEnemy() {
        return DailyAlly.targetFor(this, super.chooseEnemy());
    }

    @Override
    protected Char chooseNonHeroEnemy() {
        return DailyAlly.targetFor(this, super.chooseNonHeroEnemy());
    }

    @Override
    protected boolean prepareSkill() {
        return Level.distance(pos, Dungeon.hero.pos) <= 2
                && warn(DRILL, pos, new int[]{Dungeon.hero.pos});
    }

    @Override
    protected Message warningMessage() {
        return Message.key("daily.dm300_warning");
    }

    @Override
    protected void resolveSkill(int kind, int[] cells) {
        if (!inCurrentRun()) return;
        strike(cells, 12, 18);
        burst(cells, Speck.ROCK);
        if (visibleEffect(cells)) Camera.main.shake(1, .2f);
        recover(1);
        cooldown = 5;
    }

    @Override
    public int attackSkill(Char target) {
        return 22;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(9, 15);
    }

    @Override
    public int dr() {
        return opening() ? 0 : 5;
    }

    @Override
    public void onAttackComplete() {
        if (!inCurrentRun() || !isAlive() || enemy == null || !enemy.isAlive()) next();
        else super.onAttackComplete();
    }

    @Override
    public void damage(int damage, Object source) {
        if (inCurrentRun()) super.damage(damage, source);
    }

    @Override
    public void die(Object cause) {
        if (inCurrentRun()) dismiss();
    }

    @Override
    public String description() {
        return Messages.get("daily.dm300_desc");
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
    static {
        IMMUNITIES.add(ToxicGas.class);
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }

    private class Patrol implements AiState {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV) {
                state = HUNTING;
                return state.act(true, justAlerted);
            }
            enemySeen = false;
            int[] route = ((DailyMineLevel)Dungeon.level).patrolCells();
            if (route.length == 0) {
                spend(TICK);
                return true;
            }
            patrol %= route.length;
            if (pos == route[patrol]) patrol = (patrol + 1) % route.length;
            int previous = pos;
            if (getCloser(route[patrol])) {
                spend(1 / speed());
                return moveSprite(previous, pos);
            }
            patrol = (patrol + 1) % route.length;
            spend(TICK);
            return true;
        }

        @Override
        public String status() {
            return stateMessage("wandering");
        }
    }
}
