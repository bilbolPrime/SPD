package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.HiredMerc;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.SummonedPet;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.levels.DailyOutpostLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.BanditSprite;
import com.bilboldev.pixeldungeonskills.sprites.ShamanSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.ThiefSprite;
import com.bilboldev.utils.Random;

public class DailyRaider extends Mob {
    private final int kind;
    private int[] pendingCells;
    private int approachGate;
    private boolean[] approachFloor;

    public DailyRaider(int kind, int approachGate) {
        if (kind < 0 || kind > 3) throw new IllegalArgumentException("Unknown outpost raider");
        if (approachGate < -1 || approachGate >= Level.LENGTH) throw new IllegalArgumentException("Invalid outpost gate");
        this.kind = kind;
        this.approachGate = approachGate;
        spriteClass = kind == 0 ? ThiefSprite.class : kind == 1 ? SkeletonSprite.class
                : kind == 2 ? BanditSprite.class : ShamanSprite.class;
        name = displayName();
        HP = HT = kind == 0 ? 22 : kind == 1 ? 30 : kind == 2 ? 38 : 24;
        defenseSkill = kind == 0 ? 8 : kind == 1 ? 10 : kind == 2 ? 11 : 8;
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
        WANDERING = HUNTING = new Assault();
        state = HUNTING;
    }

