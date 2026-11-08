package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Charm;
import com.bilboldev.pixeldungeonskills.actors.buffs.Cripple;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.CombatTextIcon;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.particles.PurpleParticle;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Dart;
import com.bilboldev.pixeldungeonskills.levels.DailySealLevel;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.DailyEyeSprite;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.sprites.ScorpioSprite;
import com.bilboldev.pixeldungeonskills.sprites.SuccubusSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Random;

public class DailyDemon extends Mob {
    private final int kind;
    private final int site;
    private int[] warning = new int[0];
    private int warningOrigin, warnedAt, waits;
    private boolean rootGraceUsed;
    private float beamReady;
    private int[] pendingCells;

    public DailyDemon(int kind, int site) {
        if (kind < 0 || kind > 2 || site < -1 || site > 2)
            throw new IllegalArgumentException("Invalid daily demon");
        this.kind = kind;
        this.site = site;
        spriteClass = kind == 0 ? SuccubusSprite.class : kind == 1 ? ScorpioSprite.class : DailyEyeSprite.class;
        name = displayName();
        HP = HT = kind == 0 ? 42 : kind == 1 ? 36 : 38;
        defenseSkill = kind == 0 ? 15 : 14;
        flying = kind == 2;
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
        state = WANDERING;
    }

    public int sourceSite() {
        return site;
    }

    public static String demonName(int kind) {
        return Messages.get(kind == 0 ? Succubus.class : kind == 1 ? Scorpio.class : Eye.class, "name");
    }

    @Override
    public Message nameMessage() {
        return Message.key(kind == 0 ? Succubus.class : kind == 1 ? Scorpio.class : Eye.class, "name");
    }

    @Override
    public String displayName() {
        return demonName(kind);
    }

    @Override
    public String description() {
        return Messages.get("daily.demon_desc_" + kind);
    }

