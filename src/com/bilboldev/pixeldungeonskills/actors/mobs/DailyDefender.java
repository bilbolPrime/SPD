package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.NPC;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Arrow;
import com.bilboldev.pixeldungeonskills.levels.DailyOutpostLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.Terrain;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierArcherSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierThiefSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierWarriorSprite;
import com.bilboldev.pixeldungeonskills.windows.WndInfoMob;
import com.bilboldev.utils.Random;

public class DailyDefender extends NPC {
    private final int kind;
    private final int post;
    private boolean captive;
    private int[] pendingCells;

    public DailyDefender(int kind, int post, boolean captive) {
        if (kind < 0 || kind > 2 || post < 0 || post >= Level.LENGTH || kind == 0 && captive)
            throw new IllegalArgumentException("Invalid outpost defender");
        this.kind = kind;
        this.post = post;
        this.captive = captive;
        spriteClass = kind == 0 ? SoldierWarriorSprite.class
                : kind == 1 ? SoldierThiefSprite.class : SoldierArcherSprite.class;
        name = displayName();
        HP = HT = kind == 0 ? 120 : 70;
        defenseSkill = kind == 0 ? 12 : 10;
        EXP = 0;
        maxLvl = 0;
        noLoot();
        WANDERING = HUNTING = new Holding();
        state = WANDERING;
    }

    public boolean captive() {
        return captive;
    }