    public boolean castsMagic() {
        return kind == 3;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyOutpostLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null || buff(Sleep.class) != null) {
            spend(TICK);
            return true;
        }
        if (pendingCells != null) return false;
        if (state == SLEEPEING) state = buff(Terror.class) == null ? HUNTING : FLEEING;
        return super.act();
    }

    @Override
    protected Char chooseEnemy() {
        Terror terror = buff(Terror.class);
        if (terror != null) {
            Actor source = Actor.findById(terror.object);
            return source instanceof Char ? (Char)source : null;
        }
        if (buff(Amok.class) != null) {
            Char target = super.chooseEnemy();
            if (!(target instanceof DailyDefender) || !((DailyDefender)target).captive()) return target;
        }
        Char nearest = null;
        Char hero = Dungeon.hero;
        if (hero != null && hero.isAlive() && hero.invisible == 0 && Level.fieldOfView[hero.pos]
                && !isCharmedBy(hero)) nearest = hero;
        for (Mob mob : Dungeon.level.mobs) {
            boolean defender = mob instanceof DailyDefender && !((DailyDefender)mob).captive();
            if (!defender && !(mob instanceof SummonedPet) && !(mob instanceof HiredMerc)) continue;
            if (!mob.isAlive() || mob.invisible > 0 || !Level.fieldOfView[mob.pos] || isCharmedBy(mob)) continue;
            if (nearest == null || distance(mob) < distance(nearest)) nearest = mob;
        }
        if (nearest != null) return nearest;
        DailyDefender captain = ((DailyOutpostLevel)Dungeon.level).captain();
        return captain != null && captain.isAlive() ? captain : hero;
    }

    @Override
    protected Char chooseNonHeroEnemy() {
        return chooseEnemy();
    }

    @Override
    protected boolean canAttack(Char target) {
        if (target instanceof DailyDefender && ((DailyDefender)target).captive()) return false;
        return !isCharmedBy(target) && (Level.adjacent(pos, target.pos)
                || kind == 3 && Level.distance(pos, target.pos) <= 4
                && Ballistica.cast(pos, target.pos, false, true) == target.pos);
    }

    @Override
    protected boolean getCloser(int target) {
        if (approachGate < 0) return super.getCloser(target);
        DailyOutpostLevel level = (DailyOutpostLevel)Dungeon.level;
        if (level.insideOutpost(pos)) {
            approachGate = -1;
            approachFloor = null;
            return super.getCloser(target);
        }
        if (rooted) return false;
        if (approachFloor == null) approachFloor = new boolean[Level.LENGTH];
        System.arraycopy(RebornBoss.movementFloor(), 0, approachFloor, 0, Level.LENGTH);
        for (int side = 0; side < 4; side++) {
            int gate = level.entranceGate(side);
            if (gate == approachGate) continue;
            int across = side % 2 == 0 ? 1 : Level.WIDTH;
            for (int lane = -1; lane <= 1; lane++) approachFloor[gate + lane * across] = false;
        }
        int step = Dungeon.findPath(this, pos, target, approachFloor, Level.fieldOfView);
        if (step < 0) return false;
        move(step);
        return true;
    }

    @Override
    protected boolean doAttack(Char target) {
        final int from = pos;
        final int[] cells = {target.pos};
        pendingCells = cells;
        spend(prepareRebornAttack(target, attackDelay()));
        if (sprite != null && sprite.parent != null && (sprite.visible || Dungeon.visible[target.pos])) {
            if (kind == 3 && !Level.adjacent(from, target.pos))
                sprite.attack(target.pos, () -> castBolt(cells, target, from));
            else sprite.attack(target.pos, () -> finishAttack(cells, target, from));
        } else finishAttack(cells, target, from);
        return pendingCells == null;
    }

    private void castBolt(int[] cells, Char target, int from) {
        if (pendingCells != cells) return;
        if (!inCurrentRun() || !isAlive() || sprite == null || sprite.parent == null) {
            finishAttack(cells, target, from);
            return;
        }
        sprite.idle();
        MagicMissile.blueLight(sprite.parent, from, cells[0], () -> finishAttack(cells, target, from));
    }

    private void finishAttack(int[] cells, Char target, int from) {
        if (pendingCells != cells) return;
        pendingCells = null;
        try {
            if (inCurrentRun() && isAlive() && target.isAlive() && target.pos == cells[0]
                    && Actor.findChar(cells[0]) == target && RebornBoss.lineClear(from, cells[0])) attack(target);
        } finally {
            if (sprite != null && isAlive()) sprite.idle();
            next();
        }
    }

    @Override
    public boolean attack(Char target) {
        if (!inCurrentRun() || !isAlive() || target == null || !target.isAlive()) return false;
        if (target instanceof DailyDefender && ((DailyDefender)target).captive()) return false;
        if (target != Dungeon.hero && (!(target instanceof Mob) || !Dungeon.level.mobs.contains(target))) return false;
        return super.attack(target);
    }

    @Override
    public void onAttackComplete() {

    }

    @Override
    public int attackSkill(Char target) {
        return kind == 1 ? 16 : kind == 2 ? 18 : 15;
    }

    @Override
    public int damageRoll() {
        return kind == 1 ? Random.NormalIntRange(5, 9) : kind == 2 ? Random.NormalIntRange(6, 10)
                : Random.NormalIntRange(4, 8);
    }

    @Override
    public int dr() {
        return kind == 1 ? 1 : kind == 2 ? 2 : 0;
    }

    @Override
    public void damage(int damage, Object source) {
        if (inCurrentRun()) super.damage(damage, source);
    }

    @Override
    public void die(Object cause) {
        if (inCurrentRun()) super.die(cause);
    }

    public void withdraw() {
        if (!(Dungeon.level instanceof DailyOutpostLevel) || !Dungeon.level.mobs.contains(this)) return;
        Actor.remove(this);
        if (Actor.findChar(pos) == this) Actor.freeCell(pos);
        Dungeon.level.mobs.remove(this);
        if (sprite != null) {
            sprite.interruptMotion();
            sprite.killAndErase();
        }
    }

    @Override
    protected void onRemove() {
        pendingCells = null;
        next();
        super.onRemove();
    }

    @Override
    public Message nameMessage() {
        return Message.key("daily.raider_" + kind);
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }

    @Override
    public String description() {
        return Messages.get("daily.raider_desc_" + kind);
    }

    private class Assault implements AiState {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;
            if (enemyInFOV && canAttack(enemy)) return doAttack(enemy);
            int previous = pos;
            if (enemy != null && enemy.isAlive() && getCloser(enemy.pos)) {
                spend(1 / speed());
                return moveSprite(previous, pos);
            }
            spend(TICK);
            return true;
        }

        @Override
        public String status() {
            return stateMessage("hunting");
        }
    }
}