    public int[] warningCells() {
        return warning;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailySealLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null) {
            spend(TICK);
            return true;
        }
        if (pendingCells != null) return false;
        if (kind != 2) return super.act();
        if (paralysed || buff(Frost.class) != null || buff(Sleep.class) != null
                || buff(Terror.class) != null || buff(Amok.class) != null || isCharmedBy(Dungeon.hero)) {
            warning = new int[0];
            return super.act();
        }
        if (warning.length > 0) {
            if (Dungeon.hero.rebornActions == warnedAt && (!Dungeon.hero.paralysed || waits++ < 3)) {
                spend(TICK);
                return true;
            }
            if (Dungeon.hero.rooted && !rootGraceUsed) {
                rootGraceUsed = true;
                warnedAt = Dungeon.hero.rebornActions;
                spend(TICK);
                return true;
            }
            int[] cells = warning;
            warning = new int[0];
            if (pos != warningOrigin) {
                spend(TICK);
                return true;
            }
            pendingCells = cells;
            beamReady = DailyRun.elapsedTurns() + 5;
            spend(TICK);
            int from = warningOrigin;
            boolean visible = false;
            for (int cell : cells) if (Dungeon.visible[cell]) visible = true;
            if (visible && sprite instanceof DailyEyeSprite && sprite.parent != null)
                ((DailyEyeSprite)sprite).beam(from, cells[cells.length - 1], () -> finishBeam(cells, from));
            else finishBeam(cells, from);
            return pendingCells == null;
        }
        Dungeon.level.updateFieldOfView(this);
        Char target = chooseEnemy();
        if (state == HUNTING && DailyRun.elapsedTurns() >= beamReady && target != null
                && target.isAlive() && target.invisible == 0 && Level.fieldOfView[target.pos]
                && !isCharmedBy(target)) {
            int[] cells = RebornBoss.ray(pos, target.pos, 6);
            if (RebornBoss.contains(cells, target.pos) && RebornBoss.hasEscape(cells)) {
                warning = cells;
                warningOrigin = pos;
                warnedAt = Dungeon.hero.rebornActions;
                waits = 0;
                rootGraceUsed = false;
                Dungeon.hero.interrupt();
                Dungeon.hero.restoreHealth = false;
                if (Dungeon.visible[pos]) GLog.w("daily.eye_warning");
                spend(TICK);
                return true;
            }
        }
        return super.act();
    }

    @Override
    protected boolean canAttack(Char target) {
        if (kind == 2) return false;
        if (kind == 0) return super.canAttack(target);
        return !isCharmedBy(target) && Level.distance(pos, target.pos) <= 5
                && Ballistica.cast(pos, target.pos, false, true) == target.pos;
    }

    @Override
    protected boolean doAttack(Char target) {
        final int from = pos;
        final int[] cells = {target.pos};
        pendingCells = cells;
        spend(prepareRebornAttack(target, attackDelay()));
        if (sprite != null && sprite.parent != null && (sprite.visible || Dungeon.visible[target.pos])) {
            if (kind == 1 && !Level.adjacent(from, target.pos))
                sprite.attack(target.pos, () -> launchSpike(cells, target, from));
            else sprite.attack(target.pos, () -> finishAttack(cells, target, from));
        } else finishAttack(cells, target, from);
        return pendingCells == null;
    }

    private void launchSpike(int[] cells, Char target, int from) {
        if (pendingCells != cells) return;
        if (!inCurrentRun() || !isAlive() || sprite == null || sprite.parent == null) {
            finishAttack(cells, target, from);
            return;
        }
        sprite.idle();
        ((MissileSprite)sprite.parent.recycle(MissileSprite.class))
                .reset(from, cells[0], new Dart(), () -> finishAttack(cells, target, from));
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

    private void finishBeam(int[] cells, int from) {
        if (pendingCells != cells) return;
        pendingCells = null;
        try {
            if (!inCurrentRun() || !isAlive()) return;
            for (int cell : cells) {
                if (Level.solid[cell] || !RebornBoss.lineClear(from, cell)) break;
                Char victim = Actor.findChar(cell);
                if (victim == null || victim == this || !victim.isAlive()) continue;
                if (victim instanceof Hero && ((Hero)victim).dodgeWithArmor(this)) continue;
                int damage = Random.NormalIntRange(14, 20);
                NecroWeakened weakened = buff(NecroWeakened.class);
                if (weakened != null) damage = (int)(damage * weakened.outgoingDamageModifier());
                if (victim instanceof Hero) damage = adjustHeroDamage((Hero)victim, damage);
                damage = victim.defenseProc(this, damage);
                if (damage >= 0) victim.damageFrom(damage, this, this, CombatTextIcon.MAGIC);
                if (victim.sprite != null) victim.sprite.flash();
                if (sprite != null && sprite.parent != null && Dungeon.visible[cell])
                    CellEmitter.center(cell).burst(PurpleParticle.BURST, 2);
                if (victim instanceof Hero && !victim.isAlive())
                    Dungeon.fail(Utils.format(ResultDescriptions.MOB, Utils.indefinite(name), Dungeon.depth), this);
                if (!isAlive() || DailyRun.finished()) break;
            }
        } finally {
            if (sprite != null && isAlive()) sprite.idle();
            next();
        }
    }

    @Override
    public int attackSkill(Char target) {
        return kind == 0 ? 26 : 24;
    }

    @Override
    public int damageRoll() {
        return kind == 0 ? Random.NormalIntRange(8, 14) : Random.NormalIntRange(8, 13);
    }

    @Override
    public int dr() {
        return kind == 0 ? 3 : 2;
    }

    @Override
    public int attackProc(Char target, int damage) {
        if (inCurrentRun() && kind < 2 && damage > 0 && target == Dungeon.hero && target.buff(Charm.class) == null
                && target.buff(Cripple.class) == null && ((DailySealLevel)Dungeon.level).tryDebuff()) {
            if (kind == 0) {
                Buff.affect(target, Charm.class, 2 * Charm.durationFactor(target)).object = id();
                if (target.sprite != null && target.sprite.parent != null) {
                    target.sprite.centerEmitter().burst(Speck.factory(Speck.HEART), 3);
                    Sample.INSTANCE.play(Assets.SND_CHARMS);
                }
            } else Buff.affect(target, Cripple.class, 2);
        }
        return super.attackProc(target, damage);
    }

    @Override
    public boolean attack(Char target) {
        return inCurrentRun() && isAlive() && target != null && target.isAlive() && super.attack(target);
    }

    @Override
    public void onAttackComplete() {

    }

    @Override
    public void damage(int damage, Object source) {
        if (inCurrentRun()) super.damage(damage, source);
    }

    @Override
    public void die(Object cause) {
        if (inCurrentRun()) super.die(cause);
    }

    @Override
    protected void onRemove() {
        warning = new int[0];
        pendingCells = null;
        next();
        super.onRemove();
    }
}
