package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class HollowMiner extends IceMob {
    {
        name = "Hollow miner";
        spriteClass = IceSprite.Miner.class;
        HP = HT = 50;
        defenseSkill = 12;
        EXP = 8;
        maxLvl = 19;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(10, 18); }
    @Override public int attackSkill(Char enemy) { return 23; }
    @Override public int dr() { return 5; }
    @Override public boolean meleeWarning() { return warning.length > 0; }

    @Override
    protected boolean prepare() {
        for (Mob mob : Dungeon.level.mobs) {
            if (mob != this && mob.hostile && mob.state != mob.HUNTING && Level.distance(pos, mob.pos) <= 6)
                return warn(new int[]{pos}, 6);
        }
        return false;
    }

    @Override
    protected void resolve(int[] cells) {
        for (Mob mob : Dungeon.level.mobs) {
            if (mob != this && mob.hostile && Level.distance(pos, mob.pos) <= 6) mob.beckon(Dungeon.hero.pos);
        }
    }

    @Override
    public void damage(int damage, Object source) {
        if (damage > 0) warning = new int[0];
        super.damage(damage, source);
    }
}
