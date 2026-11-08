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
import com.bilboldev.pixeldungeonskills.levels.DailyLevel;
import com.bilboldev.pixeldungeonskills.levels.DailyMineLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.SoldierThiefSprite;
import com.bilboldev.pixeldungeonskills.sprites.SoldierWarriorSprite;
import com.bilboldev.pixeldungeonskills.sprites.ProspectorSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.windows.WndInfoMob;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class DailyAlly extends NPC {
    private int kind;
    private int rescueId = -1;
    private boolean imprisoned = true;

    public DailyAlly() {
        this(0);
    }

    public DailyAlly(int kind) {
        this.kind = kind;
        appearance();
        HP = HT = kind == 0 ? 65 : kind == 2 ? 60 : 42;
        defenseSkill = kind == 0 ? 12 : 10;
        EXP = 0;
        maxLvl = 0;
        noLoot();
        WANDERING = new Following();
        state = WANDERING;
    }

    public DailyAlly(int kind, int rescueId) {
        this(kind);
        if (kind != 2 || rescueId < 0 || rescueId > 2) throw new IllegalArgumentException("Unknown miner");
        this.rescueId = rescueId;
    }

    private void appearance() {
        if (kind < 0 || kind > 2) throw new IllegalArgumentException("Unknown daily ally");
        name = Messages.get("daily.ally_" + kind);
        spriteClass = kind == 0 ? SoldierWarriorSprite.class
                : kind == 2 ? ProspectorSprite.class : SoldierThiefSprite.class;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyLevel
                && Dungeon.level.mobs.contains(this);
    }

    public boolean imprisoned() {
        return imprisoned;
    }

    public int rescueId() {
        return rescueId;
    }

    public void evacuate() {
        if (!inCurrentRun() || kind != 2 || imprisoned || !isAlive()) return;
        Actor.remove(this);
        if (Actor.findChar(pos) == this) Actor.freeCell(pos);
        Dungeon.level.mobs.remove(this);
        if (sprite != null) {
            sprite.interruptMotion();
            sprite.killAndErase();
        }
    }

    public void free(Hero hero) {
        if (!inCurrentRun() || hero != Dungeon.hero || !imprisoned) return;
        imprisoned = false;
        GLog.p("daily.ally_freed_" + kind);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || imprisoned || !isAlive() || sprite == null || buff(Sleep.class) != null) {
            spend(TICK);
            return true;
        }
        if (state == SLEEPEING) state = buff(Terror.class) == null ? WANDERING : FLEEING;
        if (kind == 2 && buff(Terror.class) == null && buff(Amok.class) == null) state = WANDERING;
        return super.act();
    }

    @Override
    protected Char chooseEnemy() {
        Terror terror = buff(Terror.class);
        if (terror != null) {
            Actor source = Actor.findById(terror.object);
            return source instanceof Char ? (Char)source : null;
        }
        if (buff(Amok.class) != null) return super.chooseEnemy();
        if (kind == 2) return null;
        Mob nearest = null;
        for (Mob mob : Dungeon.level.mobs) {
            if (!mob.hostile || !mob.isAlive() || mob.invisible > 0 || !Level.fieldOfView[mob.pos]) continue;
            if (nearest == null || distance(mob) < distance(nearest)) nearest = mob;
        }
        return nearest;
    }

    static Char targetFor(Mob attacker, Char fallback) {
        if (attacker.buff(Terror.class) != null || attacker.buff(Amok.class) != null) return fallback;
        Char target = fallback;
        int distance = fallback != null && fallback.isAlive() && fallback.invisible <= 0
                && Level.fieldOfView[fallback.pos] ? attacker.distance(fallback) : Integer.MAX_VALUE;
        for (Mob mob : Dungeon.level.mobs) {
            if (mob instanceof DailyAlly && !((DailyAlly)mob).imprisoned && mob.isAlive()
                    && mob.invisible <= 0 && Level.fieldOfView[mob.pos] && attacker.distance(mob) < distance) {
                target = mob;
                distance = attacker.distance(mob);
            }
        }
        return target;
    }

    @Override
    public boolean attack(Char target) {
        if (!inCurrentRun() || kind == 2 || imprisoned || target == null || !target.isAlive()) return false;
        boolean raging = buff(Amok.class) != null;
        if (target == Dungeon.hero) {
            if (!raging) return false;
        } else if (!(target instanceof Mob) || !Dungeon.level.mobs.contains(target)
                || (!((Mob)target).hostile && !raging)) return false;
        boolean hit = super.attack(target);
        if (target instanceof Mob && target.isAlive()) ((Mob)target).aggro(this);
        return hit;
    }

    @Override
    protected boolean doAttack(Char target) {
        if (kind != 2) return super.doAttack(target);
        spend(TICK);
        return true;
    }

    @Override
    public void onAttackComplete() {
        if (!inCurrentRun() || !isAlive()) next();
        else super.onAttackComplete();
    }

    @Override
    public void damage(int damage, Object source) {
        if (inCurrentRun() && !imprisoned) super.damage(damage, source);
    }

    @Override
    public int attackSkill(Char target) {
        return kind == 0 ? 18 : 16;
    }

    @Override
    public int damageRoll() {
        return kind == 0 ? Random.NormalIntRange(5, 9) : Random.NormalIntRange(4, 7);
    }

    @Override
    public int dr() {
        return kind == 0 ? 3 : kind == 2 ? 2 : 1;
    }

    @Override
    public void die(Object cause) {
        if (!inCurrentRun()) return;
        destroy();
        if (sprite != null) sprite.die();
        if (kind == 2 && Dungeon.level instanceof DailyMineLevel)
            ((DailyMineLevel)Dungeon.level).minerDied(rescueId);
    }

    @Override
    public void interact() {
        Hero hero = Dungeon.hero;
        if (!inCurrentRun() || hero == null || !hero.isAlive() || !Level.adjacent(pos, hero.pos)) return;
        if (imprisoned) {
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
        sprite.move(previous, heroPosition);
        hero.sprite.move(heroPosition, previous);
    }

    @Override
    public Message nameMessage() {
        return Message.key("daily.ally_" + kind);
    }

    @Override
    public String displayName() {
        return nameMessage().toString();
    }

    @Override
    public String description() {
        return Messages.get((imprisoned ? "daily.ally_captive_desc_" : "daily.ally_free_desc_") + kind);
    }

    @Override
    public String displayDescription() {
        return description();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("daily_ally_kind", kind);
        bundle.put("daily_imprisoned", imprisoned);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        kind = bundle.getInt("daily_ally_kind");
        imprisoned = bundle.getBoolean("daily_imprisoned");
        defenseSkill = kind == 0 ? 12 : 10;
        appearance();
    }

    private class Following implements AiState {
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV) {
                state = HUNTING;
                return state.act(true, justAlerted);
            }
            enemySeen = false;
            Hero hero = Dungeon.hero;
            if (hero != null && hero.isAlive() && !Level.adjacent(pos, hero.pos)) {
                int previous = pos;
                if (getCloser(hero.pos)) {
                    spend(1 / speed());
                    return moveSprite(previous, pos);
                }
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
