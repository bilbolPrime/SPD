package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.items.weapon.missiles.Shuriken;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.levels.PrisonBossLevel;
import com.bilboldev.pixeldungeonskills.mechanics.Ballistica;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.scenes.GameScene;
import com.bilboldev.pixeldungeonskills.sprites.MissileSprite;
import com.bilboldev.pixeldungeonskills.utils.GLog;
import com.bilboldev.pixeldungeonskills.utils.Utils;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class RebornTengu extends Tengu {
    private int unlocked;
    private int recovery = 3;
    private int reposition = 6;
    private int lastSkill;
    private int[] warning = new int[0];
    private int shotOrigin;
    private int heroActionAtWarning;
    private int throwsLeft;
    private int openingUntil;
    private boolean rootGraceUsed, defeated;
    private int paralysisWaits;
    private transient Hero warningTarget;

    {
        HP = HT = Math.round(HT * 1.5f);
    }

    public int[] warningCells() {
        return warning.clone();
    }

    public int warningOrigin() {
        return shotOrigin;
    }

    public int unlockedSkills() {
        return unlocked;
    }

    public boolean opening() {
        return openingUntil > RebornBoss.heroActions();
    }

    public void shadowBroken() {
        warning = new int[0];
        throwsLeft = 0;
        openingUntil = RebornBoss.heroActions() + 3;
        recovery = 4;
        reposition = Math.max(reposition, 4);
        yell(Message.of(this, "shadow_broken"));
    }

    public static void warnings(Level level, boolean[] cells) {
        Arrays.fill(cells, false);
        for (Mob mob : level.mobs) if (mob instanceof RebornTengu && mob.isAlive()) {
            for (int cell : ((RebornTengu)mob).warning)
                if (cell >= 0 && cell < cells.length) cells[cell] = true;
        }
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

        if (paralysed || buff(Frost.class) != null || buff(Sleep.class) != null
                || buff(Terror.class) != null || buff(Amok.class) != null) {
            warning = new int[0];
            throwsLeft = 0;
            recovery = Math.max(recovery, 2);
            return super.act();
        }
        unlockSkills();
        if (opening()) {
            spend(TICK);
            return true;
        }
        if (warning.length > 0) {
            if (Dungeon.hero.paralysed ? paralysisWaits++ < 3 : heroActionAtWarning == Dungeon.hero.rebornActions) {
                spend(TICK);
                return true;
            }
            if (Dungeon.hero.rooted && !rootGraceUsed) {
                rootGraceUsed = true;
                heroActionAtWarning = Dungeon.hero.rebornActions;
                spend(TICK);
                return true;
            }
            fireMarkedShot();
            if (!isAlive() || defeated) {
                spend(TICK);
                return true;
            }
            if (--throwsLeft > 0 && Dungeon.hero.isAlive() && canAimAtHero()) {
                relocate(Dungeon.hero.pos, true, true);
                if (!markShot()) throwsLeft = 0;
            }
            if (throwsLeft <= 0) recovery = 3;
            spend(TICK);
            return true;
        }
        if (recovery > 0) recovery--;
        if (reposition > 0) reposition--;
        return super.act();
    }

    private void unlockSkills() {
        int next = unlocked;
        if (HP * 4 <= HT * 3) next |= 1;
        if (HP * 2 <= HT) next |= 2;
        if (HP * 4 <= HT) next |= 4;
        int gained = next & ~unlocked;
        unlocked = next;
        if (gained != 0) {
            yell(Message.of(this, (gained & 4) != 0 ? "double_throw"
                    : (gained & 2) != 0 ? "shadow_ready" : "dead_eye"));
        }
    }

    @Override
    protected boolean doAttack(Char target) {
        if (recovery == 0 && canAimAtHero()) {
            if ((unlocked & 2) != 0 && lastSkill != 2 && summonShadow()) {
                lastSkill = 2;
                recovery = 4;
                spend(TICK);
                return true;
            }
            if ((unlocked & 1) != 0 && markShot()) {
                throwsLeft = (unlocked & 4) != 0 ? 2 : 1;
                lastSkill = 1;
                spend(TICK);
                return true;
            }
        }
        if (reposition == 0 && relocate(target.pos, true)) {
            spend(TICK);
            return true;
        }
        boolean visible = sprite != null && Dungeon.visible[pos];
        if (visible) sprite.attack(target.pos);
        else attack(target);
        spend(attackDelay());
        return !visible;
    }

    private boolean canAimAtHero() {
        if (!Dungeon.hero.isAlive() || Dungeon.hero.invisible > 0 || isCharmedBy(Dungeon.hero)) return false;
        Dungeon.level.updateFieldOfView(this);
        if (!Level.fieldOfView[Dungeon.hero.pos]) return false;
        for (int cell : firingLine(pos, Dungeon.hero.pos)) if (cell == Dungeon.hero.pos) return true;
        return false;
    }

    public static int[] firingLine(int from, int to) {
        ArrayList<Integer> cells = new ArrayList<>();
        int x = from % Level.WIDTH, y = from / Level.WIDTH;
        int dx = Math.abs(to % Level.WIDTH-x), dy = Math.abs(to / Level.WIDTH-y);
        int sx = Integer.compare(to % Level.WIDTH, x), sy = Integer.compare(to / Level.WIDTH, y);
        if (dx == 0 && dy == 0) return new int[0];
        int error = dx-dy;
        for (int step = 0; step < Level.WIDTH+Level.HEIGHT; step++) {
            int twice = error*2;
            if (twice > -dy) {
                error -= dy;
                x += sx;
            }
            if (twice < dx) {
                error += dx;
                y += sy;
            }
            if (x < 0 || y < 0 || x >= Level.WIDTH || y >= Level.HEIGHT) break;
            int cell = x+y*Level.WIDTH;
            if (Level.solid[cell]) break;
            cells.add(cell);
        }
        int[] result = new int[cells.size()];
        for (int i = 0; i < result.length; i++) result[i] = cells.get(i);
        return result;
    }

    private boolean markShot() {
        int[] cells = firingLine(pos, Dungeon.hero.pos);
        boolean reachesHero = false;
        for (int cell : cells) if (cell == Dungeon.hero.pos) reachesHero = true;
        if (!reachesHero) return false;
        boolean escape = false;
        for (int offset : Level.NEIGHBOURS8) {
            int p = Dungeon.hero.pos+offset;
            if (p < 0 || p >= Level.LENGTH || !Level.adjacent(p, Dungeon.hero.pos)
                    || !Level.passable[p] || Level.avoid[p] || Actor.findChar(p) != null) continue;
            boolean marked = false;
            for (int cell : cells) if (p == cell) marked = true;
            if (!marked) escape = true;
        }
        if (cells.length == 0 || !escape) return false;
        warning = cells;
        warningTarget = LocalCoop.active() ? Dungeon.hero : null;
        shotOrigin = pos;
        heroActionAtWarning = Dungeon.hero.rebornActions;
        rootGraceUsed = false;
        paralysisWaits = 0;
        Dungeon.hero.interrupt();
        Dungeon.hero.restoreHealth = false;
        GLog.w(this.getClass(), "warning");
        return true;
    }

    private void fireMarkedShot() {
        int[] cells = warning;
        warning = new int[0];
        int end = shotOrigin;
        for (int cell : cells) {
            if (cell < 0 || cell >= Level.LENGTH || Level.solid[cell]) break;
            end = cell;
            Char victim = Actor.findChar(cell);
            if (victim == null || victim == this || victim instanceof TenguShadow) continue;
            if ((victim == Dungeon.hero || LocalCoop.contains(victim)) && ((Hero)victim).dodgeWithArmor(this)) continue;
            int damage = Random.NormalIntRange(12, 20);
            NecroWeakened weakened = buff(NecroWeakened.class);
            if (weakened != null) damage = (int)(damage * weakened.outgoingDamageModifier());
            if (victim == Dungeon.hero || LocalCoop.contains(victim)) {
                damage = adjustHeroDamage((Hero)victim, damage);
            }
            damage = victim.defenseProc(this, Math.max(0, damage - victim.dr()));
            if (damage >= 0) victim.damageFrom(damage, this, this);
            if (victim.sprite != null) victim.sprite.flash();
            if ((victim == Dungeon.hero || LocalCoop.contains(victim)) && !victim.isAlive())
                Dungeon.fail(Utils.format(ResultDescriptions.MOB, Utils.indefinite(name), Dungeon.depth), this);
        }
        if (end != shotOrigin && sprite != null && sprite.parent != null)
            ((MissileSprite)sprite.parent.recycle(MissileSprite.class)).reset(shotOrigin, end, new Shuriken(), null);
    }

    private boolean arenaCell(int cell) {
        return cell >= 0 && cell < Level.LENGTH && Level.passable[cell] && !Level.avoid[cell]
                && (!(Dungeon.level instanceof PrisonBossLevel) || ((PrisonBossLevel)Dungeon.level).insideArena(cell));
    }

    private boolean relocate(int target, boolean animate) {
        return relocate(target, animate, false);
    }

    private boolean relocate(int target, boolean animate, boolean flank) {
        if (rooted) return false;
        ArrayList<Integer> choices = new ArrayList<>();
        for (int cell = 0; cell < Level.LENGTH; cell++) {
            int distance = Level.distance(pos, cell), targetDistance = Level.distance(target, cell);
            if (flank) {
                int ax = pos % Level.WIDTH - target % Level.WIDTH, ay = pos / Level.WIDTH - target / Level.WIDTH;
                int bx = cell % Level.WIDTH - target % Level.WIDTH, by = cell / Level.WIDTH - target / Level.WIDTH;
                if (Math.abs(ax * by - ay * bx) <= ax * bx + ay * by) continue;
            }
            if (distance >= 2 && distance <= 4 && targetDistance >= 2 && targetDistance <= 5
                    && arenaCell(cell) && Actor.findChar(cell) == null
                    && Ballistica.cast(cell, target, false, false) == target
                    && RebornBoss.contains(firingLine(cell, target), target)) choices.add(cell);
        }
        reposition = 6;
        if (choices.isEmpty()) return false;
        int previous = pos;
        move(Random.element(choices));
        Actor.freeCell(previous);
        Actor.occupyCell(this);
        if (Dungeon.level instanceof PrisonBossLevel)
            ((PrisonBossLevel)Dungeon.level).shuffleRebornTraps();
        if (animate && sprite != null) sprite.move(previous, pos);
        Dungeon.observe();
        return true;
    }

    @Override
    protected boolean getCloser(int target) {

        return reposition == 0 && relocate(target, false);
    }

    @Override
    protected boolean canAttack(Char target) {
        return !isCharmedBy(target) && super.canAttack(target);
    }

    @Override
    protected boolean getFurther(int target) {
        return false;
    }

    private boolean summonShadow() {
        for (Mob mob : Dungeon.level.mobs)
            if (mob instanceof TenguShadow && ((TenguShadow)mob).ownerId == id() && mob.isAlive()) return false;
        ArrayList<Integer> cells = new ArrayList<>();
        for (int cell = 0; cell < Level.LENGTH; cell++)
            if (arenaCell(cell) && Actor.findChar(cell) == null && Level.distance(cell, Dungeon.hero.pos) >= 2
                    && Level.distance(cell, pos) <= 4
                    && Ballistica.cast(cell, Dungeon.hero.pos, false, false) == Dungeon.hero.pos) cells.add(cell);
        if (cells.isEmpty()) return false;
        TenguShadow shadow = new TenguShadow();
        shadow.ownerId = id();
        shadow.pos = Random.element(cells);
        GameScene.add(shadow);
        yell(Message.of(this, "summon"));
        return true;
    }

    @Override
    public void die(Object cause) {
        if (defeated) return;
        defeated = true;
        warning = new int[0];
        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0]))
            if (mob instanceof TenguShadow && ((TenguShadow)mob).ownerId == id()) mob.die(null);
        super.die(cause);
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
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("skills", unlocked);
        bundle.put("recovery", recovery);
        bundle.put("reposition", reposition);
        bundle.put("last_skill", lastSkill);
        bundle.put("warning", warning);
        bundle.put("shot_origin", shotOrigin);
        bundle.put("hero_action", heroActionAtWarning);
        bundle.put("throws_left", throwsLeft);
        bundle.put("opening_until", openingUntil);
        bundle.put("root_grace_used", rootGraceUsed);
        bundle.put("defeated", defeated);
        bundle.put("paralysis_waits", paralysisWaits);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        unlocked = bundle.getInt("skills");
        recovery = bundle.getInt("recovery");
        reposition = bundle.getInt("reposition");
        lastSkill = bundle.getInt("last_skill");
        warning = bundle.getIntArray("warning");
        if (warning == null) warning = new int[0];
        shotOrigin = bundle.getInt("shot_origin");
        heroActionAtWarning = bundle.getInt("hero_action");
        throwsLeft = bundle.getInt("throws_left");
        openingUntil = bundle.getInt("opening_until");
        rootGraceUsed = bundle.getBoolean("root_grace_used");
        defeated = bundle.getBoolean("defeated");
        paralysisWaits = bundle.getInt("paralysis_waits");
    }
}
