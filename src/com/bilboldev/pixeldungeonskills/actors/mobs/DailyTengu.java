package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.levels.DailyLevel;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.utils.Bundle;

public class DailyTengu extends RebornTengu {
    private boolean engaged;

    {
        name = "Tengu";
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
    }

    @Override
    public Message nameMessage() {
        return Message.key(Tengu.class, "name");
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyLevel
                && Dungeon.level.mobs.contains(this);
    }

    private boolean engage() {
        if (!inCurrentRun() || Dungeon.hero == null) return false;
        if (engaged) return true;
        DailyLevel level = (DailyLevel)Dungeon.level;
        if (!level.prisonSecured() || !level.insideBossRoom(Dungeon.hero.pos)) return false;
        engaged = true;
        level.sealBossRoom();
        state = HUNTING;
        aggro(Dungeon.hero);
        notice();
        return true;
    }

    @Override
    protected boolean act() {
        if (!engage()) {
            spend(TICK);
            return true;
        }
        return super.act();
    }

    @Override
    public void damage(int damage, Object source) {
        if (engage()) super.damage(damage, source);
    }

    @Override
    protected boolean arenaCell(int cell) {
        return Dungeon.level instanceof DailyLevel
                && ((DailyLevel)Dungeon.level).insideBossRoom(cell) && super.arenaCell(cell);
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
    public void onAttackComplete() {
        if (!inCurrentRun() || !isAlive() || enemy == null || !enemy.isAlive()) next();
        else super.onAttackComplete();
    }

    @Override
    public void die(Object cause) {
        if (inCurrentRun()) super.die(cause);
    }

    @Override
    protected void finishDeath(Object cause) {
        destroy();
        if (sprite != null) sprite.die();
        yell(Message.key(Tengu.class, "defeated"));
        DailyRun.finish(true);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("daily_engaged", engaged);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        engaged = bundle.getBoolean("daily_engaged");
    }
}
