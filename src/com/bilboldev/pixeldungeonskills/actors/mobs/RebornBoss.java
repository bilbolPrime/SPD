package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.pixeldungeonskills.*;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.blobs.ToxicGas;
import com.bilboldev.pixeldungeonskills.actors.buffs.*;
import com.bilboldev.pixeldungeonskills.effects.CellEmitter;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.effects.Splash;
import com.bilboldev.pixeldungeonskills.items.keys.SkeletonKey;
import com.bilboldev.pixeldungeonskills.items.scrolls.ScrollOfPsionicBlast;
import com.bilboldev.pixeldungeonskills.items.weapon.enchantments.Death;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public abstract class RebornBoss extends Mob {
    public static int heroActions() {
        return LocalCoop.active() ? LocalCoop.actionClock() : Dungeon.hero.rebornActions;
    }

    public static final int SLIME = 1, DRILL = 2, STEAM = 3, ORDER = 4, RIFT = 5, EMBER = 6, ROT = 7;
    protected int[] warning = new int[0], residue = new int[0];
    protected int warningKind, warningOrigin, warnedAt, cooldown = 3, openingUntil;
    protected int residueKind, residueTurns, residueAt, lastAction = -1, phase;
    private int paralysisWaits;
    private transient Hero warningTarget;
    protected boolean defeated, rootGraceUsed;

    public int[] warningCells() {
        return warning.clone();
    }

    public int[] residueCells() {
        return residue.clone();
    }

    public int warningKind() {
        return warningKind;
    }

    public int warningOrigin() {
        return warningOrigin;
    }

    public boolean showsWarning() {
        return true;
    }

    public int residueKind() {
        return residueKind;
    }

    public int phase() {
        return phase;
    }

    public boolean opening() {
        return Dungeon.hero != null && openingUntil > actionClock();
    }

    protected int actionClock() {
        return heroActions();
    }

    protected boolean awaitingResponse() {
        return Dungeon.hero.paralysed ? paralysisWaits++ < 3 : warnedAt == Dungeon.hero.rebornActions;
    }

    protected abstract Class<? extends Mob> original();

    @Override
    public Message nameMessage() {
        return Message.key(original(), "name");
    }
    protected abstract boolean prepareSkill();
    protected abstract void resolveSkill(int kind, int[] cells);

    @Override
    public String displayName() {
        return Messages.get(original(), "name");
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
    public void yell(Message text) {
        GLog.n("custom.generated.entry_s_s_3ac152a1c1", Message.actor(this), text);
    }

    protected boolean controlled() {
        return paralysed || buff(Frost.class) != null || buff(Sleep.class) != null
                || buff(Terror.class) != null || buff(Amok.class) != null || isCharmedBy(Dungeon.hero);
    }

    @Override
    protected boolean act() {
        if (LocalCoop.active()) {
            Hero target = warning.length > 0 && warningTarget != null && warningTarget.isAlive()
                    ? warningTarget : targetHero();
            return LocalCoop.withHero(target, () -> actWithTarget());
        }
        return actWithTarget();
    }

    private boolean actWithTarget() {
        if (defeated || !isAlive()) return true;
        tickResidue();
        if (defeated || !isAlive()) return true;
        if (controlled()) {
            if (warning.length > 0) interruptSkill();
            if (opening()) {
                spend(TICK);
                return true;
            }
            return super.act();
        }
        if (warning.length > 0) {

            if (awaitingResponse()) {
                spend(TICK);
                return true;
            }

            if (Dungeon.hero.rooted && !rootGraceUsed) {
                rootGraceUsed = true;
                warnedAt = Dungeon.hero.rebornActions;
                spend(TICK);
                return true;
            }
            int kind = warningKind;
            int[] cells = warning;
            warning = new int[0];
            skillSound(kind, cells);
            resolveSkill(kind, cells);
            spend(TICK);
            return true;
        }
        if (opening()) {
            spend(TICK);
            return true;
        }
        if (lastAction != actionClock()) {
            lastAction = actionClock();
            if (cooldown > 0) cooldown--;
        }
        if (seesHero() && cooldown <= 0 && prepareSkill()) {
            state = HUNTING;
            spend(TICK);
            return true;
        }
        return super.act();
    }

    protected boolean seesHero() {
        Hero hero = LocalCoop.active() ? targetHero() : Dungeon.hero;
        if (!hero.isAlive() || hero.invisible > 0 || isCharmedBy(hero)) return false;
        Dungeon.level.updateFieldOfView(this);
        return Level.fieldOfView[hero.pos] && lineClear(pos, hero.pos);
    }

    protected boolean warn(int kind, int from, int[] cells) {
        if (cells.length == 0 || !hasEscape(cells)) return false;
        warning = cells.clone();
        warningTarget = LocalCoop.active() ? Dungeon.hero : null;
        warningKind = kind;
        warningOrigin = from;
        warnedAt = Dungeon.hero.rebornActions;
        rootGraceUsed = false;
        paralysisWaits = 0;
        Dungeon.hero.interrupt();
        Dungeon.hero.restoreHealth = false;
        if (showsWarning()) {
            GLog.w(warningMessage());
            warningSound(kind, cells);
        }
        return true;
    }

    protected Message warningMessage() {
        return Message.of(this, "warning");
    }

    protected void warningSound(int kind, int[] cells) {
        switch (kind) {
            case SLIME: sound(Assets.SND_PUFF, .6f, .65f, cells);
            break;
            case DRILL: sound(Assets.SND_ROCKS, .45f, .75f, cells);
            break;
            case STEAM: sound(Assets.SND_PUFF, .5f, 1.3f, cells);
            break;
            case RIFT:
            case EMBER:
            case ROT: sound(Assets.SND_BURNING, .25f, .8f, cells);
            break;
        }
    }

    protected void skillSound(int kind, int[] cells) {
        switch (kind) {
            case SLIME:
                sound(Assets.SND_BLAST, .75f, .7f, cells);
                sound(Assets.SND_WATER, .8f, .65f, cells);
                break;
            case DRILL: sound(Assets.SND_ROCKS, 1, 1, cells);
            break;
            case STEAM: sound(Assets.SND_PUFF, 1, .7f, cells);
            break;
            case RIFT: sound(Assets.SND_RAY, 1, .8f, cells);
            break;
            case EMBER: sound(Assets.SND_BURNING, 1, .85f, cells);
            break;
            case ROT:
                sound(Assets.SND_WATER, 1, .6f, cells);
                sound(Assets.SND_HIT, .7f, .75f, cells);
                break;
        }
    }

    protected boolean visibleEffect(int[] cells) {
        if (Dungeon.visible == null) return false;
        if (valid(pos) && Dungeon.visible[pos]) return true;
        for (int cell : cells) if (valid(cell) && Dungeon.visible[cell]) return true;
        return false;
    }

    protected void sound(String asset, float volume, float pitch, int[] cells) {
        if (visibleEffect(cells)) Sample.INSTANCE.play(asset, volume, volume, pitch);
    }

    public static boolean hasEscape(int[] cells) {
        if (LocalCoop.active()) {
            for (Hero hero : LocalCoop.heroes()) {
                if (hero.isAlive() && contains(cells, hero.pos) && !hasEscape(cells, hero.pos)) return false;
            }
        }
        return hasEscape(cells, Dungeon.hero.pos);
    }

    private static boolean hasEscape(int[] cells, int hero) {
        for (int offset : Level.NEIGHBOURS8) {
            int p = hero + offset;
            if (!valid(p) || !Level.adjacent(hero, p) || !Level.passable[p] || Level.avoid[p]
                    || Actor.findChar(p) != null || contains(cells, p) || dangerous(p)) continue;
            return true;
        }
        return false;
    }

    public static boolean dangerous(int cell) {
        ToxicGas gas = (ToxicGas)Dungeon.level.blobs.get(ToxicGas.class);
        if (gas != null && valid(cell) && gas.cur[cell] > 0) return true;
        Fire fire = (Fire)Dungeon.level.blobs.get(Fire.class);
        if (fire != null && valid(cell) && fire.cur[cell] > 0) return true;
        for (Mob mob : Dungeon.level.mobs) if (mob.isAlive()) {
            if (mob instanceof RebornBoss) {
                RebornBoss boss = (RebornBoss)mob;
                if (contains(boss.warning, cell) || contains(boss.residue, cell)) return true;
            } else if (mob instanceof RebornTengu && contains(((RebornTengu)mob).warningCells(), cell)) return true;
        }
        return false;
    }

    public static boolean[] movementFloor() {
        if (LocalCoop.active()) {
            boolean[] floor = Level.passable;
            for (Hero hero : LocalCoop.heroes()) {
                if (!hero.isAlive()) continue;
                boolean[] reserved = LocalCoop.withHero(hero, () -> movementFloorForHero());
                if (reserved == Level.passable) continue;
                if (floor == Level.passable) floor = Level.passable.clone();
                for (int i = 0; i < floor.length; i++) floor[i] &= reserved[i];
            }
            return floor;
        }
        return movementFloorForHero();
    }

    private static boolean[] movementFloorForHero() {
        if (!Dungeon.bossLevel() || Dungeon.hero == null) return Level.passable;
        boolean aimed = false;
        for (Mob mob : Dungeon.level.mobs) if (mob.isAlive()) {
            if (mob instanceof RebornBoss && contains(((RebornBoss)mob).warning, Dungeon.hero.pos)) aimed = true;
            if (mob instanceof RebornTengu && contains(((RebornTengu)mob).warningCells(), Dungeon.hero.pos)) aimed = true;
        }
        if (!aimed) return Level.passable;
        for (int offset : Level.NEIGHBOURS8) {
            int cell = Dungeon.hero.pos + offset;
            if (valid(cell) && Level.adjacent(cell, Dungeon.hero.pos) && Level.passable[cell] && !Level.avoid[cell]
                    && Actor.findChar(cell) == null && !dangerous(cell)) {
                boolean[] floor = Level.passable.clone();
                floor[cell] = false;
                return floor;
            }
        }
        return Level.passable;
    }

    public void interruptSkill() {
        warning = new int[0];
        recover(2);
        GLog.p(RebornBoss.class, "interrupted", Message.actor(this));
    }

    protected void recover(int actions) {
        if (defeated || !isAlive()) return;
        openingUntil = actionClock() + actions;
        cooldown = actions + 2;
        if (sprite != null) sprite.idle();
    }

    protected void strike(int[] cells, int min, int max) {
        strike(cells, min, max, false);
    }

    protected void strike(int[] cells, int min, int max, boolean ranged) {
        for (int cell : cells) {
            if (!valid(cell) || Level.solid[cell] || !lineClear(warningOrigin, cell)) continue;
            Char victim = Actor.findChar(cell);
            if (victim != null && victim != this && !ally(victim)) {
                if (ranged && (victim == Dungeon.hero || LocalCoop.contains(victim)) && ((Hero)victim).dodgeWithArmor(this)) continue;
                hit(victim, Random.NormalIntRange(min, max));
            }
            if (!isAlive()) break;
        }
    }

    protected boolean ally(Char ch) {
        return ch instanceof RebornBoss;
    }

    protected void hit(Char victim, int damage) {
        hit(victim, damage, true);
    }

    private void hit(Char victim, int damage, boolean attack) {
        NecroWeakened weakened = buff(NecroWeakened.class);
        if (weakened != null) damage = (int)(damage * weakened.outgoingDamageModifier());
        if (victim == Dungeon.hero || LocalCoop.contains(victim)) {
            damage = adjustHeroDamage((Hero)victim, damage);
        }
        damage = victim.defenseProc(this, Math.max(0, damage - Random.IntRange(0, victim.dr())));
        if (damage >= 0) {
            if (attack) victim.damageFrom(damage, this, this);
            else victim.damage(damage, this);
        }
        if (victim.sprite != null) victim.sprite.flash();
        if ((victim == Dungeon.hero || LocalCoop.contains(victim)) && !victim.isAlive())
            Dungeon.fail(Utils.format(ResultDescriptions.MOB, Utils.indefinite(name), Dungeon.depth), this);
    }

    protected void leaveResidue(int[] cells, int kind, int turns, int cap) {
        if (defeated || !isAlive()) return;
        ArrayList<Integer> safe = new ArrayList<>();
        for (int cell : cells) if (valid(cell) && Level.passable[cell] && !Level.water[cell]
                && cell != Dungeon.level.entrance && cell != Dungeon.level.exit && safe.size() < cap) safe.add(cell);
        residue = array(safe);
        residueKind = kind;
        residueTurns = turns;
        residueAt = RebornBoss.heroActions();
    }

    private void tickResidue() {
        if (residue.length == 0 || residueAt == RebornBoss.heroActions()) return;
        residueAt = RebornBoss.heroActions();
        for (int cell : residue) if (valid(cell) && !Level.water[cell]) {
            Char victim = Actor.findChar(cell);
            if (victim != null && victim != this && !ally(victim)) hit(victim, residueKind == SLIME ? 4 : 8, false);
            if (!isAlive()) return;
        }
        if (--residueTurns <= 0) residue = new int[0];
    }

    public static boolean valid(int cell) {
        return cell >= 0 && cell < Level.LENGTH;
    }

    public static boolean contains(int[] cells, int cell) {
        for (int c : cells) if (c == cell) return true;
        return false;
    }

    public static boolean lineClear(int from, int to) {
        return from == to || Ballistica.cast(from, to, false, false) == to;
    }

    public static int[] ray(int from, int to, int length) {
        int[] cells = RebornTengu.firingLine(from, to);
        return Arrays.copyOf(cells, Math.min(length, cells.length));
    }

    public static int[] cone(int from, int to, int radius) {
        ArrayList<Integer> cells = new ArrayList<>();
        int dx = to % Level.WIDTH - from % Level.WIDTH, dy = to / Level.WIDTH - from / Level.WIDTH;
        for (int y = -radius; y <= radius; y++) for (int x = -radius; x <= radius; x++) {
            int cell = from + x + y * Level.WIDTH;
            if (!valid(cell) || Level.distance(from, cell) > radius || Level.solid[cell]) continue;
            int forward = dx*x + dy*y, side = Math.abs(dx*y - dy*x);
            if (forward > 0 && side <= forward && lineClear(from, cell)) cells.add(cell);
        }
        return array(cells);
    }

    public static int[] array(ArrayList<Integer> cells) {
        int[] result = new int[cells.size()];
        for (int i = 0; i < result.length; i++) result[i] = cells.get(i);
        return result;
    }

    protected void burst(int[] cells, int speck) {
        burst(cells, Speck.factory(speck));
    }

    protected void burst(int[] cells, Emitter.Factory factory) {
        if (sprite == null || sprite.parent == null) return;
        for (int cell : cells) if (valid(cell) && Dungeon.visible[cell])
            CellEmitter.get(cell).burst(factory, 3);
    }

    protected void splash(int[] cells, int color) {
        if (sprite == null || sprite.parent == null) return;
        for (int cell : cells) if (valid(cell) && Dungeon.visible[cell])
            Splash.at(cell, color, 3);
    }

    @Override
    public void die(Object source) {
        if (defeated) return;
        defeated = true;
        warning = residue = new int[0];
        clearSummons();
        super.die(source);
        GameScene.bossSlain();
        Dungeon.level.drop(new SkeletonKey(), pos).sprite.drop();
        Badges.validateBossSlain();
    }

    protected void clearSummons() { }

    protected void dismiss() {
        if (defeated) return;
        defeated = true;
        warning = residue = new int[0];
        HP = 0;
        Actor.remove(this);
        Actor.freeCell(pos);
        Dungeon.level.mobs.remove(this);
        if (sprite != null) sprite.die();
    }

    private static final HashSet<Class<?>> RESISTANCES = new HashSet<Class<?>>(Arrays.<Class<?>>asList(
            Death.class, ScrollOfPsionicBlast.class));
    @Override
    public HashSet<Class<?>> resistances() {
        return RESISTANCES;
    }

    @Override
    public void storePartyRuntime(Bundle bundle) {
        super.storePartyRuntime(bundle);
        bundle.put("warning_target", warningTarget == null ? 0 : warningTarget.id());
    }

    @Override
    public void restorePartyRuntime(Bundle bundle) {
        super.restorePartyRuntime(bundle);
        warningTarget = null;
        for (Hero hero : LocalCoop.heroes())
            if (hero.id() == bundle.getInt("warning_target")) warningTarget = hero;
    }

    @Override
    public void storeInBundle(Bundle b) {
        super.storeInBundle(b);
        b.put("commitment", warning);
        b.put("attack_kind", warningKind);
        b.put("attack_origin", warningOrigin);
        b.put("warned_at", warnedAt);
        b.put("skill_cooldown", cooldown);
        b.put("opening_until", openingUntil);
        b.put("residue", residue);
        b.put("residue_kind", residueKind);
        b.put("residue_turns", residueTurns);
        b.put("residue_at", residueAt);
        b.put("last_action", lastAction);
        b.put("phase", phase);
        b.put("defeated", defeated);
        b.put("root_grace_used", rootGraceUsed);
        b.put("paralysis_waits", paralysisWaits);
    }

    @Override
    public void restoreFromBundle(Bundle b) {
        super.restoreFromBundle(b);
        warning = b.getIntArray("commitment");
        if (warning == null) warning = new int[0];
        warningKind = b.getInt("attack_kind");
        warningOrigin = b.getInt("attack_origin");
        warnedAt = b.getInt("warned_at");
        cooldown = b.getInt("skill_cooldown");
        openingUntil = b.getInt("opening_until");
        residue = b.getIntArray("residue");
        if (residue == null) residue = new int[0];
        residueKind = b.getInt("residue_kind");
        residueTurns = b.getInt("residue_turns");
        residueAt = b.getInt("residue_at");
        lastAction = b.getInt("last_action");
        phase = b.getInt("phase");
        defeated = b.getBoolean("defeated");
        rootGraceUsed = b.getBoolean("root_grace_used");
        paralysisWaits = b.getInt("paralysis_waits");
    }
}
