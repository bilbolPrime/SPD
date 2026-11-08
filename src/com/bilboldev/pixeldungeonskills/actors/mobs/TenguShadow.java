package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.DeathBomb;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.TenguShadowSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class TenguShadow extends Mob {
    public int ownerId;
    private int turns = 6;
    private boolean removed;
    {
        name = "Tengu's shadow";
        spriteClass = TenguShadowSprite.class;
        HP = HT = 12;
        defenseSkill = 8;
        EXP = 0;
        state = HUNTING;
    }

    @Override
    protected boolean act() {
        if (!inCurrentLevel() || DailyRun.finished()) {
            spend(TICK);
            return true;
        }
        RebornTengu owner = null;

        for (Mob mob : Dungeon.level.mobs)
            if (mob instanceof RebornTengu && mob.id() == ownerId) owner = (RebornTengu)mob;
        if (--turns < 0 || owner == null || !owner.isAlive()) {
            die(null);
            return true;
        }
        state = HUNTING;
        return super.act();
    }

    @Override
    protected boolean getCloser(int cell) {
        return false;
    }

    @Override
    protected boolean getFurther(int cell) {
        return false;
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return !isCharmedBy(enemy) && Ballistica.cast(pos, enemy.pos, false, true) == enemy.pos;
    }

    @Override
    public int attackSkill(Char target) {
        return 18;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(3, 6);
    }

    private boolean inCurrentLevel() {
        return Dungeon.level != null && Dungeon.level.mobs.contains(this);
    }

    @Override
    public void onAttackComplete() {
        if (!inCurrentLevel() || DailyRun.finished() || !isAlive()) next();
        else super.onAttackComplete();
    }

    @Override
    public void destroy() {
        if (!inCurrentLevel()) return;
        HP = 0;
        Actor.remove(this);
        Actor.freeCell(pos);
        Dungeon.level.mobs.remove(this);
    }

    @Override
    public void die(Object source) {
        if (removed || !inCurrentLevel()) return;
        if (source != null) DeathBomb.leave(this);
        removed = true;
        destroy();
        if (sprite != null) sprite.die();
        if (source != null) for (Mob mob : Dungeon.level.mobs)
            if (mob instanceof RebornTengu && mob.id() == ownerId && mob.isAlive()) ((RebornTengu)mob).shadowBroken();
    }

    @Override
    public String description() {
        return Messages.get(this, "desc");
    }

    @Override
    public String displayDescription() {
        return description();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("owner", ownerId);
        bundle.put("turns", turns);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        ownerId = bundle.getInt("owner");
        turns = bundle.getInt("turns");
    }
}
