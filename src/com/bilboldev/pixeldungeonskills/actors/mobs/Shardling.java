package com.bilboldev.pixeldungeonskills.actors.mobs;

import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.MagicMissile;
import com.bilboldev.pixeldungeonskills.sprites.IceSprite;
import com.bilboldev.utils.Random;

public class Shardling extends IceMob {
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
        if (end != attackOrigin && sprite != null && sprite.parent != null)
            MagicMissile.coldLight(sprite.parent, attackOrigin, end, null);
        strike(cells, 15, 21);
    }
}
