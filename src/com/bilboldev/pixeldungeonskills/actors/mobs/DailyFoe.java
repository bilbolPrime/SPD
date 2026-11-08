package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.DailyRun;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.buffs.Buff;
import com.bilboldev.pixeldungeonskills.actors.buffs.Frostbite;
import com.bilboldev.pixeldungeonskills.actors.buffs.Poison;
import com.bilboldev.pixeldungeonskills.effects.Speck;
import com.bilboldev.pixeldungeonskills.levels.DailyLevel;
import com.bilboldev.pixeldungeonskills.messages.Message;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.sprites.GnollSprite;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.pixeldungeonskills.sprites.BanditSprite;
import com.bilboldev.pixeldungeonskills.sprites.BatSprite;
import com.bilboldev.pixeldungeonskills.sprites.BruteSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatBruteSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatSprite;
import com.bilboldev.pixeldungeonskills.sprites.RatKingSprite;
import com.bilboldev.pixeldungeonskills.sprites.SkeletonSprite;
import com.bilboldev.pixeldungeonskills.sprites.SpinnerSprite;
import com.bilboldev.pixeldungeonskills.sprites.ThiefSprite;
import com.bilboldev.pixeldungeonskills.sprites.TenguSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;
import java.util.HashSet;

public class DailyFoe extends Mob {
    private int kind;
    private int frostbiteReady;

    public DailyFoe() { this(0); }

    public DailyFoe(int kind) {
        this.kind = kind;
        appearance();
        HP = HT = leader() ? 95 : escapeCaptain() ? 40 : kind == 7 ? 26 : kind >= 2 ? 18 : 14;
        if (kind == 8 || kind == 11) HP = HT = 28;
        else if (kind == 9) HP = HT = 48;
        else if (kind == 10) HP = HT = 34;
        EXP = 0;
        maxLvl = 0;
        noLoot = true;
        state = SLEEPEING;
    }

    public boolean leader() { return kind == 4 || kind == 5; }
    public boolean escapeCaptain() { return kind == 6; }
    public boolean bodyguard() { return kind == 7; }

    private void appearance() {
        range = kind == 5 ? 4 : 1;
        switch (kind) {
            case 0: spriteClass = RatSprite.class; break;
            case 1: spriteClass = GnollSprite.class; break;
            case 2: spriteClass = SkeletonSprite.class; break;
            case 3: spriteClass = ThiefSprite.class; break;
            case 4: spriteClass = RatKingSprite.class; break;
            case 5: spriteClass = TenguSprite.class; break;
            case 6: spriteClass = BanditSprite.class; break;
            case 7: spriteClass = RatBruteSprite.class; break;
            case 8: spriteClass = BatSprite.class; break;
            case 9: spriteClass = BruteSprite.class; break;
            case 10: spriteClass = SpinnerSprite.class; break;
            case 11: spriteClass = IceSprite.Bat.class; break;
            default: throw new IllegalArgumentException("Unknown daily foe");
        }
        name = displayName();
        defenseSkill = kind >= 8 ? 13 : leader() || escapeCaptain() || kind == 7 ? 10 : 6;
        flying = kind == 8 || kind == 11;
        baseSpeed = flying ? 1.5f : 1;
    }

    private boolean inCurrentRun() {
        return DailyRun.active() && !DailyRun.finished() && Dungeon.level instanceof DailyLevel
                && Dungeon.level.mobs.contains(this);
    }

    @Override
    protected boolean act() {
        if (!inCurrentRun() || !isAlive() || sprite == null) {
            spend(TICK);
            return true;
        }
        return super.act();
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
    public void damage(int damage, Object source) {
        if (inCurrentRun()) super.damage(damage, source);
    }

    @Override public Message nameMessage() {
        return kind == 11 ? Message.key(RimeBat.class, "name") : Message.key("daily.foe_" + kind);
    }
    @Override public String displayName() { return nameMessage().toString(); }
    @Override public String description() { return Messages.get("daily.foe_desc_" + kind); }
    @Override public int attackSkill(Char target) {
        if (kind >= 8) return kind == 9 ? 19 : 16;
        return leader() ? 17 : escapeCaptain() ? 15 : kind == 7 ? 14 : 12;
    }
    @Override public int damageRoll() {
        if (kind == 8 || kind == 11) return Random.NormalIntRange(5, 9);
        if (kind == 9) return Random.NormalIntRange(9, 14);
        if (kind == 10) return Random.NormalIntRange(6, 10);
        return leader() ? Random.NormalIntRange(7, 13)
                : escapeCaptain() ? Random.NormalIntRange(5, 9)
                : kind == 7 ? Random.NormalIntRange(4, 8) : Random.NormalIntRange(3, 7);
    }
    @Override public int dr() {
        if (kind >= 8) return kind == 9 ? 3 : 2;
        return leader() ? 2 : escapeCaptain() || kind == 7 ? 1 : 0;
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return kind == 11 ? IceMob.IMMUNITIES : super.immunities();
    }

    @Override
    public int attackProc(Char target, int damage) {
        if (kind == 8 && damage > 0) {
            int restored = Math.min(2, HT - HP);
            if (restored > 0) {
                HP += restored;
                showRecovery(restored, false);
                if (sprite != null) sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
            }
        } else if (kind == 10 && damage > 0 && Random.Int(3) == 0) {
            Buff.affect(target, Poison.class).setChampionDuration(3 * Poison.durationFactor(target));
        } else if (kind == 11 && damage > 0 && RebornBoss.heroActions() >= frostbiteReady
                && target.buff(Frostbite.class) == null) {
            Buff.affect(target, Frostbite.class, 2);
            frostbiteReady = RebornBoss.heroActions() + 4;
        }
        return super.attackProc(target, damage);
    }
    @Override public void die(Object cause) {
        if (!inCurrentRun()) return;
        super.die(cause);
        if (leader()) DailyRun.finish(true);
        else if (escapeCaptain()) DailyRun.checkObjective();
    }
    @Override public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("daily_kind", kind);
    }
    @Override public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        kind = bundle.getInt("daily_kind");
        appearance();
    }
}
