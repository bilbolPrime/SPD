package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.particles.IceParticle;
import com.bilboldev.pixeldungeonskills.levels.DailyHuntLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.DailyRimehornSprite;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class DailyQuarry extends IceMob {
    private final int kind;
    private final int[] patrol;
    private int waypoint;
    private int bracedUntil;
    private int[] impactCells;

    public DailyQuarry(int kind, int[] patrol) {
        if (kind < 0 || kind > 2 || patrol == null || patrol.length == 0)
            throw new IllegalArgumentException("Invalid hunt quarry");
        this.kind = kind;
        this.patrol = patrol.clone();
        spriteClass = kind == 0 ? IceSprite.Claw.class : kind == 1 ? IceSprite.Gnoll.class : DailyRimehornSprite.class;
        name = displayName();
        HP = HT = kind == 0 ? 80 : kind == 1 ? 95 : 115;
        defenseSkill = kind == 1 ? 14 : 12;
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
        WANDERING = new Patrol();
        state = WANDERING;
    }

    public static String quarryName(int kind) {
        return Messages.get(kind == 0 ? Iceclaw.class : kind == 1 ? Frostback.class : Rimehorn.class, "name");
    }

    public boolean braced() {
        return kind == 0 && bracedUntil > 0 && inCurrentRun() && RebornBoss.heroActions() < bracedUntil;
    }

    @Override
    public Message nameMessage() {
        return Message.key(kind == 0 ? Iceclaw.class : kind == 1 ? Frostback.class : Rimehorn.class, "name");
    }

    @Override
    public String displayName() {
        return quarryName(kind);
    }

    @Override
    public String description() {
        return Messages.get("daily.quarry_desc_" + kind);
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyHuntLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null) {
            spend(TICK);
            return true;
        }
        boolean proceed = super.act();
        return impactCells == null && proceed;
    }

    @Override
    protected boolean prepare() {
        if (kind == 0) {
            if (Level.distance(pos, Dungeon.hero.pos) > 2 || buff(Burning.class) != null) return false;
            bracedUntil = RebornBoss.heroActions() + 2;
            cooldown = 5;
            return true;
        }
        if (kind == 1) return Level.adjacent(pos, Dungeon.hero.pos) && warn(new int[]{Dungeon.hero.pos}, 4);
        return !rooted && warn(RebornBoss.ray(pos, Dungeon.hero.pos, 3), 5);
    }

    @Override
    protected void resolve(int[] cells) {
        if (!inCurrentRun()) return;
        if (kind == 1) {
            impactCells = cells;
            if (sprite != null && sprite.parent != null && sprite.visible)
                sprite.attack(cells[0], () -> finishImpact(cells));
            else finishImpact(cells);
        } else if (kind == 2 && !rooted && pos == attackOrigin) {
            int destination = pos, hit = -1;
            for (int cell : cells) {
                if (!Level.passable[cell] || !RebornBoss.lineClear(pos, cell)) break;
                if (Actor.findChar(cell) != null) {
                    hit = cell;
                    break;
                }
                destination = cell;
            }
            int from = pos;
            boolean visible = sprite != null && sprite.parent != null
                    && (sprite.visible || Dungeon.visible[destination]);
            final int[] impact = hit < 0 ? new int[0] : new int[]{hit};
            impactCells = impact;
            if (destination != from) {
                move(destination);
                Actor.freeCell(from);
                Actor.occupyCell(this);
                if (visible && sprite instanceof DailyRimehornSprite)
                    ((DailyRimehornSprite)sprite).charge(from, destination, () -> finishImpact(impact));
                else {
                    if (sprite != null) sprite.place(destination);
                    finishImpact(impact);
                }
            } else if (hit >= 0 && visible) {
                sprite.attack(hit, () -> finishImpact(impact));
            } else {
                finishImpact(impact);
            }
        }
    }

    private void finishImpact(int[] cells) {
        if (impactCells != cells) return;
        impactCells = null;
        try {
            if (!inCurrentRun() || !isAlive() || !Dungeon.hero.isAlive()) return;
            if (kind == 1 && Level.adjacent(pos, cells[0])) {
                strike(cells, 22, 29);
                if (sprite != null && sprite.parent != null && Dungeon.visible[cells[0]])
                    CellEmitter.center(cells[0]).burst(IceParticle.SHARDS, 6);
            } else if (kind == 2) {
                strike(cells, 20, 28);
            }
            recover();
        } finally {
            if (sprite != null && isAlive()) sprite.idle();
            next();
        }
    }

    @Override
    public int attackSkill(Char target) {
        return kind == 0 ? 22 : kind == 1 ? 24 : 25;
    }

    @Override
    public int damageRoll() {
        return kind == 0 ? Random.NormalIntRange(8, 13)
                : kind == 1 ? Random.NormalIntRange(10, 17) : Random.NormalIntRange(11, 18);
    }

    @Override
    public int dr() {
        if (braced()) return 7;
        return kind == 2 ? 4 : 3;
    }

    @Override
    public void damage(int damage, Object source) {
        if (!inCurrentRun()) return;
        if (source instanceof Burning || source instanceof Fire) bracedUntil = 0;
        super.damage(damage, source);
    }

    @Override
    public void onAttackComplete() {
        if (!inCurrentRun() || !isAlive() || enemy == null || !enemy.isAlive()) next();
        else super.onAttackComplete();
    }

    @Override
    public void die(Object cause) {
        if (!inCurrentRun()) return;
        DailyHuntLevel level = (DailyHuntLevel)Dungeon.level;
        warning = new int[0];
        super.die(cause);
        level.quarrySlain(kind);
    }

    @Override
    protected void onRemove() {
        impactCells = null;
        next();
        super.onRemove();
    }

    private class Patrol implements AiState {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV) {
                enemySeen = true;
                notice();
                state = HUNTING;
                return state.act(true, justAlerted);
            }
            enemySeen = false;
            if (pos == patrol[waypoint]) waypoint = (waypoint + 1) % patrol.length;
            int from = pos;
            if (getCloser(patrol[waypoint])) {
                spend(1 / speed());
                return moveSprite(from, pos);
            }
            waypoint = (waypoint + 1) % patrol.length;
            spend(TICK);
            return true;
        }

        @Override
        public String status() {
            return stateMessage("wandering");
        }
    }
}