    public void free() {
        if (!inCurrentRun() || !isAlive() || !captive) return;
        captive = false;
        state = WANDERING;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyOutpostLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || captive || !isAlive() || sprite == null || buff(Sleep.class) != null) {
            spend(TICK);
            return true;
        }
        if (pendingCells != null) return false;
        if (state == SLEEPEING) state = buff(Terror.class) == null ? WANDERING : FLEEING;
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
        Mob nearest = null;
        for (Mob mob : Dungeon.level.mobs) {
            if (!mob.hostile || !mob.isAlive() || mob.invisible > 0 || !Level.fieldOfView[mob.pos]
                    || Level.distance(post, mob.pos) > (kind == 0 ? 1 : 4) || isCharmedBy(mob)) continue;
            if (nearest == null || distance(mob) < distance(nearest)) nearest = mob;
        }
        return nearest;
    }

    @Override
    protected Char chooseNonHeroEnemy() {
        return chooseEnemy();
    }

    @Override
    protected boolean canAttack(Char target) {
        if (target instanceof DailyDefender && ((DailyDefender)target).captive()) return false;
        return !captive && !isCharmedBy(target) && (Level.adjacent(pos, target.pos)
                || kind == 2 && Level.distance(pos, target.pos) <= 4
                && Ballistica.cast(pos, target.pos, false, true) == target.pos);
    }

    @Override
    protected boolean doAttack(Char target) {
        final int from = pos;
        final int[] cells = {target.pos};
        pendingCells = cells;
        spend(prepareRebornAttack(target, attackDelay()));
        if (sprite != null && sprite.parent != null && (sprite.visible || Dungeon.visible[target.pos])) {
            if (kind == 2 && !Level.adjacent(from, target.pos))
                sprite.attack(target.pos, () -> fireArrow(cells, target, from));
            else sprite.attack(target.pos, () -> finishAttack(cells, target, from));
        } else finishAttack(cells, target, from);
        return pendingCells == null;
    }

    private void fireArrow(int[] cells, Char target, int from) {
        if (pendingCells != cells) return;
        if (!inCurrentRun() || !isAlive() || sprite == null || sprite.parent == null) {
            finishAttack(cells, target, from);
            return;
        }
        sprite.idle();
        ((MissileSprite)sprite.parent.recycle(MissileSprite.class))
                .reset(from, cells[0], new Arrow(), () -> finishAttack(cells, target, from));
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
        if (!inCurrentRun() || !isAlive() || captive || target == null || !target.isAlive()) return false;
        if (target instanceof DailyDefender && ((DailyDefender)target).captive()) return false;
        boolean raging = buff(Amok.class) != null;
        if (target == Dungeon.hero) {
            if (!raging) return false;
        } else if (!(target instanceof Mob) || !Dungeon.level.mobs.contains(target)
                || !((Mob)target).hostile && !raging) return false;
        boolean hit = super.attack(target);
        if (target instanceof Mob && target.isAlive()) ((Mob)target).aggro(this);
        return hit;
    }

    @Override
    public void onAttackComplete() {

    }

    @Override
    public int attackSkill(Char target) {
        return kind == 0 ? 18 : 17;
    }

    @Override
    public int damageRoll() {
        return kind == 2 ? Random.NormalIntRange(4, 8) : Random.NormalIntRange(5, 9);
    }

    @Override
    public int dr() {
        return kind == 0 ? 4 : 3;
    }

    @Override
    public void damage(int damage, Object source) {
        if (inCurrentRun() && !captive) super.damage(damage, source);
    }

    @Override
    public void die(Object cause) {
        if (!inCurrentRun() || captive) return;
        destroy();
        if (sprite != null) sprite.die();
        if (kind == 0) DailyRun.fail("daily.outpost_lost");
    }

    @Override
    protected void onRemove() {
        pendingCells = null;
        next();
        super.onRemove();
    }

    @Override
    public void interact() {
        Hero hero = Dungeon.hero;
        if (!inCurrentRun() || hero == null || !hero.isAlive() || !Level.adjacent(pos, hero.pos)) return;
        if (captive) {
            GameScene.show(new WndInfoMob(this));
            return;
        }
        int previous = pos;
        int heroPosition = hero.pos;
        Actor.freeCell(previous);
        Actor.freeCell(heroPosition);
        move(heroPosition);
        hero.move(previous);
        Actor.occupyCell(this);
        Actor.occupyCell(hero);
        hero.spend(1 / hero.speed());
        hero.busy();
        if (sprite != null) sprite.move(previous, heroPosition);
        if (hero.sprite != null) hero.sprite.move(heroPosition, previous);
        else hero.next();
    }

    @Override
    public Message nameMessage() {
        return Message.key("daily.defender_" + kind);
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }

    @Override
    public String description() {
        return Messages.get(captive ? "daily.defender_captive_desc" : "daily.defender_desc_" + kind);
    }

    @Override
    public String displayDescription() {
        return description();
    }

    private class Holding implements AiState {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;
            if (enemyInFOV && canAttack(enemy)) return doAttack(enemy);
            int destination = post;
            if (kind != 0 && enemyInFOV && Level.distance(post, enemy.pos) <= 4) destination = enemy.pos;
            else if (Actor.findChar(post) != null && Actor.findChar(post) != DailyDefender.this) {
                if (Level.adjacent(pos, post) && Dungeon.level.map[pos] != Terrain.DOOR
                        && Dungeon.level.map[pos] != Terrain.OPEN_DOOR) {
                    spend(TICK);
                    return true;
                }
                int closest = -1;
                for (int offset : Level.NEIGHBOURS8) {
                    int cell = post + offset;
                    if (cell < 0 || cell >= Level.LENGTH || !Level.passable[cell]
                            || Actor.findChar(cell) != null || Dungeon.level.map[cell] == Terrain.DOOR
                            || Dungeon.level.map[cell] == Terrain.OPEN_DOOR) continue;
                    if (closest < 0 || Level.distance(pos, cell) < Level.distance(pos, closest)) closest = cell;
                }
                if (closest >= 0) destination = closest;
            }
            int previous = pos;
            if (pos != destination && getCloser(destination)) {
                spend(1 / speed());
                return moveSprite(previous, pos);
            }
            spend(TICK);
            return true;
        }

        @Override
        public String status() {
            return stateMessage("wandering");
        }
    }
}
