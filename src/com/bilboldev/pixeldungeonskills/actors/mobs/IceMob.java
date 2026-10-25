package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.LocalCoop;
import com.bilboldev.pixeldungeonskills.ResultDescriptions;
import com.bilboldev.pixeldungeonskills.actors.Actor;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Amok;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frost;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frostbite;
import com.bilboldev.pixeldungeonskills.actors.buffs.FrostCoating;
import com.bilboldev.pixeldungeonskills.actors.buffs.NecroWeakened;
import com.bilboldev.pixeldungeonskills.actors.buffs.Sleep;
import com.bilboldev.pixeldungeonskills.actors.buffs.Terror;
import com.bilboldev.pixeldungeonskills.actors.hero.Hero;
import com.bilboldev.pixeldungeonskills.items.potions.PotionOfFrost;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.FrostBow;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.GravefrostScythe;
import com.bilboldev.pixeldungeonskills.items.weapon.ice.HoarfrostStaff;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import com.bilboldev.pixeldungeonskills.utils.Utils;

import java.util.HashSet;

public abstract class IceMob extends Mob {
    {
        loot = PotionOfFrost.class;
        lootChance = 0.1f;
    }

    static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();
    static {
        IMMUNITIES.add(Frost.class);
        IMMUNITIES.add(Frostbite.class);
        IMMUNITIES.add(FrostCoating.class);
        IMMUNITIES.add(HoarfrostStaff.class);
        IMMUNITIES.add(FrostBow.class);
        IMMUNITIES.add(GravefrostScythe.class);
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }

    protected int cooldown = 2;
    protected int[] warning = new int[0];
    protected int attackOrigin;
    private int warnedAt, warningHero, lastAction = -1, waits;
    private int recovery;
    private boolean rootGraceUsed;

    public int[] warningCells() {
        return warning.clone();
    }

    public boolean meleeWarning() {
        return false;
    }

    @Override
    protected boolean act() {
        Hero target = targetHero();
        if (warning.length > 0 && LocalCoop.active()) {
            for (Hero hero : LocalCoop.heroes()) {
                if (hero.id() == warningHero && hero.isAlive()) target = hero;
            }
        }
        return LocalCoop.active() ? LocalCoop.withHero(target, this::actWithTarget) : actWithTarget();
    }

    private boolean actWithTarget() {
        if (!isAlive()) return true;
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
            resolve(cells);
            spend(TICK);
            return true;
        }
        if (recovery > 0) {
            recovery--;
            spend(TICK);
            return true;
        }
        int action = RebornBoss.heroActions();
        if (action != lastAction) {
            lastAction = action;
            if (cooldown > 0) cooldown--;
        }
        Dungeon.level.updateFieldOfView(this);
        Hero hero = Dungeon.hero;
        if (state == HUNTING && hero.isAlive() && hero.invisible == 0 && Level.fieldOfView[hero.pos]
                && cooldown == 0 && prepare()) {
            spend(TICK);
            return true;
        }
        return super.act();
    }

    protected boolean prepare() {
        return false;
    }

    protected void resolve(int[] cells) { }

    protected boolean warn(int[] cells, int delay) {
        if (cells.length == 0 || !RebornBoss.hasEscape(cells)) return false;
        warning = cells;
        attackOrigin = pos;
        warnedAt = Dungeon.hero.rebornActions;
        warningHero = Dungeon.hero.id();
        waits = 0;
        rootGraceUsed = false;
        cooldown = delay;
        Dungeon.hero.interrupt();
        Dungeon.hero.restoreHealth = false;
        return true;
    }

    protected void recover() {
        recovery = 1;
    }

    protected void strike(int[] cells, int low, int high) {
        for (int cell : cells) {
            if (!RebornBoss.valid(cell) || Level.solid[cell] || !RebornBoss.lineClear(attackOrigin, cell)) continue;
            Char victim = Actor.findChar(cell);
            if (victim == null || victim == this || victim instanceof Mob && ((Mob)victim).hostile) continue;
            if (victim instanceof Hero && !Level.adjacent(attackOrigin, cell) && ((Hero)victim).dodgeWithArmor(this)) continue;
            int damage = Random.NormalIntRange(low, high);
            NecroWeakened weakened = buff(NecroWeakened.class);
            if (weakened != null) damage = (int)(damage * weakened.outgoingDamageModifier());
            if (victim instanceof Hero) damage = adjustHeroDamage((Hero)victim, damage);
            damage = victim.defenseProc(this, Math.max(0, damage - Random.IntRange(0, victim.dr())));
            if (damage >= 0) victim.damageFrom(damage, this, this);
            if (victim.sprite != null) victim.sprite.flash();
            if (victim instanceof Hero && !victim.isAlive())
                Dungeon.fail(Utils.format(ResultDescriptions.MOB, Utils.indefinite(name), Dungeon.depth), this);
            if (!isAlive()) break;
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("warning", warning);
        bundle.put("attack_origin", attackOrigin);
        bundle.put("warned_at", warnedAt);
        bundle.put("warning_hero", warningHero);
        bundle.put("skill_cooldown", cooldown);
        bundle.put("last_action", lastAction);
        bundle.put("ice_recovery", recovery);
        bundle.put("paralysis_waits", waits);
        bundle.put("root_grace_used", rootGraceUsed);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        warning = bundle.getIntArray("warning");
        attackOrigin = bundle.getInt("attack_origin");
        warnedAt = bundle.getInt("warned_at");
        warningHero = bundle.getInt("warning_hero");
        cooldown = bundle.getInt("skill_cooldown");
        lastAction = bundle.getInt("last_action");
        recovery = bundle.getInt("ice_recovery");
        waits = bundle.getInt("paralysis_waits");
        rootGraceUsed = bundle.getBoolean("root_grace_used");
    }
}
