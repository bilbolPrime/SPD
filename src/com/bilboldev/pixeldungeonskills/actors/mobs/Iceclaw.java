package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.actors.blobs.Fire;
import com.bilboldev.pixeldungeonskills.actors.buffs.Burning;
import com.bilboldev.pixeldungeonskills.actors.mobs.npcs.Prospector;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Bundle;
import com.bilboldev.utils.Random;

public class Iceclaw extends IceMob {
    private int bracedUntil;

    {
        name = "Iceclaw";
        spriteClass = IceSprite.Claw.class;
        HP = HT = 58;
        defenseSkill = 9;
        EXP = 8;
        maxLvl = 18;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(9, 17); }
    @Override public int attackSkill(Char enemy) { return 22; }
    @Override public int dr() { return braced() ? 12 : 8; }

    public boolean braced() {
        return bracedUntil > 0 && RebornBoss.heroActions() < bracedUntil;
    }

    @Override
    protected boolean prepare() {
        if (Level.distance(pos, Dungeon.hero.pos) > 2 || buff(Burning.class) != null) return false;
        bracedUntil = RebornBoss.heroActions() + 2;
        cooldown = 5;
        return true;
    }

    @Override
    public void damage(int damage, Object source) {
        if (source instanceof Burning || source instanceof Fire) bracedUntil = 0;
        super.damage(damage, source);
    }

    @Override
    public void die(Object cause) {
        if (Dungeon.level.mobs.contains(this)) {
            for (Mob mob : Dungeon.level.mobs) {
                if (mob instanceof Prospector) ((Prospector)mob).iceclawSlain();
            }
        }
        super.die(cause);
    }

    @Override public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("braced_until", bracedUntil);
    }

    @Override public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        bracedUntil = bundle.getInt("braced_until");
    }
}
