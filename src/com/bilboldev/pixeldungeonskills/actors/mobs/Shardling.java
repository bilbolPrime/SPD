package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.levels.Level;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class Shardling extends IceMob {
    private int[] impactCells;

    {
        name = "Shardling";
        spriteClass = IceSprite.Shard.class;
        HP = HT = 36;
        defenseSkill = 12;
        EXP = 8;
        maxLvl = 18;
    }

    @Override public int damageRoll() { return Random.NormalIntRange(10, 16); }
    @Override public int attackSkill(Char enemy) { return 24; }
    @Override public int dr() { return 3; }

    @Override
    protected boolean act() {
        if (impactCells != null) return false;
        boolean proceed = super.act();
        return impactCells == null && proceed;
    }

    @Override
    protected boolean prepare() {
        return warn(RebornBoss.ray(pos, Dungeon.hero.pos, 5), 3);
    }

    @Override
    protected void resolve(int[] cells) {
        int end = attackOrigin;
        for (int cell : cells) {
            if (!RebornBoss.lineClear(attackOrigin, cell)) break;
            end = cell;
        }
        if (end != attackOrigin && sprite != null && sprite.parent != null) {
            Level level = Dungeon.level;
            impactCells = cells;
            MagicMissile.coldLight(sprite.parent, attackOrigin, end, () -> {
                if (impactCells != cells) return;
                impactCells = null;
                try {
                    if (Dungeon.level == level && isAlive() && level.mobs.contains(this)) strike(cells, 15, 21);
                } finally {
                    next();
                }
            });
        } else {
            strike(cells, 15, 21);
        }
    }
}
